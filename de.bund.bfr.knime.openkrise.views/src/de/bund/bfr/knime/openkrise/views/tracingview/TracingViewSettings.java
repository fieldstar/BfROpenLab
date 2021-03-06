/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
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
package de.bund.bfr.knime.openkrise.views.tracingview;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.google.common.collect.Ordering;
import de.bund.bfr.jung.LabelPosition;
import de.bund.bfr.knime.NodeSettings;
import de.bund.bfr.knime.XmlConverter;
import de.bund.bfr.knime.gis.BackwardUtils;
import de.bund.bfr.knime.gis.GisType;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.util.ArrowHeadType;
import de.bund.bfr.knime.openkrise.views.Activator;
import de.bund.bfr.knime.openkrise.views.canvas.ITracingCanvas;

public class TracingViewSettings extends NodeSettings {
	
	//private static Logger logger =  Logger.getLogger("de.bund.bfr");

	protected static final XmlConverter SERIALIZER = new XmlConverter(Activator.class.getClassLoader());

	private static final String CFG_SHOW_GIS = "ShowGis";
	private static final String CFG_GIS_TYPE = "GisType";
	private static final String CFG_EXPORT_AS_SVG = "ExportAsSvg";
	private static final String CFG_SKIP_EDGELESS_NODES = "SkipEdgelessNodes";
	private static final String CFG_SHOW_EDGES_IN_META_NODE = "ShowEdgesInMetaNode";
	private static final String CFG_JOIN_EDGES = "JoinEdges";
	private static final String CFG_HIDE_ARROW_HEAD = "HideArrowHead";
	private static final String CFG_ARROW_HEAD_IN_MIDDLE = "ArrowInMiddle";
	private static final String CFG_NODE_LABEL_POSITION = "NodeLabelPosition";
	private static final String CFG_SHOW_LEGEND = "GraphShowLegend";
	private static final String CFG_SELECTED_NODES = "GraphSelectedNodes";
	private static final String CFG_SELECTED_EDGES = "GraphSelectedEdges";
	private static final String CFG_CANVAS_SIZE = "GraphCanvasSize";
	private static final String CFG_NODE_HIGHLIGHT_CONDITIONS = "GraphNodeHighlightConditions";
	private static final String CFG_EDGE_HIGHLIGHT_CONDITIONS = "GraphEdgeHighlightConditions";
	private static final String CFG_COLLAPSED_NODES = "CollapsedNodes";
	private static final String CFG_LABEL = "Label";

	private static final String CFG_NODE_WEIGHTS = "CaseWeights";
	private static final String CFG_EDGE_WEIGHTS = "EdgeWeights";
	private static final String CFG_NODE_CROSS_CONTAMINATIONS = "CrossContaminations";
	private static final String CFG_EDGE_CROSS_CONTAMINATIONS = "EdgeCrossContaminations";
	private static final String CFG_NODE_KILL_CONTAMINATIONS = "NodeKillContaminations";
	private static final String CFG_EDGE_KILL_CONTAMINATIONS = "EdgeKillContaminations";
	private static final String CFG_OBSERVED_NODES = "Filter";
	private static final String CFG_OBSERVED_EDGES = "EdgeFilter";
	private static final String CFG_ENFORCE_TEMPORAL_ORDER = "EnforceTemporalOrder";
	private static final String CFG_SHOW_FORWARD = "ShowConnected";
	private static final String CFG_SHOW_DELIVERIES_WITHOUT_DATE = "ShowDeliveriesWithoutDate";
	private static final String CFG_SHOW_TO_DATE = "ShowToDate";

	private boolean showGis;
	private GisType gisType;
	private boolean exportAsSvg;
	private boolean skipEdgelessNodes;
	private boolean showEdgesInMetaNode;
	private boolean joinEdges;
	private boolean hideArrowHead;
	private boolean arrowHeadInMiddle;
	private LabelPosition nodeLabelPosition;
	private boolean showLegend;
	private Dimension canvasSize;
	private List<String> selectedNodes;
	private List<String> selectedEdges;
	private HighlightConditionList nodeHighlightConditions;
	private HighlightConditionList edgeHighlightConditions;
	private Map<String, Map<String, Point2D>> collapsedNodes;
	private String label;

	private Map<String, Double> nodeWeights;
	private Map<String, Double> edgeWeights;
	private Map<String, Boolean> nodeCrossContaminations;
	private Map<String, Boolean> edgeCrossContaminations;
	private Map<String, Boolean> nodeKillContaminations;
	private Map<String, Boolean> edgeKillContaminations;
	private Map<String, Boolean> observedNodes;
	private Map<String, Boolean> observedEdges;
	private boolean enforeTemporalOrder;
	private boolean showForward;
	private boolean showDeliveriesWithoutDate;
	private GregorianCalendar showToDate;

	private GraphSettings graphSettings;
	private GisSettings gisSettings;
	private ExplosionSettingsList gobjExplosionSettingsList;

	public TracingViewSettings() {
		showGis = false;
		gisType = GisType.SHAPEFILE;
		exportAsSvg = false;
		skipEdgelessNodes = false;
		showEdgesInMetaNode = false;
		joinEdges = false;
		hideArrowHead = false;
		arrowHeadInMiddle = false;
		nodeLabelPosition = LabelPosition.BOTTOM_RIGHT;
		showLegend = false;
		canvasSize = null;
		selectedNodes = new ArrayList<>();
		selectedEdges = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		edgeHighlightConditions = new HighlightConditionList();
		collapsedNodes = new LinkedHashMap<>();
		label = null;

		nodeWeights = new LinkedHashMap<>();
		edgeWeights = new LinkedHashMap<>();
		nodeCrossContaminations = new LinkedHashMap<>();
		edgeCrossContaminations = new LinkedHashMap<>();
		nodeKillContaminations = new LinkedHashMap<>();
		edgeKillContaminations = new LinkedHashMap<>();
		observedNodes = new LinkedHashMap<>();
		observedEdges = new LinkedHashMap<>();
		enforeTemporalOrder = true;
		showForward = false;
		showDeliveriesWithoutDate = true;
		showToDate = null;

		graphSettings = new GraphSettings();
		gisSettings = new GisSettings();
		this.gobjExplosionSettingsList = new ExplosionSettingsList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void loadSettings(NodeSettingsRO settings) {
		try {
			showGis = settings.getBoolean(CFG_SHOW_GIS);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisType = GisType.valueOf(settings.getString(CFG_GIS_TYPE));
		} catch (InvalidSettingsException | IllegalArgumentException e) {
		}

		try {
			exportAsSvg = settings.getBoolean(CFG_EXPORT_AS_SVG);
		} catch (InvalidSettingsException e) {
		}

		try {
			skipEdgelessNodes = settings.getBoolean(CFG_SKIP_EDGELESS_NODES);
		} catch (InvalidSettingsException e) {
		}

		try {
			showEdgesInMetaNode = settings.getBoolean(CFG_SHOW_EDGES_IN_META_NODE);
		} catch (InvalidSettingsException e) {
		}

		try {
			joinEdges = settings.getBoolean(CFG_JOIN_EDGES);
		} catch (InvalidSettingsException e) {
		}

		try {
			hideArrowHead = settings.getBoolean(CFG_HIDE_ARROW_HEAD);
		} catch (InvalidSettingsException e) {
		}

		try {
			arrowHeadInMiddle = settings.getBoolean(CFG_ARROW_HEAD_IN_MIDDLE);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeLabelPosition = LabelPosition.valueOf(settings.getString(CFG_NODE_LABEL_POSITION));
		} catch (InvalidSettingsException e) {
		}

		try {
			showLegend = settings.getBoolean(CFG_SHOW_LEGEND);
		} catch (InvalidSettingsException e) {
		}

		try {
			canvasSize = (Dimension) SERIALIZER.fromXml(settings.getString(CFG_CANVAS_SIZE));
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedNodes = (List<String>) SERIALIZER.fromXml(settings.getString(CFG_SELECTED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			selectedEdges = (List<String>) SERIALIZER.fromXml(settings.getString(CFG_SELECTED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_NODE_HIGHLIGHT_CONDITIONS)));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeHighlightConditions = (HighlightConditionList) SERIALIZER
					.fromXml(BackwardUtils.toNewHighlightingFormat(settings.getString(CFG_EDGE_HIGHLIGHT_CONDITIONS)));
		} catch (InvalidSettingsException e) {
		}

		try {
			collapsedNodes = (Map<String, Map<String, Point2D>>) SERIALIZER
					.fromXml(settings.getString(CFG_COLLAPSED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			label = settings.getString(CFG_LABEL);
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_NODE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeWeights = (Map<String, Double>) SERIALIZER.fromXml(settings.getString(CFG_EDGE_WEIGHTS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeCrossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeCrossContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_CROSS_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			nodeKillContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_NODE_KILL_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			edgeKillContaminations = (Map<String, Boolean>) SERIALIZER
					.fromXml(settings.getString(CFG_EDGE_KILL_CONTAMINATIONS));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedNodes = (Map<String, Boolean>) SERIALIZER.fromXml(settings.getString(CFG_OBSERVED_NODES));
		} catch (InvalidSettingsException e) {
		}

		try {
			observedEdges = (Map<String, Boolean>) SERIALIZER.fromXml(settings.getString(CFG_OBSERVED_EDGES));
		} catch (InvalidSettingsException e) {
		}

		try {
			enforeTemporalOrder = settings.getBoolean(CFG_ENFORCE_TEMPORAL_ORDER);
		} catch (InvalidSettingsException e) {
		}

		try {
			showForward = settings.getBoolean(CFG_SHOW_FORWARD);
		} catch (InvalidSettingsException e) {
		}

		try {
			showDeliveriesWithoutDate = settings.getBoolean(CFG_SHOW_DELIVERIES_WITHOUT_DATE);
		} catch (InvalidSettingsException e) {
		}

		try {
			showToDate = (GregorianCalendar) SERIALIZER.fromXml(settings.getString(CFG_SHOW_TO_DATE));
		} catch (InvalidSettingsException e) {
		}

		graphSettings.loadSettings(settings);
		gisSettings.loadSettings(settings);
		this.gobjExplosionSettingsList.loadSettings(settings);
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		settings.addBoolean(CFG_SHOW_GIS, showGis);
		settings.addString(CFG_GIS_TYPE, gisType.name());
		settings.addBoolean(CFG_EXPORT_AS_SVG, exportAsSvg);
		settings.addBoolean(CFG_SKIP_EDGELESS_NODES, skipEdgelessNodes);
		settings.addBoolean(CFG_SHOW_EDGES_IN_META_NODE, showEdgesInMetaNode);
		settings.addBoolean(CFG_JOIN_EDGES, joinEdges);
		settings.addBoolean(CFG_HIDE_ARROW_HEAD, hideArrowHead);
		settings.addBoolean(CFG_ARROW_HEAD_IN_MIDDLE, arrowHeadInMiddle);
		settings.addString(CFG_NODE_LABEL_POSITION, nodeLabelPosition.name());
		settings.addBoolean(CFG_SHOW_LEGEND, showLegend);
		settings.addString(CFG_CANVAS_SIZE, SERIALIZER.toXml(canvasSize));
		settings.addString(CFG_SELECTED_NODES, SERIALIZER.toXml(selectedNodes));
		settings.addString(CFG_SELECTED_EDGES, SERIALIZER.toXml(selectedEdges));
		settings.addString(CFG_NODE_HIGHLIGHT_CONDITIONS, SERIALIZER.toXml(nodeHighlightConditions));
		settings.addString(CFG_EDGE_HIGHLIGHT_CONDITIONS, SERIALIZER.toXml(edgeHighlightConditions));
		settings.addString(CFG_COLLAPSED_NODES, SERIALIZER.toXml(collapsedNodes));
		settings.addString(CFG_LABEL, label);

		settings.addString(CFG_NODE_WEIGHTS, SERIALIZER.toXml(nodeWeights));
		settings.addString(CFG_EDGE_WEIGHTS, SERIALIZER.toXml(edgeWeights));
		settings.addString(CFG_NODE_CROSS_CONTAMINATIONS, SERIALIZER.toXml(nodeCrossContaminations));
		settings.addString(CFG_EDGE_CROSS_CONTAMINATIONS, SERIALIZER.toXml(edgeCrossContaminations));
		settings.addString(CFG_NODE_KILL_CONTAMINATIONS, SERIALIZER.toXml(nodeKillContaminations));
		settings.addString(CFG_EDGE_KILL_CONTAMINATIONS, SERIALIZER.toXml(edgeKillContaminations));
		settings.addString(CFG_OBSERVED_NODES, SERIALIZER.toXml(observedNodes));
		settings.addString(CFG_OBSERVED_EDGES, SERIALIZER.toXml(observedEdges));
		settings.addBoolean(CFG_ENFORCE_TEMPORAL_ORDER, enforeTemporalOrder);
		settings.addBoolean(CFG_SHOW_FORWARD, showForward);
		settings.addBoolean(CFG_SHOW_DELIVERIES_WITHOUT_DATE, showDeliveriesWithoutDate);
		settings.addString(CFG_SHOW_TO_DATE, SERIALIZER.toXml(showToDate));

		graphSettings.saveSettings(settings);
		gisSettings.saveSettings(settings);
		this.gobjExplosionSettingsList.saveSettings(settings);
	}
	
	protected boolean isNodeSelected(String id) {
	  if(this.selectedNodes.contains(id)) return true;
	  for(Map<String, Point2D> childMap : this.collapsedNodes.values()) if(childMap.containsKey(id)) return true;
	  return false;
	}

	public void setFromCanvas(ITracingCanvas<?> canvas, boolean resized) {
		showLegend = canvas.getOptionsPanel().isShowLegend();
		joinEdges = canvas.getOptionsPanel().isJoinEdges();
		hideArrowHead = canvas.getOptionsPanel().getArrowHeadType() == ArrowHeadType.HIDE;
		arrowHeadInMiddle = canvas.getOptionsPanel().getArrowHeadType() == ArrowHeadType.IN_MIDDLE;
		nodeLabelPosition = canvas.getOptionsPanel().getNodeLabelPosition();
		skipEdgelessNodes = canvas.getOptionsPanel().isSkipEdgelessNodes();
		showEdgesInMetaNode = canvas.getOptionsPanel().isShowEdgesInMetaNode();
		label = canvas.getOptionsPanel().getLabel();

		setSelectedNodes(Ordering.natural().sortedCopy(canvas.getSelectedNodeIds()));
		setSelectedEdges(Ordering.natural().sortedCopy(canvas.getSelectedEdgeIds()));

		nodeHighlightConditions = canvas.getNodeHighlightConditions();
		edgeHighlightConditions = canvas.getEdgeHighlightConditions();
		
		if(gobjExplosionSettingsList.getActiveExplosionSettings()==null) {
			collapsedNodes = BackwardUtils.toOldCollapseFormat(canvas.getCollapsedNodes());
		}
		
		
		if (resized || canvasSize == null) {
			canvasSize = canvas.getCanvasSize();
		}

		nodeWeights = canvas.getNodeWeights();
		edgeWeights = canvas.getEdgeWeights();
		nodeCrossContaminations = canvas.getNodeCrossContaminations();
		edgeCrossContaminations = canvas.getEdgeCrossContaminations();
		nodeKillContaminations = canvas.getNodeKillContaminations();
		edgeKillContaminations = canvas.getEdgeKillContaminations();
		observedNodes = canvas.getObservedNodes();
		observedEdges = canvas.getObservedEdges();
		enforeTemporalOrder = canvas.isEnforceTemporalOrder();
		showForward = canvas.isShowForward();
		showDeliveriesWithoutDate = canvas.isShowDeliveriesWithoutDate();
		showToDate = canvas.getShowToDate();
	}

	public void setToCanvas(ITracingCanvas<?> canvas) {
		canvas.getOptionsPanel().setShowLegend(showLegend);
		canvas.getOptionsPanel().setJoinEdges(joinEdges);
		canvas.getOptionsPanel().setArrowHeadType(hideArrowHead ? ArrowHeadType.HIDE
				: (arrowHeadInMiddle ? ArrowHeadType.IN_MIDDLE : ArrowHeadType.AT_TARGET));
		canvas.getOptionsPanel().setNodeLabelPosition(nodeLabelPosition);
		canvas.getOptionsPanel().setLabel(label);
		canvas.getOptionsPanel().setSkipEdgelessNodes(skipEdgelessNodes);
		canvas.getOptionsPanel().setShowEdgesInMetaNode(showEdgesInMetaNode);
		
		canvas.setCollapsedNodes(BackwardUtils.toNewCollapseFormat(collapsedNodes));
		
		canvas.setNodeHighlightConditions(de.bund.bfr.knime.openkrise.BackwardUtils
				.renameColumns(nodeHighlightConditions, canvas.getNodeSchema().getMap().keySet()));
		canvas.setEdgeHighlightConditions(de.bund.bfr.knime.openkrise.BackwardUtils
				.renameColumns(edgeHighlightConditions, canvas.getEdgeSchema().getMap().keySet()));
		canvas.setSelectedNodeIds(new LinkedHashSet<>(this.getSelectedNodes()));
		canvas.setSelectedEdgeIds(new LinkedHashSet<>(this.getSelectedEdges()));

		if (canvasSize != null) {
			canvas.setCanvasSize(canvasSize);
		}

		canvas.setNodeWeights(nodeWeights);
		canvas.setEdgeWeights(edgeWeights);
		canvas.setNodeCrossContaminations(nodeCrossContaminations);
		canvas.setEdgeCrossContaminations(edgeCrossContaminations);
		canvas.setNodeKillContaminations(nodeKillContaminations);
		canvas.setEdgeKillContaminations(edgeKillContaminations);
		canvas.setObservedNodes(observedNodes);
		canvas.setObservedEdges(observedEdges);
		canvas.setEnforceTemporalOrder(enforeTemporalOrder);
		canvas.setShowForward(showForward);
		canvas.setShowDeliveriesWithoutDate(showDeliveriesWithoutDate);
		canvas.setShowToDate(showToDate);
	}

	public GraphSettings getGraphSettings() {
		return (this.gobjExplosionSettingsList.getActiveExplosionSettings()==null? 
				this.graphSettings:
				this.gobjExplosionSettingsList.getActiveExplosionSettings().getGraphSettings());
	}

	public GisSettings getGisSettings() {
		return (this.gobjExplosionSettingsList.getActiveExplosionSettings()==null? 
				this.gisSettings:
				this.gobjExplosionSettingsList.getActiveExplosionSettings().getGisSettings());
	}
	
	private List<String> getSelectedNodes() {
		return (this.gobjExplosionSettingsList.getActiveExplosionSettings()==null? 
				this.selectedNodes:
				this.gobjExplosionSettingsList.getActiveExplosionSettings().getSelectedNodes());
	}
	
	private List<String> getSelectedEdges() {
		return (this.gobjExplosionSettingsList.getActiveExplosionSettings()==null? 
				this.selectedEdges:
				this.gobjExplosionSettingsList.getActiveExplosionSettings().getSelectedEdges());
	}
	
	private void setSelectedNodes(List<String> selectedNodes) {
		if(this.gobjExplosionSettingsList.getActiveExplosionSettings()==null) {
			this.selectedNodes = selectedNodes;
		} else {
			this.gobjExplosionSettingsList.getActiveExplosionSettings().setSelectedNodes(selectedNodes);
		}
	}
	
	private void setSelectedEdges(List<String> selectedEdges) {
		if(this.gobjExplosionSettingsList.getActiveExplosionSettings()==null) {
			this.selectedEdges = selectedEdges;
		} else {
			this.gobjExplosionSettingsList.getActiveExplosionSettings().setSelectedEdges(selectedEdges);
		}
	}
	
	public ExplosionSettingsList getExplosionSettingsList() {
		return this.gobjExplosionSettingsList;
	}
	
	public boolean isShowGis() {
		return showGis;
	}

	public void setShowGis(boolean showGis) {
		this.showGis = showGis;
	}

	public GisType getGisType() {
		return gisType;
	}

	public void setGisType(GisType gisType) {
		this.gisType = gisType;
	}

	public boolean isExportAsSvg() {
		return exportAsSvg;
	}

	public void setExportAsSvg(boolean exportAsSvg) {
		this.exportAsSvg = exportAsSvg;
	}

	public void clearWeights() {
		nodeWeights.clear();
		edgeWeights.clear();
	}

	public void clearCrossContamination() {
		nodeCrossContaminations.clear();
		edgeCrossContaminations.clear();
	}

	public void clearKillContamination() {
		nodeKillContaminations.clear();
		edgeKillContaminations.clear();
	}

	public void clearObserved() {
		observedNodes.clear();
		observedEdges.clear();
	}
}
