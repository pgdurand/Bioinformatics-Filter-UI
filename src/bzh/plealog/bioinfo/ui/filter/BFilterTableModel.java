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

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import bzh.plealog.bioinfo.api.filter.BFilter;

/**
 * This class handles the data model of BFilterTable.
 * 
 * @author Patrick G. Durand
 */
public class BFilterTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 2366525557770286859L;
	private BFilterTableHeader[]     _columnIds;
	private ArrayList<BFilterEntry>  _filters;

	public BFilterTableModel(){
		_filters = new ArrayList<BFilterEntry>();
		_columnIds = new BFilterTableHeader[]{
        BFilterTableHeader.FILTER_NAME_HEADER,
        BFilterTableHeader.FILTER_DESCRIPTION_HEADER,
        BFilterTableHeader.FILTER_RULE
        };
	}

  public BFilterTableModel(BFilterTableHeader[] columnModel){
    _filters = new ArrayList<BFilterEntry>();
    _columnIds = columnModel;
  }

  public void addFilter(BFilterEntry filter){
		_filters.add(filter);
		this.fireTableDataChanged();
	}

	public void removeFilter(BFilterEntry filter){
		_filters.remove(filter);
		this.fireTableDataChanged();
	}

	public String getColumnName(int column){
		return _columnIds[column].getLabel();
	}

	public BFilterEntry getEntry(int row){
	  return _filters.get(row);
	}
	public int getColumnCount() { 
		return _columnIds.length; 
	}
	public int getRowCount() { 
		return _filters.size();
	}
	@Override
  public Class<?> getColumnClass(int column) {
	  return _columnIds[column].getClazz();
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return _columnIds[column].getClazz()==Boolean.class;
  }
  
  @Override
  public void setValueAt(Object aValue, int row, int column) {
    if (_columnIds[column].getClazz()==Boolean.class) {
      BFilterEntry entry = _filters.get(row);
      entry.setSelected(!_filters.get(row).isSelected());
      fireTableChanged(new BFilterTableModelEvent(this, row, column, BFilterTableModelEvent.TYPE.TYPE_FILTER_CHECKED));
    }
  }
  
	public Object getValueAt(int row, int col) {
		BFilterEntry entry;
		BFilter      filter;
		Object       val = "?";

		entry = (BFilterEntry) _filters.get(row);
		if (col==-1)
			return entry;
		filter = entry.getFilter();
		switch(_columnIds[col]){
		case FILTER_NAME_HEADER:
			val = filter.getName();
			break;
		case FILTER_DESCRIPTION_HEADER:
			val = filter.getDescription();
			break;
		case FILTER_RULE:
			val = filter.getHtmlString();
			break;
		case FILTER_CHECK:
		  val = entry.isSelected();
		}
		return val;
	}
}
