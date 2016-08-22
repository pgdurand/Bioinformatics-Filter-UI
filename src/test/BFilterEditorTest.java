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
package test;

import javax.swing.JFrame;

import bzh.plealog.bioinfo.api.core.config.CoreSystemConfigurator;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.config.FilterSystemConfigurator;
import bzh.plealog.bioinfo.ui.filter.BFilterEditorDialog;

/**
 * Illustrates the direct use of the Filter Editor Dialog box.
 * 
 * @author Patrick G. Durand
 */
public class BFilterEditorTest extends JFrame {
	private static final long serialVersionUID = 423447134510143955L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
    BFilterEditorDialog  filterDialog;
		BFilterEditorTest    frame;
		BFilter              filt;
		
    // init API factories (required)
    CoreSystemConfigurator.initializeSystem();
    FilterSystemConfigurator.initializeSystem();
		
    //setup a basic Frame
		frame = new BFilterEditorTest();
		
		System.out.println("Calling editor...");
		//launch the editor
		filterDialog = new BFilterEditorDialog(frame, "Filter Editor", FilterSystemConfigurator.getFilterableModel(), null);
		//after closing the dialog, we can get a Filter (or null if user has clicked on [cancel] button)
		filt = filterDialog.getFilter();
		//get a string representation of the filter
		System.out.println("Filter: "+filt);
	}
}
