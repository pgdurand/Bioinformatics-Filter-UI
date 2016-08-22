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

import bzh.plealog.bioinfo.ui.filter.BFilterTableModel;

/**
 * This is the SROutput filtering system. 
 * 
 * @author Patrick G. Durand
 */
public class FilterSystemUI {
	public static BFilterTableModel _filters;
	public static String _filterCentralRepositoryPath;
	private static boolean _bInited = false;

	static{
		initializeSystem();
	}

	/**
	 * Initialize the Filter UI System. Always call it before using the Filter UI.
	 */
	public static final void initializeSystem(){
		if (_bInited)
			return;

		FilterSystemUI.setFilterCentralRepository(new BFilterTableModel());

		_bInited = true;
	}
	public static BFilterTableModel getFilterCentralRepository(){
		return _filters;
	}
	public static void setFilterCentralRepository(BFilterTableModel filters){
		_filters = filters;
	}
	public static String getFilterCentralRepositoryPath() {
		return _filterCentralRepositoryPath;
	}
	public static void setFilterCentralRepositoryPath(
			String filterCentralRepositoryPath) {
		FilterSystemUI._filterCentralRepositoryPath = filterCentralRepositoryPath;
	}
}
