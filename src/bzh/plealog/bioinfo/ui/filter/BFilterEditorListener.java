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

import java.io.File;

import org.apache.log4j.Logger;

import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.config.FilterSystemConfigurator;
import bzh.plealog.bioinfo.io.filter.FilterSerializer;
import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;
import bzh.plealog.bioinfo.ui.modules.filter.FilterSystemUI;

import com.plealog.genericapp.api.EZEnvironment;

/**
 * This is a listener aims at handling BFilter editing operations.
 * 
 * @author Patrick G. Durand
 */
public class BFilterEditorListener implements BFilterEditListener {

  public static final String FILTER_HEADER_NAME = "filter";

  private static int _filterCounter = 1;
  private static final Logger _logger = Logger.getLogger("kb."+"BFilterEditorListener");

  /**
   * Utility method aims at computing the full path to store the data relating
   * to a new filter.
   * @param path the absolute path where to store filter data
   */
  private static synchronized String getFilterName(String path){
    String fName;
    File   file;

    while(true){
      fName = path+FILTER_HEADER_NAME+_filterCounter+".xml";
      file = new File(fName);
      if (file.exists()==false)
        break;
      _filterCounter++;
    }
    return fName;
  }

  private String saveFilter(BFilter filter, String fName){
    FilterSerializer  serializer;
    String               filterDir;
    File                 file;

    if (filter==null){
      return fName;
    }
    try{
      if (fName==null){
        filterDir = FilterSystemUI.getFilterCentralRepositoryPath();
        fName = BFilterEditorListener.getFilterName(filterDir);
      }
      serializer = FilterSystemConfigurator.getSerializer();
      if (serializer==null){
        throw new Exception("No serializer defined.");
      }
      file = new File(fName);
      if (file.exists()){
        file.delete();
      }
      serializer.save(filter, file);
    }
    catch (Exception ex){
      String msg = FilterMessages.getString("DDFileTypes.filter.err.msg1");
      _logger.warn(msg+": "+ex);
      EZEnvironment.displayErrorMessage(EZEnvironment.getParentFrame(), msg+".");
    }
    return fName;
  }

  private void filterAdded(BFilterEntry fEntry){
    String fName;

    fName = saveFilter(fEntry.getFilter(), null);
    fEntry.setFileName(fName);
  }

  private void filterEdited(BFilterEntry fEntry){
    saveFilter(fEntry.getFilter(), fEntry.getFileName());
  }

  private void filterCopied(BFilterEntry fEntry){
    String fName;
    fName = saveFilter(fEntry.getFilter(), null);
    fEntry.setFileName(fName);
  }

  private void filterDeleted(BFilterEntry fEntry){
    String fName;
    File   f;

    fName = fEntry.getFileName();
    if (fName!=null){
      f = new File(fName); 
      if (f.delete()==false)
        _logger.info("Unable to delete filter: "+fName);
    }
  }
  /**
   * Implementation of BFilterEditListener interface.
   */
  public void filterEdited(BFilterEditEvent event) {
    BFilterEntry fEntry;

    if (event.getSource() instanceof BFilterEntry == false)
      return;
    fEntry = (BFilterEntry) event.getSource();

    switch(event.getType()){
      case BFilterEditEvent.FILTER_ADDED:
        filterAdded(fEntry);
        break;
      case BFilterEditEvent.FILTER_EDITED:
        filterEdited(fEntry);
        break;
      case BFilterEditEvent.FILTER_DELETED:
        filterDeleted(fEntry);
        break;
      case BFilterEditEvent.FILTER_COPIED:
        filterCopied(fEntry);
        break;
    }
  }

}
