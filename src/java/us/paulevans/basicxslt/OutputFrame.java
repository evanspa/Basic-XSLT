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
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

import net.blueslate.commons.gui.GUIUtils;
import net.blueslate.commons.gui.domtree.DOMTree;
import net.blueslate.commons.io.IOUtils;
import net.blueslate.commons.xml.TransformOutputProperties;
import net.blueslate.commons.xml.XMLUtils;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Defines the output/results frame.
 * 
 * @author pevans
 * 
 */
public class OutputFrame extends DisposableFrame implements ActionListener {

	// get the i18n factory singleton instance...
	private static final LabelStringFactory stringFactory = LabelStringFactory
			.getInstance();

	// default frame width and height - these values are used if
	// a height and width are not found in the user's preferences...
	private static final String DEFAULT_FRAME_WIDTH = "635";
	private static final String DEFAULT_FRAME_HEIGHT = "615";
	
    // user-prefs property name prefix...
	private static final String PROPERTY_NAME_PREFIX = "output_frame_";
	
    // logger object...
    private static final Logger logger = Logger.getLogger(OutputFrame.class);

	// instance members...
	private JButton closeBtn, saveOutputBtn;
	private JTextArea textArea;
	private JMenuItem close, transformTimings;
	private Node node;
	private UserPreferences userPrefs;
	private XSLRow xslRows[];
	private TransformOutputProperties lastTransformOutputProps;
	private byte transformResult[];

	/**
	 * Constructor
	 * 
	 * @param aParent
	 * @param saTitle
	 * @param aText
	 * @param aXSLRows
	 */
	public OutputFrame(BasicXSLTFrame aParent, String saTitle, byte aText[],
			XSLRow aXSLRows[]) {
		this(aParent, saTitle, null, null, aText, aXSLRows, false);
	}

	/**
	 * Constructor
	 * 
	 * @param aParent
	 * @param saTitle
	 * @param aResultXml
	 * @param aLastTransformOutputProps
	 * @param aXSLRows
	 * @param aIncludeSaveOutputBtn
	 */
	public OutputFrame(BasicXSLTFrame aParent, String saTitle,
			Document aResultXml,
			TransformOutputProperties aLastTransformOutputProps,
			XSLRow aXSLRows[], boolean aIncludeSaveOutputBtn) {
		this(aParent, saTitle, aResultXml, aLastTransformOutputProps, null,
				aXSLRows, aIncludeSaveOutputBtn);
	}

	/**
	 * Constructor
	 * 
	 * @param aParent
	 * @param saTitle
	 * @param aResultXml
	 * @param aLastTransformOutputProps
	 * @param aText
	 * @param aXSLRows
	 * @param aIncludeSaveOutputBtn
	 */
	private OutputFrame(BasicXSLTFrame aParent, String saTitle,
			Document aResultXml,
			TransformOutputProperties aLastTransformOutputProps, byte aText[],
			XSLRow aXSLRows[], boolean aIncludeSaveOutputBtn) {

		JScrollPane scrollPane;
		JTree tree;
		JPanel southPanel;

		xslRows = aXSLRows;
		transformResult = aText;
		node = aResultXml;
		lastTransformOutputProps = aLastTransformOutputProps;
		buildMenuBar();
		southPanel = new JPanel(new FlowLayout());
		southPanel.add(closeBtn = new JButton(stringFactory
				.getString(LabelStringFactory.CLOSE_BUTTON)));
		if (aIncludeSaveOutputBtn) {
			southPanel.add(saveOutputBtn = new JButton(stringFactory.getString(
					LabelStringFactory.OUTPUT_FRAME_SAVE_OUTPUT_BTN)));
			saveOutputBtn.addActionListener(this);
		}
		if (aResultXml != null) {
			tree = new DOMTree(aResultXml);
			GUIUtils.expandAll(tree, true);
			scrollPane = new JScrollPane(tree,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		} else {
			textArea = new JTextArea(new String(aText));
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			scrollPane = new JScrollPane(textArea,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		closeBtn.addActionListener(this);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(southPanel, BorderLayout.SOUTH);
		setTitle(saTitle);
		setWindowCloseListener();
		setSize();
		GUIUtils.center(this, aParent);
		setVisible(true);
	}

	/**
	 * Create the window-close listener
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
	 * Set the size of the window
	 * 
	 */
	private void setSize() {

		int width, height;

		userPrefs = Utils.getUserPrefs();
		width = Integer.parseInt(userPrefs.getProperty(PROPERTY_NAME_PREFIX
				+ AppConstants.FRAME_WIDTH_PROP, DEFAULT_FRAME_WIDTH));
		height = Integer.parseInt(userPrefs.getProperty(PROPERTY_NAME_PREFIX
				+ AppConstants.FRAME_HEIGHT_PROP, DEFAULT_FRAME_HEIGHT));
		setSize(width, height);
	}

	/**
	 * Builds the GUI menu bar.
	 */
	private void buildMenuBar() {

		JMenu file, view;
		JMenuBar menuBar;

		menuBar = new JMenuBar();
		file = new JMenu(stringFactory.getString(
				LabelStringFactory.OF_FILE_MENU));
		file.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.OF_FILE_MENU));
		close = new JMenuItem(stringFactory.getString(
				LabelStringFactory.OF_FILE_CLOSE_MI));
		close.setMnemonic(stringFactory.getMnemonic(
				LabelStringFactory.OF_FILE_CLOSE_MI));
		close.addActionListener(this);
		file.add(close);
		menuBar.add(file);
		if (xslRows != null) {
			view = new JMenu(stringFactory.getString(
					LabelStringFactory.OF_VIEW_MENU));
			view.setMnemonic(stringFactory.getMnemonic(
					LabelStringFactory.OF_VIEW_MENU));
			view.add(transformTimings = new JMenuItem(stringFactory.getString(
					LabelStringFactory.OF_VIEW_TRANSFORM_TIMINGS_DETAIL_MI)));
			transformTimings.setMnemonic(stringFactory.getMnemonic(
					LabelStringFactory.OF_VIEW_TRANSFORM_TIMINGS_DETAIL_MI));
			transformTimings.addActionListener(this);
			menuBar.add(view);
		}
		setJMenuBar(menuBar);
	}

	/**
	 * Event handler method.
	 * 
	 * @param aEvt
	 */
	public void actionPerformed(ActionEvent aEvt) {

		int returnVal;
		File fileToSave;

		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		if (aEvt.getSource() == closeBtn || aEvt.getSource() == close) {
			dispose(userPrefs, PROPERTY_NAME_PREFIX);
		} else if (aEvt.getSource() == transformTimings) {
			new TimingsFrame(this, xslRows);
		} else if (aEvt.getSource() == saveOutputBtn) {
			try {
				returnVal = Utils.getInstance().getFileChooser().showSaveDialog(
						this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileToSave = 
						Utils.getInstance().getFileChooser().getSelectedFile();
					BasicXSLTFrame.setLastFileChosen(
							fileToSave.getAbsolutePath());
					if (textArea != null) {
						IOUtils.writeFile(fileToSave, transformResult);
					} else {
						IOUtils.writeFile(fileToSave, XMLUtils.serialize(node,
								lastTransformOutputProps));
					}
				}
			} catch (Throwable aAny) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				logger.error(ExceptionUtils.getFullStackTrace(aAny));
				Utils.showErrorDialog(this, aAny);
			}
		}
	}
}