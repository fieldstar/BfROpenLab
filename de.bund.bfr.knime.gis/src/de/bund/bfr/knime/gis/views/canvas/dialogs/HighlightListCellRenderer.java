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
package de.bund.bfr.knime.gis.views.canvas.dialogs;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.bund.bfr.knime.gis.views.canvas.highlighting.HighlightCondition;

public class HighlightListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);

		Color color = ((HighlightCondition) value).getColor();

		if (color == null) {
			color = Color.WHITE;
		}

		setBorder(BorderFactory.createMatteBorder(0, 20, 0, 0, color));
		setText(" " + getText());

		return this;
	}
}