/* Copyright (C) 2006-2016 Patrick G. Durand
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/agpl-3.0.txt
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 */
package bzh.plealog.bioinfo.ui.modules.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import bzh.plealog.bioinfo.api.core.config.CoreSystemConfigurator;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.config.FilterSystemConfigurator;
import bzh.plealog.bioinfo.io.filter.FilterSerializer;
import bzh.plealog.bioinfo.io.filter.FilterSerializerException;
import bzh.plealog.bioinfo.ui.filter.BFilterEditorListener;
import bzh.plealog.bioinfo.ui.filter.BFilterEntry;
import bzh.plealog.bioinfo.ui.filter.BFilterTable;
import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;
import bzh.plealog.bioinfo.util.ZipUtil;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.EZGenericApplication;
import com.plealog.genericapp.api.EZUIStarterListener;
import com.plealog.genericapp.api.file.EZFileUtils;

/**
 * This class is used to start the graphical front-end of the Filter Editor.
 *
 * @author Patrick G. Durand
 */
public class FilterManagerUI {
	private static BFilterTable   _fTable;
	private static JTextField     _field;
	private static TextTransfer   _textTransfer;
	private static Logger         _myLogger = Logger.getLogger(FilterManagerUI.class.getName());

	//folder that will maintain filters (hidden directory)
	private static final String FILTER_FOLDER = ".bft_filter";
	private static final String DEVEL_FILTER_SAMPLE_DIR="filter";
	private static final String FILTER_SAMPLE = "samples.zip";
	private static final String FILTER_CMD_LINE_ARG = "-f"; 
	private static final String FILTER_FILE_EXT = "xml";
	
	private static Properties getVersionProperties(){
		Properties  props = new Properties();
		try (InputStream in = FilterMessages.class.getResourceAsStream("version.properties");){
			
			props.load(in);
			in.close();
		}
		catch(Exception ex){//should not happen
			System.err.println("Unable to read props: "+ex.toString());
		}
		return props;
	}
	/**
	 * Launch the Filter Editor UI.
	 */
	public static void startUI(String[] args){
		Properties props = getVersionProperties();
		// This has to be done at the very beginning, i.e. first method call within
		// main().
		EZGenericApplication.initialize(props.getProperty("prg.frame.title"));
		// Add application branding
		EZApplicationBranding.setAppName(props.getProperty("prg.frame.title"));
		EZApplicationBranding.setAppVersion(props.getProperty("prg.version"));
		EZApplicationBranding.setCopyRight(props.getProperty("prg.copyright"));
		EZApplicationBranding.setProviderName(props.getProperty("prg.provider"));

		//Setup factories
		CoreSystemConfigurator.initializeSystem();
		FilterSystemConfigurator.initializeSystem();
		//prepare Filter Manager environment
		FilterSystemUI.initializeSystem();

		FilterSystemUI.setFilterCentralRepositoryPath(EZFileUtils.terminatePath(prepareEnvironment(".")));

		//Add a listener to application startup cycle (see below)
		EZEnvironment.setUIStarterListener(new MyStarterListener());

		//Start the application
		EZGenericApplication.startApplication(args);
		
	}
	
	public static void main(String[] args) {
		startUI(args);
	}

	/**
	 * Prepare the user environment. This method will create the path used to automatically manager
	 * the filters.
	 * 
	 * @param appHomePath the absolute path to the application installation directory
	 * 
	 * @return the absolute path to the user filter directory
	 */
	private static String prepareEnvironment(String appHomePath){
		String path, msg;
		File   f;
		
		path = EZFileUtils.terminatePath(System.getProperty("user.home"))+FILTER_FOLDER;
		f = new File(path);
		
		if(f.exists()==false){
			if (!f.mkdirs()){
				msg = new MessageFormat(FilterMessages.getString("FilterManagerUI.err.1")).format(new Object[]{f.getAbsolutePath()});
				_myLogger.severe(msg);
				System.exit(1);
			}
			else{
				String sourceSample = EZFileUtils.terminatePath(appHomePath)+DEVEL_FILTER_SAMPLE_DIR+File.separator+FILTER_SAMPLE;
				f = new File(sourceSample);
				if (f.exists()){
					//here, we suppose that we are in devel mode: 'filter' directory exists in the project
					try (FileInputStream fis = new FileInputStream(f);){
						ZipUtil.extractAll(fis, path, FILTER_FILE_EXT);
					}
					catch (Exception ex){
						msg = new MessageFormat(FilterMessages.getString("FilterManagerUI.err.2")).format(new Object[]{ex.toString()});
						_myLogger.warning(msg);
					}
				}
				else{
					//here, we are in production mode: samples.zip is located in the jar of the application
					try (InputStream in = FilterMessages.class.getResourceAsStream(FILTER_SAMPLE);){
						ZipUtil.extractAll(in, path, FILTER_FILE_EXT);
					}
					catch (Exception ex){
						msg = new MessageFormat(FilterMessages.getString("FilterManagerUI.err.2")).format(new Object[]{ex.toString()});
						_myLogger.warning(msg);
					}
				}
			}
		}
		return path;
	}

	private static class MyStarterListener implements EZUIStarterListener{
		private Component _mainPnl = null;
		
		private Component prepareCompo(){
			if (_mainPnl!=null)
				return _mainPnl;
			//This method is called by the framework to obtain the UI main component to be
			//displayed in the main frame.

			BFilterEditorListener listener;
			JPanel                pnl, mainPnl;
			JLabel                lbl;

			_textTransfer = new TextTransfer();

			pnl = new JPanel(new BorderLayout());
			mainPnl = new JPanel(new BorderLayout());
			lbl = new JLabel(FilterMessages.getString("FilterManagerUI.msg.1"));
			_field = new JTextField("");
			_field.setEditable(false);
			_field.getDocument().addDocumentListener(new InputFieldChangeListener());

			pnl.add(lbl, BorderLayout.NORTH);
			pnl.add(_field, BorderLayout.SOUTH);
			pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			_fTable = new BFilterTable(FilterSystemConfigurator.getFilterableModel(), true, true, false);
			listener = new BFilterEditorListener();
			_fTable.addBFilterEditListener(listener);
			_fTable.setPreferredSize(new Dimension(800,250));
			_fTable.addSelectionListener(new MySelectionListener());

			uploadExistingFilters(_fTable, FilterSystemUI.getFilterCentralRepositoryPath());

			mainPnl.add(pnl, BorderLayout.SOUTH);
			mainPnl.add(_fTable, BorderLayout.CENTER);
			_mainPnl = mainPnl;
			return mainPnl;
		}
		@Override
		public Component getApplicationComponent() {
			return prepareCompo();
		}

		@Override
		public boolean isAboutToQuit() {
			return true;
		}

		@Override
		public void postStart() {
		}

		@Override
		public void preStart() {
		}
	}

	/**
	 * Load filters from the filter storage and populates the filter table.
	 */
	private static void uploadExistingFilters(BFilterTable fTable, String filterStoragePath){
		FilterSerializer  serializer;
		BFilterEntry         fEntry;
		BFilter              filter;
		File                 path, file;
		File[]               filters;
		ArrayList<BFilterEntry> fEntries;
		String               msg;
		int                  i;

		serializer = FilterSystemConfigurator.getSerializer();
		if (serializer==null){
			_myLogger.severe(FilterMessages.getString("FilterManagerUI.err.3"));
			return;
		}
		path = new File(filterStoragePath);
		filters=path.listFiles();
		if (filters==null || filters.length==0)
			return;
		//msg = new MessageFormat(FilterMessages.getString("FilterManagerUI.msg.2")).format(new Object[]{new Integer(filters.length)});
		//_myLogger.info(msg);
		fEntries = new ArrayList<BFilterEntry>();
		for(i=0;i<filters.length;i++){
			file = filters[i];
			if (file.isDirectory())
				continue;
			try {
				filter = serializer.load(FilterSystemConfigurator.getFilterableModel(), file);
			} catch (FilterSerializerException e) {
				msg = new MessageFormat(FilterMessages.getString("FilterManagerUI.msg.4")).format(new Object[]{file.getName(), e.toString()});
				_myLogger.severe(msg);
				continue;
			}
			/* I had to do the following clone() call since it appeared that BFilter
			 * loaded with XStream does not use a constructor!!! So BFilter
			 * were badly initialized. The clone forces a correct init.
			 * Todo: check XStream doc to figure out what is wrong.*/
			try {
				fEntry = new BFilterEntry(file.getAbsolutePath(), (BFilter) filter.clone());
				fEntries.add(fEntry);
			} catch (Exception e) {//Embedded: FilterException
				msg = new MessageFormat(FilterMessages.getString("FilterManagerUI.msg.4")).format(new Object[]{file.getName(), e.toString()});
				_myLogger.severe(msg);
			}
		}
		fTable.initialize(fEntries);
	}
	/**
	 * Utility class to transfer a string to the system clipboard.
	 */
	private static class TextTransfer implements ClipboardOwner {
		@Override
		public void lostOwnership(Clipboard arg0, Transferable arg1) {
		}
		public void setClipboardContents(String aString){
			StringSelection stringSelection = new StringSelection(aString);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, this);
		}
	}
	/**
	 * Utility class to listen to a text component.
	 */
	private static class InputFieldChangeListener implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent e) {
			//no need to do anything
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateInput(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateInput(e);
		}

		private void updateInput(DocumentEvent e) {
			Document doc = (Document) e.getDocument();
			String   txt;
			try {
				txt = doc.getText(0, doc.getLength());
				if (txt!=null && txt.length()>0)
					_textTransfer.setClipboardContents(txt);
			}
			catch (BadLocationException e1) {
			}
		}
	}
	/**
	 * Utility class to listen to the filter table selection.
	 */
	private static class MySelectionListener implements ListSelectionListener{
		public void valueChanged(ListSelectionEvent e){
			BFilterEntry[] entries;

			entries = _fTable.getSelectedEntries();
			if(entries==null){
				_field.setText("");
			}
			else{
				_field.setText(FILTER_CMD_LINE_ARG+" "+entries[0].getFileName());
			}
		}
	}

}
