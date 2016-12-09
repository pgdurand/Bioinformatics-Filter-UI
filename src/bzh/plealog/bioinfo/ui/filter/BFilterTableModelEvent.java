package bzh.plealog.bioinfo.ui.filter;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

public class BFilterTableModelEvent extends TableModelEvent {

  private static final long serialVersionUID = 2528527163628548388L;

  public static enum TYPE { TYPE_FILTER_CHECKED }
  
  private TYPE type;
  
  public BFilterTableModelEvent(TableModel source, int row, int column, TYPE type) {
    super(source, row, row, column);
    this.type = type;
  }

  public TYPE getEventType(){
    return type;
  }
}
