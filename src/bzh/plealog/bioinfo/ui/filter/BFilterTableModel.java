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
import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;

/**
 * This class handles the data model of BFilterTable.
 * 
 * @author Patrick G. Durand
 */
public class BFilterTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 2366525557770286859L;
	private int[]                    _columnIds;
	private ArrayList<BFilterEntry>  _filters;

	public static final int FILTER_NAME_HEADER = 0;
	public static final int FILTER_DESCRIPTION_HEADER   = 1;
	public static final int FILTER_RULE  = 2;

	//NOTE: when modifying the following array, also modify initColumnSize() 
	//and createStandardColHeaders() 
	protected static final String[] HEADERS = 
		{
				FilterMessages.getString("BFilterTable.column.1"),
				FilterMessages.getString("BFilterTable.column.2"),
				FilterMessages.getString("BFilterTable.column.3")
		};

	public BFilterTableModel(){
		_filters = new ArrayList<BFilterEntry>();
		createStandardColHeaders();
	}

	public void addFilter(BFilterEntry filter){
		_filters.add(filter);
		this.fireTableDataChanged();
	}

	public void removeFilter(BFilterEntry filter){
		_filters.remove(filter);
		this.fireTableDataChanged();
	}

	private void createStandardColHeaders(){
		_columnIds = new int[HEADERS.length];
		_columnIds[0] = FILTER_NAME_HEADER;
		_columnIds[1] = FILTER_DESCRIPTION_HEADER;
		_columnIds[2] = FILTER_RULE;
	}
	public String getColumnName(int column){
		return HEADERS[_columnIds[column]];
	}

	public int getColumnCount() { 
		return _columnIds.length; 
	}
	public int getRowCount() { 
		return _filters.size();
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
		}
		return val;
	}
}
