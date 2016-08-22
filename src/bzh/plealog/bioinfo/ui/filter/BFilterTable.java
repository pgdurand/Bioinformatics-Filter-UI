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
package bzh.plealog.bioinfo.ui.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import bzh.plealog.bioinfo.api.filter.BDataAccessors;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.config.FilterSystemConfigurator;
import bzh.plealog.bioinfo.io.filter.FilterSerializer;
import bzh.plealog.bioinfo.io.filter.FilterSerializerException;
import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;
import bzh.plealog.bioinfo.ui.modules.filter.FilterSystemUI;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.file.EZFileUtils;

/**
 * This class handles an GUI that aims at managing BFilters. These are displayed
 * within a JTable and the UI proposes buttons to add, copy, delete and edit 
 * BFilters.
 * 
 * @author Patrick G. Durand
 */
public class BFilterTable extends JPanel {
  private static final long serialVersionUID = -4560985083271901616L;
  private BDataAccessors    _filterModel;
  private FilterTable       _table;
  private JButton           _newBtn;
  private JButton           _copyBtn;
  private JButton           _editBtn;
  private JButton           _deleteBtn;
  private JButton           _exportBtn;
  private JButton           _importBtn;
  private EventListenerList _listenerList;
  private Component         _parent;
  private JLabel            _filterName;
  private RulesTableCellRenderer _rulesRenderer;

  public BFilterTable(BDataAccessors fModel){
    this(fModel, true, true, true);
  }

  public BFilterTable(BDataAccessors fModel, boolean showControls, boolean showIOControls, boolean allowMultipleSelection){
    JPanel      btnPanel, ctrlPnl;
    JScrollPane scroll;

    _filterModel = fModel;
    _listenerList = new EventListenerList();
    _rulesRenderer = new RulesTableCellRenderer();
    _table = new FilterTable(FilterSystemUI.getFilterCentralRepository());
    _table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
    _table.getTableHeader().setReorderingAllowed(false);
    _table.setColumnSelectionAllowed(false);
    _table.setRowSelectionAllowed(true);
    _table.setGridColor(Color.LIGHT_GRAY);
    _table.getSelectionModel().setSelectionMode(
        allowMultipleSelection ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
    _table.getSelectionModel().addListSelectionListener(new MySelectionListener());
    scroll = new JScrollPane(_table);

    _newBtn = new JButton(FilterMessages.getString("BFilterTable.ui.newBtn"));
    _newBtn.addActionListener(new NewActionListener());
    _copyBtn = new JButton(FilterMessages.getString("BFilterTable.ui.copyBtn"));
    _copyBtn.addActionListener(new CopyActionListener());
    _editBtn = new JButton(FilterMessages.getString("BFilterTable.ui.editBtn"));
    _editBtn.addActionListener(new EditActionListener());
    _deleteBtn = new JButton(FilterMessages.getString("BFilterTable.ui.deleteBtn"));
    _deleteBtn.addActionListener(new DeleteActionListener());
    _importBtn = new JButton(FilterMessages.getString("BFilterTable.ui.importBtn"));
    _importBtn.addActionListener(new ImportActionListener());
    _exportBtn = new JButton(FilterMessages.getString("BFilterTable.ui.exportBtn"));
    _exportBtn.addActionListener(new ExportActionListener());

    _filterName = new JLabel("-");
    this.setLayout(new BorderLayout());
    this.add(scroll, BorderLayout.CENTER);
    ctrlPnl = new JPanel(new BorderLayout());
    if (showControls){
      btnPanel = new JPanel();
      btnPanel.add(_newBtn);
      btnPanel.add(_copyBtn);
      btnPanel.add(_editBtn);
      btnPanel.add(_deleteBtn);
      if (showIOControls){
        btnPanel.add(new JLabel("/"));
        btnPanel.add(_importBtn);
        btnPanel.add(_exportBtn);
      }
      ctrlPnl.add(btnPanel, BorderLayout.SOUTH);
    }
    ctrlPnl.add(_filterName, BorderLayout.EAST);
    ctrlPnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
    this.add(ctrlPnl, BorderLayout.SOUTH);
    this.addComponentListener(new TableComponentAdapter());
    reassignRowHeight();
    if (_table.getModel().getRowCount()!=0){
      _table.getSelectionModel().setSelectionInterval(0, 0);
    }
    this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
  }
  public void setParent(Component parent){
    _parent = parent;
  }

  public void addSelectionListener(ListSelectionListener listener){
    _table.getSelectionModel().addListSelectionListener(listener);
  }

  private class MySelectionListener implements ListSelectionListener{
    public void valueChanged(ListSelectionEvent e){
      if (e.getValueIsAdjusting())
        return;
      ListSelectionModel sModel = (ListSelectionModel)e.getSource();
      int[]              rows;
      boolean            selected, empty;

      empty= sModel.isSelectionEmpty();
      if (empty){
        _copyBtn.setEnabled(false);
        _editBtn.setEnabled(false);
        _deleteBtn.setEnabled(false);
        _exportBtn.setEnabled(false);
        _filterName.setText("-");
        return;
      }
      rows = _table.getSelectedRows();
      selected = (rows.length == 1);
      _copyBtn.setEnabled(selected);
      _editBtn.setEnabled(selected);
      _exportBtn.setEnabled(selected);
      _deleteBtn.setEnabled(true);
      if (rows.length==1){
        BFilterTableModel dModel = (BFilterTableModel) _table.getModel();
        BFilterEntry entry = (BFilterEntry) dModel.getValueAt(rows[0], -1);
        String fName = EZFileUtils.getFileName(new File(entry.getFileName()));
        _filterName.setText(fName);
      }
      else{
        _filterName.setText("-");
      }
    }
  }
  /**
   * Initializes the table with a list of BFilterEntry objects.
   */
  public void initialize(List<BFilterEntry> entries){
    BFilterTableModel dModel;
    Iterator<BFilterEntry>  iter;
    BFilterEntry            obj;

    dModel = (BFilterTableModel) _table.getModel();

    iter = entries.iterator();
    while(iter.hasNext()){
      obj = iter.next();
      dModel.addFilter((BFilterEntry)obj);
    }
    reassignRowHeight();
    if (_table.getModel().getRowCount()!=0){
      _table.getSelectionModel().setSelectionInterval(0, 0);
    }
  }
  private class FilterTable extends JTable {
    private static final long serialVersionUID = -331746387726689702L;
    private FilterTable(TableModel dm) {
      super(dm);
    }
    public void tableChanged(TableModelEvent event){
      super.tableChanged(event);
      reassignRowHeight();
    }
    public TableCellRenderer getCellRenderer(int row, int column) {
      if (column==2)
        return _rulesRenderer;
      else
        return super.getCellRenderer(row, column);
    }
  }
  private class RulesTableCellRenderer extends JLabel
  implements TableCellRenderer {
    private static final long serialVersionUID = 2711647318794306290L;
    public RulesTableCellRenderer(){
      this.setOpaque(true);
      this.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
    }
    public Component getTableCellRendererComponent(
        JTable table, Object value,
        boolean isSelected, boolean hasFocus,
        int row, int column) {
      if (isSelected) {
        this.setBackground(table.getSelectionBackground());
        this.setForeground(table.getSelectionForeground());
      }
      else{
        this.setForeground(table.getForeground());
        this.setBackground(table.getBackground());
      }
      this.setText(value.toString());
      this.setEnabled(table.isEnabled());
      this.setFont(table.getFont());
      return this;
    }
    public void paintComponent(Graphics g){
      ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      super.paintComponent(g);
      ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_OFF);
    }
  }

  protected void fireHitChange(BFilterEditEvent mge) {
    Object[] listeners = _listenerList.getListenerList();
    for (int i = listeners.length-2; i>=0; i-=2) {
      if (listeners[i]==BFilterEditListener.class) {
        ((BFilterEditListener)listeners[i+1]).filterEdited(mge);
      }
    }
  }

  /**
   * Adds a listener to catch BFilter edition operations.
   */
  public void addBFilterEditListener(BFilterEditListener listener){
    if (listener==null)
      return;
    _listenerList.add(BFilterEditListener.class, listener);
  }

  /**
   * Removes a BFilterEditListener.
   */
  public void removeBFilterEditListener(BFilterEditListener listener){
    if (listener==null)
      return;
    _listenerList.remove(BFilterEditListener.class, listener);
  }    

  /**
   * Helper method invoking the BFilterEditorDialog.
   */
  private BFilter editFilter(BFilter initFilter){
    BFilterEditorDialog filterDialog;
    BFilter             filter;

    if (_parent instanceof Dialog){
      filterDialog = new BFilterEditorDialog(
          (Dialog)_parent, 
          FilterMessages.getString("BFilterTable.ui.editorDlg"), 
          _filterModel, 
          initFilter);		}
    else{
      filterDialog = new BFilterEditorDialog(
          (Frame)_parent, 
          FilterMessages.getString("BFilterTable.ui.editorDlg"), 
          _filterModel,
          initFilter);		}

    filter = filterDialog.getFilter();
    return filter;
  }

  /**
   * Adds a new BFilter in the Table.
   */
  private BFilterEntry addFilterInTable(BFilter filter){
    BFilterEntry     fEntry;
    BFilterTableModel dModel;

    if (filter==null)
      return null;

    fEntry = new BFilterEntry();
    fEntry.setFilter(filter);
    dModel = (BFilterTableModel) _table.getModel();
    dModel.addFilter(fEntry);
    return fEntry;
  }

  /**
   * Gets the selected row of the table.
   */
  public BFilterEntry[] getSelectedEntries(){
    int[]          rows;
    BFilterEntry[] entries;
    int            i;

    rows = _table.getSelectedRows();
    if (rows.length==0)
      return null;
    entries = new BFilterEntry[rows.length];
    for(i=0;i<rows.length;i++){
      entries[i] = (BFilterEntry) _table.getModel().getValueAt(rows[i], -1);
    }
    return entries;
  }

  /**
   * Returns a particular filter given its name.
   */
  public BFilterEntry getFilter(String aliasName){
    BFilterTableModel dModel;
    BFilterEntry     fEntry;
    int              i, size;

    dModel = (BFilterTableModel) _table.getModel();
    size = dModel.getRowCount();
    for(i=0;i<size;i++){
      fEntry = (BFilterEntry) dModel.getValueAt(i, -1);
      if (fEntry.getFilter().getName().equals(aliasName)){
        return fEntry;
      }
    }
    return null;
  }

  /**
   * Returns a list of all Filters available. The returned list contains
   * String objects, each of them being the alias name of a Filter.
   */
  public List<String> getFilterAliases(){
    BFilterTableModel dModel;
    BFilterEntry      fEntry;
    int               i, size;
    ArrayList<String> aliases;

    aliases = new ArrayList<String>();
    dModel = (BFilterTableModel) _table.getModel();
    size = dModel.getRowCount();
    for(i=0;i<size;i++){
      fEntry = (BFilterEntry) dModel.getValueAt(i, -1);
      aliases.add(fEntry.getFilter().getName());
    }

    return aliases;
  }

  private class ImportActionListener implements ActionListener{
    private File chooseFile(){
      return EZFileManager.chooseFileForOpenAction(
          BFilterTable.this,
          FilterMessages.getString("BFilterTable.import.dlg.header"),
          null);
    }
    public void actionPerformed(ActionEvent e){
      FilterSerializer  serializer;
      BFilter           filter = null;
      BFilterEntry      fEntry;
      File              f;

      f = chooseFile();
      if (f==null)
        return;
      serializer = FilterSystemConfigurator.getSerializer();
      try {
        filter = serializer.load(_filterModel, f);
      } catch (FilterSerializerException e1) {
        EZEnvironment.displayWarnMessage(BFilterTable.this,FilterMessages.getString("BFilterTable.import.err"));
        return;
      }
      fEntry = addFilterInTable(filter);
      if (fEntry==null)
        return;
      fireHitChange(new BFilterEditEvent(fEntry, BFilterEditEvent.FILTER_ADDED));
      reassignRowHeight();
      int sel = _table.getRowCount() - 1;
      _table.getSelectionModel().setSelectionInterval(sel, sel);
      _table.scrollRectToVisible(_table.getCellRect(sel, 0, true));
    }
  }
  private class ExportActionListener implements ActionListener{
    private File chooseFile(){
      return EZFileManager.chooseFileForSaveAction(BFilterTable.this, "Save filter", null);
      /*DDFileExt fe = DDFileTypes.getFileForSaveAction(
	        		FilterMessages.getString("BFilterTable.export.dlg.header"), 
	                DDFileTypes.getFileFilter(DDFileTypes.KBF_FEXT));
	        if (fe!=null){
	        	return DDFileTypes.forceFileExtension(fe.getFile(), DDFileTypes.KBF_FEXT);
	        }
	        else{
	        	return null;
	        }*/
    }
    public void actionPerformed(ActionEvent e){
      FilterSerializer serializer;
      BFilter          curFilter;
      BFilterEntry[]   fEntries;
      BFilterEntry     fEntry;
      File             f;

      fEntries = getSelectedEntries();
      if (fEntries==null)
        return;
      fEntry = fEntries[0];
      curFilter = fEntry.getFilter();

      f = chooseFile();
      if (f==null)
        return;
      serializer = FilterSystemConfigurator.getSerializer();
      try {
        serializer.save(curFilter, f);
      } catch (FilterSerializerException e1) {
        EZEnvironment.displayWarnMessage(BFilterTable.this,FilterMessages.getString("BFilterTable.export.err"));
      }
    }
  }

  private class NewActionListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      BFilterEntry fEntry;
      fEntry = addFilterInTable(editFilter(null));
      if (fEntry==null)
        return;
      fireHitChange(new BFilterEditEvent(fEntry, BFilterEditEvent.FILTER_ADDED));
      reassignRowHeight();
      int sel = _table.getRowCount() - 1;
      _table.getSelectionModel().setSelectionInterval(sel, sel);
      _table.scrollRectToVisible(_table.getCellRect(sel, 0, true));
    }
  }

  private class CopyActionListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      BFilter        curFilter, newFilter;
      BFilterEntry[] fEntries;
      BFilterEntry   fEntry;

      fEntries = getSelectedEntries();
      if (fEntries==null)
        return;
      curFilter = fEntries[0].getFilter();
      newFilter = (BFilter) curFilter.clone();
      newFilter.setName("CopyOf"+newFilter.getName());
      fEntry = addFilterInTable(newFilter);
      if (fEntry==null)
        return;
      reassignRowHeight();
      fireHitChange(new BFilterEditEvent(fEntry, BFilterEditEvent.FILTER_COPIED));
      int sel = _table.getRowCount() - 1;
      _table.getSelectionModel().setSelectionInterval(sel, sel);
      _table.scrollRectToVisible(_table.getCellRect(sel, 0, true));
    }
  }

  private class EditActionListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      BFilter        curFilter, newFilter;
      BFilterEntry[] fEntries;
      BFilterEntry   fEntry;
      int            sel;

      fEntries = getSelectedEntries();
      if (fEntries==null)
        return;
      sel = _table.getSelectedRows()[0];
      fEntry = fEntries[0];
      curFilter = fEntry.getFilter();
      newFilter = editFilter(curFilter);
      if (newFilter!=null){
        fEntry.setFilter(newFilter);
        reassignRowHeight();
        fireHitChange(new BFilterEditEvent(fEntry, BFilterEditEvent.FILTER_EDITED));
        _table.getSelectionModel().setSelectionInterval(sel, sel);
        _table.scrollRectToVisible(_table.getCellRect(sel, 0, true));
      }
    }
  }

  private class DeleteActionListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      BFilterTableModel dModel;
      BFilterEntry[]          fEntries;
      BFilterEntry            fEntry;
      int                     sel;

      fEntries = getSelectedEntries();
      if (fEntries==null)
        return;
      sel = _table.getSelectedRows()[0];
      dModel = (BFilterTableModel) _table.getModel();

      for(int i=0;i<fEntries.length;i++){
        fEntry = fEntries[i];
        dModel.removeFilter(fEntry);
        reassignRowHeight();
        fireHitChange(new BFilterEditEvent(fEntry, BFilterEditEvent.FILTER_DELETED));
      }
      if (_table.getRowCount()!=0){
        sel = Math.max(0, sel-1);
        _table.getSelectionModel().setSelectionInterval(sel, sel);
        _table.scrollRectToVisible(_table.getCellRect(sel, 0, true));
      }
    }
  }
  private void reassignRowHeight(){
    TableModel   tm;
    BFilterEntry bfe;
    int          fmH;

    if (_table==null)
      return;
    fmH = _table.getFontMetrics(_table.getFont()).getHeight();
    tm = _table.getModel();
    for(int i=0;i<tm.getRowCount();i++){
      bfe = (BFilterEntry) tm.getValueAt(i, -1);
      _table.setRowHeight(i, bfe.getFilter().size()*fmH+10);
    }
    _table.repaint();  
  }
  private void initColumnSize(int width){
    FontMetrics      fm;
    TableColumnModel tcm;
    TableColumn      tc, lastTc=null;
    String           header;
    int              i, size, tot, val;

    fm = _table.getFontMetrics(_table.getFont());
    tcm = _table.getColumnModel();
    size = tcm.getColumnCount();
    tot=0;
    for (i=0;i<size;i++){
      tc = tcm.getColumn(i);
      header = tc.getHeaderValue().toString();
      if (i<2){
        val = 4*fm.stringWidth(header);
        tc.setPreferredWidth(val);
        /* Following can be used to lock the width of a column
                Could be interesting to add to the TableHeaderColumItem a field
                specifying which column has a locked size.
                tc.setMinWidth(val);
                tc.setMaxWidth(val);*/
        tot+=val;
      }
      else{
        lastTc=tc;
      }
    }
    if (lastTc!=null){
      lastTc.setPreferredWidth(width-tot-2);
    }
  }
  private class TableComponentAdapter extends ComponentAdapter{
    public void componentResized(ComponentEvent e){
      Component parent;

      int   width;
      parent = (Component) e.getSource();
      width = parent.getBounds().width;
      initColumnSize(width);
    }
  }
}
