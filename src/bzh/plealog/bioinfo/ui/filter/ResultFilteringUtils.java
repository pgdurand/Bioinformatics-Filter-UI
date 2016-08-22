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

import bzh.plealog.bioinfo.api.filter.BDataAccessors;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.config.FilterSystemConfigurator;

/**
 * This class defines some basic methods to easily starts the Filter Editor.
 * 
 * @author Patrick G. Durand
 */
public class ResultFilteringUtils {

  private static BFilterEditor _filterDialog;

  private static BFilter _filter;

  /**
   * Open a Filter editor.
   * 
   * @param dlgHeader the editor dialogue box header. Can be null.
   * @param filter opens the editor with a given filter. Can be null.
   * 
   * @return a filter or null.
   */
  public static BFilter selectFilter(String dlgHeader,BFilter filter){
    BFilter filt;

    if(_filterDialog!=null){
      filt = _filterDialog.getFilter();

    }else{
      _filterDialog=getEditor(filter);
      filt = _filterDialog.getFilter();
      _filter=filter;
    }

    return filt;
  }


  /**
   * Open a Filter editor.
   * 
   * @param filter opens the editor with a given filter. Can be null.
   * 
   * @return a filter or null.
   */
  public static BFilterEditor getEditor(BFilter filter){
    BFilterEditor editor;

    editor = new BFilterEditor(FilterSystemConfigurator.getFilterableModel(), 
        (BFilter) filter,
        FilterSystemConfigurator.getFilterFactory(), 
        FilterSystemConfigurator.getRuleFactory());

    _filter=filter;

    return editor;
  }

  /**
   * Open a Filter editor.
   * 
   * @param filter opens the editor with a given filter. Can be null.
   * @param showRulesOnly show or do not show name and description fields
   * 
   * @return a filter or null.
   */
  public static BFilterEditor getEditor(BFilter filter, boolean showRulesOnly){
    BFilterEditor editor;

    editor = new BFilterEditor(FilterSystemConfigurator.getFilterableModel(), 
        (BFilter) filter,
        FilterSystemConfigurator.getFilterFactory(), 
        FilterSystemConfigurator.getRuleFactory(),
        showRulesOnly);

    _filter=filter;

    return editor;
  }

  /**
   * Open a Filter editor.
   * 
   * @param filterModel the data model to use. Cannot be null.
   * @param filter opens the editor with a given filter. Can be null.
   * @param showRulesOnly show or do not show name and description fields
   * 
   * @return a filter or null.
   */
  public static BFilterEditor getEditor(BDataAccessors filterModel, BFilter filter, boolean showRulesOnly){
    BFilterEditor editor;

    editor = new BFilterEditor(filterModel, 
        (BFilter) filter,
        FilterSystemConfigurator.getFilterFactory(), 
        FilterSystemConfigurator.getRuleFactory(),
        showRulesOnly);

    _filter=filter;

    return editor;
  }

  /**
   * Returns the editor instance created by one of the above method.
   */
  public static BFilterEditor getFilterDialog() {
    return _filterDialog;
  }

  /**
   * Set a filter.
   */
  public static void set_filter(BFilter filter) {
    _filter = filter;
  }

  /**
   * Get a filter.
   */
  public static BFilter get_filter() {
    return _filter;
  }

}
