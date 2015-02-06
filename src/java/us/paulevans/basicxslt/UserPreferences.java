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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 * Mechanism to store and retreive the user preferences for the tool
 * @author pevans
 *
 */
public class UserPreferences extends Properties {
	
    // logger object...
    private static final Logger logger = Logger.getLogger(
    		UserPreferences.class);

	// instance members...
	private String currentConfiguration;
	
	/**
	 * Returns the value for property aName; if the value is empty/null, then
	 * aDefaultValue is returned.
	 * @param aName
	 * @param aDefaultValue
	 */
	public String getProperty(String aName, String aDefaultValue) {
		
		String value;
		
		value = getProperty(aName);
		if (StringUtils.isBlank(value)) {
			value = aDefaultValue;
		}
		return value;
	}
	
	/**
	 * Returns the value for property aName
	 * @param aName
	 */
	public String getProperty(String aName) {
		return super.getProperty(currentConfiguration + "." + aName);
	}
	
	/**
	 * Sets the property
	 * @param aName
	 * @param aValue
	 */
	public Object setProperty(String aName, String aValue) {
		if (aName == null) {
			throw new NullPointerException();
		}
		return super.setProperty(currentConfiguration + "." + aName, aValue);
	}
	
	/**
	 * Returns the value for property aName; the "currentConfiguration" prefix
	 * is not applied to the property-name.
	 * @param aName
	 * @return
	 */
	public String getPropertyNoPrefix(String aName) {
		return super.getProperty(aName);
	}
	
	/**
	 * Sets the property - this method will not prefix aName with the current
	 * configuration name
	 * @param aName
	 * @param aValue
	 * @return
	 */
	public Object setPropertyNoPrefix(String aName, String aValue) {
		return super.setProperty(aName, aValue);
	}
	
	/**
	 * Sets the configuration prefix string used in subsequent
	 * property-retrieval attempts.
	 * @param aConfiguration
	 * @param aMakeAsDefault
	 */
	public void setConfiguration(String aConfiguration, 
			boolean aMakeAsDefault) {
		currentConfiguration = aConfiguration;
		if (aMakeAsDefault) {
			setPropertyNoPrefix(AppConstants.DEFAULT_CONFIGURATION_PROP, 
					currentConfiguration);
		}		
	}
	
	/**
	 * Returns the configuration prefix string
	 * @return
	 */
	public String getConfiguration() {
		return currentConfiguration;
	}
	
	/**
	 * Initializes this object setting the "current configuration" prefix
	 * string and returns it.
	 * @return
	 */
	public String loadDefaultConfiguration() {
		currentConfiguration = getPropertyNoPrefix(
				AppConstants.DEFAULT_CONFIGURATION_PROP);
		if (StringUtils.isBlank(currentConfiguration)) {
			currentConfiguration = AppConstants.DEFAULT_CONFIGURATION;
		}
		return currentConfiguration;
	}

	/**
	 * Copies the current preferences using the configuration prefix string
	 * aNewConfig
	 * @param aNewConfig
	 */
	public void copyCurrentPreferences(String aNewConfig) {
		
		String fullPropName, propNameSuffix;
		Enumeration propertyNames;
		
		propertyNames = propertyNames();
		while (propertyNames.hasMoreElements()) {
			fullPropName = (String)propertyNames.nextElement();	
			if (fullPropName.startsWith(currentConfiguration + ".")) {
				propNameSuffix = fullPropName.substring(
						fullPropName.indexOf(".")+1);
				setPropertyNoPrefix(aNewConfig + "." + propNameSuffix, 
					getPropertyNoPrefix(fullPropName));	
			}
		}
	}

	/**
	 * Clears all those properties in the current configuration that start
	 * with "xsl_"
	 *
	 */
	public void clearDynamicPrefs() {
		
		Enumeration propertyNames;
		String propertyName;
		
		propertyNames = propertyNames();
		while(propertyNames.hasMoreElements()) {
			propertyName = (String)propertyNames.nextElement();
			if (propertyName.startsWith(currentConfiguration + ".xsl_") ||
			propertyName.startsWith(
					currentConfiguration + ".xml_identity_transform")) {
				remove(propertyName);		
			}
		}
	}

	/**
	 * Persists userPrefs to its properties file on disk.
	 */
	public void persistUserPrefs() throws IOException {
		
		OutputStream out;
		
		out = null;
		try {
	    	store(out = Utils.getUserPrefsOutputStream(), 
	    		LabelStringFactory.getInstance().getString(
	    				LabelStringFactory.TOOL_USERPREFERENCES_TITLE));
	    	out.flush();
		} catch (IOException aException) {
			// log and re-throw...
			logger.error(ExceptionUtils.getFullStackTrace(aException));
			throw aException;
		} finally {
			Utils.closeQuietly(out);
		}
	}
	
	/**
	 * Returns all of the configurations that exist for the user
	 * @return
	 */
	public String[] getAllConfigurations() {
		
		Map<String,String> configurations;
		Enumeration propertyNames;
		String propertyName, configuration;
		String configs[];
		int index;
		
		configurations = new HashMap<String,String>();
		propertyNames = propertyNames();
		while(propertyNames.hasMoreElements()) {
			propertyName = (String)propertyNames.nextElement();
			if ((!propertyName.startsWith(currentConfiguration)) &&
				(index = propertyName.indexOf(".")) != -1) {
				configuration = propertyName.substring(0, index);
				configurations.put(configuration, configuration); 
			}							
		}
		configs = (String[])configurations.values().toArray(
				new String[configurations.size()]);
		Arrays.sort(configs);
		return configs;		
	}
}
