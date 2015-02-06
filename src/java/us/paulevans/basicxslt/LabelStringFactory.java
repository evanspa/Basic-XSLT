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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Contains method for retreiving localized strings for the purpose of i18n 
 * support.
 * @author pevans
 */
public class LabelStringFactory {
	
	// singleton instance...
	private static LabelStringFactory instance;
	
	// mnemonic key post-fix...
	private static final String MNEMONIC_KEY = ".mnemonic";

	// main frame message keys...
	public static final String MAIN_FRAME_TITLE = "frame.main.title";
	public static final String MAIN_FRAME_TITLE_BAR = "frame.main.titlebar";
	public static final String MAIN_FRAME_XSL_PREFIX = "frame.main.xslprefix";
	public static final String MAIN_FRAME_AUTOSAVERESULT = 
		"frame.main.autosaveresult";
	public static final String MAIN_FRAME_SUPRESS_OUTPUT_WINDOW = 
		"frame.main.supressoutputwindow";
	public static final String MAIN_FRAME_DISPLAY_OUTPUT_AS_TEXT_IF_XML = 
		"frame.main.displayoutputastextifxml";
	public static final String MAIN_FRAME_EXIT_BTN = "frame.main.exitbutton";
	public static final String MAIN_FRAME_TRANSFORM_BTN = 
		"frame.main.transformbutton";
	public static final String MAIN_FRAME_CURRENT_CONFIGURATION = 
		"frame.main.currentconfiguration";
	public static final String MAIN_FRAME_TOTAL_TRANSFORM_TIME = 
		"frame.main.totaltransformtime";
	public static final String MAIN_FRAME_MILLISECONDS_ABBREVIATION = 
		"frame.main.millisecondsabbreviation";
	public static final String MAIN_FRAME_ADD_XSL_BTN = 
		"frame.main.addxslbutton";
	public static final String MAIN_FRAME_REMOVE_CHECKED_BTN = 
		"frame.main.removecheckedbutton";
	public static final String MAIN_FRAME_VALIDATE_BTN = 
		"frame.main.validatebutton";
	public static final String MAIN_FRAME_BROWSE_BTN = 
		"frame.main.browsebutton";
	public static final String MAIN_FRAME_XML_FILE_NOT_SPECIFIED = 
		"frame.main.xmlfilenotspecified";
	public static final String MAIN_FRAME_ERROR_LBL = 
		"frame.main.error";
	public static final String 
	MAIN_FRAME_XML_FILE_NOT_EXIST_SPECIFY_VALID_FILE = 
		"frame.main.xmlnotexistspecifyvalidfile";
	public static final String MAIN_FRAME_AUTOSAVE_PATH_DOES_NOT_EXIST = 
		"frame.main.autosavepathdoesnotexist";
	public static final String MAIN_FRAME_INVALID_AUTOSAVE_PATH = 
		"frame.main.invalidautosavepath";
	public static final String MAIN_FRAME_PLEASE_SPECIFY_AUTOSAVE_PATH = 
		"frame.main.pleasespecifyautosavepath";
	public static final String MAIN_FRAME_XML_FILE_WITH_COLON = 
		"frame.main.xmlfilewithcolon";
	public static final String MAIN_FRAME_XSL_FILE = "frame.main.xslfile";
	public static final String MAIN_FRAME_XML_INDICATOR_ITOPSPECIFIED = 
		"frame.main.xmlindicatoridentitytransformoutputpropertiesset";
	public static final String MAIN_FRAME_XML_INDICATOR_ITOPSPECIFIED_TOOL_TIP = 
		"frame.main.xmlindicatoridentitytransformoutputpropertiesset.tooltip";
	public static final String MAIN_FRAME_XML_FILE = "frame.main.xmlfile";
	public static final String MAIN_FRAME_SPECIFICY_AT_LEAST_ONE_STYLESHEET = 
		"frame.main.specifyatleastonestylesheet";
	public static final String MAIN_FRAME_TRANSFORM_MESSAGE = 
		"frame.main.transformmessage";
	public static final String MAIN_FRAME_ONLY_CONFIGURATION = 
		"frame.main.onlyconfiguration";
	public static final String MAIN_FRAME_MESSAGE = 
		"frame.main.message";
	public static final String MAIN_FRAME_SELECT_FILE_FOR_IT_RESULT = 
		"frame.main.selectfileforitresult";
	public static final String MAIN_FRAME_PICK_FILE_FOR_IT = 
		"frame.main.pickfileforit";
	public static final String MAIN_FRAME_VALID_XML_MSG = 
		"frame.main.validxmlmessage";
	public static final String MAIN_FRAME_VALID_XML_MSG_HDR_YES = 
		"frame.main.validxmlmessageheader.yes";
	public static final String MAIN_FRAME_VALID_XML_MSG_HDR_NO = 
		"frame.main.validxmlmessageheader.no";
	public static final String MAIN_FRAME_XML_VALIDATION_ERR = 
		"frame.main.xmlvalidationerror";
	public static final String MAIN_FRAME_IDENTITY_TRANSFORM = 
		"frame.main.identitytransform";
	public static final String MAIN_FRAME_TRANSFORM_RESULT_NOT_XML = 
		"frame.main.transformresultnotxml";
	public static final String MAIN_FRAME_TRANSFORM_ERR_MSG = 
		"frame.main.transformerrormessage";
	public static final String MAIN_FRAME_ERR_IN_XSL = "frame.main.errorinxsl";
	public static final String MAIN_FRAME_XSL_TRANSFORMATION_ERR = 
		"frame.main.xsltransformationerror";
	public static final String MAIN_FRAME_TRANSFORM_RESULTS = 
		"frame.main.transformresults";
	
	// main frame file menu and associated menu items message keys...
	public static final String MF_FILE_MENU = "frame.main.menus.file";
	public static final String MF_FILE_RESET_FORM_MI = 
		"frame.main.menus.file.menuitems.reset";
	public static final String MF_FILE_LOAD_CONFIGURATION_MI = 
		"frame.main.menus.file.menuitems.loadconfig";
	public static final String MF_FILE_SAVE_CONFIGURATION_MI = 
		"frame.main.menus.file.menuitems.saveconfig";
	public static final String MF_FILE_SAVE_CONFIGURATION_AS_MI = 
		"frame.main.menus.file.menuitems.saveconfigas";
	public static final String MF_FILE_EXIT_MI = 
		"frame.main.menus.file.menuitems.exit";
	
	// main frame validation menu and associated menu items message keys...
	public static final String MF_VALIDATION_MENU = 
		"frame.main.menus.validation";
	public static final String MF_VALIDATION_CHECK_SAX_WARNINGS_MI = 
		"frame.main.menus.validation.menuitems.saxwarnings";
	public static final String MF_VALIDATION_CHECK_SAX_ERRORS_MI = 
		"frame.main.menus.validation.menuitems.saxerrors";
	public static final String MF_VALIDATION_CHECK_SAX_FATAL_MI = 
		"frame.main.menus.validation.menuitems.saxfatal";
	
	//  main frame view menu and associated menu items message keys...
	public static final String MF_VIEW_MENU = "frame.main.menus.view";
	public static final String MF_VIEW_LAST_TIMINGS_MI = 
		"frame.main.menus.view.menuitems.lasttimings";
	
	// main frame help menu and associated menu items message keys...
	public static final String MF_HELP_MENU = "frame.main.menus.help";
	public static final String MF_HELP_ABOUT_MI = 
		"frame.main.menus.help.menuitems.about";
	
	// tool tip message keys...
	public static final String TOOL_TIP_TRANSFORM_TIMINGS = 
		"tooltips.transformtimings";
	public static final String TOOL_TIP_XML_ACTION = "tooltips.xmlaction";
	public static final String TOOL_TIP_TRANSFORM_BTN = 
		"tooltips.transformbutton";
	public static final String TOOL_TIP_EXIT_BTN = "tooltips.exitbutton";
	public static final String TOOL_TIP_ADD_XSL_BTN = 
		"tooltips.addxslbutton";
	public static final String TOOL_TIP_REMOVE_CHECKED_BTN = 
		"tooltips.removecheckedbutton";
	public static final String TOOL_TIP_VALIDATE_AUTOSAVE_BTN = 
		"tooltips.validateautosavebutton";
	public static final String TOOL_TIP_AUTOSAVE_TEXTFIELD = 
		"tooltips.autosavetextfield";
	public static final String TOOL_TIP_SOURCE_XML_TEXTFIELD = 
		"tooltips.sourcexmltextfield";
	public static final String TOOL_TIP_BROWSE_AUTOSAVE_PATH_BTN = 
		"tooltips.browseautosavepathbutton";
	public static final String TOOL_TIP_BROWSE_XML_BTN = 
		"tooltips.browsexmlbutton";
	public static final String TOOL_TIP_SUPPRESS_OUTPUT_WINDOW_CB = 
		"tooltips.suppressoutputwindowcheckbox";
	public static final String TOOL_TIP_AUTOSAVE_CB = 
		"tooltips.autosavecheckbox";
	public static final String TOOL_TIP_OUTPUT_AS_TEXT_IF_XML_CB = 
		"tooltips.outputastextifxmlcheckbox";
	
	// XML action message keys...
	public static final String XML_ACTION_TAKE_ACTION = "xmlactions.takeaction";
	public static final String XML_ACTION_VALIDATE = "xmlactions.validate";
	public static final String XML_ACTION_IT_OUTPUT_PROPERITES = 
		"xmlactions.identitytransformoutputproperties";
	public static final String XML_ACTION_CLEAR_IT_PROPERTIES = 
		"xmlactions.clearitoutputproperties";
	public static final String XML_ACTION_PERFORM_IT = 
		"xmlactions.performidentitytransform";
	public static final String XML_ACTION_TURNONOFF_PREFIX = 
		"xmlactions.turnonoff.prefix";
	public static final String XML_ACTION_TURNONOFF_ON = 
		"xmlactions.turnonoff.on";
	public static final String XML_ACTION_TURNONOFF_OFF = 
		"xmlactions.turnonoff.off";
	public static final String XML_ACTION_OUTPUT_PROPERTIES = 
		"xmlactions.outputproperties";
	public static final String XML_ACTION_CLEAR_OUTPUT_PROPERTIES = 
		"xmlactions.clearoutputproperties";
	public static final String XML_ACTION_PARAMETERS = 
		"xmlactions.parameters";
	public static final String XML_ACTION_CLEAR_PARAMETERS = 
		"xmlactions.clearparameters";
	
	// tool description message keys...
	public static final String TOOL_DESCRIPTION = "tool.description";
	public static final String TOOL_LICENSE = "tool.license";
	public static final String TOOL_DEVELOPED_BY = "tool.developedby";
	public static final String TOOL_ABOUTDIALOG_TITLE = 
		"tool.aboutdialog.title";
	public static final String TOOL_ABOUTDIALOG_TOOLTITLE = 
		"tool.aboutdialog.tooltitle";
	public static final String TOOL_ABOUTDIALOG_VERSION = 
		"tool.aboutdialog.version";
	public static final String TOOL_USERPREFERENCES_TITLE = 
		"tool.userpreferences.title";
	
	// XSLRow message keys...
	public static final String XSLROW_OUTPUT_PROPERTIES_SPECIFIED = 
		"xslrow.outputpropertiesspecified";
	public static final String XSLROW_TOOL_TIP_INSERT_STYLESHEET = 
		"xslrow.tooltips.insertstylesheet";
	public static final String XSLROW_TOOL_TIP_PICK_STYLESHEET = 
		"xslrow.tooltips.pickstylesheet";
	public static final String XSLROW_TOOL_TIP_REMOVE_CHECKBOX = 
		"xslrow.tooltips.removecheckbox";
	public static final String XSLROW_TOOL_TIP_BROWSE_BTN = 
		"xslrow.tooltips.browsebutton";
	public static final String XSLROW_TOOL_TIP_TAKE_ACTION = 
		"xslrow.tooltips.takeaction";
	public static final String XSLROW_XSL_LABEL = 
		"xslrow.xsllabel";
	
	// general component message keys...
	public static final String OK_BUTTON = "component.okbutton";
	public static final String CANCEL_BUTTON = "component.cancelbutton";
	public static final String CLOSE_BUTTON = "component.closebutton";
	
	// configuration load frame message keys...
	public static final String LOADCONFIG_FRAME_LOAD_CONFIGURATION = 
		"frame.loadconfig.loadconfiguration";
	public static final String LOADCONFIG_FRAME_CURRENT_CONFIGURATION = 
		"frame.loadconfig.currentconfiguration";
	public static final String LOADCONFIG_FRAME_CONFIGURATION = 
		"frame.loadconfig.configuration";
	public static final String LOADCONFIG_FRAME_MAKE_DEFAULT = 
		"frame.loadconfig.makedefault";
	
	// configuration save-as frame message keys...
	public static final String SAVEASCONFIG_FRAME_SAVE_CONFIGURATION_AS = 
		"frame.saveasconfig.saveconfigurationas";
	public static final String SAVEASCONFIG_FRAME_SAVE_NEW_CONFIGURATION = 
		"frame.saveasconfig.savenewconfiguration";
	public static final String SAVEASCONFIG_FRAME_CURRENT_CONFIGURATION = 
		"frame.saveasconfig.currentconfiguration";
	public static final String SAVEASCONFIG_FRAME_CONFIGURATION_NAME = 
		"frame.saveasconfig.configurationname";
	public static final String SAVEASCONFIG_FRAME_MAKE_DEFAULT = 
		"frame.saveasconfig.makedefault";
	public static final String SAVEASCONFIG_FRAME_CONFIG_STR_CANNOT_BE_EMPTY  = 
		"frame.saveasconfig.configstringcannotbeempty";
	public static final String SAVEASCONFIG_FRAME_ERROR = 
		"frame.saveasconfig.error";
	
	// timings frame message keys...
	public static final String TIMINGS_FRAME_XSL_TRANSFORMATION_TIMINGS = 
		"frame.timings.xsltransformationtimings";
	public static final String TIMINGS_FRAME_TRANSFORMATION_XSL = 
		"frame.timings.transformationxsl";
	public static final String TIMINGS_FRAME_TIME_TO_TRANSFORM = 
		"frame.timings.timetotransform";
	public static final String TIMINGS_FRAME_TOTAL_LBL = "frame.timings.total";
	
	// output properties frame message keys...
	public static final String OUTPUTPROPS_FRAME_METHODS_XML = 
		"frame.outputprops.methods.xml";
	public static final String OUTPUTPROPS_FRAME_METHODS_HTML = 
		"frame.outputprops.methods.html";
	public static final String OUTPUTPROPS_FRAME_METHODS_TEXT = 
		"frame.outputprops.methods.text";
	public static final String OUTPUTPROPS_FRAME_METHODS_OTHER = 
		"frame.outputprops.methods.other";
	public static final String OUTPUTPROPS_FRAME_IT_OUTPUT_PROPERTIES = 
		"frame.outputprops.identitytransformoutputproperties";
	public static final String OUTPUTPROPS_FRAME_TRANSFORM_OUTPUT_PROPERTIES = 
		"frame.outputprops.transformoutputproperties";
	public static final String OUTPUTPROPS_FRAME_FILE_LBL = 
		"frame.outputprops.filelabel";
	public static final String OUTPUTPROPS_FRAME_CDATA_SECTION_ELEMENTS = 
		"frame.outputprops.cdatasectionelements";
	public static final String OUTPUTPROPS_FRAME_DOCTYPE_PUBLIC = 
		"frame.outputprops.doctypepublic";
	public static final String OUTPUTPROPS_FRAME_DOCTYPE_SYSTEM = 
		"frame.outputprops.doctypesystem";
	public static final String OUTPUTPROPS_FRAME_ENCODING = 
		"frame.outputprops.encoding";
	public static final String OUTPUTPROPS_FRAME_METHOD =
		"frame.outputprops.method";
	public static final String OUTPUTPROPS_FRAME_MEDIA_TYPE = 
		"frame.outputprops.mediatype";
	public static final String OUTPUTPROPS_FRAME_VERSION = 
		"frame.outputprops.version";
	public static final String OUTPUTPROPS_FRAME_INDENT =
		"frame.outputprops.indent";
	public static final String OUTPUTPROPS_FRAME_OMIT_XML_DECLARATION =
		"frame.outputprops.omitxmldeclaration";
	public static final String OUTPUTPROPS_FRAME_IS_STANDALONE = 
		"frame.outputprops.isstandalone";
	
	// parameters frame message keys...
	public static final String PARAMS_FRAME_TRANSFORM_PARAMETERS = 
		"frame.params.transformparameters";
	public static final String PARAMS_FRAME_FILE_LBL_WITH_COLON = 
		"frame.params.filelabelwithcolon";
	public static final String PARAMS_FRAME_ADD_PARAMETER = 
		"frame.params.addparameter";
	public static final String PARAMS_FRAME_REMOVE_CHECKED = 
		"frame.params.removechecked";
	public static final String PARAMS_FRAME_NAME_LBL = "frame.params.namelabel";
	public static final String PARAMS_FRAME_VALUE_LBL = 
		"frame.params.valuelabel";
	public static final String PARAMS_FRAME_NAMESPACE_URI_LBL = 
		"frame.params.namespaceurilabel";
	public static final String PARAMS_FRAME_REMOVE_LBL = 
		"frame.params.removelabel";
	public static final String PARAMS_FRAME_INVALID_PARAMETER = 
		"frame.params.invalidparameter";
	public static final String PARAMS_FRAME_CANNOT_HAVE_EMPTY_PARAM_VALUE = 
		"frame.params.cannothaveemptyparametervalue";
	
	// message keys referenced in Utils.java...
	public static final String UTILS_FILE_DOES_NOT_EXIST = 
		"utils.filedoesnotexist";
	
	// output frame message keys...
	public static final String OUTPUT_FRAME_SAVE_OUTPUT_BTN = 
		"frame.output.saveoutputbutton";
	
	// output frame menu and menu item message keys...
	public static final String OF_FILE_MENU = "frame.output.menus.file";
	public static final String OF_FILE_CLOSE_MI = 
		"frame.output.menus.file.menuitems.close";
	public static final String OF_VIEW_MENU = "frame.output.menus.view";
	public static final String OF_VIEW_TRANSFORM_TIMINGS_DETAIL_MI = 
		"frame.output.menus.view.menuitems.transformtimingsdetail";
	
	// validation error frame menu and menu item message keys...
	public static final String VF_FILE_MENU = "frame.validationerr.menus.file";
	public static final String VF_FILE_CLOSE_MI = 
		"frame.validationerr.menus.file.menuitems.close";
	
	// validation error frame message keys...
	public static final String VALIDATIONERR_FRAME_FILE = 
		"frame.validationerr.file";
	public static final String VALIDATIONERR_FRAME_LINE_NUM = 
		"frame.validationerr.linenumber";
	public static final String VALIDATIONERR_FRAME_COLUMN_NUM = 
		"frame.validationerr.columnnumber";
	public static final String VALIDATIONERR_FRAME_MSG = 
		"frame.validationerr.message";
	public static final String VALIDATIONERR_FRAME_NOT_AVAILABLE = 
		"frame.validationerr.notavailable";
	
	// error message keys...
	public static final String ERRORS_MESSAGE = "errors.message";
	public static final String ERRORS_TITLE = "errors.title";
	public static final String ERRORS_NETWORK_CONNECT = "errors.networkconnect";
	
	// local object...
	private Locale locale;
	
	// resource bundle for the locale...
	private ResourceBundle resources;
	
	/**
	 * Private constructor
	 *
	 */
	private LabelStringFactory() {
		
		// get the default locale...
		locale = Locale.getDefault();
		
		// load the resource bundle for the default locale...
		resources = ResourceBundle.getBundle("resources", locale);
	}
	
	/**
	 * Returns the label given the inputted key.
	 * @param aKey
	 * @return
	 */
	public String getString(String aKey) {
		return resources.getString(aKey);
	}
	
	/**
	 * Returns the mnemonic char associated with the resource key.
	 * @param aKey
	 * @return
	 */
	public char getMnemonic(String aKey) {
		return getString(aKey + MNEMONIC_KEY).toCharArray()[0];
	}
	
	/**
	 * Returns the singleton instance.
	 * @return
	 */
	public synchronized static LabelStringFactory getInstance() {
		if (instance == null) {
			instance = new LabelStringFactory();
		}
		return instance;
	}
}
