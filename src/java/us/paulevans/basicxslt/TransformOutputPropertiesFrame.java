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
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.blueslate.commons.gui.GUIUtils;
import net.blueslate.commons.xml.TransformOutputProperties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Defines the output-properties frame
 * @author pevans
 *
 */
public class TransformOutputPropertiesFrame extends JFrame 
	implements ActionListener {		
	
	// get the i18n factory singleton instance...
	private static final LabelStringFactory stringFactory = 
		LabelStringFactory.getInstance();
	
	// frame width and height values...
	private static final int FRAME_WIDTH = 585;
	private static final int FRAME_HEIGHT = 420;

	// static constants...
	private static final int XML_INDEX = 0;
	private static final int HTML_INDEX = 1;
	private static final int TEXT_INDEX = 2;
	private static final String VALID_METHODS[] = { "xml", "html", "text" };

	// instance members...
	private JButton cancelBtn, okayBtn;
	private XSLRow xslRow;
	private JTextArea CDATASectionElements;
	private JTextField doctypePublic, doctypeSystem, encoding, mediaType, 
	version;
	private JRadioButton xml, html, text, other;
	private JTextField otherMethod;
	private JCheckBox indent, omitXmlDeclaration, standalone;
	private BasicXSLTFrame parent;
	private TransformOutputProperties xmlIdentityTransformOutputProps;

	/**
	 * Constructor
	 * @param aParent
	 * @param aXmlIdentityTransformOutputProps
	 * @param aXmlFile
	 */
	public TransformOutputPropertiesFrame(BasicXSLTFrame aParent,
		TransformOutputProperties aXmlIdentityTransformOutputProps,
		String aXmlFile) {
		parent = aParent;
		xmlIdentityTransformOutputProps = aXmlIdentityTransformOutputProps;
		buildGui(buildNorthPanel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_IT_OUTPUT_PROPERTIES), 
			aXmlFile), xmlIdentityTransformOutputProps);
	}

	/**
	 * Constructor
	 * @param aParent
	 * @param aXSLRow
	 * @throws IOException
	 */
	public TransformOutputPropertiesFrame(BasicXSLTFrame aParent, 
			XSLRow aXSLRow) {
		parent = aParent;
		xslRow = aXSLRow;
		buildGui(buildNorthPanel(stringFactory.getString(
				LabelStringFactory.
				OUTPUTPROPS_FRAME_TRANSFORM_OUTPUT_PROPERTIES) + " (" 
			+ xslRow.getDescription() + ")", xslRow.getTextField().getText()),
			xslRow.getTransformOutputProperties());
	}
	
	/**
	 * Builds the GUI
	 * @param aNorthPanel
	 * @param aOutputProps
	 */
	private void buildGui(JPanel aNorthPanel, 
			TransformOutputProperties aOutputProps) {
		
		JPanel southPanel, mainPanel;	
		
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
		getContentPane().add(aNorthPanel, BorderLayout.NORTH);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		getContentPane().add(new JScrollPane(mainPanel), BorderLayout.CENTER);		
		setTitle(stringFactory.getString(LabelStringFactory.
				OUTPUTPROPS_FRAME_TRANSFORM_OUTPUT_PROPERTIES));		
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		GUIUtils.center(this, parent);
		initializeGUI(aOutputProps);
		setVisible(true);		
	}
	
	/**
	 * Builds the north panel
	 * @param aHeaderLabel
	 * @param aFile
	 * @return
	 */
	private JPanel buildNorthPanel(String aHeaderLabel, String aFile) {
		
		GridBagLayout layout;
		GridBagConstraints constraints;
		JPanel panel;
		int row, col;
		JLabel headerLabel, fileLabel;
		
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		panel = new JPanel(layout);
		headerLabel = new JLabel(aHeaderLabel);
		row = 0;
		col = 0;
		headerLabel.setFont(new Font("arial", Font.PLAIN, 18));
		fileLabel = new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_FILE_LBL) + aFile);
		fileLabel.setFont(new Font("arial", Font.PLAIN, 12));			
		GUIUtils.add(panel, headerLabel, layout, constraints, row++, col, 
			1, 1, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH,
			GUIUtils.MED_LARGE_INSETS);
		GUIUtils.add(panel, new JSeparator(), layout, constraints, row++, col, 
			1, 1, 1, 1, GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, 
			GUIUtils.MED_LARGE_INSETS);
		GUIUtils.add(panel, fileLabel, layout, constraints, row++, col, 
			1, 1, 1, 1, GridBagConstraints.NORTHEAST, 
			GridBagConstraints.BOTH, GUIUtils.MED_LARGE_INSETS);				
		return panel;
	}    
	
	/**
	 * Initializes the GUI
	 * @param aOutputProperties
	 */
	private void initializeGUI(TransformOutputProperties aOutputProperties) {
		
		String method;
		
		CDATASectionElements.setText(
			StringUtils.defaultString(
				aOutputProperties.getCDATA_SECTION_ELEMENTS()));
		doctypePublic.setText(
			StringUtils.defaultString(
				aOutputProperties.getDOCTYPE_PUBLIC()));
		doctypeSystem.setText(
			StringUtils.defaultString(aOutputProperties.getDOCTYPE_SYSTEM()));
		encoding.setText(
			StringUtils.defaultString(aOutputProperties.getENCODING()));
		indent.setSelected(
			BooleanUtils.toBoolean(aOutputProperties.getINDENT()));
		mediaType.setText(
			StringUtils.defaultString(aOutputProperties.getMEDIA_TYPE()));
		version.setText(
			StringUtils.defaultString(aOutputProperties.getVERSION()));
		omitXmlDeclaration.setSelected(
			BooleanUtils.toBoolean(
					aOutputProperties.getOMIT_XML_DECLARATION()));
		standalone.setSelected(
			BooleanUtils.toBoolean(aOutputProperties.getSTANDALONE()));
		method = aOutputProperties.getMETHOD();
		if (StringUtils.isNotBlank(method)) {
			if (method.equals(VALID_METHODS[XML_INDEX])) {
				xml.setSelected(true);			
			} else if (method.equals(VALID_METHODS[HTML_INDEX])) {
				html.setSelected(true);
				setEnableXmlCheckboxes(false);
			} else if (method.equals(VALID_METHODS[TEXT_INDEX])) {
				text.setSelected(true);
				setEnableXmlCheckboxes(false);
			} else {
				setEnableXmlCheckboxes(false);
				other.setSelected(true);
				otherMethod.setEnabled(true);
				otherMethod.setBackground(Color.WHITE);
				otherMethod.setText(aOutputProperties.getMETHOD());
			}
		} else {
			xml.setSelected(true);	
		}
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
		
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		main = new JPanel(layout);	
		row = 0;	
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_CDATA_SECTION_ELEMENTS)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.NORTHEAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JScrollPane(CDATASectionElements = 
			new JTextArea(3, 30)), layout, constraints, row++, 1, 1, 1, 
			GridBagConstraints.WEST, GridBagConstraints.NONE, 
			GUIUtils.SMALL_INSETS);			
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_DOCTYPE_PUBLIC)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, doctypePublic = new JTextField(30), layout, 
			constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_DOCTYPE_SYSTEM)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, doctypeSystem = new JTextField(30), layout, 
			constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);			
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_ENCODING)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, encoding = new JTextField(30), layout, 
			constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_MEDIA_TYPE)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, mediaType = new JTextField(30), layout, 
			constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_METHOD)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, buildMethodPanel(), layout, 
			constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_VERSION)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, version = new JTextField(30), layout, 
			constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_INDENT)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, indent = new JCheckBox(), layout, 
			constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_OMIT_XML_DECLARATION)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, omitXmlDeclaration = new JCheckBox(), layout, 
			constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, new JLabel(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_IS_STANDALONE)),
			layout, constraints, row, 0, 1, 1, GridBagConstraints.EAST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(main, standalone = new JCheckBox(), layout, 
			constraints, row++, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		return main;
	}
	
	/**
	 * Builds the method panel
	 * @return
	 */
	private JPanel buildMethodPanel() {
		
		int row;
		ButtonGroup group;
		GridBagLayout layout;
		GridBagConstraints constraints;
		JPanel panel;
		
		row = 0;
		group = new ButtonGroup();
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		panel = new JPanel(layout);
		GUIUtils.add(panel, xml = new JRadioButton(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_METHODS_XML) + " | "), 
				layout, constraints, row, 0, 1, 1, GridBagConstraints.WEST, 
				GridBagConstraints.NONE, GUIUtils.NO_INSETS);
		GUIUtils.add(panel, html = new JRadioButton(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_METHODS_HTML) + " | "), 
				layout, constraints, row, 1, 1, 1, GridBagConstraints.WEST, 
				GridBagConstraints.NONE, GUIUtils.NO_INSETS);
		GUIUtils.add(panel, text = new JRadioButton(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_METHODS_TEXT) + " | "), 
				layout, constraints, row, 2, 1, 1, GridBagConstraints.WEST, 
				GridBagConstraints.NONE, GUIUtils.NO_INSETS);
		GUIUtils.add(panel, other = new JRadioButton(stringFactory.getString(
				LabelStringFactory.OUTPUTPROPS_FRAME_METHODS_OTHER)), layout, 
			constraints, row, 3, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.NO_INSETS);
		GUIUtils.add(panel, otherMethod = new JTextField(10), layout, 
			constraints, row, 4, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.NO_INSETS);
		group.add(xml);
		group.add(html);
		group.add(text);
		group.add(other);		
		xml.addActionListener(this);
		html.addActionListener(this);
		text.addActionListener(this);
		other.addActionListener(this);
		xml.setSelected(true);
		otherMethod.setEnabled(false);
		otherMethod.setBackground(Color.LIGHT_GRAY);
		return panel;
	}
	
	/**
	 * Sets aOutputProperties from the state of the GUI
	 * @param aOutputProperties
	 */
	private void setOutputProperties(
			TransformOutputProperties aOutputProperties) {
		
		String val;
		
		aOutputProperties.setCDATA_SECTION_ELEMENTS(
				StringUtils.strip(CDATASectionElements.getText()));
		aOutputProperties.setDOCTYPE_PUBLIC(
				StringUtils.strip(doctypePublic.getText()));
		aOutputProperties.setDOCTYPE_SYSTEM(doctypeSystem.getText());
		aOutputProperties.setENCODING(encoding.getText());
		aOutputProperties.setMEDIA_TYPE(mediaType.getText());
		aOutputProperties.setENCODING(encoding.getText());
		aOutputProperties.setVERSION(version.getText());
		aOutputProperties.setOMIT_XML_DECLARATION(
			omitXmlDeclaration.isSelected());
		aOutputProperties.setINDENT(indent.isSelected());
		aOutputProperties.setSTANDALONE(standalone.isSelected());
		if (xml.isSelected()) {
			val = VALID_METHODS[XML_INDEX];
		} else if (html.isSelected()) {
			val = VALID_METHODS[HTML_INDEX];
		} else if (text.isSelected()) {
			val = VALID_METHODS[TEXT_INDEX];
		} else {
			val = otherMethod.getText();
		}
		aOutputProperties.setMETHOD(val);
	}
	
	/**
	 * Enable/disable the xml-checkboxes
	 * @param aEnable
	 */
	private void setEnableXmlCheckboxes(boolean aEnable) {
		standalone.setEnabled(aEnable);
		indent.setEnabled(aEnable);
		omitXmlDeclaration.setEnabled(aEnable);
	}
	
	/**
	 * Event handler
	 */
	public void actionPerformed(ActionEvent aEvent) {
		
		Object eventSource;
		
		eventSource = aEvent.getSource();
		if (eventSource == cancelBtn) {
			dispose();
		} else if (eventSource instanceof JRadioButton) {
			setEnableXmlCheckboxes(eventSource == xml);
			if (eventSource == other) {
				otherMethod.setEnabled(true);
				otherMethod.requestFocus();
				otherMethod.setBackground(Color.WHITE);
			} else {
				otherMethod.setEnabled(false);
				otherMethod.setBackground(Color.LIGHT_GRAY);
			}
		} else if (eventSource == okayBtn) {
			if (xslRow != null) {
				setOutputProperties(xslRow.getTransformOutputProperties());
				xslRow.setAreOutputPropertiesSet(true);		
			} else {
				setOutputProperties(xmlIdentityTransformOutputProps);
				parent.setAreOutputPropertiesSet(true);
			}
			dispose();
		}
	}
}

