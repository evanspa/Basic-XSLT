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
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import net.blueslate.commons.xml.TransformParameters;

import org.apache.commons.lang.StringUtils;

/**
 * Defines the parameters frame
 * @author pevans
 *
 */
public class TransformParametersFrame extends JFrame 
	implements ActionListener {		
	
	// static constants...
	private static final int PARAM_NAME_TF_SIZE = 15;
	private static final int PARAM_VALUE_TF_SIZE = 15;
	private static final int PARAM_NS_TF_SIZE = 20;
	
	// default number of parameter-rows to display...
	private static final int INITIAL_PARAMETERS = 3;
	
	// frame width and height values...
	private static final int FRAME_WIDTH = 650;
	private static final int FRAME_HEIGHT = 330;
	
	// get the i18n factory singleton instance...
	private static final LabelStringFactory stringFactory = 
		LabelStringFactory.getInstance();

	// instance members...
	private JButton cancelBtn, okayBtn, addParamBtn, removeCheckedBtn;
	private XSLRow xslRow;
	private JPanel parametersPanel;
	private GridBagLayout parametersPanelLayout;
	private GridBagConstraints parametersPanelConstraints;
	private List<ParameterTextFieldGroup> parameterTextFieldGroups;
	private TransformParameters parameters;

	/**
	 * Constructor
	 * @param aParent
	 * @param aXSLRow
	 * @throws IOException
	 */
	public TransformParametersFrame(Frame aParent, XSLRow aXSLRow) {
		
		JPanel southPanel;
		
		xslRow = aXSLRow;
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				dispose();
			}
		});	
		parameterTextFieldGroups = new ArrayList<ParameterTextFieldGroup>();
		parameters = xslRow.getTransformParameters();
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
		getContentPane().add(buildMainPanel(), BorderLayout.CENTER);		
		setTitle(stringFactory.getString(
				LabelStringFactory.PARAMS_FRAME_TRANSFORM_PARAMETERS));		
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		GUIUtils.center(this, aParent);
		initializeGUI();
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
				LabelStringFactory.PARAMS_FRAME_TRANSFORM_PARAMETERS) + " (" + 
				xslRow.getDescription() + ")");
		headerLabel.setFont(new Font("arial", Font.PLAIN, 18));
		JLabel fileLabel = new JLabel(stringFactory.getString(
				LabelStringFactory.PARAMS_FRAME_FILE_LBL_WITH_COLON) + 
				xslRow.getTextField().getText());
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
	 *
	 */
	private void initializeGUI() {
		removeCheckedBtn.setEnabled(false);
	}
	
	/**
	 * Builds the main panel
	 * @return
	 */
	private JPanel buildMainPanel() {
		
		int loop, size;
		JPanel main;
		
		main = new JPanel(new BorderLayout());
		parametersPanelLayout = new GridBagLayout();
		parametersPanelConstraints = new GridBagConstraints();
		parametersPanel = new JPanel(parametersPanelLayout);
		size = parameters.getSize();
		if (size == 0) {
			size = INITIAL_PARAMETERS;
		}
		for (loop = 0; loop < size; loop++) {
			parameterTextFieldGroups.add(new ParameterTextFieldGroup(
					parametersPanel, 
					new JTextField(PARAM_NAME_TF_SIZE),
					new JTextField(PARAM_VALUE_TF_SIZE), 
					new JTextField(PARAM_NS_TF_SIZE),
					new JCheckBox()));
		}
		rebuildParametersPanel();
		main.add(new JScrollPane(parametersPanel,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		main.add(buildAddRemovePanel(), BorderLayout.SOUTH);
		return main;
	}
	
	/**
	 * Builds the add/remove panel
	 * @return
	 */
	private JPanel buildAddRemovePanel() {
		
		int row;
		GridBagLayout layout;
		GridBagConstraints constraints;
		JPanel buttons;
		
		row = 0;
		layout = new GridBagLayout();
		constraints = new GridBagConstraints();
		buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttons.add(addParamBtn = new JButton(stringFactory.getString(
				LabelStringFactory.PARAMS_FRAME_ADD_PARAMETER)));
		buttons.add(removeCheckedBtn = new JButton(stringFactory.getString(
				LabelStringFactory.PARAMS_FRAME_REMOVE_CHECKED)));
		addParamBtn.addActionListener(this);
		removeCheckedBtn.addActionListener(this);
		JPanel addRemovePanel = new JPanel(layout);
		GUIUtils.add(addRemovePanel, buttons, layout, 
			constraints, row++, 0, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.NONE, GUIUtils.SMALL_INSETS);
		GUIUtils.add(addRemovePanel, new JSeparator(), layout, 
			constraints, row++, 0, 1, 1, 1, 1, GridBagConstraints.WEST, 
			GridBagConstraints.HORIZONTAL, GUIUtils.SMALL_INSETS);
		return addRemovePanel;
	}
	
	/**
	 * Rebuilds the parameters panel
	 *
	 */
	private void rebuildParametersPanel() {
		
		int loop, row, size;
		JTextField paramNameTf, paramValueTf, paramNamespaceURITf;
		JCheckBox removeCb;
        String paramNames[];
        ParameterTextFieldGroup paramTfGroup;
        
        row = 0;
		parametersPanel.removeAll();                
		GUIUtils.add(parametersPanel, new JLabel(stringFactory.getString(
				LabelStringFactory.PARAMS_FRAME_NAME_LBL)), 
				parametersPanelLayout, parametersPanelConstraints, row, 0, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.BOTH, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(parametersPanel, new JLabel(stringFactory.getString(
				LabelStringFactory.PARAMS_FRAME_VALUE_LBL)), 
				parametersPanelLayout, parametersPanelConstraints, row, 1, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.BOTH, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(parametersPanel, new JLabel(stringFactory.getString(
				LabelStringFactory.PARAMS_FRAME_NAMESPACE_URI_LBL)), 
				parametersPanelLayout, parametersPanelConstraints, row, 2, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.BOTH, 
				GUIUtils.SMALL_INSETS);
		GUIUtils.add(parametersPanel, new JLabel(stringFactory.getString(
				LabelStringFactory.PARAMS_FRAME_REMOVE_LBL)), 
				parametersPanelLayout, parametersPanelConstraints, row++, 3, 1, 
				1, GridBagConstraints.WEST, GridBagConstraints.BOTH, 
				GUIUtils.SMALL_INSETS);
		paramNames = parameters.getParameterNames();
		size = parameterTextFieldGroups.size();
		for (loop = 0; loop < size; loop++) {
			paramTfGroup = (ParameterTextFieldGroup)
			parameterTextFieldGroups.get(loop);
			GUIUtils.add(parametersPanel, paramNameTf = 
				paramTfGroup.getParamNameTf(), parametersPanelLayout, 
				parametersPanelConstraints, row, 0, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
				GUIUtils.SMALL_INSETS);
			GUIUtils.add(parametersPanel, paramValueTf = 
				paramTfGroup.getParamValueTf(), parametersPanelLayout, 
				parametersPanelConstraints, row, 1, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
				GUIUtils.SMALL_INSETS);
			GUIUtils.add(parametersPanel, paramNamespaceURITf = 
				paramTfGroup.getParamNamespaceURITf(), parametersPanelLayout, 
				parametersPanelConstraints, row, 2, 1, 1, 
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 
				GUIUtils.SMALL_INSETS);
			GUIUtils.add(parametersPanel, removeCb = paramTfGroup.getRemoveCb(), 
					parametersPanelLayout, parametersPanelConstraints, row++, 3, 
					1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 
					GUIUtils.SMALL_INSETS);
			removeCb.addActionListener(this);
			if (loop < paramNames.length) {
				paramNameTf.setText(TransformParameters.getParameterName(
						paramNames[loop]));
				paramValueTf.setText(parameters.getParameter(
						paramNames[loop]).toString());
				paramNamespaceURITf.setText(TransformParameters.getNamespaceURI(
						paramNames[loop]));
			}		
		}
	}
	
	/**
	 * Writes the parameter values to the "parameters" object.
	 *
	 */
	private void saveParameters() {
		
		int loop, size;
		ParameterTextFieldGroup paramTextFieldGroup;
		
		parameters.clear();
		size = parameterTextFieldGroups.size();
		for (loop = 0; loop < size; loop++) {
			paramTextFieldGroup = (ParameterTextFieldGroup)
			parameterTextFieldGroups.get(loop);
			
			// ignore row if all 3 textfields are blank...
			if (!(StringUtils.isBlank(
					paramTextFieldGroup.getParamNamespaceURITf().getText()) &&
				StringUtils.isBlank(
						paramTextFieldGroup.getParamNameTf().getText()) &&
				StringUtils.isBlank(
						paramTextFieldGroup.getParamValueTf().getText()))) {
				parameters.setParameter(
					paramTextFieldGroup.getParamNamespaceURITf().getText(),
					paramTextFieldGroup.getParamNameTf().getText(),
					paramTextFieldGroup.getParamValueTf().getText());
			}
		}
	}
	
	/**
	 * Refreshes the parameters panel
	 *
	 */
	private void refreshParametersPanel() {
		rebuildParametersPanel();
		parametersPanel.repaint();
		parametersPanel.revalidate();
	}
	
	/**
	 * Enables or disables the remove-checked button
	 *
	 */
	private void setEnabledRemoveCheckedBtn() {
		
		int loop, size;
		ParameterTextFieldGroup paramTextFieldGroup;
		boolean enable;
		
		size = parameterTextFieldGroups.size();
		enable = false;
		for (loop = 0; loop < size; loop++) {
			paramTextFieldGroup = (ParameterTextFieldGroup)
			parameterTextFieldGroups.get(loop);
			if (paramTextFieldGroup.getRemoveCb().isSelected()) {
				enable = true;
				break;
			}
		}
		removeCheckedBtn.setEnabled(enable);
	}
	
	/**
	 * Validates the entered-parameter values
	 * @return
	 */
	private boolean validateParams() {
		
		boolean areAllValid;
		int loop, size;
		ParameterTextFieldGroup paramTextFieldGroup;
		
		areAllValid = true;
		size = parameterTextFieldGroups.size();
		for (loop = 0; loop < size; loop++) {
			paramTextFieldGroup = (ParameterTextFieldGroup)
			parameterTextFieldGroups.get(loop);
			if (StringUtils.isNotBlank(
					paramTextFieldGroup.getParamValueTf().getText()) &&
				StringUtils.isBlank(
						paramTextFieldGroup.getParamNameTf().getText())) {
				areAllValid = false;
				Utils.showDialog(this, 
						stringFactory.getString(LabelStringFactory.
								PARAMS_FRAME_CANNOT_HAVE_EMPTY_PARAM_VALUE),
						stringFactory.getString(LabelStringFactory.
								PARAMS_FRAME_INVALID_PARAMETER), 
						JOptionPane.ERROR_MESSAGE);
				break;
			} else if (StringUtils.isNotBlank(
					paramTextFieldGroup.getParamNamespaceURITf().getText()) &&
					StringUtils.isBlank(
							paramTextFieldGroup.getParamNameTf().getText())) {
				areAllValid = false;
				Utils.showDialog(this, 
						stringFactory.getString(LabelStringFactory.
								PARAMS_FRAME_CANNOT_HAVE_EMPTY_PARAM_VALUE),
						stringFactory.getString(LabelStringFactory.
								PARAMS_FRAME_INVALID_PARAMETER), 
						JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		return areAllValid;
	}
	
	/**
	 * Event handler
	 */
	public void actionPerformed(ActionEvent aEvent) {
		
		Object eventSource;
		
		eventSource = aEvent.getSource();
		if (eventSource == cancelBtn) {
			dispose();
		} else if (eventSource == okayBtn) {
			if (validateParams()) {
				saveParameters();
				xslRow.refreshIndicatorLabel();
				dispose();
			}
		} else if (eventSource == addParamBtn) {
			parameterTextFieldGroups.add(new ParameterTextFieldGroup(
					parametersPanel, new JTextField(PARAM_NAME_TF_SIZE),
					new JTextField(PARAM_VALUE_TF_SIZE), 
					new JTextField(PARAM_NS_TF_SIZE), new JCheckBox()));
			refreshParametersPanel();
		} else if (eventSource instanceof JCheckBox) {
			setEnabledRemoveCheckedBtn();
		} else if (eventSource == removeCheckedBtn) {
			ParameterTextFieldGroup.removeChecked(parameterTextFieldGroups);
			// always make sure there's at least one parameter-entry row...
			if (parameterTextFieldGroups.size() == 0) {
				parameterTextFieldGroups.add(new ParameterTextFieldGroup(
						parametersPanel, new JTextField(PARAM_NAME_TF_SIZE),
						new JTextField(PARAM_VALUE_TF_SIZE), 
						new JTextField(PARAM_NS_TF_SIZE), new JCheckBox()));
				refreshParametersPanel();
			}
			removeCheckedBtn.setEnabled(false);
		}
	}
}

