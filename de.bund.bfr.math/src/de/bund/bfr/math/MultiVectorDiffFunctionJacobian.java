/*******************************************************************************
 * Copyright (c) 2015 Federal Institute for Risk Assessment (BfR), Germany
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

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.nfunk.jep.ParseException;

public class MultiVectorDiffFunctionJacobian implements MultivariateMatrixFunction {

	private MultiVectorDiffFunction[] diffFunctions;
	private int nParams;
	private int nValues;

	public MultiVectorDiffFunctionJacobian(String[] formulas, String[] dependentVariables, double[] initValues,
			List<String[]> initParameters, String[] parameters, Map<String, List<double[]>> variableValues,
			List<double[]> timeValues, String dependentVariable, String timeVariable, IntegratorFactory integrator,
			InterpolationFactory interpolator) throws ParseException {
		nParams = parameters.length;
		nValues = timeValues.stream().mapToInt(t -> t.length).sum();
		diffFunctions = new MultiVectorDiffFunction[nParams];

		for (int ip = 0; ip < nParams; ip++) {
			diffFunctions[ip] = new MultiVectorDiffFunction(formulas, dependentVariables, initValues, initParameters,
					parameters, variableValues, timeValues, dependentVariable, timeVariable, integrator, interpolator);
		}
	}

	@Override
	public double[][] value(double[] point) throws IllegalArgumentException {
		return MathUtils.aproxJacobianParallel(diffFunctions, point, nParams, nValues);
	}
}
