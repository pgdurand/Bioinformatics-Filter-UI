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

import java.util.EventObject;

/**
 * This is an event object used to handle BFilter editing operations.
 * 
 * @author Patrick G. Durand
 */
public class BFilterEditEvent extends EventObject {
  private static final long serialVersionUID = -5998538828855850969L;
  public static final int FILTER_ADDED = 1;
  public static final int FILTER_EDITED = 2;
  public static final int FILTER_DELETED = 3;
  public static final int FILTER_COPIED = 4;

  private int _type;

  /**
   * Constructor.
   * 
   * @param filter this must be an instance of BFilterEntry.
   * @param type one of the FILTER_XXX constants defined here.
   */
  public BFilterEditEvent(Object filter, int type){
    super(filter);
    _type = type;
  }

  /**
   * Returns one of the FILTER_XXX constants defined here.
   */
  public int getType() {
    return _type;
  }
}
