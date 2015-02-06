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
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import net.blueslate.commons.gui.GUIUtils;
import net.blueslate.commons.io.IOUtils;
import net.blueslate.commons.xml.TransformOutputProperties;
import net.blueslate.commons.xml.TransformParameters;
import net.blueslate.commons.xml.XMLUtils;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Defines the main application window.
 * @author pevans
 *
 */
public class BasicXSLTFrame extends JFrame implements ActionListener,
	Runnable {
    
    // static members...
    private static UserPreferences userPrefs;
    private static String lastFileChosen;
    private static FileSystemManager fsManager;
    
    // get the i18n factory singleton instance...
    private static final LabelStringFactory stringFactory = 
    	LabelStringFactory.getInstance();
    
	// constants used by Runnable...
	private static final int THREADMODE_DO_TRANSFORM = 0;
	private static final int THREADMODE_DO_VALIDATE  = 1;
	private static final int THREADMODE_DO_XML_IDENTITY_TRANSFORM = 2;
	
    // constants...
    private static final int XML_VALIDATE_ACTION_INDEX = 2;
    private static final int XML_VIEW_EDIT_OUTPUT_PROPS_INDEX = 3;
    private static final int XML_CLEAR_OUTPUT_PROPS_INDEX = 4;
    private static final int XML_DO_IDENTITY_TRANSFORM_ACTION_INDEX = 5;
    
	// default frame width and height - these values are used if
	// a height and width are not found in the user's preferences...
	private static final String DEFAULT_FRAME_WIDTH = "930";
	private static final String DEFAULT_FRAME_HEIGHT = "415";
    
    // XML action labels...
    private static final String XML_ACTIONS[] = {
    	stringFactory.getString(LabelStringFactory.XML_ACTION_TAKE_ACTION),
    	AppConstants.SEPARATOR,
    	stringFactory.getString(LabelStringFactory.XML_ACTION_VALIDATE),
    	stringFactory.getString(
    			LabelStringFactory.XML_ACTION_IT_OUTPUT_PROPERITES),
    	stringFactory.getString(
    			LabelStringFactory.XML_ACTION_CLEAR_IT_PROPERTIES),
    	stringFactory.getString(LabelStringFactory.XML_ACTION_PERFORM_IT)
    };
    
    // logger object...
    private static final Logger logger = Logger.getLogger(BasicXSLTFrame.class);
    
	// instance members...
	private int threadMode;
	private String label, identityTransformMessage;
	private String identityTransformSourceXmlFile, 
	identityTransformResultXmlFile;
	private JTextField textField;
	private boolean suppressSuccessDialog;
	private List<Component> components;
	private JTextField sourceXmlTf, autosavePathTf;
    private List<XSLRow> xslRows;
    private JPanel xslPanel;
    private GridBagLayout xslPanelLayout;
    private GridBagConstraints xslPanelConstraints;
    private JLabel transformTimeLabel, currentConfigLabel, 
    	outputAsTextIfXmlLabel, xmlIndicatorLabel;
    private JButton exitBtn, transformBtn, addXslBtn, 
		removeCheckedBtn, validateAutosaveBtn;
	private JComboBox xmlAction;
    private JButton browseXmlBtn, browseAutosavePathBtn;
    private JCheckBoxMenuItem checkSaxWarning, checkSaxError, 
    checkSaxFatalError;
    private JCheckBox autosaveCb, suppressOutputWindowCb, outputAsTextIfXml;
    private JMenuItem exit, about, resetForm, transformTimings, 
    	saveConfiguration, saveAsConfiguration, loadConfiguration;
    private long lastTotalTransformTime;
    private TransformOutputProperties xmlIdentityTransformOutputProps;
    private boolean areXmlOutputPropertiesSet;

    /**
     * private class constructor
     */
    private BasicXSLTFrame() throws IOException {
    	
    	int width, height;
    	
    	fsManager = VFS.getManager();
		userPrefs = Utils.getUserPrefs();
		userPrefs.loadDefaultConfiguration();
		lastFileChosen = 
			userPrefs.getProperty(AppConstants.LAST_FILE_CHOSEN_PROP);
		components = new ArrayList<Component>();
        buildGui();
        setTitle(stringFactory.getString(
        		LabelStringFactory.MAIN_FRAME_TITLE_BAR));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                destroy();
            }
        });
        width = Integer.parseInt(userPrefs.getProperty(
        		AppConstants.FRAME_WIDTH_PROP, DEFAULT_FRAME_WIDTH));
		height = Integer.parseInt(userPrefs.getProperty(
				AppConstants.FRAME_HEIGHT_PROP, DEFAULT_FRAME_HEIGHT));
        setSize(width, height);
        initializeControls();
        setToolTips();
        setVisible(true);
    }
    
    /**
     * Set the various tool-tips on the GUI components.
     *
     */
    private void setToolTips() {
		transformTimings.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_TRANSFORM_TIMINGS));
		xmlAction.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_XML_ACTION));
		transformBtn.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_TRANSFORM_BTN));	
		exitBtn.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_EXIT_BTN));
		addXslBtn.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_ADD_XSL_BTN));
		removeCheckedBtn.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_REMOVE_CHECKED_BTN));
		validateAutosaveBtn.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_VALIDATE_AUTOSAVE_BTN));
		autosavePathTf.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_AUTOSAVE_TEXTFIELD));
		sourceXmlTf.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_SOURCE_XML_TEXTFIELD));
		browseAutosavePathBtn.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_BROWSE_AUTOSAVE_PATH_BTN));
		browseXmlBtn.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_BROWSE_XML_BTN));
		suppressOutputWindowCb.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_SUPPRESS_OUTPUT_WINDOW_CB));
		autosaveCb.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_AUTOSAVE_CB));
		outputAsTextIfXml.setToolTipText(stringFactory.getString(
				LabelStringFactory.TOOL_TIP_OUTPUT_AS_TEXT_IF_XML_CB));
    }
    
    private static void loadParameters(XSLRow aXSLRow) {
    	
    	// local declarations...
    	TransformParameters parameters;
    	Enumeration propertyNames;
    	String propName, propValue, prefix;
    	int index;
    	
    	parameters = aXSLRow.getTransformParameters();
    	propertyNames = userPrefs.propertyNames();
    	prefix = userPrefs.getConfiguration() + ".xsl_" + 
    		aXSLRow.getIndex() + "_params_";
    	while (propertyNames.hasMoreElements()) {
    		propName = (String)propertyNames.nextElement();
    		if (propName.startsWith(prefix)) {
    			propValue = userPrefs.getPropertyNoPrefix(propName);
    			index = propName.indexOf('{');
    			if (index != -1) {
    				propName = propName.substring(index);
    			} else {
    				index = propName.indexOf("_", prefix.length());
    				propName = propName.substring(index + 1);
    			}    	
    			parameters.setParameter(
					TransformParameters.getNamespaceURI(propName),
    				TransformParameters.getParameterName(propName),
    				propValue);
    		}
    	}
    }
    
    /**
     * Load the output properties on the aOutputProperties object from the 
     * user's preferences.
     * @param aOutputProperties
     * @param aPropertyNamePrefix
     */
    private void loadOutputProperties(
    		TransformOutputProperties aOutputProperties, 
    		String aPropertyNamePrefix) {
    		
		aOutputProperties.setCDATA_SECTION_ELEMENTS(
			StringUtils.defaultString(userPrefs.getProperty(
				aPropertyNamePrefix + AppConstants.CDATA_SECTION_ELEMENTS)));
		aOutputProperties.setDOCTYPE_PUBLIC(
			StringUtils.defaultString(userPrefs.getProperty(
				aPropertyNamePrefix + AppConstants.DOCTYPE_PUBLIC)));
		aOutputProperties.setDOCTYPE_SYSTEM(
			StringUtils.defaultString(userPrefs.getProperty(
				aPropertyNamePrefix + AppConstants.DOCTYPE_SYSTEM)));
		aOutputProperties.setENCODING(
			StringUtils.defaultString(userPrefs.getProperty(
				aPropertyNamePrefix + AppConstants.ENCODING)));
		aOutputProperties.setINDENT(BooleanUtils.toBoolean(
				userPrefs.getProperty(aPropertyNamePrefix + 
						AppConstants.INDENT)));
		aOutputProperties.setMEDIA_TYPE(
			StringUtils.defaultString(userPrefs.getProperty(
				aPropertyNamePrefix + AppConstants.MEDIA_TYPE)));
		aOutputProperties.setMETHOD(
			StringUtils.defaultString(userPrefs.getProperty(
				aPropertyNamePrefix + AppConstants.METHOD)));
		aOutputProperties.setOMIT_XML_DECLARATION(
			BooleanUtils.toBoolean(userPrefs.getProperty(
				aPropertyNamePrefix + AppConstants.OMIT_XML_DECLARATION)));
		aOutputProperties.setSTANDALONE(
			BooleanUtils.toBoolean(userPrefs.getProperty(
				aPropertyNamePrefix + AppConstants.STANDALONE)));		
		aOutputProperties.setVERSION(
			StringUtils.defaultString(userPrefs.getProperty(
				aPropertyNamePrefix + AppConstants.VERSION)));    	
    }

    /**
     * Initialize the gui
     *
     */
    private void initializeControls() {
    	
        String val, xCoord, yCoord, propertyNamePrefix;
        boolean areGoodCoordinates;
		int loop;
		XSLRow xslRow;
        
		loop = 0;
        val = userPrefs.getProperty(AppConstants.CHK_WARNINGS_PROP);
        checkSaxWarning.setSelected(val != null ? 
        		Boolean.valueOf(val).booleanValue() : false);
        val = userPrefs.getProperty(AppConstants.CHK_ERRORS_PROP);
        checkSaxError.setSelected(val != null ? 
        		Boolean.valueOf(val).booleanValue() : false);
        val = userPrefs.getProperty(AppConstants.CHK_FATAL_ERRORS_PROP);
        checkSaxFatalError.setSelected(val != null ? 
        		Boolean.valueOf(val).booleanValue() : false);
        xCoord = userPrefs.getProperty(AppConstants.X_COORD_PROP);
        yCoord = userPrefs.getProperty(AppConstants.Y_COORD_PROP);
        areGoodCoordinates = false;
        if (xCoord != null && yCoord != null) {
           this.setLocation(Integer.parseInt(xCoord), Integer.parseInt(yCoord));
           areGoodCoordinates = true;
        }
        if (!areGoodCoordinates) {
           GUIUtils.center(this, null);
        }
        sourceXmlTf.setText(userPrefs.getProperty(
        		AppConstants.LAST_XML_FILE_PROP));
        val = userPrefs.getProperty(AppConstants.SUPPRESS_OUTPUT_WINDOW_PROP);
        suppressOutputWindowCb.setSelected(BooleanUtils.toBoolean(val));
		val = userPrefs.getProperty(AppConstants.OUTPUT_AS_TEXT_IF_XML_PROP);
		outputAsTextIfXml.setSelected(BooleanUtils.toBoolean(val));
        outputAsTextIfXmlLabel.setEnabled(!suppressOutputWindowCb.isSelected());
		outputAsTextIfXml.setEnabled(!suppressOutputWindowCb.isSelected());
        val = userPrefs.getProperty(AppConstants.AUTOSAVE_RESULT_PROP);
        autosaveCb.setSelected(BooleanUtils.toBoolean(val));
        autosavePathTf.setText(userPrefs.getProperty(
        		AppConstants.AUTOSAVE_FILE_PROP));
        autosavePathTf.setEnabled(autosaveCb.isSelected());
        browseAutosavePathBtn.setEnabled(autosaveCb.isSelected());
        removeCheckedBtn.setEnabled(false);
           
		xmlIdentityTransformOutputProps = new TransformOutputProperties();
		// load xml output props from user prefs
		val = userPrefs.getProperty("xml_identity_transform_opInd");
		setAreOutputPropertiesSet(BooleanUtils.toBoolean(val));
		loadOutputProperties(xmlIdentityTransformOutputProps,
			"xml_identity_transform_outputproperties_");
		
		refreshXmlIndicatorLabel();
		
        do {
			propertyNamePrefix = "xsl_" + loop + "_outputproperties_";
        	val = userPrefs.getProperty("xsl_" + loop + "_file");
        	if (val != null) {
        		xslRow = xslRows.get(loop);
        		loadParameters(xslRow);
        		xslRow.getTextField().setText(val);
        		val = userPrefs.getProperty("xsl_" + loop + "_onoff"); 
        		xslRow.setOn(BooleanUtils.toBoolean(val));   
        		val = userPrefs.getProperty("xsl_" + loop + "_opInd");
        		xslRow.setAreOutputPropertiesSet(BooleanUtils.toBoolean(val));  
        		loadOutputProperties(xslRow.getTransformOutputProperties(),
        			propertyNamePrefix);        		        		
       			loop++;
        	}
        } while (val != null);        
    }
    
    public static void setLastFileChosen(String aLastFileChosen) {
    	lastFileChosen = aLastFileChosen;
    	userPrefs.setProperty(AppConstants.LAST_FILE_CHOSEN_PROP,
    		lastFileChosen);
    }

    /**
     * Create the menubar.
     *
     */
    private void buildMenuBar() {
    	
    	// local declarations...
        JMenuBar menuBar;
        JMenu help, file, validation, view;
        
        // build the file menu and associated menu items...
        file = new JMenu(stringFactory.getString(LabelStringFactory.MF_FILE_MENU));
        file.setMnemonic(stringFactory.getMnemonic(
        		LabelStringFactory.MF_FILE_MENU));
        resetForm = new JMenuItem(stringFactory.getString(
        		LabelStringFactory.MF_FILE_RESET_FORM_MI));
        resetForm.setMnemonic(stringFactory.getMnemonic(
        		LabelStringFactory.MF_FILE_RESET_FORM_MI));
        resetForm.addActionListener(this);
        file.add(resetForm);
        file.add(new JSeparator());
		file.add(loadConfiguration = new JMenuItem(stringFactory.getString(
				LabelStringFactory.MF_FILE_LOAD_CONFIGURATION_MI)));
		loadConfiguration.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.MF_FILE_LOAD_CONFIGURATION_MI));
        file.add(saveConfiguration = new JMenuItem(stringFactory.getString(
				LabelStringFactory.MF_FILE_SAVE_CONFIGURATION_MI)));
        saveConfiguration.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.MF_FILE_SAVE_CONFIGURATION_MI));
        file.add(saveAsConfiguration = new JMenuItem(stringFactory.getString(
				LabelStringFactory.MF_FILE_SAVE_CONFIGURATION_AS_MI)));
        saveAsConfiguration.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.MF_FILE_SAVE_CONFIGURATION_AS_MI));   
        loadConfiguration.addActionListener(this);
        saveConfiguration.addActionListener(this);
        saveAsConfiguration.addActionListener(this);
        file.add(new JSeparator());     
        exit = new JMenuItem(stringFactory.getString(
				LabelStringFactory.MF_FILE_EXIT_MI));
        exit.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.MF_FILE_EXIT_MI));
        exit.addActionListener(this);
        file.add(exit);              
        
        // build the validation menu and associated menu items...
		validation = new JMenu(stringFactory.getString(
				LabelStringFactory.MF_VALIDATION_MENU));
		validation.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.MF_VALIDATION_MENU));
		validation.add(checkSaxWarning = new JCheckBoxMenuItem(
				stringFactory.getString(
						LabelStringFactory.MF_VALIDATION_CHECK_SAX_WARNINGS_MI)));
		checkSaxWarning.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.MF_VALIDATION_CHECK_SAX_WARNINGS_MI));
		validation.add(checkSaxError = new JCheckBoxMenuItem(
				stringFactory.getString(
				LabelStringFactory.MF_VALIDATION_CHECK_SAX_ERRORS_MI)));
		checkSaxError.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.MF_VALIDATION_CHECK_SAX_ERRORS_MI));
		validation.add(checkSaxFatalError = new JCheckBoxMenuItem(
				stringFactory.getString(
				LabelStringFactory.MF_VALIDATION_CHECK_SAX_FATAL_MI)));
		checkSaxFatalError.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.MF_VALIDATION_CHECK_SAX_FATAL_MI));
        
		// build the view menu and associate menu items...
        view = new JMenu(stringFactory.getString(LabelStringFactory.MF_VIEW_MENU));
        view.setMnemonic(stringFactory.getMnemonic(
        		LabelStringFactory.MF_VIEW_MENU));
        view.add(transformTimings = new JMenuItem(stringFactory.getString(
        		LabelStringFactory.MF_VIEW_LAST_TIMINGS_MI)));
        transformTimings.setMnemonic(stringFactory.getMnemonic(
        		LabelStringFactory.MF_VIEW_LAST_TIMINGS_MI));
        transformTimings.setEnabled(false);
        transformTimings.addActionListener(this);
        
        // build the help menu and associated menu items...
        help = new JMenu(stringFactory.getString(LabelStringFactory.MF_HELP_MENU));
        help.setMnemonic(stringFactory.getMnemonic(
        		LabelStringFactory.MF_HELP_MENU));
        about = new JMenuItem(stringFactory.getString(
        		LabelStringFactory.MF_HELP_ABOUT_MI));
        about.setMnemonic(stringFactory.getMnemonic(
        		LabelStringFactory.MF_HELP_ABOUT_MI));
        about.addActionListener(this);
        help.add(about);
        
        // build the menubar...
        menuBar = new JMenuBar();
        menuBar.add(file);
        menuBar.add(validation);
        menuBar.add(view);
        menuBar.add(help);
        setJMenuBar(menuBar);
    }

    /**
     * Rebuilds the XSL panel; first it removes all of the components; and then
     * it loops over the xslRows object and re-adds the components.
     *
     */
    private void rebuildXSLPanel() {
    	
    	int loop, size;
    	int col, xslPanelRow;
    	JTextField xslTf;
        JButton browseXslBtn, insertBtn;
        JComboBox action;
        JLabel xslLabel, indicatorLabel;
        JCheckBox removeCb;
        XSLRow xslRow;
        
        xslPanel.removeAll();                
		xslPanelRow = 0;
		GUIUtils.add(xslPanel, new JLabel(""), xslPanelLayout,
			xslPanelConstraints, xslPanelRow, col=0, 1, 1, 
			GridBagConstraints.EAST, GridBagConstraints.BOTH, 
			GUIUtils.SMALL_INSETS);
		GUIUtils.add(xslPanel, new JLabel(stringFactory.getString(
				LabelStringFactory.MAIN_FRAME_XML_FILE_WITH_COLON)), 
				xslPanelLayout, xslPanelConstraints, xslPanelRow, ++col, 1, 1, 
				GridBagConstraints.EAST, GridBagConstraints.BOTH, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(xslPanel, sourceXmlTf, xslPanelLayout, xslPanelConstraints, 
			xslPanelRow, ++col, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.BOTH, GUIUtils.SMALL_INSETS);
		GUIUtils.add(xslPanel, new JLabel(""), xslPanelLayout, 
				xslPanelConstraints, xslPanelRow, ++col, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.BOTH, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(xslPanel, browseXmlBtn, 
				xslPanelLayout, xslPanelConstraints, xslPanelRow, ++col, 1, 1, 
				GridBagConstraints.EAST, GridBagConstraints.BOTH, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(xslPanel, xmlAction, 
				xslPanelLayout, xslPanelConstraints, xslPanelRow, ++col, 1, 1, 
				GridBagConstraints.EAST, GridBagConstraints.BOTH, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(xslPanel, xmlIndicatorLabel, xslPanelLayout, 
				xslPanelConstraints, xslPanelRow++, ++col, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.BOTH, 
				GUIUtils.SMALL_INSETS);        
        size = xslRows.size();
        for (loop = 0; loop < size; loop++) {
        	xslRow = xslRows.get(loop);
        	xslLabel = xslRow.getLabel();
        	xslTf = xslRow.getTextField();
        	removeCb = xslRow.getRemoveCb();
        	browseXslBtn = xslRow.getBrowseBtn();
        	insertBtn = xslRow.getInsertBtn();
        	indicatorLabel = xslRow.getIndicatorLabel();
        	action = xslRow.getAction();  	   
			GUIUtils.add(xslPanel, insertBtn, xslPanelLayout,
						xslPanelConstraints, xslPanelRow, col=0, 1, 1, 
						GridBagConstraints.EAST, GridBagConstraints.EAST, 
						GUIUtils.NO_INSETS);     	
			GUIUtils.add(xslPanel, xslLabel, xslPanelLayout, 
					xslPanelConstraints, xslPanelRow, ++col, 1, 1,  
					GridBagConstraints.EAST, GridBagConstraints.NONE, 
					GUIUtils.SMALL_INSETS);
			GUIUtils.add(xslPanel, xslTf, xslPanelLayout, xslPanelConstraints, 
				xslPanelRow, ++col, 1, 1, GridBagConstraints.WEST, 
				GridBagConstraints.BOTH, GUIUtils.SMALL_INSETS);
			GUIUtils.add(xslPanel, removeCb, 
					xslPanelLayout, xslPanelConstraints, xslPanelRow, ++col, 1, 
					1, GridBagConstraints.EAST, GridBagConstraints.BOTH, 
					GUIUtils.SMALL_INSETS);
			GUIUtils.add(xslPanel, browseXslBtn, 
					xslPanelLayout, xslPanelConstraints, xslPanelRow, ++col, 1, 
					1, GridBagConstraints.EAST, GridBagConstraints.BOTH, 
					GUIUtils.SMALL_INSETS);
			GUIUtils.add(xslPanel, action, 
					xslPanelLayout, xslPanelConstraints, xslPanelRow, ++col, 1, 
					1, GridBagConstraints.EAST, GridBagConstraints.BOTH, 
					GUIUtils.SMALL_INSETS);	
			GUIUtils.add(xslPanel, indicatorLabel, xslPanelLayout, 
				xslPanelConstraints, xslPanelRow++, ++col, 1, 1, 
				GridBagConstraints.EAST, GridBagConstraints.BOTH,
				GUIUtils.SMALL_INSETS);	
		}
    }
    
    /**
     * Refreshes the stylesheets panel
     *
     */
    private void refreshStylesheets() {
    	
    	int loop;
		int numStylesheets;
		
		numStylesheets = Integer.parseInt(userPrefs.getProperty(
				AppConstants.NUM_STYLESHEETS_PROP, 
				AppConstants.DEFAULT_NUM_STYLESHEETS));
		xslRows.clear();
		for (loop = 0; loop < numStylesheets; loop++) {        	
			addXSLRow();
		}
		rebuildXSLPanel();
    }
    
    /**
     * Setter
     * @param aAreOutputPropertiesSet
     */
	public void setAreOutputPropertiesSet(boolean aAreOutputPropertiesSet) {
		areXmlOutputPropertiesSet = aAreOutputPropertiesSet;
		refreshXmlIndicatorLabel();
	}
    
	/**
	 * Refreshes the XML indicator label and tool-tip
	 *
	 */
    private void refreshXmlIndicatorLabel() {
    	
		StringBuffer labelText;
		StringBuffer toolTip;
		
		labelText = new StringBuffer();
		toolTip = new StringBuffer();
		if (areXmlOutputPropertiesSet) {
			labelText.append(stringFactory.getString(
					LabelStringFactory.MAIN_FRAME_XML_INDICATOR_ITOPSPECIFIED));
			toolTip.append(stringFactory.getString(
					LabelStringFactory.
					MAIN_FRAME_XML_INDICATOR_ITOPSPECIFIED_TOOL_TIP));
		}
		xmlIndicatorLabel.setText(labelText.toString());
		xmlIndicatorLabel.setToolTipText(toolTip.toString());
    }
    
    /**
     * Method to build the GUI
     */
    private void buildGui() {
    	
        JPanel centerPanel;
        buildMenuBar();
        JScrollPane stylesheetsPane;
        	
        xslPanelLayout = new GridBagLayout();
        xslPanelConstraints = new GridBagConstraints();
        centerPanel = new JPanel();
        xslPanel = new JPanel(xslPanelLayout);
        stylesheetsPane = new JScrollPane(xslPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
        sourceXmlTf = new JTextField(AppConstants.TF_LENGTH);
        browseXmlBtn = new JButton(stringFactory.getString(
        		LabelStringFactory.MAIN_FRAME_BROWSE_BTN));
        xmlAction = new JComboBox(XML_ACTIONS);
        browseXmlBtn.addActionListener(this);
        xmlIndicatorLabel = new JLabel("");
        xmlIndicatorLabel.setFont(new Font("arial", Font.PLAIN, 10));
        xmlAction.addActionListener(this);
        xslRows = new ArrayList<XSLRow>();
        refreshStylesheets();        
        centerPanel.setLayout(new BorderLayout());
		centerPanel.add(stylesheetsPane, BorderLayout.CENTER);
		centerPanel.add(new JScrollPane(buildAutosavePanel(),
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.SOUTH);
	    browseAutosavePathBtn.addActionListener(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildNorthPanel(), BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(buildSouthPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Builds the north/top-most panel and returns it.
     * @return
     */
    private JPanel buildNorthPanel() {
    	
		JPanel northPanel;
		JLabel title;
		
		northPanel = new JPanel(new FlowLayout());
		title = new JLabel(stringFactory.getString(
				LabelStringFactory.MAIN_FRAME_TITLE));
		title.setFont(new Font("helvetica", Font.BOLD, 18));
		northPanel.add(title);
		return northPanel;
    }
    
    /**
     * Builds the auto-save panel
     * @return
     */
    private JPanel buildAutosavePanel() {
    	
    	int row;
		JPanel buttons;
		JPanel autosavePanel;
		
		row = 0;
		buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttons.add(addXslBtn = new JButton(stringFactory.getString(
				LabelStringFactory.MAIN_FRAME_ADD_XSL_BTN)));
		buttons.add(removeCheckedBtn = new JButton(
				stringFactory.getString(
						LabelStringFactory.MAIN_FRAME_REMOVE_CHECKED_BTN)));
		addXslBtn.addActionListener(this);
		removeCheckedBtn.addActionListener(this);
		autosavePanel = new JPanel(xslPanelLayout);
		GUIUtils.add(autosavePanel, buttons, xslPanelLayout, 
				xslPanelConstraints, row++, 0, 1, 3, GridBagConstraints.WEST, 
				GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(autosavePanel, new JLabel(stringFactory.getString(
				LabelStringFactory.MAIN_FRAME_AUTOSAVERESULT)), xslPanelLayout, 
				xslPanelConstraints, row, 0, 1, 1, GridBagConstraints.EAST, 
				GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(autosavePanel, autosaveCb = new JCheckBox(), 
				xslPanelLayout, xslPanelConstraints, row, 1, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.NONE, 
				GUIUtils.SMALL_INSETS);
		autosaveCb.addActionListener(this);
		GUIUtils.add(autosavePanel, autosavePathTf = new JTextField(
				AppConstants.TF_LENGTH-4), xslPanelLayout, xslPanelConstraints, 
				row, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 
				GUIUtils.SMALL_INSETS);			
		GUIUtils.add(autosavePanel, browseAutosavePathBtn = new JButton(
				stringFactory.getString(
						LabelStringFactory.MAIN_FRAME_BROWSE_BTN)), 
				xslPanelLayout, xslPanelConstraints, row, 3, 1, 1, 
				GridBagConstraints.EAST, GridBagConstraints.NONE, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(autosavePanel, validateAutosaveBtn = new JButton(
				stringFactory.getString(
						LabelStringFactory.MAIN_FRAME_VALIDATE_BTN)), 
				xslPanelLayout, xslPanelConstraints, row++, 4, 1, 1, 
				GridBagConstraints.EAST, GridBagConstraints.NONE, 
				GUIUtils.SMALL_INSETS);
		validateAutosaveBtn.addActionListener(this);
		GUIUtils.add(autosavePanel, new JLabel(stringFactory.getString(
				LabelStringFactory.MAIN_FRAME_SUPRESS_OUTPUT_WINDOW)), 
				xslPanelLayout, xslPanelConstraints, row, 0, 1, 1, 
				GridBagConstraints.EAST, GridBagConstraints.NONE, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(autosavePanel, suppressOutputWindowCb = new JCheckBox(), 
				xslPanelLayout, xslPanelConstraints, row++, 1, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.NONE, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(autosavePanel, outputAsTextIfXmlLabel = new JLabel(
				stringFactory.getString(
						LabelStringFactory.
						MAIN_FRAME_DISPLAY_OUTPUT_AS_TEXT_IF_XML)), 
				xslPanelLayout, xslPanelConstraints, row, 0, 1, 1, 
				GridBagConstraints.EAST, GridBagConstraints.NONE, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(autosavePanel, outputAsTextIfXml = new JCheckBox(), 
				xslPanelLayout, xslPanelConstraints, row++, 1, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.NONE, 
				GUIUtils.SMALL_INSETS);
		suppressOutputWindowCb.addActionListener(this);
		return autosavePanel;
    }
    
    /**
     * Builds the south panel
     * @return
     */
    private JPanel buildSouthPanel() {
    	
		JPanel transformBtnPanel;
		JPanel footerPanel;
		JPanel southPanel;
		JPanel panel;
		JLabel label;
		Font footerPanelFont;
		Font footerPanelFontBold;
		
		transformBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		transformBtnPanel.add(transformBtn = new JButton(
				stringFactory.getString(
						LabelStringFactory.MAIN_FRAME_TRANSFORM_BTN)));		
		transformBtnPanel.add(exitBtn = new JButton(stringFactory.getString(
				LabelStringFactory.MAIN_FRAME_EXIT_BTN)));
        
		footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		southPanel = new JPanel(new BorderLayout());
		southPanel.add(transformBtnPanel, BorderLayout.CENTER);
		southPanel.add(footerPanel, BorderLayout.SOUTH);        
		footerPanelFont = new Font("arial", Font.PLAIN, 12);
		footerPanelFontBold = new Font("arial", Font.BOLD, 12);
		footerPanel.setBorder(BorderFactory.createBevelBorder(
				BevelBorder.LOWERED));
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(label = new JLabel(stringFactory.getString(
				LabelStringFactory.MAIN_FRAME_CURRENT_CONFIGURATION)));
		label.setFont(footerPanelFontBold);
		label.setForeground(Color.BLUE);
		panel.add(currentConfigLabel = new JLabel(
				userPrefs.getConfiguration())); 
		currentConfigLabel.setFont(footerPanelFont);		
		footerPanel.add(panel);
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(label = new JLabel(stringFactory.getString(
				LabelStringFactory.MAIN_FRAME_TOTAL_TRANSFORM_TIME)));
		label.setFont(footerPanelFontBold);
		label.setForeground(Color.BLUE);
		panel.add(transformTimeLabel = new JLabel(lastTotalTransformTime + " " + 
				stringFactory.getString(LabelStringFactory.
						MAIN_FRAME_MILLISECONDS_ABBREVIATION)));
		transformTimeLabel.setFont(footerPanelFont);		
		footerPanel.add(panel);
		
		transformTimeLabel.setFont(footerPanelFont);
		footerPanel.add(new JLabel(""));
		footerPanel.add(new JLabel(""));		
		transformBtn.addActionListener(this);
		exitBtn.addActionListener(this);
		return southPanel;
    }

    /**
     * Displays the 'about' dialog
     *
     */
    private void about() {
    	
        StringBuffer aboutMsg;
        
        aboutMsg = new StringBuffer();
        aboutMsg.append(stringFactory.getString(
        		LabelStringFactory.TOOL_ABOUTDIALOG_TOOLTITLE) + "\n\n");
        aboutMsg.append(stringFactory.getString(
        		LabelStringFactory.TOOL_ABOUTDIALOG_VERSION)).append(
        				AppConstants.APP_VERSION).append("\n\n");
        aboutMsg.append(stringFactory.getString(
        		LabelStringFactory.TOOL_DESCRIPTION)).append("\n\n");
        aboutMsg.append(stringFactory.getString(
        		LabelStringFactory.TOOL_DEVELOPED_BY) + "\n\n");
        aboutMsg.append(stringFactory.getString(
        		LabelStringFactory.TOOL_LICENSE));
        Utils.showDialog(this, aboutMsg.toString(), stringFactory.getString(
        		LabelStringFactory.TOOL_ABOUTDIALOG_TITLE), 
        		JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Returns if aTextField is empty or not
     * @param aTextField
     * @return
     */
    private static boolean isEmpty(JTextField aTextField) {
        return StringUtils.isBlank(aTextField.getText());
    }

    /**
     * Returns if the path specified in aFileTextField points to a valid
     * file or not.
     * @param aFileTextField
     * @return
     */
    private static boolean isValidFile(JTextField aFileTextField) {
    	
    	try {
    		fsManager.resolveFile(aFileTextField.getText());
    		return true;
    	} catch (Throwable any) {
    		return false;
    	}
    }
    
    /**
     * Validates the xml file pointed-to by aTextField.
     * @param aLabel
     * @param aTextField
     * @param aSuppressSuccessDialog
     * @return
     */
    private boolean validateXml(String aLabel, JTextField aTextField, 
    		boolean aSuppressSuccessDialog) {
    	
    	boolean isValid;
    	
    	isValid = false;
    	if (isEmpty(aTextField)) {
    		Utils.showDialog(this, stringFactory.getString(
    				LabelStringFactory.MAIN_FRAME_XML_FILE_NOT_SPECIFIED), 
    				stringFactory.getString(
    				LabelStringFactory.MAIN_FRAME_ERROR_LBL), 
    				JOptionPane.ERROR_MESSAGE);
        } else if (!isValidFile(aTextField)) {        	
        	Utils.showDialog(this, stringFactory.getString(
    				LabelStringFactory.
    				MAIN_FRAME_XML_FILE_NOT_EXIST_SPECIFY_VALID_FILE), 
    				stringFactory.getString(
    						LabelStringFactory.MAIN_FRAME_ERROR_LBL), 
					JOptionPane.ERROR_MESSAGE);
        } else {
        	isValid = validateXml(aLabel, aTextField.getText(), 
        			checkSaxWarning.isSelected(), checkSaxError.isSelected(),
                    checkSaxFatalError.isSelected(), this, 
                    aSuppressSuccessDialog);
        }
    	return isValid;
    }
    
    /**
     * Validates the input xml, each of the stylesheets.
     * @return
     */
    private boolean validateAll() {
    	
    	boolean isValid;
    	int loop, size;
    	String autosavePath;
    	XSLRow xslRow;
    	JTextField textField;
    	File file;
    	
    	isValid = true;
    	if (autosaveCb.isSelected()) {
    		autosavePath = autosavePathTf.getText();
    		if (StringUtils.isNotBlank(autosavePath)) {
    			file = new File(autosavePathTf.getText());
   				if ((file.getParentFile() == null) ||  
   						(!(isValid = file.getParentFile().exists()))) {
   					isValid = false;
   					Utils.showDialog(this, MessageFormat.format(
   							stringFactory.getString(LabelStringFactory.
   									MAIN_FRAME_AUTOSAVE_PATH_DOES_NOT_EXIST),
	   						file.getAbsolutePath()), stringFactory.getString(
	   								LabelStringFactory.
	   								MAIN_FRAME_INVALID_AUTOSAVE_PATH),
   						JOptionPane.ERROR_MESSAGE);
   				}
   			} else {
   				isValid = false;
				Utils.showDialog(this, stringFactory.getString(
						LabelStringFactory.
						MAIN_FRAME_PLEASE_SPECIFY_AUTOSAVE_PATH), 
						stringFactory.getString(LabelStringFactory.
								MAIN_FRAME_INVALID_AUTOSAVE_PATH) ,
					JOptionPane.ERROR_MESSAGE);
    		}
    	}
    	if (isValid) {
    	   	if (isValid = validateXml(stringFactory.getString(
    	   			LabelStringFactory.MAIN_FRAME_XML_FILE), sourceXmlTf, 
    	   			true)) {
    			size = xslRows.size();
    			for (loop = 0; (loop < size) && isValid; loop++) {
    				xslRow = xslRows.get(loop);
    				textField = xslRow.getTextField();
    				if (xslRow.isOnAndNotEmpty()) {
    					isValid = validateXml(xslRow.getDescription(), 
    							textField, true);
    				}
    			}
    	   	}
    	}
    	return isValid;
    }
    
    /**
     * Returns true if any XSL input textfields are turned on and have a file
     * specified.
     * @return
     */
    private boolean areAnyStylesheets() {
    	
    	boolean areAnyStylesheets;
    	XSLRow xslRow;
    	int loop, size;
    	
    	areAnyStylesheets = false;
    	size = xslRows.size();
    	for (loop = 0; loop < size; loop++) {
    		xslRow = xslRows.get(loop);
    		if (xslRow.isOnAndNotEmpty()) {
    			areAnyStylesheets = true;
    			break;
    		}
    	}
    	return areAnyStylesheets;
    }
    
    /**
     * Refreshes the XSL-panel
     *
     */
    private void refreshXSLPanel() {
    	rebuildXSLPanel();
		xslPanel.repaint();
		xslPanel.revalidate();
    }
    
    /**
     * Resets the form
     *
     */
    private void resetForm() {
		XSLRow.removeAll(xslRows);
		addXSLRow();
		addXSLRow();
		addXSLRow();
		refreshXSLPanel();
		sourceXmlTf.setText("");
		xmlIdentityTransformOutputProps.clear(); 
		setAreOutputPropertiesSet(false);
		autosaveCb.setSelected(false);
		autosavePathTf.setText("");
		autosavePathTf.setEnabled(false);
		browseAutosavePathBtn.setEnabled(false);
		checkSaxError.setSelected(false);
		checkSaxWarning.setSelected(false);
		checkSaxFatalError.setSelected(false);
		removeCheckedBtn.setEnabled(false);
		suppressOutputWindowCb.setSelected(false);
		outputAsTextIfXml.setSelected(false);
		outputAsTextIfXml.setEnabled(true);
		outputAsTextIfXmlLabel.setEnabled(true);
    }

    /**
     * Method to handle button clicks.
     * @param evt
     */
    public void actionPerformed(ActionEvent evt) {
    	
    	String actionCommand, action, allConfigurations[];
        Object eventSource;
        XSLRow xslRow;
        
        eventSource = evt.getSource();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
	        if (eventSource == transformBtn) {
    	    	if (areAnyStylesheets()) {
        			doTransform();
  	    	  	} else {
    	    		Utils.showDialog(this, 
						stringFactory.getString(LabelStringFactory.
								MAIN_FRAME_SPECIFICY_AT_LEAST_ONE_STYLESHEET), 
								stringFactory.getString(LabelStringFactory.
										MAIN_FRAME_TRANSFORM_MESSAGE), 
										JOptionPane.ERROR_MESSAGE);
      	  		}
  	      	} else if (eventSource == about) {
   	       		about();
   	       		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   	     	} else if (eventSource == exit) {
   	         	destroy();
	        } else if (eventSource == transformTimings) {
	        	new TimingsFrame(this, Utils.toArray(xslRows));
	        	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   	     	} else if (eventSource == validateAutosaveBtn) {
				doValidateXml(stringFactory.getString(
	    	   			LabelStringFactory.MAIN_FRAME_XML_FILE), autosavePathTf, 
	    	   			false);
			} else if (eventSource == exitBtn) {
   	     	   	destroy();
	        } else if (eventSource == resetForm) {
				resetForm();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   	    	} else if (eventSource == browseAutosavePathBtn) {
   	    		populateTFFromFileDialog(autosavePathTf);
   	    		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   	   		} else if (eventSource == browseXmlBtn) {
		   		populateTFFromFileDialog(sourceXmlTf);
		   		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   	    	} else if (eventSource == autosaveCb) { 
   	     		autosavePathTf.setEnabled(autosaveCb.isSelected());
				browseAutosavePathBtn.setEnabled(autosaveCb.isSelected());
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			} else if (eventSource == suppressOutputWindowCb) {
				outputAsTextIfXmlLabel.setEnabled(
						!suppressOutputWindowCb.isSelected());
				outputAsTextIfXml.setEnabled(
						!suppressOutputWindowCb.isSelected());
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	        } else if (eventSource == removeCheckedBtn) {
	        	transformTimings.setEnabled(false);
				XSLRow.removeChecked(xslRows);
				if (xslRows.size() == 0) {
					addXSLRow();
					refreshXSLPanel();
				}
				enableRemoveCheckedBtn();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			} else if (eventSource == addXslBtn) {
				transformTimings.setEnabled(false);
				addXSLRow();
				refreshXSLPanel();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			} else if (eventSource == saveConfiguration) {
				persistUserPrefs();
				userPrefs.persistUserPrefs();
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	        } else if (eventSource == saveAsConfiguration) {
	        	new SaveAsConfigurationFrame(this, 
	        			userPrefs.getConfiguration());
	        	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        	} else if (eventSource == loadConfiguration) {
        		allConfigurations = userPrefs.getAllConfigurations();
        		if (allConfigurations.length > 0) {
        			new LoadConfigurationFrame(this, 
        					userPrefs.getConfiguration(), allConfigurations);
        			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        		} else {
        			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        			Utils.showDialog(this, MessageFormat.format(
        					stringFactory.getString(LabelStringFactory.
        						MAIN_FRAME_ONLY_CONFIGURATION),
        						userPrefs.getConfiguration(), 
        						saveAsConfiguration.getText()),
        						stringFactory.getString(LabelStringFactory.
                						MAIN_FRAME_MESSAGE), 
                						JOptionPane.INFORMATION_MESSAGE);
        		} 
	        } else if (eventSource == xmlAction) {
	        	if (xmlAction.getItemCount() > 0) {
	        		action = (String)xmlAction.getSelectedItem();
	        		xmlAction.setSelectedIndex(0);
	        		if (action.equals(XML_ACTIONS[XML_VALIDATE_ACTION_INDEX])) {
						components.clear();
						components.add(sourceXmlTf);
						components.add(xmlAction);
						components.add(browseXmlBtn);
	        			doValidateXml(stringFactory.getString(
	    	    	   			LabelStringFactory.MAIN_FRAME_XML_FILE), 
	    	    	   			sourceXmlTf, false);
	        		} else if (action.equals(
	        				XML_ACTIONS[XML_VIEW_EDIT_OUTPUT_PROPS_INDEX])) {
	        			new TransformOutputPropertiesFrame(this,
	        				xmlIdentityTransformOutputProps, 
	        				sourceXmlTf.getText());
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	        		} else if (action.equals(
	        				XML_ACTIONS[XML_CLEAR_OUTPUT_PROPS_INDEX])) {
						xmlIdentityTransformOutputProps.clear();
						setAreOutputPropertiesSet(false);	
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));					
	        		} else if (action.equals(
	        			XML_ACTIONS[XML_DO_IDENTITY_TRANSFORM_ACTION_INDEX])) {
	        			
	        			if (validateXml(stringFactory.getString(
	    	    	   			LabelStringFactory.MAIN_FRAME_XML_FILE), 
	    	    	   			sourceXmlTf, true)) {							
							components.clear();
							components.add(sourceXmlTf);
							components.add(browseXmlBtn);
							components.add(xmlAction);
							doIdentityTransform(
								stringFactory.getString(
										LabelStringFactory.
										MAIN_FRAME_SELECT_FILE_FOR_IT_RESULT),
								sourceXmlTf.getText(), 
								sourceXmlTf.getText());
	        			} else {
							setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	        			}
	        		} else {
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	        		}
	        	}
	        } else {	        
				actionCommand = evt.getActionCommand();
				if (AppConstants.INSERT.equals(actionCommand)) {
					xslRow = XSLRow.getRowByInsertBtn(xslRows,
						(JButton)eventSource);
					insertXSLRow(xslRow.getIndex());
					refreshXSLPanel();
				} else if (AppConstants.REMOVE_CB.equals(actionCommand)) {
					enableRemoveCheckedBtn();
				} else if (stringFactory.getString(
		        		LabelStringFactory.MAIN_FRAME_BROWSE_BTN).equals(
		        				actionCommand)) {
					xslRow = XSLRow.getRowByBrowseBtn(xslRows, 
						(JButton)eventSource);
					populateTFFromFileDialog(xslRow.getTextField());
				} else if (AppConstants.TAKE_ACTION.equals(actionCommand)) {
					xslRow = XSLRow.getRowByAction(xslRows, 
							(JComboBox)eventSource);
					if (xslRow.getAction().getItemCount() > 0) {
						action = (String)xslRow.getAction().getSelectedItem();
						xslRow.setSelectedActionIndex(0);
						if (action.equals(
								XSLRow.ACTIONS[XSLRow.VALIDATE_INDEX])) {
							components.clear();
							components.add(xslRow.getTextField());
							components.add(xslRow.getAction());
							components.add(xslRow.getRemoveCb());
							components.add(xslRow.getInsertBtn());
							components.add(xslRow.getBrowseBtn());
							doValidateXml(stringFactory.getString(
									LabelStringFactory.MAIN_FRAME_XSL_PREFIX) + 
									(xslRow.getIndex() + 1), 
								xslRow.getTextField(), false);
						} else if (
								action.startsWith(XSLRow.ON_OFF_ITEM_PREFIX)) {
							xslRow.toggleOnOffBtn();
						} else if (action.equals(
						XSLRow.ACTIONS[XSLRow.VIEW_EDIT_OUTPUT_PROPS_INDEX])) {
							new TransformOutputPropertiesFrame(this, xslRow);
						} else if (action.equals(
							XSLRow.ACTIONS[XSLRow.CLEAR_OUTPUT_PROPS_INDEX])) {
							xslRow.setAreOutputPropertiesSet(false);
							xslRow.clearOutputProperties();
						} else if (action.equals(
								XSLRow.ACTIONS[XSLRow.
								               CLEAR_ALL_PARAMETERS_INDEX])) {
							xslRow.clearAllParameters();							
						} else if (action.equals(XSLRow.ACTIONS[XSLRow.
						                         VIEW_EDIT_PARAMETERS_INDEX])) {
							new TransformParametersFrame(this, xslRow);
						} else if (action.equals(XSLRow.ACTIONS[XSLRow.
						                   PERFORM_IDENTITY_TRANSFORM_INDEX])) {
							if (validateXml(stringFactory.getString(
									LabelStringFactory.MAIN_FRAME_XSL_PREFIX) + 
									(xslRow.getIndex()+1),
								xslRow.getTextField(), true)) {
								components.clear();
								components.add(xslRow.getBrowseBtn());
								components.add(xslRow.getRemoveCb());
								components.add(xslRow.getInsertBtn());
								components.add(xslRow.getTextField());
								doIdentityTransform(
									stringFactory.getString(
											LabelStringFactory.
											MAIN_FRAME_PICK_FILE_FOR_IT),
									xslRow.getTextField().getText(),
									xslRow.getTextField().getText());									
							} else {
								setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
							}							
						}
					}
				}
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
   	    	}
        } catch (Throwable aAny) {
        	// log and show dialog...
        	logger.error(ExceptionUtils.getFullStackTrace(aAny));
        	Utils.showErrorDialog(this, aAny);
        }
    }
    
    /**
     * Refreshes the configuration label
     *
     */
    public void refreshConfigurationLabel() {
    	currentConfigLabel.setText(userPrefs.getConfiguration());
    }
    
    /**
     * Refreshes the entire application window
     *
     */
    public void refreshApplication() {
    	refreshStylesheets();
		refreshConfigurationLabel();
		initializeControls();
    }
    
    /**
     * Helper method that populates aTextField by presenting a file-open dialog
     * to the user.
     * @param aTextField
     */
    public void populateTFFromFileDialog(JTextField aTextField) 
    throws IOException {
    	
    	int returnVal;
    	File file;
    	
		Utils.getInstance().getFileChooser().setDialogType(
				JFileChooser.OPEN_DIALOG);
		returnVal = Utils.getInstance().getFileChooser().showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = Utils.getInstance().getFileChooser().getSelectedFile();
			setLastFileChosen(file.getAbsolutePath());
			aTextField.setText(file.getAbsolutePath());
		}
    }
    
    /**
     * Inserts a new XSL row
     * @param aIndex
     */
    private void insertXSLRow(int aIndex) {
    	
    	int size, loop;
    	
    	xslRows.add(aIndex, new XSLRow(this, xslPanel, 
    			new JButton(AppConstants.INSERT), 
    			// label text is updated by subsequent XSLRow.setIndex() call...
				new JLabel(""), 
				new JTextField(AppConstants.TF_LENGTH),
				new JCheckBox(), new JButton(stringFactory.getString(
		        		LabelStringFactory.MAIN_FRAME_BROWSE_BTN)), 
				new JComboBox(), new JLabel(), -1));
    	size = xslRows.size();
    	for (loop = 0; loop < size; loop++) {
    		((XSLRow)(xslRows.get(loop))).setIndex(loop);
    	}
    }
    
    /**
     * Appends a new XSL row
     *
     */
    private void addXSLRow() {
    	
    	int size;
    	
    	size = xslRows.size();
		xslRows.add(new XSLRow(this, xslPanel, new JButton(AppConstants.INSERT), 
					new JLabel(MessageFormat.format(stringFactory.getString(
							LabelStringFactory.XSLROW_XSL_LABEL), 
							xslRows.size()+1)),
					new JTextField(AppConstants.TF_LENGTH),
					new JCheckBox(), new JButton(stringFactory.getString(
			        		LabelStringFactory.MAIN_FRAME_BROWSE_BTN)), 
					new JComboBox(), new JLabel(), size));
    }
    
    /**
     * Enables or disables the "remove checked" button.  If any of the xsl
     * rows are checked, the button will be enabled.
     *
     */
    private void enableRemoveCheckedBtn() {
    	
    	boolean enable;
    	int size, loop;
    	
    	enable = false;
    	size = xslRows.size();
    	for (loop = 0; loop < size; loop++) {
    		if (((XSLRow)(xslRows.get(loop))).getRemoveCb().isSelected()) {
    			enable = true;
    			break;
    		}
    	}
    	removeCheckedBtn.setEnabled(enable);
    }

    /**
     * Method to validate the source xml.
     */
    private boolean validateXml(String aLabel, String sourceXmlFile, 
    		boolean checkWarnings, boolean checkErrors, 
			boolean checkFatalErrors, JFrame parent, 
			boolean aSuppressSuccessDialog) {
    	
        boolean isValid;
        FileContent content;
        
        isValid = false;
        try {
        	content = fsManager.resolveFile(sourceXmlFile).getContent();
            Utils.getInstance().isValidXml(content, checkWarnings, checkErrors, 
            		checkFatalErrors);
            isValid = true;
            if (!aSuppressSuccessDialog) {
            	Utils.showDialog(parent, MessageFormat.format(
            			stringFactory.getString(LabelStringFactory.
            					MAIN_FRAME_VALID_XML_MSG), 
            					sourceXmlFile), 
            					stringFactory.getString(LabelStringFactory.
                    					MAIN_FRAME_VALID_XML_MSG_HDR_YES), 
					JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (UnknownHostException aException) {
        	logger.error(aException);
        	Utils.handleXMLError(stringFactory.getString(LabelStringFactory.
					MAIN_FRAME_VALID_XML_MSG_HDR_NO), aLabel, 
					stringFactory.getString(LabelStringFactory.
							MAIN_FRAME_XML_VALIDATION_ERR), 
            		sourceXmlFile, this, aException.getMessage());
        } catch (SocketException aException) {
        	logger.error(aException);
        	Utils.handleXMLError(stringFactory.getString(LabelStringFactory.
					MAIN_FRAME_VALID_XML_MSG_HDR_NO), aLabel, 
					stringFactory.getString(LabelStringFactory.
							MAIN_FRAME_XML_VALIDATION_ERR), 
            		sourceXmlFile, this, aException.getMessage());
        } catch (Throwable aAny) {
        	Utils.handleXMLError(stringFactory.getString(LabelStringFactory.
					MAIN_FRAME_VALID_XML_MSG_HDR_NO), aLabel, 
					stringFactory.getString(LabelStringFactory.
							MAIN_FRAME_XML_VALIDATION_ERR), 
            		sourceXmlFile, this, aAny);
        }
        return isValid;
    }

    /**
     * Returns true if a stylesheet between the indexes of aStartIndex
     * and the end of the list are enabled and have a file specified.
     * @param aStartIndex
     * @param aSizeOfList
     * @return
     */
    private boolean isNextStylesheet(int aStartIndex, int aSizeOfList) {
    	
    	boolean isNextStylesheet;
    	int loop;
    	
    	isNextStylesheet = false;
    	for (loop = aStartIndex; loop < aSizeOfList; loop++) {
    		if (((XSLRow)(xslRows.get(loop))).isOnAndNotEmpty()) {
    			isNextStylesheet = true;
    			break;
    		}
    	}
    	return isNextStylesheet;
    }
    
    /**
     * Initiates the xml-validation in a separate thread.
     * @param aLabel
     * @param aTextField
     * @param aSuppressSuccessDialog
     */
    private void doValidateXml(String aLabel, JTextField aTextField, 
    		boolean aSuppressSuccessDialog) {
    	label = aLabel;
    	textField = aTextField;
    	suppressSuccessDialog = aSuppressSuccessDialog;
    	threadMode = THREADMODE_DO_VALIDATE;
    	Utils.setEnabled(components, false);
    	new Thread(this).start();
    }
    
    /**
     * Entry point of threads.
     */
    public void run() { 
    	
    	String sresultsFile;   	
    	File xmlFile, resultsFile;
    	byte results[];
    	
    	try {
    		switch (threadMode) {
    		case THREADMODE_DO_TRANSFORM:
    			executeTransform();
    			break;
    		case THREADMODE_DO_VALIDATE:
    			validateXml(label, textField, suppressSuccessDialog);
    			break;
    		case THREADMODE_DO_XML_IDENTITY_TRANSFORM:
    			xmlFile = new File(identityTransformSourceXmlFile);
    			sresultsFile = (String)JOptionPane.showInputDialog(this, 
    					identityTransformMessage, stringFactory.getString(
    							LabelStringFactory.
    							MAIN_FRAME_IDENTITY_TRANSFORM), 
						JOptionPane.QUESTION_MESSAGE,
						null, null, identityTransformResultXmlFile);
    			if (sresultsFile != null) {
    				resultsFile = new File(sresultsFile);
    				resultsFile.getParentFile().mkdirs();
    				results = XMLUtils.transform(xmlFile, 
    	    		xmlIdentityTransformOutputProps);
    	    		IOUtils.writeFile(resultsFile, results);	
    			}
    			break;
    		}
    	} catch (Throwable aAny) {
    		logger.error(ExceptionUtils.getFullStackTrace(aAny));
    		Utils.showErrorDialog(this, aAny);
    	} finally {
			Utils.setEnabled(components, true);
    		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	}
    }
    
    /**
     * Initiates the transform operation in a seperate thread.
     *
     */
    private void doTransform() {
    	components.clear();
    	components.add(transformBtn);
    	threadMode = THREADMODE_DO_TRANSFORM;
    	Utils.setEnabled(components, false);
    	new Thread(this).start();
    }
    
    /**
     * Initiates the identity-transform operation in a separate thread.
     * @param aMessage
     * @param aIdentityTransformSourceXmlFile
     * @param aIdentityTransformResultXmlFile
     */
    private void doIdentityTransform(String aMessage, 
    	String aIdentityTransformSourceXmlFile,
    	String aIdentityTransformResultXmlFile) {
    	Utils.setEnabled(components, false);
    	threadMode = THREADMODE_DO_XML_IDENTITY_TRANSFORM;
    	identityTransformMessage = aMessage;
    	identityTransformSourceXmlFile = aIdentityTransformSourceXmlFile;
		identityTransformResultXmlFile = aIdentityTransformResultXmlFile;    	
    	new Thread(this).start();
    }
    
    /**
     * Performs transforms.
     * @throws Exception
     */
    private void executeTransform() throws Exception {
    	
    	int loop;
    	XSLRow lrows[];
    	
    	if (validateAll()) {
    		lrows = Utils.toArray(xslRows);
    		for (loop = 0; loop < lrows.length; loop++) {
    			lrows[loop].setTimeToTransform(0);
    		}
			transform(Utils.getXMLContents(fsManager, sourceXmlTf.getText()));        			
		}
    }
    
    /**
     * Method to do xslt transform.
     */
    private void transform(byte aXmlContents[]) throws TransformerException,
		IOException, ParserConfigurationException, SAXException {

		int loop, size;
		long transformTime;
		byte tmp[];
    	XSLRow xslRow;
    	byte transformResult[];
    	boolean success;
    	TransformOutputProperties transformOutputProps;
    	TransformParameters transformParameters;
    	
    	xslRow = null;
    	transformOutputProps = null;
    	transformParameters = null;
    	size = xslRows.size();
		transformResult = aXmlContents;
		success = true;
		lastTotalTransformTime = 0;
		try {
	    	for (loop = 0; loop < size; loop++) {
   		 		xslRow = (XSLRow)xslRows.get(loop);
   	 			transformOutputProps = xslRow.getTransformOutputProperties();
   	 			transformParameters = xslRow.getTransformParameters();
   	 			if (xslRow.isOnAndNotEmpty()) {
   	 				transformResult = XMLUtils.transform(tmp = transformResult, 
   	 					Utils.getXSLSource(fsManager, 
   	 							xslRow.getTextField().getText()),
   	 					transformOutputProps, transformParameters);
   	 				transformTime = XMLUtils.getTransformTime(tmp, 
   	 					Utils.getXSLSource(fsManager, 
   	 							xslRow.getTextField().getText()),
	   	 					transformOutputProps, transformParameters);
    				xslRow.setTimeToTransform(transformTime);
    				lastTotalTransformTime += transformTime;
    				if (!XMLUtils.isXml(transformResult)) {
    					if (isNextStylesheet(loop + 1, size)) {
    						success = false;
    						Utils.showDialog(this, MessageFormat.format(
    								stringFactory.getString(
    								LabelStringFactory.
    								MAIN_FRAME_TRANSFORM_RESULT_NOT_XML), 
    								(loop + 1)), stringFactory.getString(
    	    								LabelStringFactory.
    	    								MAIN_FRAME_TRANSFORM_ERR_MSG), 
    							JOptionPane.INFORMATION_MESSAGE);  
    						break;  					
    					}
    				}
    			}
    		}
		} catch (TransformerException aTransformerException) {
			success = false;
			Utils.handleXMLError(stringFactory.getString(
					LabelStringFactory.MAIN_FRAME_ERR_IN_XSL), 
					xslRow.getDescription(),
				stringFactory.getString(
						LabelStringFactory.MAIN_FRAME_XSL_TRANSFORMATION_ERR), 
						xslRow.getTextField().getText(),
				this, aTransformerException);
		}
    	if (success) {
    		transformTimeLabel.setText(lastTotalTransformTime + " " +
    				stringFactory.getString(
    						LabelStringFactory.
    						MAIN_FRAME_MILLISECONDS_ABBREVIATION));
			logger.info("total transform time: " + lastTotalTransformTime + 
					" " + stringFactory.getString(LabelStringFactory.
					MAIN_FRAME_MILLISECONDS_ABBREVIATION));
    		transformTimings.setEnabled(true);
			if (autosaveCb.isSelected()) {
				IOUtils.writeFile(new File(autosavePathTf.getText()), 
					transformResult);    		
			}
			if (!suppressOutputWindowCb.isSelected()) {
				if (XMLUtils.isXml(transformResult)) {
					if (outputAsTextIfXml.isSelected()) {
						new OutputFrame(this, stringFactory.getString(
								LabelStringFactory.
								MAIN_FRAME_TRANSFORM_RESULTS), transformResult,
							Utils.toArray(xslRows));
					} else {
						new OutputFrame(this, stringFactory.getString(
								LabelStringFactory.
								MAIN_FRAME_TRANSFORM_RESULTS), 
							XMLUtils.getDocument(transformResult), 
							transformOutputProps, 
							Utils.toArray(xslRows), true);
					}					
				} else {
					new OutputFrame(this, stringFactory.getString(
							LabelStringFactory.MAIN_FRAME_TRANSFORM_RESULTS), 
							transformResult, Utils.toArray(xslRows));
				}
			}
    	}
    }

    /**
     * Writes transform output property values to the user preferences
     * object.
     * @param aOutputProperties
     * @param aPropertyNamePrefix
     */
	private void persistOutputProperties(
			TransformOutputProperties aOutputProperties,
			String aPropertyNamePrefix) {
			
		String val;
		
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getCDATA_SECTION_ELEMENTS())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.CDATA_SECTION_ELEMENTS, val);
		}
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getDOCTYPE_PUBLIC())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.DOCTYPE_PUBLIC, val);
		}           	
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getDOCTYPE_SYSTEM())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.DOCTYPE_SYSTEM, val);
		}
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getENCODING())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.ENCODING, val);
		}
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getINDENT())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.INDENT, val);
		}
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getMEDIA_TYPE())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.MEDIA_TYPE, val);
		}			
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getMETHOD())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.METHOD, val);
		}			
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getOMIT_XML_DECLARATION())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.OMIT_XML_DECLARATION, val);
		}			
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getSTANDALONE())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.STANDALONE, val);
		}			
		if (StringUtils.isNotBlank(val = 
			aOutputProperties.getVERSION())) {
			userPrefs.setProperty(aPropertyNamePrefix + 
				AppConstants.VERSION, val);
		}		
	}

	/**
	 * Persists the user preferences object to the preferences file.
	 *
	 */
    private void persistUserPrefs() {
    	
    	int loop, size, innerLoop;
    	String paramNames[];
    	XSLRow xslRow;
    	String propertyName;
        TransformParameters parameters;
        
        userPrefs.clearDynamicPrefs();
        userPrefs.setProperty(AppConstants.CHK_WARNINGS_PROP, 
			"" + checkSaxWarning.isSelected());
        userPrefs.setProperty(AppConstants.CHK_ERRORS_PROP, 
			"" + checkSaxError.isSelected());
        userPrefs.setProperty(AppConstants.CHK_FATAL_ERRORS_PROP, 
			"" + checkSaxFatalError.isSelected());
        userPrefs.setProperty(AppConstants.X_COORD_PROP, 
           	Integer.toString(this.getX()));
        userPrefs.setProperty(AppConstants.Y_COORD_PROP, 
           	Integer.toString(this.getY()));
        userPrefs.setProperty(AppConstants.LAST_XML_FILE_PROP, 
           	sourceXmlTf.getText());
        userPrefs.setProperty(AppConstants.SUPPRESS_OUTPUT_WINDOW_PROP, 
			"" + suppressOutputWindowCb.isSelected());
		userPrefs.setProperty(AppConstants.OUTPUT_AS_TEXT_IF_XML_PROP, 
			"" + outputAsTextIfXml.isSelected());
        userPrefs.setProperty(AppConstants.AUTOSAVE_RESULT_PROP, 
			"" + autosaveCb.isSelected());
        userPrefs.setProperty(AppConstants.AUTOSAVE_FILE_PROP, 
           	autosavePathTf.getText());
        
		persistOutputProperties(xmlIdentityTransformOutputProps, 
			"xml_identity_transform_outputproperties_");
		userPrefs.setProperty("xml_identity_transform_opInd", 
			"" + areXmlOutputPropertiesSet);
		size = xslRows.size();
        for (loop = 0; loop < size; loop++) {
          	xslRow = (XSLRow)xslRows.get(loop);
           	// text field value
           	propertyName = "xsl_" + loop + "_file";
           	userPrefs.setProperty(propertyName, 
           		xslRow.getTextField().getText());
           	// on/off
           	propertyName = "xsl_" + loop + "_onoff";
           	userPrefs.setProperty(propertyName, "" + xslRow.isOn());   
           	// output properties indicator
           	propertyName = "xsl_" + loop + "_opInd";
           	userPrefs.setProperty(propertyName, "" + 
           			xslRow.areOutputPropertiesSet());
           	
           	persistOutputProperties(xslRow.getTransformOutputProperties(), 
				"xsl_" + loop + "_outputproperties_");
			parameters = xslRow.getTransformParameters();
			paramNames = parameters.getParameterNames();
			for (innerLoop = 0; innerLoop < paramNames.length; innerLoop++) {
				userPrefs.setProperty("xsl_" + loop + "_params_" + 
					innerLoop + "_" + paramNames[innerLoop], 
					(String)parameters.getParameter(paramNames[innerLoop]));
			}
        }           
        userPrefs.setProperty(AppConstants.NUM_STYLESHEETS_PROP, 
           	Integer.toString(size)); 
        userPrefs.setProperty(AppConstants.FRAME_HEIGHT_PROP,
           	Integer.toString(getHeight()));
		userPrefs.setProperty(AppConstants.FRAME_WIDTH_PROP,
			Integer.toString(getWidth()));
		if (StringUtils.isNotBlank(lastFileChosen)) {
			userPrefs.setProperty(AppConstants.LAST_FILE_CHOSEN_PROP,
				lastFileChosen);
		}
    }

    /**
     * Method to shutdown the app.
     */
    private void destroy() {
    	try {
    		persistUserPrefs();
    		userPrefs.persistUserPrefs();
    	} catch (IOException aException) {
    		logger.error(ExceptionUtils.getFullStackTrace(aException));
    		Utils.showErrorDialog(this, aException);
    	} finally {
    		dispose();
    		System.exit(0);
    	}
    }

    /**
     * class main() method - application entry point
     * @param aArgs
     */
    public static void main(String aArgs[]) throws Exception {
		JFrame.setDefaultLookAndFeelDecorated(true);
        new BasicXSLTFrame();
    }
}