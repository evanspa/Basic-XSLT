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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import net.blueslate.commons.gui.GUIUtils;

/**
 * Defines the validation-error frame
 * @author pevans
 *
 */
public class ValidationErrorFrame extends DisposableFrame 
implements ActionListener {
	
	// default frame width and height - these values are used if
	// a height and width are not found in the user's preferences...
	private static final String DEFAULT_FRAME_WIDTH = "590";
	private static final String DEFAULT_FRAME_HEIGHT = "350";
	
	// user-prefs property name prefix... 
	private static final String PROPERTY_NAME_PREFIX = "validationerror_";
	
	// get the i18n factory singleton instance...
	private static final LabelStringFactory stringFactory = 
		LabelStringFactory.getInstance();
	
	// instance members...
    private JButton closeBtn;
    private JMenuItem close;
    private UserPreferences userPrefs;
	private String propertyNamePrefix;
    
    /**
     * Constructor
     * @param aParent
     * @param aTitle
     * @param aHeaderLabel
     * @param aErrHeader
     * @param aErrColumn
     * @param aErrLine
     * @param aErrText
     * @param aFileName
     * @param aXSLRows
     */
    public ValidationErrorFrame(BasicXSLTFrame aParent, String aTitle, 
    	String aHeaderLabel, String aErrHeader, int aErrColumn, int aErrLine, 
    	String aErrText, String aFileName, XSLRow aXSLRows[]) {
    	
        Component panel;
        
        buildMenuBar();
        JPanel southPanel = new JPanel(new FlowLayout());
        southPanel.add(closeBtn = new JButton("Close"));
		panel = buildMainPanel(aHeaderLabel, aErrHeader, aErrColumn, 
			aErrLine, aErrText, aFileName);
		closeBtn.addActionListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(southPanel, BorderLayout.SOUTH);
        setTitle(aTitle);
		setWindowCloseListener();
        setSize();
        GUIUtils.center(this, aParent);
        setVisible(true);
    }
    
    /**
     * Set the window-close listener
     *
     */
    private void setWindowCloseListener() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				dispose(userPrefs, PROPERTY_NAME_PREFIX);
			}
		});	
    }
    
    /**
     * Set the frame size
     *
     */
    private void setSize() {
    	
		int width, height;
		
    	userPrefs = Utils.getUserPrefs();
		width = Integer.parseInt(userPrefs.getProperty(propertyNamePrefix + 
			AppConstants.FRAME_WIDTH_PROP, DEFAULT_FRAME_WIDTH));
		height = Integer.parseInt(userPrefs.getProperty(propertyNamePrefix + 
			AppConstants.FRAME_HEIGHT_PROP, DEFAULT_FRAME_HEIGHT));
		setSize(width, height);
    }
    
    /**
     * Builds the main panel
     * @param aHeaderLabel
     * @param aErrHeader
     * @param aErrColumn
     * @param aErrLine
     * @param aErrText
     * @param aFileName
     * @return
     */
    private static JScrollPane buildMainPanel(String aHeaderLabel, 
    	String aErrHeader, int aErrColumn, int aErrLine, String aErrText, 
    	String aFileName) {
    	
    	GridBagLayout layout;
    	GridBagConstraints constraints;
    	JPanel panel;
    	JScrollPane scrollPane;
		int row;
		int col;
		JLabel headerLabel;
		JLabel fileLabel;
		JLabel errLineLabel;
		JLabel errLine;
		JLabel errColLabel;
		JLabel errCol;
		JLabel errTextLabel;
		JTextArea errText;
		
		layout = new GridBagLayout();
    	constraints = new GridBagConstraints();
    	panel = new JPanel(layout);
    	scrollPane = new JScrollPane(panel, 
    			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
    			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		row = 0;
		col = 0;
		headerLabel = new JLabel("!" + aErrHeader + " (" + aHeaderLabel + ")");
		headerLabel.setForeground(Color.RED);
		headerLabel.setFont(new Font("arial", Font.PLAIN, 18));
		fileLabel = new JLabel(stringFactory.getString(
				LabelStringFactory.VALIDATIONERR_FRAME_FILE) + aFileName);
		fileLabel.setFont(new Font("arial", Font.PLAIN, 12));
		errLineLabel = new JLabel(stringFactory.getString(
				LabelStringFactory.VALIDATIONERR_FRAME_LINE_NUM));
		errLine = new JLabel(aErrLine == -1 ? stringFactory.getString(
				LabelStringFactory.VALIDATIONERR_FRAME_NOT_AVAILABLE) : 
			Integer.toString(aErrLine));
		errColLabel = new JLabel(stringFactory.getString(
				LabelStringFactory.VALIDATIONERR_FRAME_COLUMN_NUM));
		errCol = new JLabel(aErrColumn == -1 ? stringFactory.getString(
				LabelStringFactory.VALIDATIONERR_FRAME_NOT_AVAILABLE) : 
			Integer.toString(aErrColumn));
		errTextLabel = new JLabel(stringFactory.getString(
				LabelStringFactory.VALIDATIONERR_FRAME_MSG));
		errText = new JTextArea(7, 30);
		errText.setBorder(BorderFactory.createLoweredBevelBorder());
		errText.setText(aErrText);
		errText.setEditable(false);
		errText.setLineWrap(true);
		errText.setWrapStyleWord(true);
		GUIUtils.add(panel, headerLabel, layout, constraints, row++, col, 
			1, 2, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
			GUIUtils.MED_LARGE_INSETS);
		GUIUtils.add(panel, new JSeparator(), layout, constraints, row++, col, 
			1, 2, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, 
			GUIUtils.MED_LARGE_INSETS);
		GUIUtils.add(panel, fileLabel, layout, constraints, row++, col, 
			1, 2, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
			GUIUtils.MED_LARGE_INSETS);
		GUIUtils.add(panel, new JSeparator(), layout, constraints, row++, col, 
			1, 2, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, 
			GUIUtils.MED_LARGE_INSETS);
		GUIUtils.add(panel, errLineLabel, layout, constraints, row, col++, 
			1, 1, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 
			GUIUtils.SMALL_INSETS);
		GUIUtils.add(panel, errLine, layout, constraints, row++, col, 
			1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 
			GUIUtils.SMALL_INSETS);
		GUIUtils.add(panel, errColLabel, layout, constraints, row, col=0, 
			1, 1, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 
			GUIUtils.SMALL_INSETS);
		GUIUtils.add(panel, errCol, layout, constraints, row++, ++col, 
			1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 
			GUIUtils.SMALL_INSETS);
		GUIUtils.add(panel, errTextLabel, layout, constraints, row, col=0, 
			1, 1, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, 
			GUIUtils.SMALL_INSETS);
		GUIUtils.add(panel, new JScrollPane(errText, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), layout, 
				constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
				GridBagConstraints.BOTH, GUIUtils.SMALL_INSETS);
    	return scrollPane;
    }    

    /**
     * Builds the GUI menu bar.
     */
    private void buildMenuBar() {
    	
    	JMenu file;
        JMenuBar menuBar;
        
        menuBar = new JMenuBar();
        file = new JMenu(stringFactory.getString(
        		LabelStringFactory.VF_FILE_MENU));
        file.setMnemonic(stringFactory.getMnemonic(
        		LabelStringFactory.VF_FILE_MENU));
        close = new JMenuItem(stringFactory.getString(
        		LabelStringFactory.VF_FILE_CLOSE_MI));
        close.setMnemonic(stringFactory.getMnemonic(
        		LabelStringFactory.VF_FILE_CLOSE_MI));
        close.addActionListener(this);
        file.add(close);
		menuBar.add(file);
        setJMenuBar(menuBar);
    }

    /**
     * Event handler method.
     * @param aEvt
     */
    public void actionPerformed(ActionEvent aEvt) {
    	if (aEvt.getSource() == closeBtn || aEvt.getSource() == close) {
        	dispose(userPrefs, PROPERTY_NAME_PREFIX);
        } 
    }
}