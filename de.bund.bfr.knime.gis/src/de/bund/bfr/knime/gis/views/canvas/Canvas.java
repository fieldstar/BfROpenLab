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
package de.bund.bfr.knime.gis.views.canvas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import de.bund.bfr.knime.KnimeUtils;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightConditionChecker;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightListDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.HighlightSelectionDialog;
import de.bund.bfr.knime.gis.views.canvas.dialogs.ImageFileChooser;
import de.bund.bfr.knime.gis.views.canvas.dialogs.PropertiesDialog;
import de.bund.bfr.knime.gis.views.canvas.element.Edge;
import de.bund.bfr.knime.gis.views.canvas.element.Node;
import de.bund.bfr.knime.gis.views.canvas.highlighting.AndOrHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightConditionList;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.LogicalValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.highlighting.ValueHighlightCondition;
import de.bund.bfr.knime.gis.views.canvas.transformer.EdgeDrawTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.FontTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.MiddleEdgeArrowRenderingSupport;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeFillTransformer;
import de.bund.bfr.knime.gis.views.canvas.transformer.NodeStrokeTransformer;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeArrowRenderingSupport;
import edu.uci.ics.jung.visualization.transform.MutableAffineTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

public abstract class Canvas<V extends Node> extends JPanel implements
		ActionListener, ChangeListener, ItemListener, KeyListener,
		MouseListener, CanvasPopupMenu.ClickListener,
		CanvasOptionsPanel.ChangeListener {

	private static final String DEFAULT_NODE_NAME = "Node";
	private static final String DEFAULT_EDGE_NAME = "Edge";
	private static final String DEFAULT_NODES_NAME = "Nodes";
	private static final String DEFAULT_EDGES_NAME = "Edges";

	private static final long serialVersionUID = 1L;
	private static final String COPY = "Copy";
	private static final String PASTE = "Paste";
	private static final String IS_META_NODE = "IsMeta";

	protected VisualizationViewer<V, Edge<V>> viewer;
	protected double scaleX;
	protected double scaleY;
	protected double translationX;
	protected double translationY;

	protected List<V> allNodes;
	protected List<Edge<V>> allEdges;
	protected Set<V> nodes;
	protected Set<Edge<V>> edges;
	protected Map<String, V> nodeSaveMap;
	protected Map<String, Edge<V>> edgeSaveMap;
	protected Map<Edge<V>, Set<Edge<V>>> joinMap;
	protected Map<String, Set<String>> collapsedNodes;

	protected NodePropertySchema nodeSchema;
	protected EdgePropertySchema edgeSchema;
	protected String metaNodeProperty;

	protected HighlightConditionList nodeHighlightConditions;
	protected HighlightConditionList edgeHighlightConditions;

	private CanvasOptionsPanel optionsPanel;
	private CanvasPopupMenu popup;

	private List<CanvasListener> canvasListeners;

	public Canvas(List<V> nodes, List<Edge<V>> edges,
			NodePropertySchema nodeSchema, EdgePropertySchema edgeSchema) {
		this.nodeSchema = nodeSchema;
		this.edgeSchema = edgeSchema;
		scaleX = Double.NaN;
		scaleY = Double.NaN;
		translationX = Double.NaN;
		translationY = Double.NaN;
		canvasListeners = new ArrayList<>();
		nodeHighlightConditions = new HighlightConditionList();
		edgeHighlightConditions = new HighlightConditionList();

		this.nodes = new LinkedHashSet<>();
		this.edges = new LinkedHashSet<>();
		CanvasUtils.copyNodesAndEdges(nodes, edges, this.nodes, this.edges);
		allNodes = nodes;
		allEdges = edges;
		nodeSaveMap = CanvasUtils.getElementsById(this.nodes);
		edgeSaveMap = CanvasUtils.getElementsById(this.edges);
		joinMap = new LinkedHashMap<>();
		collapsedNodes = new LinkedHashMap<>();
		metaNodeProperty = KnimeUtils.createNewValue(IS_META_NODE, nodeSchema
				.getMap().keySet());
		nodeSchema.getMap().put(metaNodeProperty, Boolean.class);

		viewer = new VisualizationViewer<>(new StaticLayout<>(
				new DirectedSparseMultigraph<V, Edge<V>>()));
		viewer.setBackground(Color.WHITE);
		viewer.addKeyListener(this);
		viewer.addMouseListener(this);
		viewer.getPickedVertexState().addItemListener(this);
		viewer.getPickedEdgeState().addItemListener(this);
		viewer.getRenderContext().setVertexFillPaintTransformer(
				new NodeFillTransformer<>(viewer,
						new LinkedHashMap<V, List<Double>>(),
						new ArrayList<Color>()));
		viewer.getRenderContext().setVertexStrokeTransformer(
				new NodeStrokeTransformer<V>(metaNodeProperty));
		viewer.getRenderContext().setEdgeDrawPaintTransformer(
				new EdgeDrawTransformer<>(viewer,
						new LinkedHashMap<Edge<V>, List<Double>>(),
						new ArrayList<Color>()));
		((MutableAffineTransformer) viewer.getRenderContext()
				.getMultiLayerTransformer().getTransformer(Layer.LAYOUT))
				.addChangeListener(this);
		viewer.addPostRenderPaintable(new PostPaintable(false));
		viewer.registerKeyboardAction(this, COPY, KeyStroke.getKeyStroke(
				KeyEvent.VK_C, ActionEvent.CTRL_MASK, false),
				JComponent.WHEN_FOCUSED);
		viewer.registerKeyboardAction(this, PASTE, KeyStroke.getKeyStroke(
				KeyEvent.VK_V, ActionEvent.CTRL_MASK, false),
				JComponent.WHEN_FOCUSED);
		viewer.getGraphLayout().setGraph(
				CanvasUtils.createGraph(this.nodes, this.edges));

		setLayout(new BorderLayout());
		add(viewer, BorderLayout.CENTER);
	}

	public void addCanvasListener(CanvasListener listener) {
		canvasListeners.add(listener);
	}

	public void removeCanvasListener(CanvasListener listener) {
		canvasListeners.remove(listener);
	}

	public Set<V> getNodes() {
		return nodes;
	}

	public Set<Edge<V>> getEdges() {
		return edges;
	}

	public Dimension getCanvasSize() {
		return viewer.getSize();
	}

	public void setCanvasSize(Dimension canvasSize) {
		viewer.setPreferredSize(canvasSize);
	}

	public Mode getEditingMode() {
		return optionsPanel.getEditingMode();
	}

	public void setEditingMode(Mode editingMode) {
		optionsPanel.setEditingMode(editingMode);
	}

	public boolean isShowLegend() {
		return optionsPanel.isShowLegend();
	}

	public void setShowLegend(boolean showLegend) {
		optionsPanel.setShowLegend(showLegend);
	}

	public boolean isJoinEdges() {
		return optionsPanel.isJoinEdges();
	}

	public void setJoinEdges(boolean joinEdges) {
		optionsPanel.setJoinEdges(joinEdges);
	}

	public boolean isSkipEdgelessNodes() {
		return optionsPanel.isSkipEdgelessNodes();
	}

	public void setSkipEdgelessNodes(boolean skipEdgelessNodes) {
		optionsPanel.setSkipEdgelessNodes(skipEdgelessNodes);
	}

	public int getFontSize() {
		return optionsPanel.getFontSize();
	}

	public void setFontSize(int fontSize) {
		optionsPanel.setFontSize(fontSize);
	}

	public boolean isFontBold() {
		return optionsPanel.isFontBold();
	}

	public void setFontBold(boolean fontBold) {
		optionsPanel.setFontBold(fontBold);
	}

	public int getNodeSize() {
		return optionsPanel.getNodeSize();
	}

	public void setNodeSize(int nodeSize) {
		optionsPanel.setNodeSize(nodeSize);
	}

	public boolean isArrowInMiddle() {
		return optionsPanel.isArrowInMiddle();
	}

	public void setArrowInMiddle(boolean arrowInMiddle) {
		optionsPanel.setArrowInMiddle(arrowInMiddle);
	}

	public String getLabel() {
		return optionsPanel.getLabel();
	}

	public void setLabel(String label) {
		optionsPanel.setLabel(label);
	}

	public int getBorderAlpha() {
		return optionsPanel.getBorderAlpha();
	}

	public void setBorderAlpha(int borderAlpha) {
		optionsPanel.setBorderAlpha(borderAlpha);
	}

	public NodePropertySchema getNodeSchema() {
		return nodeSchema;
	}

	public EdgePropertySchema getEdgeSchema() {
		return edgeSchema;
	}

	public Set<V> getSelectedNodes() {
		Set<V> selected = new LinkedHashSet<>(viewer.getPickedVertexState()
				.getPicked());

		selected.retainAll(nodes);

		return selected;
	}

	public void setSelectedNodes(Set<V> selectedNodes) {
		viewer.getPickedVertexState().clear();

		for (V node : selectedNodes) {
			if (nodes.contains(node)) {
				viewer.getPickedVertexState().pick(node, true);
			}
		}
	}

	public Set<Edge<V>> getSelectedEdges() {
		Set<Edge<V>> selected = new LinkedHashSet<>(viewer.getPickedEdgeState()
				.getPicked());

		selected.retainAll(edges);

		return selected;
	}

	public void setSelectedEdges(Set<Edge<V>> selectedEdges) {
		viewer.getPickedEdgeState().clear();

		for (Edge<V> edge : selectedEdges) {
			if (edges.contains(edge)) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	public Set<String> getSelectedNodeIds() {
		return CanvasUtils.getElementIds(getSelectedNodes());
	}

	public void setSelectedNodeIds(Set<String> selectedNodeIds) {
		setSelectedNodes(CanvasUtils.getElementsById(nodes, selectedNodeIds));
	}

	public Set<String> getSelectedEdgeIds() {
		return CanvasUtils.getElementIds(getSelectedEdges());
	}

	public void setSelectedEdgeIds(Set<String> selectedEdgeIds) {
		setSelectedEdges(CanvasUtils.getElementsById(edges, selectedEdgeIds));
	}

	public HighlightConditionList getNodeHighlightConditions() {
		return nodeHighlightConditions;
	}

	public void setNodeHighlightConditions(
			HighlightConditionList nodeHighlightConditions) {
		this.nodeHighlightConditions = nodeHighlightConditions;
		applyChanges();
		fireNodeHighlightingChanged();
	}

	public HighlightConditionList getEdgeHighlightConditions() {
		return edgeHighlightConditions;
	}

	public void setEdgeHighlightConditions(
			HighlightConditionList edgeHighlightConditions) {
		this.edgeHighlightConditions = edgeHighlightConditions;
		applyChanges();
		fireEdgeHighlightingChanged();
	}

	public Map<String, Set<String>> getCollapsedNodes() {
		return collapsedNodes;
	}

	public void setCollapsedNodes(Map<String, Set<String>> collapsedNodes) {
		this.collapsedNodes = collapsedNodes;
		applyChanges();
		fireCollapsedNodesChanged();
	}

	public double getScaleX() {
		return scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public double getTranslationX() {
		return translationX;
	}

	public double getTranslationY() {
		return translationY;
	}

	public void setTransform(double scaleX, double scaleY, double translationX,
			double translationY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.translationX = translationX;
		this.translationY = translationY;

		((MutableAffineTransformer) viewer.getRenderContext()
				.getMultiLayerTransformer().getTransformer(Layer.LAYOUT))
				.setTransform(new AffineTransform(scaleX, 0, 0, scaleY,
						translationX, translationY));
		applyTransform();
		viewer.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(COPY)) {
			List<String> selected = new ArrayList<>(getSelectedNodeIds());
			StringSelection stsel = new StringSelection(
					KnimeUtils.listToString(selected));

			Toolkit.getDefaultToolkit().getSystemClipboard()
					.setContents(stsel, stsel);
			JOptionPane.showMessageDialog(this, getNodeName()
					+ " selection has been copied to clipboard", "Clipboard",
					JOptionPane.INFORMATION_MESSAGE);
		} else if (e.getActionCommand().equals(PASTE)) {
			Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
			String selected = null;

			try {
				selected = (String) system.getContents(this).getTransferData(
						DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			setSelectedNodeIds(new LinkedHashSet<>(
					KnimeUtils.stringToList(selected)));
			JOptionPane.showMessageDialog(this, getNodeName()
					+ " selection has been pasted from clipboard", "Clipboard",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		AffineTransform transform = ((MutableAffineTransformer) viewer
				.getRenderContext().getMultiLayerTransformer()
				.getTransformer(Layer.LAYOUT)).getTransform();

		if (transform.getScaleX() != 0.0 && transform.getScaleY() != 0.0) {
			scaleX = transform.getScaleX();
			scaleY = transform.getScaleY();
			translationX = transform.getTranslateX();
			translationY = transform.getTranslateY();
		} else {
			scaleX = Double.NaN;
			scaleY = Double.NaN;
			translationX = Double.NaN;
			translationY = Double.NaN;
		}

		applyTransform();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getItem() instanceof Node) {
			if (viewer.getPickedVertexState().getPicked().isEmpty()) {
				popup.setNodeSelectionEnabled(false);
			} else {
				popup.setNodeSelectionEnabled(true);
			}

			fireNodeSelectionChanged();
		} else if (e.getItem() instanceof Edge) {
			if (viewer.getPickedEdgeState().getPicked().isEmpty()) {
				popup.setEdgeSelectionEnabled(false);
			} else {
				popup.setEdgeSelectionEnabled(true);
			}

			fireEdgeSelectionChanged();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Point2D center = viewer.getRenderContext().getMultiLayerTransformer()
				.inverseTransform(Layer.VIEW, viewer.getCenter());
		MutableTransformer transformer = viewer.getRenderContext()
				.getMultiLayerTransformer().getTransformer(Layer.LAYOUT);

		if (e.getKeyCode() == KeyEvent.VK_UP) {
			transformer.scale(1 / 1.1f, 1 / 1.1f, center);
			viewer.repaint();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			transformer.scale(1.1f, 1.1f, center);
			viewer.repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2) {
			switch (getEditingMode()) {
			case TRANSFORMING:
				setEditingMode(Mode.PICKING);
				viewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				break;
			case PICKING:
				setEditingMode(Mode.TRANSFORMING);
				viewer.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		viewer.requestFocus();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void saveAsItemClicked() {
		ImageFileChooser chooser = new ImageFileChooser();

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			if (chooser.getFileFormat() == ImageFileChooser.Format.PNG_FORMAT) {
				try {
					VisualizationImageServer<V, Edge<V>> server = getVisualizationServer(false);
					BufferedImage img = new BufferedImage(viewer.getWidth(),
							viewer.getHeight(), BufferedImage.TYPE_INT_RGB);

					server.paint(img.getGraphics());
					ImageIO.write(img, "png", chooser.getImageFile());
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this,
							"Error saving png file", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else if (chooser.getFileFormat() == ImageFileChooser.Format.SVG_FORMAT) {
				try {
					VisualizationImageServer<V, Edge<V>> server = getVisualizationServer(true);
					DOMImplementation domImpl = GenericDOMImplementation
							.getDOMImplementation();
					Document document = domImpl.createDocument(null, "svg",
							null);
					SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
					Writer outsvg = new OutputStreamWriter(
							new FileOutputStream(chooser.getImageFile()),
							StandardCharsets.UTF_8);

					svgGenerator.setSVGCanvasSize(new Dimension(viewer
							.getWidth(), viewer.getHeight()));
					server.paint(svgGenerator);
					svgGenerator.stream(outsvg, true);
					outsvg.close();
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this,
							"Error saving svg file", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public void selectConnectionsItemClicked() {
		Set<V> selected = getSelectedNodes();

		for (Edge<V> edge : edges) {
			if (selected.contains(edge.getFrom())
					&& selected.contains(edge.getTo())) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	@Override
	public void selectIncomingItemClicked() {
		Set<V> selected = getSelectedNodes();

		for (Edge<V> edge : edges) {
			if (selected.contains(edge.getTo())) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	@Override
	public void selectOutgoingItemClicked() {
		Set<V> selected = getSelectedNodes();

		for (Edge<V> edge : edges) {
			if (selected.contains(edge.getFrom())) {
				viewer.getPickedEdgeState().pick(edge, true);
			}
		}
	}

	@Override
	public void clearSelectedNodesItemClicked() {
		viewer.getPickedVertexState().clear();
	}

	@Override
	public void clearSelectedEdgesItemClicked() {
		viewer.getPickedEdgeState().clear();
	}

	@Override
	public void highlightSelectedNodesItemClicked() {
		HighlightListDialog dialog = openNodeHighlightDialog();
		List<List<LogicalHighlightCondition>> conditions = new ArrayList<>();

		for (String id : getSelectedNodeIds()) {
			LogicalHighlightCondition c = new LogicalHighlightCondition(
					nodeSchema.getId(), LogicalHighlightCondition.EQUAL_TYPE,
					id);

			conditions.add(Arrays.asList(c));
		}

		AndOrHighlightCondition condition = new AndOrHighlightCondition(
				conditions, null, false, Color.RED, false, false, null);

		dialog.setAutoAddCondition(condition);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setNodeHighlightConditions(dialog.getHighlightConditions());
		}
	}

	@Override
	public void highlightSelectedEdgesItemClicked() {
		HighlightListDialog dialog = openEdgeHighlightDialog();
		List<List<LogicalHighlightCondition>> conditions = new ArrayList<>();

		for (String id : getSelectedEdgeIds()) {
			LogicalHighlightCondition c = new LogicalHighlightCondition(
					edgeSchema.getId(), LogicalHighlightCondition.EQUAL_TYPE,
					id);

			conditions.add(Arrays.asList(c));
		}

		AndOrHighlightCondition condition = new AndOrHighlightCondition(
				conditions, null, false, Color.RED, false, false, null);

		dialog.setAutoAddCondition(condition);
		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setEdgeHighlightConditions(dialog.getHighlightConditions());
		}
	}

	@Override
	public void highlightNodesItemClicked() {
		HighlightListDialog dialog = openNodeHighlightDialog();

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setNodeHighlightConditions(dialog.getHighlightConditions());
		}
	}

	@Override
	public void highlightEdgesItemClicked() {
		HighlightListDialog dialog = openEdgeHighlightDialog();

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setEdgeHighlightConditions(dialog.getHighlightConditions());
		}
	}

	@Override
	public void clearHighlightedNodesItemClicked() {
		if (JOptionPane.showConfirmDialog(this,
				"Do you really want to remove all "
						+ getNodeName().toLowerCase()
						+ " highlight conditions?", "Please Confirm",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			setNodeHighlightConditions(new HighlightConditionList());
		}
	}

	@Override
	public void clearHighlightedEdgesItemClicked() {
		if (JOptionPane.showConfirmDialog(this,
				"Do you really want to remove all "
						+ getEdgeName().toLowerCase()
						+ " highlight conditions?", "Please Confirm",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			setEdgeHighlightConditions(new HighlightConditionList());
		}
	}

	@Override
	public void selectHighlightedNodesItemClicked() {
		HighlightSelectionDialog dialog = new HighlightSelectionDialog(this,
				nodeHighlightConditions.getConditions());

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedNodes(CanvasUtils.getHighlightedElements(nodes,
					dialog.getHighlightConditions()));
		}
	}

	@Override
	public void selectHighlightedEdgesItemClicked() {
		HighlightSelectionDialog dialog = new HighlightSelectionDialog(this,
				edgeHighlightConditions.getConditions());

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedEdges(CanvasUtils.getHighlightedElements(edges,
					dialog.getHighlightConditions()));
		}

	}

	@Override
	public void selectNodesItemClicked() {
		HighlightDialog dialog = new HighlightDialog(this, nodeSchema.getMap(),
				false, false, false, false, false, false, null, null);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedNodes(CanvasUtils.getHighlightedElements(nodes,
					Arrays.asList(dialog.getHighlightCondition())));
		}
	}

	@Override
	public void selectEdgesItemClicked() {
		HighlightDialog dialog = new HighlightDialog(this, edgeSchema.getMap(),
				false, false, false, false, false, false, null, null);

		dialog.setVisible(true);

		if (dialog.isApproved()) {
			setSelectedEdges(CanvasUtils.getHighlightedElements(edges,
					Arrays.asList(dialog.getHighlightCondition())));
		}
	}

	@Override
	public void nodePropertiesItemClicked() {
		PropertiesDialog<V> dialog = PropertiesDialog.createNodeDialog(this,
				getSelectedNodes(), nodeSchema, true);

		dialog.setVisible(true);
	}

	@Override
	public void edgePropertiesItemClicked() {
		PropertiesDialog<V> dialog = PropertiesDialog.createEdgeDialog(this,
				getSelectedEdges(), edgeSchema, true);

		dialog.setVisible(true);
	}

	@Override
	public void nodeAllPropertiesItemClicked() {
		Set<V> pickedAll = new LinkedHashSet<>();

		for (V node : getSelectedNodes()) {
			if (collapsedNodes.containsKey(node.getId())) {
				for (String id : collapsedNodes.get(node.getId())) {
					pickedAll.add(nodeSaveMap.get(id));
				}
			} else {
				pickedAll.add(node);
			}
		}

		PropertiesDialog<V> dialog = PropertiesDialog.createNodeDialog(this,
				pickedAll, nodeSchema, false);

		dialog.setVisible(true);
	}

	@Override
	public void edgeAllPropertiesItemClicked() {
		Set<Edge<V>> allPicked = new LinkedHashSet<>();

		for (Edge<V> p : getSelectedEdges()) {
			if (joinMap.containsKey(p)) {
				allPicked.addAll(joinMap.get(p));
			} else {
				allPicked.add(p);
			}
		}

		PropertiesDialog<V> dialog = PropertiesDialog.createEdgeDialog(this,
				allPicked, edgeSchema, false);

		dialog.setVisible(true);
	}

	@Override
	public void collapseToNodeItemClicked() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : selectedIds) {
			if (collapsedNodes.keySet().contains(id)) {
				JOptionPane.showMessageDialog(this, "Some of the selected "
						+ getNodesName().toLowerCase()
						+ " are already collapsed", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		String newId = CanvasUtils.openNewIdDialog(this, nodeSaveMap.keySet(),
				getNodeName());

		collapsedNodes.put(newId, selectedIds);
		applyChanges();
		fireCollapsedNodesChanged();
		setSelectedNodeIds(new LinkedHashSet<>(Arrays.asList(newId)));
	}

	@Override
	public void expandFromNodeItemClicked() {
		Set<String> selectedIds = getSelectedNodeIds();

		for (String id : selectedIds) {
			if (!collapsedNodes.keySet().contains(id)) {
				JOptionPane.showMessageDialog(this, "Some of the selected "
						+ getNodesName().toLowerCase() + " are not collapsed",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		Set<String> newIds = new LinkedHashSet<>();

		for (String id : selectedIds) {
			newIds.addAll(collapsedNodes.remove(id));
			nodeSaveMap.remove(id);
		}

		applyChanges();
		fireCollapsedNodesChanged();
		setSelectedNodeIds(newIds);
	}

	@Override
	public void collapseByPropertyItemClicked() {
		Map<Object, Set<V>> nodesByProperty = CanvasUtils
				.openCollapseByPropertyDialog(this, nodeSchema.getMap()
						.keySet(), CanvasUtils.getElementIds(allNodes),
						nodeSaveMap);

		if (nodesByProperty.isEmpty()) {
			return;
		}

		for (String id : collapsedNodes.keySet()) {
			nodeSaveMap.remove(id);
		}

		collapsedNodes.clear();

		for (Object value : nodesByProperty.keySet()) {
			String newId = KnimeUtils.createNewValue(value.toString(),
					nodeSaveMap.keySet());

			collapsedNodes.put(newId,
					CanvasUtils.getElementIds(nodesByProperty.get(value)));
		}

		applyChanges();
		fireCollapsedNodesChanged();
		setSelectedNodeIds(collapsedNodes.keySet());
	}

	@Override
	public void clearCollapsedNodesItemClicked() {
		for (String id : collapsedNodes.keySet()) {
			nodeSaveMap.remove(id);
		}

		collapsedNodes.clear();
		applyChanges();
		fireCollapsedNodesChanged();
		viewer.getPickedVertexState().clear();
	}

	@Override
	public void editingModeChanged() {
		viewer.setGraphMouse(createMouseModel(optionsPanel.getEditingMode()));
	}

	@Override
	public void showLegendChanged() {
		viewer.repaint();
	}

	@Override
	public void joinEdgesChanged() {
		applyChanges();
		fireEdgeJoinChanged();
	}

	@Override
	public void skipEdgelessNodesChanged() {
		applyChanges();
		fireSkipEdgelessChanged();
	}

	@Override
	public void fontChanged() {
		viewer.getRenderContext().setVertexFontTransformer(
				new FontTransformer<V>(optionsPanel.getFontSize(), optionsPanel
						.isFontBold()));
		viewer.getRenderContext().setEdgeFontTransformer(
				new FontTransformer<Edge<V>>(optionsPanel.getFontSize(),
						optionsPanel.isFontBold()));
		viewer.repaint();
	}

	@Override
	public void nodeSizeChanged() {
		applyChanges();
	}

	@Override
	public void arrowInMiddleChanged() {
		viewer.getRenderer()
				.getEdgeRenderer()
				.setEdgeArrowRenderingSupport(
						optionsPanel.isArrowInMiddle() ? new MiddleEdgeArrowRenderingSupport<>()
								: new BasicEdgeArrowRenderingSupport<>());
		viewer.repaint();
	}

	@Override
	public void labelChanged() {
		viewer.repaint();
	}

	public VisualizationViewer<V, Edge<V>> getViewer() {
		return viewer;
	}

	public VisualizationImageServer<V, Edge<V>> getVisualizationServer(
			final boolean toSvg) {
		VisualizationImageServer<V, Edge<V>> server = new VisualizationImageServer<>(
				viewer.getGraphLayout(), viewer.getSize());

		server.setBackground(Color.WHITE);
		server.setRenderContext(viewer.getRenderContext());
		server.setRenderer(viewer.getRenderer());
		server.addPostRenderPaintable(new PostPaintable(true));

		return server;
	}

	public CanvasOptionsPanel getOptionsPanel() {
		return optionsPanel;
	}

	public void setOptionsPanel(CanvasOptionsPanel optionsPanel) {
		if (this.optionsPanel != null) {
			remove(this.optionsPanel);
		}

		this.optionsPanel = optionsPanel;
		optionsPanel.addChangeListener(this);
		add(optionsPanel, BorderLayout.SOUTH);
		revalidate();
	}

	public CanvasPopupMenu getPopupMenu() {
		return popup;
	}

	public void setPopupMenu(CanvasPopupMenu popup) {
		this.popup = popup;
		popup.addClickListener(this);
		viewer.setComponentPopupMenu(popup);
	}

	public void applyChanges() {
		Set<String> selectedNodeIds = getSelectedNodeIds();
		Set<String> selectedEdgeIds = getSelectedEdgeIds();

		applyNodeCollapse();
		applyInvisibility();
		applyJoinEdgesAndSkipEdgeless();
		viewer.getGraphLayout().setGraph(CanvasUtils.createGraph(nodes, edges));
		applyHighlights();

		setSelectedNodeIds(selectedNodeIds);
		setSelectedEdgeIds(selectedEdgeIds);
		viewer.repaint();
	}

	public void applyNodeCollapse() {
		nodes.clear();
		edges.clear();

		Map<String, String> collapseTo = new LinkedHashMap<>();

		for (String to : collapsedNodes.keySet()) {
			for (String from : collapsedNodes.get(to)) {
				collapseTo.put(from, to);
			}
		}

		Map<String, V> nodesById = new LinkedHashMap<>();

		for (String id : CanvasUtils.getElementIds(allNodes)) {
			if (!collapseTo.keySet().contains(id)) {
				V newNode = nodeSaveMap.get(id);

				nodes.add(newNode);
				nodesById.put(id, newNode);
			}
		}

		Set<V> metaNodes = new LinkedHashSet<>();

		for (String newId : collapsedNodes.keySet()) {
			V newNode = nodeSaveMap.get(newId);

			if (newNode == null) {
				Set<V> nodes = CanvasUtils.getElementsById(nodeSaveMap,
						collapsedNodes.get(newId));

				newNode = createMetaNode(newId, nodes);
				nodeSaveMap.put(newId, newNode);
			}

			nodes.add(newNode);
			nodesById.put(newNode.getId(), newNode);
			metaNodes.add(newNode);
		}

		for (Edge<V> edge : allEdges) {
			V from = nodesById.get(edge.getFrom().getId());
			V to = nodesById.get(edge.getTo().getId());

			if (from == null) {
				from = nodesById.get(collapseTo.get(edge.getFrom().getId()));
			}

			if (to == null) {
				to = nodesById.get(collapseTo.get(edge.getTo().getId()));
			}

			if (from == to && metaNodes.contains(from)) {
				continue;
			}

			Edge<V> newEdge = edgeSaveMap.get(edge.getId());

			if (!newEdge.getFrom().equals(from) || !newEdge.getTo().equals(to)) {
				newEdge = new Edge<>(newEdge.getId(), newEdge.getProperties(),
						from, to);
				newEdge.getProperties().put(edgeSchema.getFrom(), from.getId());
				newEdge.getProperties().put(edgeSchema.getTo(), to.getId());
				edgeSaveMap.put(newEdge.getId(), newEdge);
			}

			edges.add(newEdge);
		}
	}

	public void applyInvisibility() {
		CanvasUtils.removeInvisibleElements(nodes, nodeHighlightConditions);
		CanvasUtils.removeInvisibleElements(edges, edgeHighlightConditions);
		CanvasUtils.removeNodelessEdges(edges, nodes);
	}

	public void applyJoinEdgesAndSkipEdgeless() {
		joinMap.clear();

		if (isJoinEdges()) {
			joinMap = CanvasUtils.joinEdges(edges, edgeSchema,
					CanvasUtils.getElementIds(allEdges));
			edges = new LinkedHashSet<>(joinMap.keySet());
		}

		if (isSkipEdgelessNodes()) {
			CanvasUtils.removeEdgelessNodes(nodes, edges);
		}
	}

	public void applyHighlights() {
		CanvasUtils.applyNodeHighlights(viewer, nodeHighlightConditions,
				getNodeSize());
		CanvasUtils.applyEdgeHighlights(viewer, edgeHighlightConditions);
	}

	protected String getNodeName() {
		return DEFAULT_NODE_NAME;
	}

	protected String getEdgeName() {
		return DEFAULT_EDGE_NAME;
	}

	protected String getNodesName() {
		return DEFAULT_NODES_NAME;
	}

	protected String getEdgesName() {
		return DEFAULT_EDGES_NAME;
	}

	protected Point2D toGraphCoordinates(int x, int y) {
		return new Point2D.Double((x - translationX) / scaleX,
				(y - translationY) / scaleY);
	}

	protected Point toWindowsCoordinates(double x, double y) {
		return new Point((int) (x * scaleX + translationX),
				(int) (y * scaleY + translationY));
	}

	protected HighlightListDialog openNodeHighlightDialog() {
		return new HighlightListDialog(this, nodeSchema.getMap(),
				nodeHighlightConditions);
	}

	protected HighlightListDialog openEdgeHighlightDialog() {
		HighlightListDialog dialog = new HighlightListDialog(this,
				edgeSchema.getMap(), edgeHighlightConditions);

		dialog.addChecker(new EdgeHighlightChecker());

		return dialog;
	}

	protected abstract void applyTransform();

	protected abstract GraphMouse<V, Edge<V>> createMouseModel(Mode editingMode);

	protected abstract V createMetaNode(String id, Collection<V> nodes);

	private void fireNodeSelectionChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.nodeSelectionChanged(this);
		}
	}

	private void fireEdgeSelectionChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.edgeSelectionChanged(this);
		}
	}

	private void fireNodeHighlightingChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.nodeHighlightingChanged(this);
		}
	}

	private void fireEdgeHighlightingChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.edgeHighlightingChanged(this);
		}
	}

	private void fireEdgeJoinChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.edgeJoinChanged(this);
		}
	}

	private void fireSkipEdgelessChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.skipEdgelessChanged(this);
		}
	}

	private void fireCollapsedNodesChanged() {
		for (CanvasListener listener : canvasListeners) {
			listener.collapsedNodesChanged(this);
		}
	}

	private class EdgeHighlightChecker implements HighlightConditionChecker {

		@Override
		public String findError(HighlightCondition condition) {
			String error = "The column \""
					+ edgeSchema.getId()
					+ "\" cannot be used with \"Invisible\" option as it is used as "
					+ getEdgeName().toLowerCase() + " ID";

			if (condition != null && condition.isInvisible()) {
				AndOrHighlightCondition logicalCondition = null;
				ValueHighlightCondition valueCondition = null;

				if (condition instanceof AndOrHighlightCondition) {
					logicalCondition = (AndOrHighlightCondition) condition;
				} else if (condition instanceof ValueHighlightCondition) {
					valueCondition = (ValueHighlightCondition) condition;
				} else if (condition instanceof LogicalValueHighlightCondition) {
					logicalCondition = ((LogicalValueHighlightCondition) condition)
							.getLogicalCondition();
					valueCondition = ((LogicalValueHighlightCondition) condition)
							.getValueCondition();
				}

				if (logicalCondition != null) {
					for (List<LogicalHighlightCondition> cc : logicalCondition
							.getConditions()) {
						for (LogicalHighlightCondition c : cc) {
							if (edgeSchema.getId().equals(c.getProperty())) {
								return error;
							}
						}
					}
				}

				if (valueCondition != null) {
					if (edgeSchema.getId().equals(valueCondition.getProperty())) {
						return error;
					}
				}
			}

			return null;
		}
	}

	private class PostPaintable implements Paintable {

		private boolean toImage;

		public PostPaintable(boolean toImage) {
			this.toImage = toImage;
		}

		@Override
		public boolean useTransform() {
			return false;
		}

		@Override
		public void paint(Graphics g) {
			if (getLabel() != null && !getLabel().isEmpty()) {
				paintLabel(g);
			}

			if (optionsPanel.isShowLegend()) {
				new CanvasLegend<>(Canvas.this, nodeHighlightConditions, nodes,
						edgeHighlightConditions, edges).paint(g,
						getCanvasSize().width, getCanvasSize().height,
						optionsPanel.getFontSize(), optionsPanel.isFontBold());
			}

			if (toImage) {
				g.setColor(Color.BLACK);
				g.drawRect(0, 0, getCanvasSize().width - 1,
						getCanvasSize().height - 1);
			}
		}

		private void paintLabel(Graphics g) {
			int w = getCanvasSize().width;
			Font font = new Font("Default", Font.BOLD, 10);
			int fontHeight = g.getFontMetrics(font).getHeight();
			int fontAscent = g.getFontMetrics(font).getAscent();
			int dy = 2;

			int dx = 5;
			int sw = (int) font.getStringBounds(getLabel(),
					((Graphics2D) g).getFontRenderContext()).getWidth();

			g.setColor(new Color(230, 230, 230));
			g.fillRect(w - sw - 2 * dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
			g.setColor(Color.BLACK);
			g.drawRect(w - sw - 2 * dx, -1, sw + 2 * dx, fontHeight + 2 * dy);
			g.setFont(font);
			g.drawString(getLabel(), w - sw - dx, dy + fontAscent);
		}
	}
}
