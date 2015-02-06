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
import java.awt.Cursor;
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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import net.blueslate.commons.gui.GUIUtils;

/**
 * Defines the configuration frame.
 * @author pevans
 *
 */
public class LoadConfigurationFrame extends JFrame implements ActionListener {		
	
	// frame width and height...
	private static final int FRAME_WIDTH = 485;
	private static final int FRAME_HEIGHT = 185;
	
	// get the i18n factory singleton instance...
	private static final LabelStringFactory stringFactory = 
		LabelStringFactory.getInstance();

	private JButton cancelBtn, okayBtn;
	private UserPreferences userPrefs;
	private BasicXSLTFrame parent;
	private JComboBox configurations;
	private JCheckBox makeDefault;

	/**
	 * Constructor
	 * @param aParent
	 * @param aCurrentConfiguration
	 * @param aAllConfigurations
	 */
	public LoadConfigurationFrame(BasicXSLTFrame aParent, 
			String aCurrentConfiguration, String aAllConfigurations[]) {
		
		JPanel southPanel, mainPanel;
		
		parent = aParent;
		userPrefs = Utils.getUserPrefs();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent aEvt) {
				dispose();
			}
		});	
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(buildMainPanel(aAllConfigurations), BorderLayout.CENTER);		
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
				LabelStringFactory.LOADCONFIG_FRAME_LOAD_CONFIGURATION));		
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		GUIUtils.center(this, aParent);
		setVisible(true);
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
		JLabel headerLabel;
		
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		panel = new JPanel(layout);
		row = 0;
		col = 0;
		headerLabel = new JLabel(stringFactory.getString(
				LabelStringFactory.LOADCONFIG_FRAME_LOAD_CONFIGURATION));
		headerLabel.setFont(new Font("arial", Font.PLAIN, 18));
		GUIUtils.add(panel, headerLabel, layout, constraints, row++, col, 
			1, 1, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
			GUIUtils.MED_LARGE_INSETS);
		return panel;
	}    
	
	/**
	 * Builds main panel
	 * @param aAllConfigurations
	 * @return
	 */
	private JPanel buildMainPanel(String aAllConfigurations[]) {
		
		int row;
		JLabel label;
		GridBagLayout layout;
		GridBagConstraints constraints;
		JPanel main;		
		
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		main = new JPanel(layout);
		row = 0;	
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.LOADCONFIG_FRAME_LOAD_CONFIGURATION)),
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
				LabelStringFactory.LOADCONFIG_FRAME_CONFIGURATION)), layout, 
				constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
				GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, configurations = new JComboBox(aAllConfigurations), 
				layout, constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
				GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.LOADCONFIG_FRAME_MAKE_DEFAULT)), layout, 
				constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
				GridBagConstraints.NONE, GUIUtils.MED_INSETS);
		GUIUtils.add(main, makeDefault = new JCheckBox(), layout, constraints, 
				row++, 1, 1, 1, GridBagConstraints.WEST, 
				GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		return main;
	}	
	
	/**
	 * Event handler
	 */
	public void actionPerformed(ActionEvent aEvent) {
		
		Object eventSource;
		
		eventSource = aEvent.getSource();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		if (eventSource == cancelBtn) {
			dispose();
		} else if (eventSource == okayBtn) {
			userPrefs.setConfiguration((String)configurations.getSelectedItem(), 
				makeDefault.isSelected());
			parent.refreshApplication();
			dispose();
		}
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
}

