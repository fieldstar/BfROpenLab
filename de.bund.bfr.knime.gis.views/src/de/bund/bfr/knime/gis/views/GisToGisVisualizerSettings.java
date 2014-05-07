/*******************************************************************************
 * Copyright (c) 2014 Federal Institute for Risk Assessment (BfR), Germany 
 * 
 * Developers and contributors are 
 * Christian Thoens (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
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
package de.bund.bfr.knime.gis.views;

import java.awt.Dimension;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.gis.views.canvas.RegionCanvas;

public class GisToGisVisualizerSettings extends SimpleGraphVisualizerSettings {

	public static final int DEFAULT_GIS_BORDER_ALPHA = 255;
	public static final String DEFAULT_GIS_EDITING_MODE = RegionCanvas.PICKING_MODE;
	public static final Dimension DEFAULT_GIS_CANVAS_SIZE = new Dimension(400,
			600);

	private static final String CFG_SHAPE_COLUMN = "ShapeColumn";

	private static final String CFG_GIS_SCALE_X = "GisScaleX";
	private static final String CFG_GIS_SCALE_Y = "GisScaleY";
	private static final String CFG_GIS_TRANSLATION_X = "GisTranslationX";
	private static final String CFG_GIS_TRANSLATION_Y = "GisTranslationY";
	private static final String CFG_GIS_BORDER_ALPHA = "GisBorderAlpha";
	private static final String CFG_GIS_EDITING_MODE = "GisEditingMode";
	private static final String CFG_GIS_CANVAS_SIZE = "GisCanvasSize";

	private String shapeColumn;

	private double gisScaleX;
	private double gisScaleY;
	private double gisTranslationX;
	private double gisTranslationY;
	private int gisBorderAlpha;
	private String gisEditingMode;
	private Dimension gisCanvasSize;

	public GisToGisVisualizerSettings() {
		shapeColumn = null;

		gisScaleX = Double.NaN;
		gisScaleY = Double.NaN;
		gisTranslationX = Double.NaN;
		gisTranslationY = Double.NaN;
		gisBorderAlpha = DEFAULT_GIS_BORDER_ALPHA;
		gisEditingMode = DEFAULT_GIS_EDITING_MODE;
		gisCanvasSize = DEFAULT_GIS_CANVAS_SIZE;
	}

	@Override
	public void loadSettings(NodeSettingsRO settings) {
		super.loadSettings(settings);

		try {
			shapeColumn = settings.getString(CFG_SHAPE_COLUMN);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisScaleX = settings.getDouble(CFG_GIS_SCALE_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisScaleY = settings.getDouble(CFG_GIS_SCALE_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisTranslationX = settings.getDouble(CFG_GIS_TRANSLATION_X);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisTranslationY = settings.getDouble(CFG_GIS_TRANSLATION_Y);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisBorderAlpha = settings.getInt(CFG_GIS_BORDER_ALPHA);
		} catch (InvalidSettingsException e) {
		}

		try {
			gisEditingMode = settings.getString(CFG_GIS_EDITING_MODE);
		} catch (InvalidSettingsException e) {
		}
		try {
			gisCanvasSize = (Dimension) SERIALIZER.fromXml(settings
					.getString(CFG_GIS_CANVAS_SIZE));
		} catch (InvalidSettingsException e) {
		}
	}

	@Override
	public void saveSettings(NodeSettingsWO settings) {
		super.saveSettings(settings);

		settings.addString(CFG_SHAPE_COLUMN, shapeColumn);

		settings.addDouble(CFG_GIS_SCALE_X, gisScaleX);
		settings.addDouble(CFG_GIS_SCALE_Y, gisScaleY);
		settings.addDouble(CFG_GIS_TRANSLATION_X, gisTranslationX);
		settings.addDouble(CFG_GIS_TRANSLATION_Y, gisTranslationY);
		settings.addInt(CFG_GIS_BORDER_ALPHA, gisBorderAlpha);
		settings.addString(CFG_GIS_EDITING_MODE, gisEditingMode);
		settings.addString(CFG_GIS_CANVAS_SIZE, SERIALIZER.toXml(gisCanvasSize));
	}

	public String getShapeColumn() {
		return shapeColumn;
	}

	public void setShapeColumn(String shapeColumn) {
		this.shapeColumn = shapeColumn;
	}

	public double getGisScaleX() {
		return gisScaleX;
	}

	public void setGisScaleX(double gisScaleX) {
		this.gisScaleX = gisScaleX;
	}

	public double getGisScaleY() {
		return gisScaleY;
	}

	public void setGisScaleY(double gisScaleY) {
		this.gisScaleY = gisScaleY;
	}

	public double getGisTranslationX() {
		return gisTranslationX;
	}

	public void setGisTranslationX(double gisTranslationX) {
		this.gisTranslationX = gisTranslationX;
	}

	public double getGisTranslationY() {
		return gisTranslationY;
	}

	public void setGisTranslationY(double gisTranslationY) {
		this.gisTranslationY = gisTranslationY;
	}

	public int getGisBorderAlpha() {
		return gisBorderAlpha;
	}

	public void setGisBorderAlpha(int gisBorderAlpha) {
		this.gisBorderAlpha = gisBorderAlpha;
	}

	public String getGisEditingMode() {
		return gisEditingMode;
	}

	public void setGisEditingMode(String gisEditingMode) {
		this.gisEditingMode = gisEditingMode;
	}

	public Dimension getGisCanvasSize() {
		return gisCanvasSize;
	}

	public void setGisCanvasSize(Dimension gisCanvasSize) {
		this.gisCanvasSize = gisCanvasSize;
	}

}