package de.bund.bfr.knime.openkrise;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.port.PortObjectSpec;

/**
 * <code>NodeDialog</code> for the "MyKrisenInterfaces" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author draaw
 */
public class MyKrisenInterfacesNodeDialog extends NodeDialogPane {

	private DbConfigurationUi dbui;
	private JCheckBox doCrossContaminateAll, enforceTemporalOrder;
	private JCheckBox doAnonymize;

	protected MyKrisenInterfacesNodeDialog() {
		JPanel panel = new JPanel();
    	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));   	
    	
    	dbui = new DbConfigurationUi();

    	JPanel panelTracing = new JPanel();
    	panelTracing.setBorder(new TitledBorder("Tracing"));
    	doCrossContaminateAll = new JCheckBox(); doCrossContaminateAll.setText("cross-contamination?"); panelTracing.add(doCrossContaminateAll);
    	enforceTemporalOrder = new JCheckBox(); enforceTemporalOrder.setText("enforce temporal order?"); enforceTemporalOrder.setSelected(true); panelTracing.add(enforceTemporalOrder);
    	panel.add(panelTracing);
    	
    	doAnonymize = new JCheckBox(); doAnonymize.setText("Anonymize?"); panel.add(doAnonymize);
    	
    	addTab("Tracing/Filtering", panel);
    	addTab("Database connection", dbui);
    }
	
	@Override
	protected void saveSettingsTo( final NodeSettingsWO settings )
			throws InvalidSettingsException {
		
		settings.addString( MyKrisenInterfacesNodeModel.PARAM_FILENAME, dbui.getFilename() );
		settings.addString( MyKrisenInterfacesNodeModel.PARAM_LOGIN, dbui.getLogin() );
		settings.addString( MyKrisenInterfacesNodeModel.PARAM_PASSWD, dbui.getPasswd() );
		settings.addBoolean( MyKrisenInterfacesNodeModel.PARAM_OVERRIDE, dbui.isOverride() );
		//settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_TRACINGBACK, tracingBack.isSelected());
		//settings.addInt(MyKrisenInterfacesNodeModel.PARAM_TRACINGTYPE, tracingType.getSelectedIndex());
		settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_CC, doCrossContaminateAll.isSelected());
		settings.addBoolean(MyKrisenInterfacesNodeModel.PARAM_ETO, enforceTemporalOrder.isSelected());
		settings.addBoolean( MyKrisenInterfacesNodeModel.PARAM_ANONYMIZE, doAnonymize.isSelected() );
		
	}

	@Override
	protected void loadSettingsFrom( final NodeSettingsRO settings, final PortObjectSpec[] specs )  {		
		try {
			
			dbui.setFilename( settings.getString( MyKrisenInterfacesNodeModel.PARAM_FILENAME ) );
			dbui.setLogin( settings.getString( MyKrisenInterfacesNodeModel.PARAM_LOGIN ) );
			dbui.setPasswd( settings.getString( MyKrisenInterfacesNodeModel.PARAM_PASSWD ) );
			dbui.setOverride( settings.getBoolean( MyKrisenInterfacesNodeModel.PARAM_OVERRIDE ) );
			//tracingBack.setSelected(settings.getBoolean(MyKrisenInterfacesNodeModel.PARAM_TRACINGBACK));
			//tracingType.setSelectedIndex(settings.getInt(MyKrisenInterfacesNodeModel.PARAM_TRACINGTYPE));
			if (settings.containsKey(MyKrisenInterfacesNodeModel.PARAM_CC)) doCrossContaminateAll.setSelected(settings.getBoolean(MyKrisenInterfacesNodeModel.PARAM_CC));
			if (settings.containsKey(MyKrisenInterfacesNodeModel.PARAM_ETO)) enforceTemporalOrder.setSelected(settings.getBoolean(MyKrisenInterfacesNodeModel.PARAM_ETO));
			doAnonymize.setSelected(settings.getBoolean(MyKrisenInterfacesNodeModel.PARAM_ANONYMIZE));

		}
		catch( InvalidSettingsException ex ) {
			
			ex.printStackTrace( System.err );
		}
		
	}
}