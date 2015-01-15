/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Annemarie Kaesbohrer (BfR)
 * Bernd Appel (BfR)
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
 ******************************************************************************/
package de.bund.bfr.knime.openkrise;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.geometry.jts.JTS;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.geocode.GeocodingNodeModel;
import de.bund.bfr.knime.gis.shapecell.ShapeBlobCell;
import de.bund.bfr.knime.gis.views.canvas.CanvasUtils;
import de.bund.bfr.knime.gis.views.canvas.Naming;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.GraphNode;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.element.RegionNode;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;

public class TracingUtils {

	public static final Naming NAMING = new Naming("Station", "Stations",
			"Delivery", "Deliveries");

	private TracingUtils() {
	}

	public static Map<String, Class<?>> getTableColumns(DataTableSpec spec) {
		Map<String, Class<?>> tableColumns = new LinkedHashMap<>();

		for (int i = 0; i < spec.getNumColumns(); i++) {
			DataColumnSpec cSpec = spec.getColumnSpec(i);

			if (cSpec.getType().equals(IntCell.TYPE)) {
				tableColumns.put(cSpec.getName(), Integer.class);
			} else if (cSpec.getType().equals(DoubleCell.TYPE)) {
				tableColumns.put(cSpec.getName(), Double.class);
			} else if (cSpec.getType().equals(BooleanCell.TYPE)) {
				tableColumns.put(cSpec.getName(), Boolean.class);
			} else {
				tableColumns.put(cSpec.getName(), String.class);
			}
		}

		return tableColumns;
	}

	public static <V extends Node> List<Edge<V>> readEdges(
			BufferedDataTable edgeTable, Map<String, Class<?>> edgeProperties,
			Map<String, V> nodes) throws InvalidSettingsException {
		List<Edge<V>> edges = new ArrayList<>();
		int idIndex = edgeTable.getSpec().findColumnIndex(TracingColumns.ID);
		int fromIndex = edgeTable.getSpec()
				.findColumnIndex(TracingColumns.FROM);
		int toIndex = edgeTable.getSpec().findColumnIndex(TracingColumns.TO);

		if (idIndex == -1) {
			throw new InvalidSettingsException("Column \"" + TracingColumns.ID
					+ "\" is missing");
		}

		if (fromIndex == -1) {
			throw new InvalidSettingsException("Column \""
					+ TracingColumns.FROM + "\" is missing");
		}

		if (toIndex == -1) {
			throw new InvalidSettingsException("Column \"" + TracingColumns.TO
					+ "\" is missing");
		}

		edgeProperties.put(TracingColumns.ID, String.class);
		edgeProperties.put(TracingColumns.FROM, String.class);
		edgeProperties.put(TracingColumns.TO, String.class);

		for (DataRow row : edgeTable) {
			String id = IO.getToCleanString(row.getCell(idIndex));
			String from = IO.getToCleanString(row.getCell(fromIndex));
			String to = IO.getToCleanString(row.getCell(toIndex));
			V node1 = nodes.get(from);
			V node2 = nodes.get(to);

			if (node1 != null && node2 != null) {
				Map<String, Object> properties = new LinkedHashMap<>();

				TracingUtils.addToProperties(properties, edgeProperties,
						edgeTable, row);
				properties.put(TracingColumns.ID, id);
				properties.put(TracingColumns.FROM, from);
				properties.put(TracingColumns.TO, to);
				replaceNullsInInputProperties(properties, edgeProperties);
				edges.add(new Edge<>(id, properties, node1, node2));
			}
		}

		return edges;
	}

	public static Map<String, GraphNode> readGraphNodes(
			BufferedDataTable nodeTable, Map<String, Class<?>> nodeProperties)
			throws InvalidSettingsException {
		int idIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.ID);
		Map<String, GraphNode> nodes = new LinkedHashMap<>();

		if (idIndex == -1) {
			throw new InvalidSettingsException("Column \"" + TracingColumns.ID
					+ "\" is missing");
		}

		nodeProperties.put(TracingColumns.ID, String.class);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(idIndex));
			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, nodeProperties, nodeTable,
					row);
			properties.put(TracingColumns.ID, id);
			replaceNullsInInputProperties(properties, nodeProperties);
			nodes.put(id, new GraphNode(id, properties, null));
		}

		return nodes;
	}

	public static Map<String, LocationNode> readLocationNodes(
			BufferedDataTable nodeTable, Map<String, Class<?>> nodeProperties)
			throws InvalidSettingsException {
		Map<String, LocationNode> nodes = new LinkedHashMap<>();
		int idIndex = nodeTable.getSpec().findColumnIndex(TracingColumns.ID);
		int latIndex = nodeTable.getSpec().findColumnIndex(
				GeocodingNodeModel.LATITUDE_COLUMN);
		int lonIndex = nodeTable.getSpec().findColumnIndex(
				GeocodingNodeModel.LONGITUDE_COLUMN);
		GeometryFactory factory = new GeometryFactory();

		if (idIndex == -1) {
			throw new InvalidSettingsException("Column \"" + TracingColumns.ID
					+ "\" is missing");
		}

		if (latIndex == -1) {
			throw new InvalidSettingsException("Column \""
					+ GeocodingNodeModel.LATITUDE_COLUMN + "\" is missing");
		}

		if (lonIndex == -1) {
			throw new InvalidSettingsException("Column \""
					+ GeocodingNodeModel.LONGITUDE_COLUMN + "\" is missing");
		}

		nodeProperties.put(TracingColumns.ID, String.class);

		for (DataRow row : nodeTable) {
			String id = IO.getToCleanString(row.getCell(idIndex));
			Double lat = IO.getDouble(row.getCell(latIndex));
			Double lon = IO.getDouble(row.getCell(lonIndex));

			if (lat == null || lon == null) {
				continue;
			}

			Point p = null;

			try {
				p = (Point) JTS.transform(
						factory.createPoint(new Coordinate(lat, lon)),
						GisUtils.LATLON_TO_VIZ);
			} catch (MismatchedDimensionException | TransformException e) {
				e.printStackTrace();
				continue;
			}

			Map<String, Object> properties = new LinkedHashMap<>();

			TracingUtils.addToProperties(properties, nodeProperties, nodeTable,
					row);
			properties.put(TracingColumns.ID, id);
			replaceNullsInInputProperties(properties, nodeProperties);
			nodes.put(
					id,
					new LocationNode(id, properties, new Point2D.Double(p
							.getX(), p.getY())));
		}

		return nodes;
	}

	public static List<RegionNode> readRegionNodes(BufferedDataTable shapeTable)
			throws InvalidSettingsException {
		List<RegionNode> nodes = new ArrayList<>();
		List<String> shapeColumns = KnimeUtils.getColumnNames(KnimeUtils
				.getColumns(shapeTable.getSpec(), ShapeBlobCell.TYPE));

		if (shapeColumns.isEmpty()) {
			throw new InvalidSettingsException("No Shape Column in table");
		}

		int shapeIndex = shapeTable.getSpec().findColumnIndex(
				shapeColumns.get(0));
		int index = 0;

		for (DataRow row : shapeTable) {
			Geometry shape = GisUtils.getShape(row.getCell(shapeIndex));

			if (shape instanceof MultiPolygon) {
				try {
					nodes.add(new RegionNode(index + "",
							new LinkedHashMap<String, Object>(),
							(MultiPolygon) JTS.transform(shape,
									GisUtils.LATLON_TO_VIZ)));
					index++;
				} catch (MismatchedDimensionException | TransformException e) {
					e.printStackTrace();
				}
			}
		}

		return nodes;
	}

	public static Set<String> toString(Set<?> set) {
		Set<String> stringSet = new LinkedHashSet<>();

		for (Object o : set) {
			stringSet.add(o.toString());
		}

		return stringSet;
	}

	private static void addToProperties(Map<String, Object> properties,
			Map<String, Class<?>> propertyTypes, BufferedDataTable table,
			DataRow row) {
		for (String property : propertyTypes.keySet()) {
			Class<?> type = propertyTypes.get(property);
			int column = table.getSpec().findColumnIndex(property);

			if (column != -1) {
				DataCell cell = row.getCell(column);

				TracingUtils.addCellContentToMap(properties, property, type,
						cell);
			}
		}
	}

	private static void addCellContentToMap(Map<String, Object> map,
			String property, Class<?> type, DataCell cell) {
		Object obj = null;

		if (type == String.class) {
			obj = IO.getToCleanString(cell);
		} else if (type == Integer.class) {
			obj = IO.getInt(cell);
		} else if (type == Double.class) {
			obj = IO.getDouble(cell);
		} else if (type == Boolean.class) {
			obj = IO.getBoolean(cell);
		}

		CanvasUtils.addObjectToMap(map, property, type, obj);
	}

	private static void replaceNullsInInputProperties(
			Map<String, Object> properties, Map<String, Class<?>> allProperties) {
		if (allProperties.containsKey(TracingColumns.WEIGHT)
				&& properties.get(TracingColumns.WEIGHT) == null) {
			properties.put(TracingColumns.WEIGHT, 0.0);
		}

		if (allProperties.containsKey(TracingColumns.CROSS_CONTAMINATION)
				&& properties.get(TracingColumns.CROSS_CONTAMINATION) == null) {
			properties.put(TracingColumns.CROSS_CONTAMINATION, false);
		}

		if (allProperties.containsKey(TracingColumns.OBSERVED)
				&& properties.get(TracingColumns.OBSERVED) == null) {
			properties.put(TracingColumns.OBSERVED, false);
		}
	}

	public static HighlightConditionList renameColumns(
			HighlightConditionList list, Set<String> columns) {
		HighlightConditionList newList = new HighlightConditionList(list);
		List<HighlightCondition> newL = new ArrayList<>();

		for (HighlightCondition c : list.getConditions()) {
			if (c instanceof AndOrHighlightCondition) {
				newL.add(renameColumn((AndOrHighlightCondition) c, columns));
			} else if (c instanceof ValueHighlightCondition) {
				newL.add(renameColumn((ValueHighlightCondition) c, columns));
			} else if (c instanceof LogicalValueHighlightCondition) {
				newL.add(new LogicalValueHighlightCondition(renameColumn(
						((LogicalValueHighlightCondition) c)
								.getValueCondition(), columns), renameColumn(
						((LogicalValueHighlightCondition) c)
								.getLogicalCondition(), columns)));
			}
		}

		newList.setConditions(newL);

		return newList;
	}

	private static AndOrHighlightCondition renameColumn(
			AndOrHighlightCondition c, Set<String> columns) {
		AndOrHighlightCondition newC = new AndOrHighlightCondition(c);
		List<List<LogicalHighlightCondition>> newL1 = new ArrayList<>();

		newC.setLabelProperty(renameColumn(c.getLabelProperty(), columns));

		for (List<LogicalHighlightCondition> l2 : c.getConditions()) {
			List<LogicalHighlightCondition> newL2 = new ArrayList<>();

			for (LogicalHighlightCondition l3 : l2) {
				LogicalHighlightCondition newL3 = new LogicalHighlightCondition(
						l3);

				newL3.setProperty(renameColumn(l3.getProperty(), columns));
				newL2.add(newL3);
			}

			newL1.add(newL2);
		}

		newC.setConditions(newL1);

		return newC;
	}

	private static ValueHighlightCondition renameColumn(
			ValueHighlightCondition c, Set<String> columns) {
		ValueHighlightCondition newC = new ValueHighlightCondition(c);

		newC.setProperty(renameColumn(c.getProperty(), columns));
		newC.setLabelProperty(renameColumn(c.getLabelProperty(), columns));

		return newC;
	}

	private static String renameColumn(String column, Set<String> columns) {
		if (column == null) {
			return null;
		}

		if (column.equals(TracingColumns.OLD_WEIGHT)
				&& !columns.contains(TracingColumns.OLD_WEIGHT)) {
			return TracingColumns.WEIGHT;
		}

		if (column.equals(TracingColumns.OLD_OBSERVED)
				&& !columns.contains(TracingColumns.OLD_OBSERVED)) {
			return TracingColumns.OBSERVED;
		}

		return column;
	}

	public static HashMap<Integer, MyDelivery> getDeliveries(
			BufferedDataTable dataTable, BufferedDataTable edgeTable)
			throws NotConfigurableException {
		DataTableSpec dataSpec = dataTable.getSpec();
		DataTableSpec edgeSpec = edgeTable.getSpec();

		if (dataTable.getSpec().findColumnIndex(TracingColumns.ID) == -1
				|| dataTable.getSpec().findColumnIndex(TracingColumns.NEXT) == -1) {
			throw new NotConfigurableException(
					"Tracing Data Model cannot be read."
							+ " Try reexecuting the Supply Chain Reader or"
							+ " downloading an up-to-date workflow.");
		}

		HashMap<Integer, MyDelivery> deliveries = new HashMap<>();

		for (DataRow row : edgeTable) {
			int id = Integer.parseInt(IO.getToString(row.getCell(edgeSpec
					.findColumnIndex(TracingColumns.ID))));
			int from = Integer.parseInt(IO.getToString(row.getCell(edgeSpec
					.findColumnIndex(TracingColumns.FROM))));
			int to = Integer.parseInt(IO.getToString(row.getCell(edgeSpec
					.findColumnIndex(TracingColumns.TO))));
			String date = IO.getString(row.getCell(edgeSpec
					.findColumnIndex(TracingColumns.DELIVERY_DATE)));
			MyDelivery delivery = new MyDelivery(id, from, to, getDay(date),
					getMonth(date), getYear(date));

			deliveries.put(id, delivery);
		}

		for (DataRow row : dataTable) {
			int id = IO.getInt(row.getCell(dataSpec
					.findColumnIndex(TracingColumns.ID)));
			int next = IO.getInt(row.getCell(dataSpec
					.findColumnIndex(TracingColumns.NEXT)));

			if (deliveries.containsKey(id) && deliveries.containsKey(next)) {
				deliveries.get(id).getAllNextIDs().add(next);
				deliveries.get(next).getAllPreviousIDs().add(id);
			}
		}

		return deliveries;
	}

	private static Integer getYear(String date) {
		if (date == null) {
			return null;
		}

		String year = null;

		if (date.contains(".")) {
			year = date.substring(date.lastIndexOf('.') + 1);
		} else if (date.contains("-")) {
			year = date.substring(0, date.indexOf('-'));
		} else {
			year = date;
		}

		try {
			return Integer.parseInt(year);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}

	private static Integer getMonth(String date) {
		if (date == null) {
			return null;
		}

		String month = null;

		if (date.contains(".")) {
			int i1 = date.indexOf('.');
			int i2 = date.lastIndexOf('.');

			if (i1 == i2) {
				month = date.substring(0, i1);
			} else {
				month = date.substring(i1 + 1, i2);
			}
		} else if (date.contains("-")) {
			int i1 = date.indexOf('-');
			int i2 = date.lastIndexOf('-');

			if (i1 == i2) {
				month = date.substring(i1 + 1);
			} else {
				month = date.substring(i1 + 1, i2);
			}
		}

		try {
			return Integer.parseInt(month);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}

	private static Integer getDay(String date) {
		if (date == null) {
			return null;
		}

		String day = null;

		if (date.contains(".")) {
			day = date.substring(0, date.indexOf('.'));
		} else if (date.contains("-")) {
			day = date.substring(date.lastIndexOf('-') + 1);
		}

		try {
			return Integer.parseInt(day);
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
	}
}
