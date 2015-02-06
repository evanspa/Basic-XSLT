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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Contains many helper activities and also serves as an event-handler for 
 * SAX events (for XML validation).
 * @author pevans
 *
 */
public class Utils extends DefaultHandler {
	
	// XML parser features...
	private static final String VALIDATION_FEATURE = 
		"http://xml.org/sax/features/validation";
    private static final String SCHEMA_FEATURE = 
    	"http://apache.org/xml/features/validation/schema";
    
	// static constant...
    private static final JFileChooser FILE_CHOOSER = new JFileChooser();
    
    // singleton instance...
    private static final Utils instance = new Utils();
    
	// get the i18n factory singleton instance...
	private static final LabelStringFactory stringFactory = 
		LabelStringFactory.getInstance();
    
    // static user preferences object...
    private static UserPreferences userPrefs;
    
    // logger object...
    private static final Logger logger = Logger.getLogger(Utils.class);
    
    // instance members...
    private boolean checkWarning;
    private boolean checkError;
    private boolean checkFatalError;

    /**
     * private class constructor
     */
    private Utils() {
    	// does nothing...
    }
    
    /**
     * Enables/disables the collection of aComponents
     * @param aComponents
     * @param aEnable
     */
    public static void setEnabled(List aComponents, boolean aEnable) {
    	
    	int size, loop;
    	
    	size = aComponents.size();
    	for (loop = 0; loop < size; loop++) {
    		((Component)aComponents.get(loop)).setEnabled(aEnable);
    	}
    }
    
    /**
     * Returns the contents of the input XML file as an InputStream.  Uses
     * Apache Jakarta commons-VFS to resolve the location.
     * @param aURI
     * @return
     */
    public static final byte[] getXMLContents(FileSystemManager aFSMgr, 
    		String aURI) throws IOException {
    	
    	FileObject file;
    	FileContent content;

    	file = aFSMgr.resolveFile(aURI);
    	content = file.getContent();
    	return IOUtils.toByteArray(content.getInputStream());
    }
    
    /**
     * Returns the contents of the input XSL file as a Source object.  Uses
     * Apache Jakarta commons-VFS to resolve the location.
     * @param aURI
     * @return
     */
    public static final Source getXSLSource(FileSystemManager aFSMgr, 
    		String aURI) {
    	
    	FileObject file;
    	FileContent content;

    	try {
    		file = aFSMgr.resolveFile(aURI);
    		content = file.getContent();
    		return new StreamSource(content.getInputStream(), 
    				file.getURL().toURI().toString());
    	} catch (Throwable any) {
    		throw new RuntimeException(any);
    	} 
    }
    
    /**
     * Builds and returns a JFileChooser - the current directory of the 
     * JFileChooser is determined from the user-prefs properties file.
     * @return JFileChooser
     * @throws Exception
     */
    public JFileChooser getFileChooser() throws IOException {
    	
        String file;
        
        file = Utils.getUserPrefs().getProperty(
        		AppConstants.LAST_FILE_CHOSEN_PROP);
        if (StringUtils.isBlank(file)) {
            file = "/"; 
        }
        FILE_CHOOSER.setCurrentDirectory(new File(file));
        return FILE_CHOOSER;
    }
    
    /**
     * Displays a message dialog
     * @param aParent
     * @param aMsg
     * @param aTitle
     * @param aType
     */
    public static void showDialog(Frame aParent, String aMsg, String aTitle,
    	int aType) {
    	aParent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		JOptionPane.showMessageDialog(aParent, aMsg, aTitle, aType);
    }
    
    /**
     * Displays an error dialog
     * @param aParent
     * @param aThrowable
     */
    public static void showErrorDialog(Frame aParent, Throwable aThrowable) {
    	
    	String message;
    	Throwable throwableToReport;
    	
    	throwableToReport = ExceptionUtils.getRootCause(aThrowable);
    	if (throwableToReport == null) {
    		throwableToReport = aThrowable;
    	}
    	message = throwableToReport.getMessage();
    	if (StringUtils.isBlank(message)) {
    		message = ExceptionUtils.getStackTrace(throwableToReport);
    	}
    	if (aParent != null) {
    		aParent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    	}
    	JOptionPane.showMessageDialog(aParent, MessageFormat.format(
    			stringFactory.getString(LabelStringFactory.ERRORS_MESSAGE), 
    			message, AppConstants.BUG_HOME_URL), 
    			stringFactory.getString(LabelStringFactory.ERRORS_TITLE), 
    					JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Retrieves the user preferences from disk.
     * @return Properties
     * @throws Exception
     */
    public synchronized static UserPreferences getUserPrefs() {
    	
    	FileInputStream in;
		File appPrefsDir, appPrefsFile;
		
    	if (userPrefs == null) {
			userPrefs = new UserPreferences();
			in = null;
			try {
				appPrefsDir = new File(SystemUtils.USER_HOME + "/" + 
					AppConstants.APP_PREFS_DIR);
				appPrefsDir.mkdir();
				appPrefsFile = new File(appPrefsDir, 
						AppConstants.APP_PREFS_FILE);
				appPrefsFile.createNewFile();
				userPrefs.load(in = new FileInputStream(appPrefsFile));
			} catch (IOException aException) {
				logger.error(ExceptionUtils.getFullStackTrace(aException));
				Utils.showErrorDialog(null, aException);
			} finally {
				closeQuietly(in);
			}
    	}                
        return userPrefs;
    } 
    
    /**
     * Closes the inputted closeable object.
     * @param aCloseable
     */
    public static final void closeQuietly(Closeable aCloseable) {
    	if (aCloseable != null) {
    		try {
				aCloseable.close();
			} catch (IOException e) {
				logger.error(ExceptionUtils.getFullStackTrace(e));
			}
    	}
    }

    /**
     * Returns an output stream from the user prefs properties file.
     * @return OutputStream
     * @throws Exception
     */
    public static OutputStream getUserPrefsOutputStream() throws IOException {
    	
        File appPrefsDir;
        
        try {
        	appPrefsDir = new File(SystemUtils.USER_HOME + "/" + 
        			AppConstants.APP_PREFS_DIR);
        	appPrefsDir.mkdir();
        	return new FileOutputStream(new File(appPrefsDir, 
        			AppConstants.APP_PREFS_FILE));
        } catch (IOException aException) {
        	// log and re-throw...
        	logger.error(ExceptionUtils.getFullStackTrace(aException));
        	throw aException;
        }
    }

    /**
     * Returns the singleton instance
     * @return Utils
     */
    public static Utils getInstance() {
        return instance;
    }
    
    /**
     * Returns aXSLRows as an array
     * @param aXSLRows
     * @return
     */
    public static XSLRow[] toArray(List<XSLRow> aXSLRows) {
    	return aXSLRows.toArray(new XSLRow[aXSLRows.size()]);
    }

    /**
     * Error handler - displays exception stack trace in a 
     * ValidationErrorFrame.
     * @param aTitle
     * @param aHeaderLabel
     * @param aHeader
     * @param aFileName
     * @param aParent
     * @param aThrowable
     * @throws IOException
     */
    public static void handleXMLError(String aTitle, String aHeaderLabel, 
    		String aHeader, String aFileName, BasicXSLTFrame aParent, 
    		Throwable aThrowable) {
    	
        Error error;
        
        logger.info("XML error: " + aThrowable.getMessage());
        error = getErrorDetail(aThrowable);
        new ValidationErrorFrame(aParent, aTitle, aHeaderLabel, aHeader, 
        		error.getColumn(), error.getLine(), error.getMessage(), 
				aFileName, null); 
    }
    
    /**
     * Error handler
     * @param aTitle
     * @param aHeaderLabel
     * @param aHeader
     * @param aFileName
     * @param aParent
     * @param aMessage
     */
    public static void handleXMLError(String aTitle, String aHeaderLabel, 
    		String aHeader, String aFileName, BasicXSLTFrame aParent, 
    		String aMessage) {
    	logger.info("XML error: " + aMessage);
        new ValidationErrorFrame(aParent, aTitle, aHeaderLabel, aHeader, 
        		-1, -1, aMessage, 
				aFileName, null); 
    }

    /**
     * This method gets line and column info about the inputted throwable.
     * @return Error
     */
    public static Error getErrorDetail(Throwable aThrowable) {
    	
    	String errText;
    	SourceLocator sourceLocator;
        Throwable root;
        int column, line;
        
        errText = "";
        column = -1;
        line = -1;
        if ((root = ExceptionUtils.getRootCause(aThrowable)) != null) {
        	aThrowable = root;        	
        }
        if (aThrowable instanceof SAXParseException) {
        	SAXParseException e = (SAXParseException)aThrowable;
        	column = e.getColumnNumber();
        	line = e.getLineNumber();
            errText = e.getMessage();
        } else if (aThrowable instanceof TransformerConfigurationException) {
        	TransformerConfigurationException e = 
        		(TransformerConfigurationException)aThrowable;
        	sourceLocator = e.getLocator();
            if (sourceLocator != null) {
            	column = sourceLocator.getColumnNumber();
				line = sourceLocator.getLineNumber();
                errText = e.getMessage();
            } else {
            	errText = e.getMessageAndLocation();
            }
        } else if (aThrowable instanceof TransformerException) {
        	TransformerException e = (TransformerException)aThrowable;
            sourceLocator = e.getLocator();
            if (sourceLocator != null) {
            	column = sourceLocator.getColumnNumber();
				line = sourceLocator.getLineNumber();
                errText = e.getMessage();
            } else {
            	errText = e.getMessageAndLocation();
            }
        }
        if (StringUtils.isBlank(errText)) {
        	errText = ExceptionUtils.getStackTrace(aThrowable);
        }
        return new Error(column, line, errText);
    }
    
    

    /**
     * Method to determine if inputted xml file is valid and well-formed.
     * @param saXmlFile
     * @throws Exception If saXmlFile is invalid or not well-formed.
     */
    public void isValidXml(FileContent faXmlFile, boolean aCheckWarning,
                                  boolean aCheckError, 
								  boolean aCheckFatalError) throws 
								  SAXNotSupportedException, 
								  SAXNotRecognizedException, 
								  ParserConfigurationException, SAXException, 
								  IOException {
    	
    	SAXParserFactory factory;
    	
        checkWarning = aCheckWarning;
        checkError = aCheckError;
        checkFatalError = aCheckFatalError;
        factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true); 
        try {
        	factory.setFeature(VALIDATION_FEATURE, true);
        	factory.setFeature(SCHEMA_FEATURE, true);       	
        	SAXParser parser = factory.newSAXParser();    
        	parser.parse(faXmlFile.getInputStream(), this);
        } catch (UnknownHostException aException) {
        	// log and re-throw runtime exception...
        	logger.error(ExceptionUtils.getFullStackTrace(aException));
        	throw new UnknownHostException(stringFactory.getString(
        			LabelStringFactory.ERRORS_NETWORK_CONNECT));
        } catch (SocketException aException) {
        	// log and re-throw runtime exception...
        	logger.error(ExceptionUtils.getFullStackTrace(aException));
        	throw new SocketException(stringFactory.getString(
        			LabelStringFactory.ERRORS_NETWORK_CONNECT));
        } catch (SAXNotSupportedException aException) {
        	// log and re-throw...
        	logger.error(ExceptionUtils.getFullStackTrace(aException));
        	throw aException;
        } catch (SAXNotRecognizedException aException) {
        	// log and re-throw...
        	logger.error(ExceptionUtils.getFullStackTrace(aException));
        	throw aException;
        } catch (ParserConfigurationException aException) {
        	// log and re-throw...
        	logger.error(ExceptionUtils.getFullStackTrace(aException));
        	throw aException;
        } catch (SAXException aException) {
        	// log and re-throw...
        	logger.error(ExceptionUtils.getFullStackTrace(aException));
        	throw aException;
        } catch (IOException aException) {
        	// log and re-throw...
        	logger.error(ExceptionUtils.getFullStackTrace(aException));
        	throw aException;
        }
    }

    /**
     * Throws exception if 'checkWarning' is true when this method is called.
     * @param aException
     * @throws SAXException
     */
    public void warning(SAXParseException aException) throws SAXException {
        if (checkWarning) {
            throw aException;
        }
    }

    /**
     * Throws exception if 'checkError' is true when this method is called.
     * @param aException
     * @throws SAXException
     */
    public void error(SAXParseException aException) throws SAXException {
        if (checkError) {
            throw aException;
        }
    }

    /**
     * Throws exception if 'checkFatalError' is true when this method is called.
     * @param aException
     * @throws SAXException
     */
    public void fatalError(SAXParseException aException) throws SAXException {
        if (checkFatalError) {
            throw aException;
        }
    } 
}