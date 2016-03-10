/*******************************************************************************
 * Copyright (c) 2016 Federal Institute for Risk Assessment (BfR), Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
package de.bund.bfr.math;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.nfunk.jep.ParseException;

import com.google.common.collect.ObjectArrays;

import de.bund.bfr.math.MathUtils.ParamRange;
import de.bund.bfr.math.MathUtils.StartValues;

public class MultivariateOptimization implements Optimization {

	private static final double EPSILON = 0.00001;

	private MultivariateFunction optimizerFunction;

	private String[] parameters;

	private Map<String, Double> minValues;
	private Map<String, Double> maxValues;

	private List<ProgressListener> progressListeners;

	private String sdParam;

	public MultivariateOptimization(String formula, String[] parameters, double[] targetValues,
			Map<String, double[]> variableValues, double levelOfDetection) throws ParseException {
		sdParam = Stream.of(parameters).collect(Collectors.joining());
		this.parameters = ObjectArrays.concat(parameters, sdParam);
		optimizerFunction = new LodFunction(formula, this.parameters, variableValues, targetValues, levelOfDetection,
				sdParam);
		minValues = new LinkedHashMap<>();
		maxValues = new LinkedHashMap<>();
		progressListeners = new ArrayList<>();
	}

	public Map<String, Double> getMinValues() {
		return minValues;
	}

	public Map<String, Double> getMaxValues() {
		return maxValues;
	}

	@Override
	public void addProgressListener(ProgressListener listener) {
		progressListeners.add(listener);
	}

	@Override
	public void removeProgressListener(ProgressListener listener) {
		progressListeners.remove(listener);
	}

	@Override
	public Result optimize(int nParameterSpace, int nOptimizations, boolean stopWhenSuccessful,
			Map<String, Double> minStartValues, Map<String, Double> maxStartValues, int maxIterations) {
		List<ParamRange> paramRanges = new ArrayList<>();
		int paramsWithRange = 0;
		int maxStepCount = nParameterSpace;

		for (String param : parameters) {
			Double min = minStartValues.get(param);
			Double max = maxStartValues.get(param);

			if (min != null && max != null) {
				paramsWithRange++;
			}
		}

		if (paramsWithRange != 0) {
			maxStepCount = (int) Math.pow(nParameterSpace, 1.0 / paramsWithRange);
		}

		for (String param : parameters) {
			Double min = minStartValues.get(param);
			Double max = maxStartValues.get(param);

			if (param.equals(sdParam)) {
				paramRanges.add(new ParamRange(1.0, 1, 1.0));
			} else if (min != null && max != null) {
				paramRanges.add(new ParamRange(min, maxStepCount, (max - min) / (maxStepCount - 1)));
			} else if (min != null) {
				paramRanges.add(new ParamRange(min != 0.0 ? min : EPSILON, 1, 1.0));
			} else if (max != null) {
				paramRanges.add(new ParamRange(max != 0.0 ? max : -EPSILON, 1, 1.0));
			} else {
				paramRanges.add(new ParamRange(EPSILON, 1, 1.0));
			}
		}

		fireProgressChanged(0.0);

		List<StartValues> startValuesList = MathUtils.createStartValuesList(paramRanges.toArray(new ParamRange[0]),
				nOptimizations, values -> optimizerFunction.value(values),
				progress -> fireProgressChanged(0.5 * progress));

		return optimize(startValuesList, stopWhenSuccessful, maxIterations);
	}

	private Result optimize(final List<StartValues> startValuesList, final boolean stopWhenSuccessful,
			final int maxIterations) {
		Result result = null;
		final AtomicInteger currentIteration = new AtomicInteger();
		SimplexOptimizer optimizer = new SimplexOptimizer(new SimpleValueChecker(1e-10, 1e-10) {

			@Override
			public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
				if (super.converged(iteration, previous, current)) {
					return true;
				}

				return currentIteration.incrementAndGet() >= maxIterations;
			}
		});
		final AtomicInteger count = new AtomicInteger(0);

		for (StartValues startValues : startValuesList) {
			fireProgressChanged(0.5 * count.get() / startValuesList.size() + 0.5);

			try {
				PointValuePair optimizerResults = optimizer.optimize(new MaxEval(Integer.MAX_VALUE),
						new MaxIter(maxIterations), new InitialGuess(startValues.getValues()),
						new ObjectiveFunction(optimizerFunction), GoalType.MAXIMIZE,
						new NelderMeadSimplex(parameters.length));
				double logLikelihood = optimizerResults.getValue() != null ? optimizerResults.getValue() : Double.NaN;

				if (result == null || logLikelihood > result.logLikelihood) {
					result = getResults(optimizerResults);

					if (result.logLikelihood == 0.0 || stopWhenSuccessful) {
						break;
					}
				}
			} catch (TooManyEvaluationsException | TooManyIterationsException | ConvergenceException e) {
			}

			count.incrementAndGet();
		}

		return result != null ? result : getResults();
	}

	private Result getResults() {
		Result r = new Result();

		r.parameterValues = new LinkedHashMap<>();
		r.sdValue = null;
		r.logLikelihood = null;

		return r;
	}

	private Result getResults(PointValuePair optimizerResults) {
		Result r = getResults();

		r.logLikelihood = optimizerResults.getValue();

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].equals(sdParam)) {
				r.sdValue = optimizerResults.getPoint()[i];
			} else {
				r.parameterValues.put(parameters[i], optimizerResults.getPoint()[i]);
			}
		}

		return r;
	}

	private void fireProgressChanged(double progress) {
		progressListeners.forEach(l -> l.progressChanged(progress));
	}

	public static class Result implements OptimizationResult {

		private Map<String, Double> parameterValues;
		private Double sdValue;
		private Double logLikelihood;

		@Override
		public Map<String, Double> getParameterValues() {
			return parameterValues;
		}

		public Double getSdValue() {
			return sdValue;
		}

		public Double getLogLikelihood() {
			return logLikelihood;
		}

		public Result copy() {
			Result r = new Result();

			r.parameterValues = new LinkedHashMap<>(parameterValues);
			r.sdValue = sdValue;
			r.logLikelihood = logLikelihood;

			return r;
		}
	}
}
