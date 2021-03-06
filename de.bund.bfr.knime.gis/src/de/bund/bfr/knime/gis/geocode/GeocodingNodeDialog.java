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
package de.bund.bfr.knime.gis.geocode;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import com.google.common.collect.Lists;

import de.bund.bfr.knime.IO;
import de.bund.bfr.knime.UI;
import de.bund.bfr.knime.ui.ColumnComboBox;

/**
 * <code>NodeDialog</code> for the "Geocoding" Node.
 * 
 * @author Christian Thoens
 */
public class GeocodingNodeDialog extends NodeDialogPane {

	private GeocodingSettings set;

	private JComboBox<GeocodingSettings.Provider> providerBox;
	private JTextField delayField;
	private JComboBox<GeocodingSettings.Multiple> multipleBox;

	private ColumnComboBox addressBox;
	private ColumnComboBox streetBox;
	private ColumnComboBox cityBox;
	private ColumnComboBox zipBox;
	private ColumnComboBox countryCodeBox;
	private JTextField serverField;

	private JPanel panel;

	/**
	 * New pane for configuring the Geocoding node.
	 */
	public GeocodingNodeDialog() {
		set = new GeocodingSettings();
		providerBox = new JComboBox<>(GeocodingSettings.Provider.values());
		providerBox.addActionListener(e -> updatePanel());
		delayField = new JTextField();
		multipleBox = new JComboBox<>(GeocodingSettings.Multiple.values());

		addressBox = new ColumnComboBox(false);
		streetBox = new ColumnComboBox(true);
		cityBox = new ColumnComboBox(true);
		zipBox = new ColumnComboBox(true);
		countryCodeBox = new ColumnComboBox(false);
		serverField = new JTextField();

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		addTab("Options", UI.createNorthPanel(panel));
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		addressBox.removeAllColumns();
		streetBox.removeAllColumns();
		cityBox.removeAllColumns();
		zipBox.removeAllColumns();
		countryCodeBox.removeAllColumns();

		for (DataColumnSpec column : IO.getColumns(specs[0], StringCell.TYPE)) {
			addressBox.addColumn(column);
			streetBox.addColumn(column);
			cityBox.addColumn(column);
			zipBox.addColumn(column);
			countryCodeBox.addColumn(column);
		}

		set.loadSettings(settings);

		providerBox.setSelectedItem(set.getServiceProvider());
		delayField.setText(set.getRequestDelay() + "");
		multipleBox.setSelectedItem(set.getMultipleResults());

		addressBox.setSelectedColumnName(set.getAddressColumn());
		streetBox.setSelectedColumnName(set.getStreetColumn());
		cityBox.setSelectedColumnName(set.getCityColumn());
		zipBox.setSelectedColumnName(set.getZipColumn());
		countryCodeBox.setSelectedColumnName(set.getCountryCodeColumn());
		if (set.getServiceProvider() == GeocodingSettings.Provider.GISGRAPHY) {
			serverField.setText(set.getGisgraphyServer() != null ? set.getGisgraphyServer() : "");
		}
		else if (set.getServiceProvider() == GeocodingSettings.Provider.PHOTON) {
			serverField.setText(set.getPhotonServer() != null ? set.getPhotonServer() : "");
		}
		else {
			serverField.setText("");
		}

		updatePanel();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		if (delayField.getText().trim().isEmpty()) {
			throw new InvalidSettingsException("No Request Delay specified");
		}

		try {
			Integer.parseInt(delayField.getText());
		} catch (NumberFormatException e) {
			throw new InvalidSettingsException("Request Delay invalid");
		}

		set.setAddressColumn(addressBox.getSelectedColumnName());
		set.setServiceProvider((GeocodingSettings.Provider) providerBox.getSelectedItem());
		set.setRequestDelay(Integer.parseInt(delayField.getText()));
		set.setMultipleResults((GeocodingSettings.Multiple) multipleBox.getSelectedItem());

		if (set.getServiceProvider() == GeocodingSettings.Provider.GISGRAPHY) {
			if (serverField.getText().trim().isEmpty()) {
				throw new InvalidSettingsException("No Server specified");
			}

			set.setCountryCodeColumn(countryCodeBox.getSelectedColumnName());
			set.setGisgraphyServer(serverField.getText().trim());
		} else if (set.getServiceProvider() == GeocodingSettings.Provider.PHOTON) {
			if (serverField.getText().trim().isEmpty()) {
				throw new InvalidSettingsException("No Server specified");
			}

			set.setPhotonServer(serverField.getText().trim());
		} else if (set.getServiceProvider() == GeocodingSettings.Provider.MAPQUEST) {
			set.setStreetColumn(streetBox.getSelectedColumnName());
			set.setCityColumn(cityBox.getSelectedColumnName());
			set.setZipColumn(zipBox.getSelectedColumnName());
		}

		set.saveSettings(settings);
	}

	private void updatePanel() {
		List<JLabel> addressLabels = Lists.newArrayList(new JLabel("Address:"));
		List<ColumnComboBox> addressBoxes = Lists.newArrayList(addressBox);
		List<JLabel> otherLabels = Lists.newArrayList(new JLabel("Delay between Request (ms):"),
				new JLabel("When multiple Results:"));
		List<Component> otherFields = Lists.newArrayList(delayField, multipleBox);

		if (providerBox.getSelectedItem() == GeocodingSettings.Provider.GISGRAPHY) {
			addressLabels.add(new JLabel("Country Code:"));
			addressBoxes.add(countryCodeBox);
			otherLabels.add(0, new JLabel("Server Address:"));
			otherFields.add(0, serverField);
		} else if (providerBox.getSelectedItem() == GeocodingSettings.Provider.PHOTON) {
			otherLabels.add(0, new JLabel("Server Address:"));
			otherFields.add(0, serverField);
		} else if (providerBox.getSelectedItem() == GeocodingSettings.Provider.MAPQUEST) {
			addressLabels.add(new JLabel("Street:")); addressBoxes.add(streetBox);
			addressLabels.add(new JLabel("City:")); addressBoxes.add(cityBox);
			addressLabels.add(new JLabel("Zip:")); addressBoxes.add(zipBox);
			addressLabels.add(new JLabel("Country Code:")); addressBoxes.add(countryCodeBox);
		}

		panel.removeAll();
		panel.add(UI.createOptionsPanel("Provider", Arrays.asList(new JLabel("Service Provider")),
				Arrays.asList(providerBox)));
		panel.add(UI.createOptionsPanel("Addresses", addressLabels, addressBoxes));
		panel.add(UI.createOptionsPanel("Other Options", otherLabels, otherFields));
		panel.revalidate();
	}
}
