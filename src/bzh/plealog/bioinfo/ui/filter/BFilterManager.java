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

import java.util.List;

/**
 * This interface defines a Filter Manager.
 * 
 * @author Patrick G. Durand
 */
public interface BFilterManager {

	public static final String SYS_NAME = "BFilterManager";
	
	/**
	 * Returns a particular filter given its name.
	 */
	public BFilterEntry getFilter(String aliasName);
	
	/**
	 * Returns a list of all Filters available. The returned list contains
	 * String objects, each of them being the alias name of a Filter.
	 */
	public List<String> getFilterAliases();
	
	/**
	 * Adds a listener to the manager.
	 */
	public void addBFilterEditListener(BFilterEditListener listener);
	
	/**
	 * Removes a listener from the manager.
	 */
	public void removeBFilterEditListener(BFilterEditListener listener);
}
