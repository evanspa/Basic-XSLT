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

import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Helper class used by the TransformParametersFrame - models each parameter
 * row in the parameters-frame and provides a 'remove-row' method.
 * @author pevans
 *
 */
public class ParameterTextFieldGroup {
	
	// instance members...
	private JPanel parent;
	private JTextField paramName, paramValue, paramNamespaceURI;
	private JCheckBox removeCb;

	/**
	 * Constructor
	 * @param aParent
	 * @param aParamName
	 * @param aParamValue
	 * @param aParamNamespaceURI
	 * @param aRemoveCb
	 */
	public ParameterTextFieldGroup(JPanel aParent, JTextField aParamName,
		JTextField aParamValue, JTextField aParamNamespaceURI,
		JCheckBox aRemoveCb) {
		parent = aParent;
		paramName = aParamName;
		paramValue = aParamValue;
		removeCb = aRemoveCb;
		paramNamespaceURI = aParamNamespaceURI;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public JCheckBox getRemoveCb() {
		return removeCb;
	}

	/**
	 * Getter
	 * @return
	 */
	public JTextField getParamNameTf() {
		return paramName;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public JTextField getParamValueTf() {
		return paramValue;
	}
	
	/**
	 * Getter
	 * @return
	 */
	public JTextField getParamNamespaceURITf() {
		return paramNamespaceURI;
	}
	
	/**
	 * Loops over aRows - for each row the components are removed from their
	 * parent panel.
	 * @param aRows
	 * @return
	 */
	public static int removeChecked(List aRows) {
		
		int loop, size, numRemoved;
		ParameterTextFieldGroup parameterTfGrp;
		JPanel parent;
		
		numRemoved = 0;
		size = aRows.size();
		parent = null;
		for (loop = size -1; loop >= 0; loop--) {
			parameterTfGrp = (ParameterTextFieldGroup)aRows.get(loop);
			parent = parameterTfGrp.parent;
			if (parameterTfGrp.removeCb.isSelected()) {
				parameterTfGrp.parent.remove(parameterTfGrp.paramName);
				parameterTfGrp.parent.remove(parameterTfGrp.paramValue);
				parameterTfGrp.parent.remove(parameterTfGrp.paramNamespaceURI);
				parameterTfGrp.parent.remove(parameterTfGrp.removeCb);
				aRows.remove(loop);				
				numRemoved++;
			}
		}
		if (numRemoved > 0) {
			parent.repaint();
			parent.revalidate();
		}
		return numRemoved;
	}
}
