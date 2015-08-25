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
package de.bund.bfr.knime;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.util.FileUtil;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class KnimeUtils {

	private KnimeUtils() {
	}

	public static String listToString(List<?> list) {
		return Joiner.on(",").useForNull("null").join(list);
	}

	public static List<String> stringToList(String s) {
		return Lists.newArrayList(Splitter.on(",").omitEmptyStrings().split(s));
	}

	public static List<Double> stringToDoubleList(String s) {
		List<String> list = stringToList(s);
		List<Double> doubleList = new ArrayList<>();

		for (String value : list) {
			try {
				doubleList.add(Double.parseDouble(value));
			} catch (NumberFormatException e) {
				doubleList.add(null);
			}
		}

		return doubleList;
	}

	public static File getFile(String fileName) throws InvalidPathException, MalformedURLException {
		return FileUtil.getFileFromURL(FileUtil.toURL(fileName));
	}

	public static List<DataColumnSpec> getColumns(DataTableSpec spec, DataType... types) {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : spec) {
			for (DataType type : types) {
				if (column.getType().equals(type)) {
					columns.add(column);
					break;
				}
			}
		}

		return columns;
	}

	@SuppressWarnings("unchecked")
	public static List<DataColumnSpec> getColumns(DataTableSpec spec, Class<? extends DataValue>... types) {
		List<DataColumnSpec> columns = new ArrayList<>();

		for (DataColumnSpec column : spec) {
			for (Class<? extends DataValue> type : types) {
				if (column.getType().isCompatible(type)) {
					columns.add(column);
					break;
				}
			}
		}

		return columns;
	}

	public static List<String> getColumnNames(List<DataColumnSpec> columns) {
		List<String> names = new ArrayList<>();

		for (DataColumnSpec column : columns) {
			names.add(column.getName());
		}

		return names;
	}

	public static String createNewValue(String value, Collection<String> values) {
		if (!values.contains(value)) {
			return value;
		}

		for (int i = 2;; i++) {
			String newValue = value + "_" + i;

			if (!values.contains(newValue)) {
				return newValue;
			}
		}
	}

	public static <T> List<T> nullToEmpty(List<T> list) {
		return list != null ? list : new ArrayList<T>(0);
	}

	public static <T> Set<T> nullToEmpty(Set<T> set) {
		return set != null ? set : new LinkedHashSet<T>(0);
	}

	public static <V, K> Map<V, K> nullToEmpty(Map<V, K> map) {
		return map != null ? map : new LinkedHashMap<V, K>(0);
	}

	public static <T extends Comparable<? super T>> List<T> toSortedList(Collection<T> values) {
		List<T> list = new ArrayList<>(values);

		Collections.sort(list);

		return list;
	}
}
