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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import net.blueslate.commons.gui.GUIUtils;

/**
 * Defines the timings frame
 * @author pevans
 *
 */
public class TimingsFrame extends DisposableFrame implements ActionListener {
	
	// default frame width and height - these values are used if
	// a height and width are not found in the user's preferences...
	private static final String DEFAULT_FRAME_WIDTH = "400";
	private static final String DEFAULT_FRAME_HEIGHT = "200";
	
    // get the i18n factory singleton instance...
    private static final LabelStringFactory stringFactory = 
    	LabelStringFactory.getInstance();
    
    // user-prefs property name prefix... 
	private static final String PROPERTY_NAME_PREFIX = "timings_";

	// instance members...
	private JButton closeBtn;
	private UserPreferences userPrefs;

	/**
	 * Constructor
	 * @param aParent
	 * @param aXSLRows
	 */
	public TimingsFrame(Frame aParent, XSLRow aXSLRows[]) {
		
		JPanel southPanel, mainPanel;
		int width, height;
		
		userPrefs = Utils.getUserPrefs();		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				dispose(userPrefs, PROPERTY_NAME_PREFIX);
			}
		});		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(buildMainPanel(aXSLRows), BorderLayout.CENTER);		
		southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		southPanel.add(closeBtn = new JButton(stringFactory.getString(
				LabelStringFactory.CLOSE_BUTTON)));
		closeBtn.addActionListener(this);		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		getContentPane().add(new JScrollPane(mainPanel), BorderLayout.CENTER);		
		setTitle(stringFactory.getString(
				LabelStringFactory.TIMINGS_FRAME_XSL_TRANSFORMATION_TIMINGS));
		width = Integer.parseInt(userPrefs.getProperty(PROPERTY_NAME_PREFIX + 
				AppConstants.FRAME_WIDTH_PROP, DEFAULT_FRAME_WIDTH));
		height = Integer.parseInt(userPrefs.getProperty(PROPERTY_NAME_PREFIX + 
				AppConstants.FRAME_HEIGHT_PROP, DEFAULT_FRAME_HEIGHT));
		setSize(width, height);
		GUIUtils.center(this, aParent);
		setVisible(true);
	}
	
	/**
	 * Builds the main panel
	 * @param aXSLRows
	 * @return
	 */
	private JPanel buildMainPanel(XSLRow aXSLRows[]) {
		
		int row, col, loop;
		GridBagLayout layout;
		GridBagConstraints constraints;
		JPanel main;	
		JLabel xslHeading, timingHeading;
		long totalTransformTime;
		
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		main = new JPanel(layout);
		totalTransformTime = 0;
		row = 0;
		col = 0;
		xslHeading = new JLabel(stringFactory.getString(
				LabelStringFactory.TIMINGS_FRAME_TRANSFORMATION_XSL));
		timingHeading = new JLabel(" | " + stringFactory.getString(
				LabelStringFactory.TIMINGS_FRAME_TIME_TO_TRANSFORM));		
		GUIUtils.add(main, xslHeading, layout, constraints, row, col++, 1, 1,
			GridBagConstraints.WEST, GridBagConstraints.NONE, 
			GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, timingHeading, layout, constraints, row++, col++, 1, 
			1, GridBagConstraints.WEST, GridBagConstraints.NONE, 
			GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JSeparator(), layout, constraints, row++, col=0, 
			1, 2);
		for (loop = 0; loop < aXSLRows.length; loop++) {
			totalTransformTime += aXSLRows[loop].getTimeToTransform();
			col = 0;
			GUIUtils.add(main, new JLabel(aXSLRows[loop].getLabel().getText()),
					layout, constraints, row, col++, 1, 1, 
					GridBagConstraints.WEST, GridBagConstraints.NONE, 
					GUIUtils.SMALL_INSETS);
			GUIUtils.add(main, new JLabel(" | " + 
					aXSLRows[loop].getTimeToTransform()), layout, constraints, 
					row++, col, 1, 1, GridBagConstraints.WEST, 
					GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		}
		GUIUtils.add(main, new JSeparator(), layout, constraints, row++, col=0, 
					1, 2);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.TIMINGS_FRAME_TOTAL_LBL)),
			layout, constraints, row, col++, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JLabel(" | " + 
			totalTransformTime), layout, constraints, 
			row++, col, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		return main;
	}
	
	/**
	 * Event handler
	 */
	public void actionPerformed(ActionEvent aEvent) {
		
		Object eventSource;
		
		eventSource = aEvent.getSource();
		if (eventSource == closeBtn) {
			dispose(userPrefs, PROPERTY_NAME_PREFIX);
		}
	}
}
