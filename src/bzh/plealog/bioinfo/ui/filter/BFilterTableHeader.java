package bzh.plealog.bioinfo.ui.filter;

import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;

public enum BFilterTableHeader {
  FILTER_CHECK(             0, "-"                                              , Boolean.class),
  FILTER_NAME_HEADER(       1, FilterMessages.getString("BFilterTable.column.1"), Integer.class),
  FILTER_DESCRIPTION_HEADER(2, FilterMessages.getString("BFilterTable.column.2"), String.class ),
  FILTER_RULE(              3, FilterMessages.getString("BFilterTable.column.3"), String.class );
  
  private final int id;
  private final String label;
  private final Class<?> clazz;
  
  BFilterTableHeader(int id, String lbl, Class<?> clazz){
    this.id = id;
    this.label = lbl;
    this.clazz = clazz;
  }
  
  public int getID(){
    return id;
  }
  
  public String getLabel(){
    return label;
  }
  
  public Class<?> getClazz(){
    return clazz;
  }
}
