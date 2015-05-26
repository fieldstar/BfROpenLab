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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Polygon;

import de.bund.bfr.knime.gis.GisUtils;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.LocationNode;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeShapeTransformer;

public class LocationOsmCanvas extends OsmCanvas<LocationNode> {

	private static final long serialVersionUID = 1L;

	private Polygon invalidArea;
	private Double lastScaleX;

	public LocationOsmCanvas(boolean allowEdges, Naming naming) {
		this(new ArrayList<LocationNode>(), new ArrayList<Edge<LocationNode>>(),
				new NodePropertySchema(), new EdgePropertySchema(), naming, allowEdges);
	}

	public LocationOsmCanvas(List<LocationNode> nodes, NodePropertySchema nodeSchema, Naming naming) {
		this(nodes, new ArrayList<Edge<LocationNode>>(), nodeSchema, new EdgePropertySchema(),
				naming, false);
	}

	public LocationOsmCanvas(List<LocationNode> nodes, List<Edge<LocationNode>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema, Naming naming) {
		this(nodes, edges, nodeSchema, edgeSchema, naming, true);
	}

	private LocationOsmCanvas(List<LocationNode> nodes, List<Edge<LocationNode>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema, Naming naming,
			boolean allowEdges) {
		super(nodes, edges, nodeSchema, edgeSchema, naming);
		invalidArea = null;
		lastScaleX = null;

		setPopupMenu(new CanvasPopupMenu(this, allowEdges, false, true));
		setOptionsPanel(new CanvasOptionsPanel(this, allowEdges, true, false, true));
		viewer.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<LocationNode>(getNodeSize()));

		for (LocationNode node : this.nodes) {
			if (node.getCenter() != null) {
				node.updateCenter(GisUtils.latLonToViz(node.getCenter()));
			}
		}

		invalidArea = LocationCanvasUtils.placeNodes(this.nodes, this.edges,
				viewer.getGraphLayout());
	}

	@Override
	public void resetLayoutItemClicked() {
		Rectangle2D bounds = CanvasUtils.getBounds(getNodePositions(nodes).values());

		if (bounds != null) {
			setTransform(CanvasUtils.getTransformForBounds(getCanvasSize(), bounds, 2.0));
		} else {
			super.resetLayoutItemClicked();
		}
	}

	@Override
	public void avoidOverlayChanged() {
		LocationCanvasUtils.updateNodeLocations(nodes, viewer.getGraphLayout(), transform,
				getNodeSize(), isAvoidOverlay());
	}

	@Override
	protected void applyTransform() {
		super.applyTransform();

		if (isAvoidOverlay()) {
			if (lastScaleX == null || lastScaleX != transform.getScaleX()) {
				LocationCanvasUtils.updateNodeLocations(nodes, viewer.getGraphLayout(), transform,
						getNodeSize(), true);
			}
		}
	}

	@Override
	protected void paintGis(Graphics g, boolean toSvg) {
		super.paintGis(g, toSvg);

		if (invalidArea != null) {
			LocationCanvasUtils.paintNonLatLonArea(g, getCanvasSize().width,
					getCanvasSize().height, transform.apply(invalidArea));
		}
	}

	@Override
	protected LocationNode createMetaNode(String id, Collection<LocationNode> nodes) {
		return LocationCanvasUtils.createMetaNode(id, nodes, nodeSchema, metaNodeProperty,
				viewer.getGraphLayout());
	}
}
