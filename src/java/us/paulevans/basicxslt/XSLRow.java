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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.blueslate.commons.xml.TransformOutputProperties;
import net.blueslate.commons.xml.TransformParameters;

import org.apache.commons.lang.StringUtils;

/**
 * Models a row in the XSL-array.  This is a helper class that makes it easier
 * to manage the adding, removing and inserting of XSL stylesheet rows.
 * @author Paul Evans
 */
public class XSLRow {
	
	// get the i18n factory singleton instance...
	private static final LabelStringFactory stringFactory = 
		LabelStringFactory.getInstance();
	
	// static constants...
	public static final int VALIDATE_INDEX = 1;
	public static final int ON_OFF_INDEX = 2;
	public static final int VIEW_EDIT_OUTPUT_PROPS_INDEX = 3;
	public static final int CLEAR_OUTPUT_PROPS_INDEX = 4;
	public static final int VIEW_EDIT_PARAMETERS_INDEX = 5;
	public static final int CLEAR_ALL_PARAMETERS_INDEX = 6;
	public static final int PERFORM_IDENTITY_TRANSFORM_INDEX = 7;
	public static final String ON_OFF_ITEM_PREFIX = stringFactory.getString(
			LabelStringFactory.XML_ACTION_TURNONOFF_PREFIX);
	
	// The set of actions that can be taken on this stylesheet...
	public static final String ACTIONS[] = {
		AppConstants.SEPARATOR,
		stringFactory.getString(LabelStringFactory.XML_ACTION_VALIDATE),
		ON_OFF_ITEM_PREFIX, 
		stringFactory.getString(
				LabelStringFactory.XML_ACTION_OUTPUT_PROPERTIES),
		stringFactory.getString(
				LabelStringFactory.XML_ACTION_CLEAR_OUTPUT_PROPERTIES),
		stringFactory.getString(LabelStringFactory.XML_ACTION_PARAMETERS),
		stringFactory.getString(LabelStringFactory.XML_ACTION_CLEAR_PARAMETERS),
		stringFactory.getString(LabelStringFactory.XML_ACTION_PERFORM_IT)
	};
	
	// instance members...
	private JPanel parent;
	private JButton browseBtn, insertBtn;
	private JTextField textField;
	private JCheckBox removeCb;
	private JLabel label, indicatorLabel;
	private boolean onOffButtonValue;
	private long timeToTransform;
	private TransformOutputProperties transformOutputProperties;
	private TransformParameters transformParameters;
	private JComboBox action;
	private int index;
	private boolean areOutputPropertiesSet;
	
	/**
	 * Constructor
	 * @param aActionListener
	 * @param aParent
	 * @param aInsertBtn
	 * @param aLabel
	 * @param aTextField
	 * @param aRemoveCb
	 * @param aBrowse
	 * @param aAction
	 * @param aOutputPropertiesIndicator
	 * @param aIndex
	 */
	public XSLRow(ActionListener aActionListener, JPanel aParent, 
			JButton aInsertBtn, JLabel aLabel, JTextField aTextField, 
			JCheckBox aRemoveCb, JButton aBrowse, JComboBox aAction, 
			JLabel aOutputPropertiesIndicator, int aIndex) {
		parent = aParent;
		insertBtn = aInsertBtn;
		label = aLabel;
		browseBtn = aBrowse;
		textField = aTextField;
		removeCb = aRemoveCb;
		action = aAction;
		indicatorLabel = aOutputPropertiesIndicator;
		index = aIndex;
		onOffButtonValue = true; // init to true...
		refreshActionItems();
		indicatorLabel.setFont(new Font("arial", Font.PLAIN, 10));
		insertBtn.addActionListener(aActionListener);
		indicatorLabel.setToolTipText(stringFactory.getString(
				LabelStringFactory.XSLROW_OUTPUT_PROPERTIES_SPECIFIED));
		removeCb.addActionListener(aActionListener);
		browseBtn.addActionListener(aActionListener);
		action.addActionListener(aActionListener);
		action.setActionCommand(AppConstants.TAKE_ACTION);
		removeCb.setActionCommand(AppConstants.REMOVE_CB);
		textField.setBackground(Color.GREEN);
		transformOutputProperties = new TransformOutputProperties();
		transformParameters = new TransformParameters();
		setToolTips();
	}
	
	/**
	 * Sets the various tool tips on the GUI components found on this row.
	 *
	 */
	private void setToolTips() {
		insertBtn.setToolTipText(stringFactory.getString(
				LabelStringFactory.XSLROW_TOOL_TIP_INSERT_STYLESHEET));
		textField.setToolTipText(stringFactory.getString(
				LabelStringFactory.XSLROW_TOOL_TIP_PICK_STYLESHEET));
		removeCb.setToolTipText(stringFactory.getString(
				LabelStringFactory.XSLROW_TOOL_TIP_REMOVE_CHECKBOX));
		browseBtn.setToolTipText(stringFactory.getString(
				LabelStringFactory.XSLROW_TOOL_TIP_BROWSE_BTN));
		action.setToolTipText(stringFactory.getString(
				LabelStringFactory.XSLROW_TOOL_TIP_TAKE_ACTION));
	}
	
	/**
	 * Setter - sets the index of the XSL row
	 * @param aIndex
	 */
	public void setIndex(int aIndex) {
		index = aIndex;
		label.setText(MessageFormat.format(stringFactory.getString(
				LabelStringFactory.XSLROW_XSL_LABEL), index+1));
	}
	
	/**
	 * Getter
	 * @return
	 */
	public JButton getInsertBtn() {
		return insertBtn;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public String getDescription() {
		return StringUtils.replace(label.getText(), ":", "").trim();
	}
	
	/**
	 * Clears the output properties set on this stylesheet
	 *
	 */
	public void clearOutputProperties() {
		transformOutputProperties.clear();
	}
	
	/**
	 * Clears the parameters set on this stylesheet
	 *
	 */
	public void clearAllParameters() {
		transformParameters.clear();
		refreshIndicatorLabel();
	}
	
	/**
	 * Getter
	 * @return
	 */
	public JLabel getIndicatorLabel() {
		return indicatorLabel;
	}
	
	/**
	 * Sets if output properties are set or not on this stylesheet
	 * @param aAreOutputPropertiesSet
	 */
	public void setAreOutputPropertiesSet(boolean aAreOutputPropertiesSet) {
		areOutputPropertiesSet = aAreOutputPropertiesSet;
		refreshIndicatorLabel();
	}
	
	/**
	 * Refreshes the indicator label
	 *
	 */
	public void refreshIndicatorLabel() {
		
		StringBuffer labelText;
		StringBuffer toolTip;
		
		labelText = new StringBuffer("");
		toolTip = new StringBuffer();
		if (areOutputPropertiesSet) {
			labelText.append("OP");
			toolTip.append("Output properties");
		}
		if (transformParameters.getSize() > 0) {
			if (areOutputPropertiesSet) {
				labelText.append(",");
				toolTip.append(" and ");
			} 
			labelText.append("P");
			toolTip.append("Parameters");
		}
		if (areOutputPropertiesSet || (transformParameters.getSize() > 0)) {
			toolTip.append(" have been specified for this Stylesheet");
		}
		indicatorLabel.setText(labelText.toString());
		indicatorLabel.setToolTipText(toolTip.toString());
	}
	
	/**
	 * Getter
	 * @return
	 */
	public boolean areOutputPropertiesSet() {
		return areOutputPropertiesSet;
	}
	
	/**
	 * Refreshes the action-items combo-box
	 *
	 */
	public void refreshActionItems() {
		
		int loop;
		String item;
		
		action.removeAllItems();
		action.addItem(stringFactory.getString(
				LabelStringFactory.XML_ACTION_TAKE_ACTION));
		for (loop = 0; loop < ACTIONS.length; loop++) {
			item = ACTIONS[loop];
			if (loop == ON_OFF_INDEX) {
				item += onOffButtonValue ? stringFactory.getString(
						LabelStringFactory.XML_ACTION_TURNONOFF_OFF) : 
							stringFactory.getString(
									LabelStringFactory.XML_ACTION_TURNONOFF_ON);
			}
			action.addItem(item);
		}
		action.setSelectedIndex(0);
	}
	
	/**
	 * Setter
	 * @param aIndex
	 */
	public void setSelectedActionIndex(int aIndex) {
		action.setSelectedIndex(aIndex);
	}
	
	/**
	 * Getter
	 * @return
	 */
	public JComboBox getAction() {
		return action;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public TransformOutputProperties getTransformOutputProperties() {
		return transformOutputProperties;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public TransformParameters getTransformParameters() {
		return transformParameters;
	}
	
	/**
	 * Setter
	 * @param aTimeToTransform
	 */
	public void setTimeToTransform(long aTimeToTransform) {
		timeToTransform = aTimeToTransform;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public long getTimeToTransform() {
		return timeToTransform;
	}
	
	/**
	 * Returns true if this xsl row is toggled-on and the contents of the
	 * text-field is not blank
	 * @return
	 */
	public boolean isOnAndNotEmpty() {
		return onOffButtonValue && StringUtils.isNotBlank(textField.getText());
	}
	
	/**
	 * Returns if this row is toggled-on or not.
	 * @return
	 */
	public boolean isOn() {
		return onOffButtonValue; 
	}
	
	/**
	 * Overridden toStrings
	 */
	public String toString() {
		
		StringBuffer str;
		
		str = new StringBuffer();
		str.append("label text: [" + label.getText() + "]");
		return str.toString();
	}
	
	/**
	 * Removes all of the rows
	 * @param aRows
	 */
	public static void removeAll(List aRows) {
		
		int loop, size;
		XSLRow xslRow;
		
		size = aRows.size();
		for (loop = size -1; loop >= 0; loop--) {
			xslRow = (XSLRow)aRows.get(loop);
			xslRow.parent.remove(xslRow.insertBtn);
			xslRow.parent.remove(xslRow.label);
			xslRow.parent.remove(xslRow.textField);
			xslRow.parent.remove(xslRow.browseBtn);
			xslRow.parent.remove(xslRow.removeCb);
			xslRow.parent.remove(xslRow.action);
			xslRow.parent.remove(xslRow.indicatorLabel);
			aRows.remove(loop);					
		}
	}
	
	/**
	 * Removes all the checked rows from aRows
	 * @param aRows
	 * @return
	 */
	public static int removeChecked(List aRows) {
		
		int loop, size, numRemoved;
		XSLRow xslRow;
		JPanel parent;
		
		numRemoved = 0;
		size = aRows.size();
		parent = null;
		for (loop = size -1; loop >= 0; loop--) {
			xslRow = (XSLRow)aRows.get(loop);
			parent = xslRow.parent;
			if (xslRow.removeCb.isSelected()) {
				xslRow.parent.remove(xslRow.insertBtn);
				xslRow.parent.remove(xslRow.label);
				xslRow.parent.remove(xslRow.textField);
				xslRow.parent.remove(xslRow.browseBtn);
				xslRow.parent.remove(xslRow.removeCb);
				xslRow.parent.remove(xslRow.action);
				xslRow.parent.remove(xslRow.indicatorLabel);
				aRows.remove(loop);				
				numRemoved++;
			}
		}
		if (numRemoved > 0) {
			parent.repaint();
			parent.revalidate();
		}
		size = aRows.size();
		for (loop = 0; loop < size; loop++) {
			((XSLRow)aRows.get(loop)).setIndex(loop);
		}
		return numRemoved;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public JTextField getTextField() {
		return textField;
	}
	
	/**
	 * Toggles this row
	 *
	 */
	public void toggleOnOffBtn() {
		onOffButtonValue = !onOffButtonValue;
		setOn(onOffButtonValue);
	}
	
	/**
	 * Turns this row 'on'
	 * @param aOn
	 */
	public void setOn(boolean aOn) {
		onOffButtonValue = aOn;
		textField.setBackground(aOn ? Color.GREEN : Color.lightGray);
		insertBtn.setEnabled(aOn);
		label.setEnabled(aOn);
		browseBtn.setEnabled(aOn);
		removeCb.setEnabled(aOn);
		refreshActionItems();
		textField.setEnabled(aOn);
	}
	
	/**
	 * Returns the row from aRows that holds the reference to aAction
	 * @param aRows
	 * @param aAction
	 * @return
	 */
	public static XSLRow getRowByAction(List aRows, JComboBox aAction) {
		
		XSLRow row, tmpRow;
		int loop, size;
		
		row = null;
		size = aRows.size();
		for (loop = 0; loop < size; loop++) {
			if ((tmpRow = (XSLRow)aRows.get(loop)).action == aAction) {
				row = tmpRow;
				break;
			}
		}
		return row;
	}	
	
	/**
	 * Returns the row from aRows that holds the reference to aInsertBtn
	 * @param aRows
	 * @param aInsertBtn
	 * @return
	 */
	public static XSLRow getRowByInsertBtn(List aRows, 
			JButton aInsertBtn) {
		
		XSLRow row, tmpRow;
		int loop, size;
		
		row = null;
		size = aRows.size();
		for (loop = 0; loop < size; loop++) {
			if ((tmpRow = (XSLRow)aRows.get(loop)).insertBtn == aInsertBtn) {
				row = tmpRow;
				break;
			}
		}
		return row;
	}
	
	/**
	 * Returns the row from aRows that holds the reference to aBrowseBtn
	 * @param aRows
	 * @param aBrowseBtn
	 * @return
	 */
	public static XSLRow getRowByBrowseBtn(List aRows, JButton aBrowseBtn) {
		
		XSLRow row, tmpRow;
		int loop, size;
		
		row = null;
		size = aRows.size();
		for (loop = 0; loop < size; loop++) {
			if ((tmpRow = (XSLRow)aRows.get(loop)).browseBtn == aBrowseBtn) {
				row = tmpRow;
				break;
			}
		}
		return row;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public JButton getBrowseBtn() {
		return browseBtn;
	}

	/**
	 * Getter
	 * @return
	 */
	public JLabel getLabel() {
		return label;
	}

	/**
	 * Getter
	 * @return
	 */
	public JCheckBox getRemoveCb() {
		return removeCb;
	}

	/**
	 * Setter
	 * @param label
	 */
	public void setLabel(JLabel label) {
		this.label = label;
	}
}
