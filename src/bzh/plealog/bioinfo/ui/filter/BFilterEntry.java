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

import bzh.plealog.bioinfo.api.filter.BFilter;

/**
 * This is bag to put BFilter in the BFilterTable. This entry associates
 * a BFilter with additional data.
 * 
 * @author Patrick G. Durand
 */
public class BFilterEntry{
  private String  fileName;
  private BFilter filter;

  public BFilterEntry(){}

  public BFilterEntry(String file, BFilter filter){
    setFileName(file);
    setFilter(filter);
  }
  /**
   * Returns the file name where the BFilter has been saved. This must be
   * an absolute path.
   */
  public String getFileName() {
    return fileName;
  }
  /**
   * Sets the file name where the BFilter has been saved. This must be
   * an absolute path.
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  /**
   * Returns the BFilter.
   */
  public BFilter getFilter() {
    return filter;
  }
  /**
   * Sets the BFilter.
   */
  public void setFilter(BFilter filter) {
    this.filter = filter;
  }

}
