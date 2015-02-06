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

/**
 * Collection of application constants
 * @author pevans
 *
 */
public class AppConstants {

    /**
     * Private constructor to prevent instantiation
     */
    private AppConstants() {
    	// does nothing...
    }

    // Application version number...
    public static final String APP_VERSION = "01.02.08";
    
    // Application bug URL...
    public static final String BUG_HOME_URL = 
    	"http://sourceforge.net/tracker/?group_id=136476&atid=735979";
    
    // Default number of stylesheet rows to use
    public static final String DEFAULT_NUM_STYLESHEETS = "3";

    // user-prefs property names
	public static final String SEPARATOR = "---------------------";
    public static final String CHK_WARNINGS_PROP = "chk_warnings";
    public static final String CHK_ERRORS_PROP = "chk_errors";
    public static final String CHK_FATAL_ERRORS_PROP = "chk_fatal_errors";
    public static final String X_COORD_PROP = "x_coord";
    public static final String Y_COORD_PROP = "y_coord";
    public static final String LAST_FILE_CHOSEN_PROP = "last_file_chosen";
    public static final String LAST_XML_FILE_PROP = "last_xml_file";
    public static final String SUPPRESS_OUTPUT_WINDOW_PROP = 
		"suppress_output_window";
	public static final String OUTPUT_AS_TEXT_IF_XML_PROP = 
		"output_as_text_if_xml";
    public static final String AUTOSAVE_RESULT_PROP = "autosave_result";
    public static final String AUTOSAVE_FILE_PROP = "autosave_file";
    public static final String NUM_STYLESHEETS_PROP = "num_stylesheets";
    public static final String FRAME_WIDTH_PROP = "frame_width";
    public static final String FRAME_HEIGHT_PROP = "frame_height";
	public static final String CDATA_SECTION_ELEMENTS = 
		"cdata_section_elements";
	public static final String DOCTYPE_PUBLIC = "doctype_public";
	public static final String DOCTYPE_SYSTEM = "doctype_system";
	public static final String ENCODING = "encoding";
	public static final String INDENT = "indent";
	public static final String MEDIA_TYPE = "media_type";
	public static final String METHOD = "method";
	public static final String OMIT_XML_DECLARATION = "omit_xml_declaration";
	public static final String STANDALONE = "standalone";
	public static final String VERSION = "version";
	
	// default configuration property name...
	public static final String DEFAULT_CONFIGURATION_PROP = 
		"default_configuration";
    
    // constants used relating to user preferences...
    public static final String APP_PREFS_DIR = ".basicxslt";
    public static final String APP_PREFS_FILE = "basicxslt.properties";
    public static final String DEFAULT_DIR_PROP = "default_dir";
    public static final String DEFAULT_CONFIGURATION = "default";
    
    // gui labels...
	public static final String INSERT = "->";
    
    // action commands...
    public static final String REMOVE_CB = "remove_cb";
    public static final String TAKE_ACTION = "take_action";
    
    // default length of textifields used in the system...
    public static final int TF_LENGTH = 40;
}