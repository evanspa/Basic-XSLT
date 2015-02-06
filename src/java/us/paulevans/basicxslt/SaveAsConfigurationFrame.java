/*
	Copyright 2006 Paul Evans

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License. 
	You may obtain a copy of the License at 

		http://www.apache.org/licenses/LICENSE-2.0 

	Unless required by applicable law or agreed to in writing, software 
	distributed under the License is distributed on an "AS IS" BASIS, 
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
	See the License for the specific language governing permissions and 
	limitations under the License.
 */
package us.paulevans.basicxslt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.blueslate.commons.gui.GUIUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Defines the save-as configuration frame.
 * @author pevans
 *
 */
public class SaveAsConfigurationFrame extends JFrame 
	implements ActionListener {		
	
	// get the i18n factory singleton instance...
	private static final LabelStringFactory stringFactory = 
		LabelStringFactory.getInstance();
	
	// frame width and height values...
	private static final int FRAME_WIDTH = 470;
	private static final int FRAME_HEIGHT = 195;

	// instance members...
	private JButton cancelBtn, okayBtn;
	private UserPreferences userPrefs;
	private JTextField configurationName;
	private JCheckBox makeDefault;
	private BasicXSLTFrame parent;

	/**
	 * Constructor
	 * @param aParent
	 * @param aCurrentConfiguration
	 */
	public SaveAsConfigurationFrame(BasicXSLTFrame aParent, 
			String aCurrentConfiguration) {
		
		JPanel southPanel, mainPanel;
		
		parent = aParent;
		userPrefs = Utils.getUserPrefs();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				dispose();
			}
		});	
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(buildMainPanel(), BorderLayout.CENTER);		
		southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		southPanel.add(okayBtn = new JButton(stringFactory.getString(
				LabelStringFactory.OK_BUTTON)));
		southPanel.add(cancelBtn = new JButton(stringFactory.getString(
				LabelStringFactory.CANCEL_BUTTON)));
		okayBtn.addActionListener(this);
		cancelBtn.addActionListener(this);		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(buildNorthPanel(), BorderLayout.NORTH);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		getContentPane().add(new JScrollPane(mainPanel), BorderLayout.CENTER);		
		setTitle(stringFactory.getString(
				LabelStringFactory.SAVEASCONFIG_FRAME_SAVE_CONFIGURATION_AS));		
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		GUIUtils.center(this, aParent);
		setVisible(true);
		configurationName.requestFocus();
	}
	
	/**
	 * Builds the north panel
	 * @return
	 */
	private JPanel buildNorthPanel() {
		
		GridBagLayout layout;
		GridBagConstraints constraints;
		JPanel panel;
		int row, col;
		
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		panel = new JPanel(layout);
		row = 0;
		col = 0;
		JLabel headerLabel = new JLabel(stringFactory.getString(
				LabelStringFactory.SAVEASCONFIG_FRAME_SAVE_NEW_CONFIGURATION));
		headerLabel.setFont(new Font("arial", Font.PLAIN, 18));
		GUIUtils.add(panel, headerLabel, layout, constraints, row++, col, 
			1, 1, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
			GUIUtils.MED_LARGE_INSETS);
		return panel;
	}    
	
	/**
	 * Builds the main panel
	 * @return
	 */
	private JPanel buildMainPanel() {
		
		int row;
		GridBagLayout layout;
		GridBagConstraints constraints;
		JPanel main;
		JLabel label;		
		
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		main = new JPanel(layout);
		row = 0;	
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.SAVEASCONFIG_FRAME_CURRENT_CONFIGURATION)),
				layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
				GridBagConstraints.NONE, GUIUtils.MED_INSETS);
		GUIUtils.add(main, label = new JLabel(userPrefs.getConfiguration()), 
				layout, constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
				GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		label.setForeground(Color.BLUE);		
		GUIUtils.add(main, new JSeparator(), layout, constraints, row++, 0, 1, 
				2, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
				GUIUtils.MED_INSETS);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.SAVEASCONFIG_FRAME_CONFIGURATION_NAME)),
				layout, constraints, row, 0, 1, 1, GridBagConstraints.NORTHEAST, 
				GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JScrollPane(configurationName = 
				new JTextField(20)), layout, constraints, row++, 1, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.NONE, 
				GUIUtils.SMALL_INSETS);		
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.SAVEASCONFIG_FRAME_MAKE_DEFAULT)),
				layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
				GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, makeDefault = new JCheckBox(), layout, 
				constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
				GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		return main;
	}	
	
	/**
	 * Event handler
	 */
	public void actionPerformed(ActionEvent aEvent) {
		
		Object eventSource;
		String newConfigurationName;
		
		eventSource = aEvent.getSource();
		if (eventSource == cancelBtn) {
			dispose();
		} else if (eventSource == okayBtn) {
			newConfigurationName = 
				StringUtils.strip(configurationName.getText());
			if (StringUtils.isNotBlank(newConfigurationName)) {
				userPrefs.copyCurrentPreferences(newConfigurationName);
				userPrefs.setConfiguration(newConfigurationName, 
					makeDefault.isSelected());
				parent.refreshConfigurationLabel();				
				dispose();
			} else {
				Utils.showDialog(this, stringFactory.getString(
						LabelStringFactory.
						SAVEASCONFIG_FRAME_CONFIG_STR_CANNOT_BE_EMPTY), 
						stringFactory.getString(
								LabelStringFactory.SAVEASCONFIG_FRAME_ERROR), 
								JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}

