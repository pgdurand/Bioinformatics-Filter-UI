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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import bzh.plealog.bioinfo.api.filter.BAccessorEntry;
import bzh.plealog.bioinfo.api.filter.BDataAccessors;
import bzh.plealog.bioinfo.api.filter.BRule;
import bzh.plealog.bioinfo.api.filter.BRuleException;
import bzh.plealog.bioinfo.api.filter.BRuleFactory;
import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;
import bzh.plealog.hge.api.datamodel.DGMAttribute;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.plealog.genericapp.ui.common.ResizableComboboxPopupMenuListener;

/**
 * This is a BRule editor.
 * 
 * @author Patrick G. Durand
 */
public class BRuleEditor extends JPanel {

  private static final long serialVersionUID = 262464227390422576L;
  private JComboBox<BAccessorEntry> _accessors;
  private JComboBox<String >        _operators;
  private JTextField       _value;
  private JCheckBox        _caseBox;
  private JTextArea        _helpArea;
  private BDataAccessors   _filterModel;
  private BRuleFactory     _ruleFactory;

  private static final Font CBX_FNT = new Font("Arial", Font.PLAIN, 10);
  private static final MessageFormat CS_FORM = new MessageFormat("(?i)({0})");

  public BRuleEditor(BDataAccessors fModel, BRuleFactory ruleFactory){
    this(fModel, ruleFactory, null, null);
  }

  public BRuleEditor(BDataAccessors fModel, BRuleFactory ruleFactory, JButton add, JButton remove){
    _filterModel = fModel;
    _ruleFactory = ruleFactory;
    createGUI(add, remove);
  }
  public void setHelperArea(JTextArea text){
    _helpArea = text;
  }
  /**
   * Returns a valid BRule given the parameters entered by the users.
   */
  public BRule getRule() throws BRuleException {
    BAccessorEntry entry;
    String         accName, ope, value;
    Object         oValue;

    //get accessor
    entry = (BAccessorEntry) _accessors.getSelectedItem();
    if (entry == null)
      throw new BRuleException(FilterMessages.getString("BRuleEditor.err.1"));
    accName = entry.getAccessorVisibleName();

    //get operator
    ope = (String) _operators.getSelectedItem();
    if (ope == null)
      throw new BRuleException(FilterMessages.getString("BRuleEditor.err.2"));
    ope = (String) _filterModel.getOperatorForText(ope);

    //get value
    value = _value.getText();
    if (value == null || value.length()==0)
      throw new BRuleException(FilterMessages.getString("BRuleEditor.err.3"));
    value = value.trim();

    //special case: case sensitive combo box
    if (entry.getDataType()==DGMAttribute.DT_STRING && entry.isAllowCaseSensitive()){
      if (!_caseBox.isSelected()){
        value = CS_FORM.format(new Object[]{value});
      }
    }
    //check value
    if (ope.equals(BDataAccessors.OPE_InRangeExclusive) || ope.equals(BDataAccessors.OPE_InRangeInclusive)){
      oValue = entry.getValidValuesAsList(value, null);
      if (oValue == null)
        throw new BRuleException(FilterMessages.getString("BRuleEditor.err.4")+" "+
            DGMAttribute.DT_REPR[entry.getDataType()]);
      if (((List<?>)oValue).size()!=2){
        throw new BRuleException(FilterMessages.getString("BRuleEditor.err.5"));
      }
    }
    else{
      oValue = entry.getValidValue(value);
      if (oValue == null)
        throw new BRuleException(FilterMessages.getString("BRuleEditor.err.4")+" "+
            DGMAttribute.DT_REPR[entry.getDataType()]);
    }

    //everything seems ok!
    return (_ruleFactory.createRule(accName, ope, oValue));
  }

  /**
   * Initializes this editor with an existing rule.
   */
  public void setRule(BRule rule){
    if (rule==null)
      return;
    setValues(rule.getAccessor(), rule.getOperator(), rule.getValue());
  }

  /**
   * Initializes this editor with some new values.
   */
  public void setValues(String accVisibleName, String ope, Object value){
    BAccessorEntry entry;
    String         lbl;
    StringBuffer   buf;

    if (accVisibleName==null || ope==null || value==null)
      return;

    entry = _filterModel.getAccessorEntry(accVisibleName);
    if (entry==null)
      return;
    _accessors.setSelectedItem(entry);
    lbl = (String) _filterModel.getTextForOperator(ope);
    if (lbl!=null)
      _operators.setSelectedItem(lbl);
    if (value instanceof Collection == false){//basic type
      lbl = value.toString().trim();
      if (lbl.startsWith("(?i)(")){
        lbl = lbl.substring(5, lbl.length()-1);
        _caseBox.setSelected(false);
      }
      else{
        _caseBox.setSelected(true);
      }
      _value.setText(lbl);
    }
    else{//Collections: List or Set
      buf = new StringBuffer();
      Iterator<?> iter = ((Collection<?>)value).iterator();
      while(iter.hasNext()){
        buf.append(iter.next().toString());
        if (iter.hasNext()){
          buf.append(";");
        }
      }
      _value.setText(buf.toString());
    }
  }

  /**
   * Helper method to create a JTextField with custom settings.
   */
  private JTextField createTextField(){
    JTextField tf;

    tf = new JTextField();
    //tf.setEditable(false);
    //tf.setBorder(null);
    tf.addFocusListener(new HelpFocusListener());
    return tf;
  }
  /**
   * Helper method to fill in the combo box displaying the BAccessors.
   */
  private void fillAccessorsCombo(JComboBox<BAccessorEntry> combo){
    Enumeration<String> myEnum;
    ArrayList<String>   accs;
    int                 i, size;

    accs = new ArrayList<String>();
    myEnum = _filterModel.getAccessorVisibleNames();
    while(myEnum.hasMoreElements()){
      accs.add(myEnum.nextElement());
    }

    Collections.sort(accs);
    size = accs.size();
    for(i=0;i<size;i++){
      combo.addItem(_filterModel.getAccessorEntry(accs.get(i)));
    }
  }

  /**
   * Helper method to fill in a the combo box displaying the valid operators
   * for a particular BAccessorEntry.
   */
  private void fillOperatorCombo(JComboBox<String> combo, BAccessorEntry baEntry){
    String[]    opes;
    String      lbl;
    int         i, size;

    if (combo.getItemCount()!=0)
      combo.removeAllItems();
    if (baEntry==null || baEntry.getOperators()==null || baEntry.getOperators().length==0){
      return;
    }
    opes = baEntry.getOperators();
    size = opes.length;
    for(i=0;i<size;i++){
      lbl = (String) _filterModel.getTextForOperator(opes[i]);
      if (lbl!=null)
        combo.addItem(lbl);
    }

    _caseBox.setVisible(
        baEntry.getDataType()==DGMAttribute.DT_STRING
        &&
        baEntry.isAllowCaseSensitive());
  }

  /**
   * Creates the GUI.
   */
  private void createGUI(JButton add, JButton remove){
    DefaultFormBuilder builder;
    FormLayout         layout;
    JPanel             valPnl;

    //create GUI components
    _accessors = new JComboBox<BAccessorEntry>();
    _accessors.addPopupMenuListener(new ResizableComboboxPopupMenuListener());
    _operators = new JComboBox<String>();
    _accessors.addPopupMenuListener(new ResizableComboboxPopupMenuListener());
    _value = createTextField();
    valPnl = new JPanel(new BorderLayout());
    valPnl.add(_value, BorderLayout.CENTER);
    _caseBox = new JCheckBox(FilterMessages.getString("BRuleEditor.ui.cbox.1"));
    _caseBox.setFont(CBX_FNT);
    valPnl.add(_caseBox, BorderLayout.SOUTH);
    _caseBox.setSelected(true);
    _caseBox.setVisible(false);
    //install listeners
    _accessors.addActionListener(new AccComboSelectionActionListener(_operators));

    //layout the GUI
    layout = new FormLayout("95dlu, 1dlu, 110dlu, 1dlu, 80dlu, 1dlu, 30dlu, 1dlu, 30dlu", "top:pref");
    builder = new DefaultFormBuilder(layout);
    //builder.setDefaultDialogBorder();
    builder.append(_accessors);
    builder.append(_operators);
    builder.append(valPnl);
    if (add!=null)
      builder.append(add);
    if (remove!=null)
      builder.append(remove);

    //sets default values
    fillAccessorsCombo(_accessors);
    _accessors.setSelectedIndex(0);

    this.setLayout(new BorderLayout());
    this.add(builder.getContainer(), BorderLayout.CENTER);
  }

  /**
   * This class handles actions made by a user on the Accessor ComboBox. It
   * is used to display the valid operators of the selected accessor.
   */
  private class AccComboSelectionActionListener implements ActionListener {
    private JComboBox<String> _opeCombo;

    public AccComboSelectionActionListener(JComboBox<String> opeCombo){
      _opeCombo = opeCombo;
    }
    public void actionPerformed(ActionEvent event){
      JComboBox<?>   combo;
      BAccessorEntry entry;
      String         dType;

      combo = (JComboBox<?>) event.getSource();
      entry = (BAccessorEntry) combo.getSelectedItem();

      if (entry==null)
        return;
      fillOperatorCombo(_opeCombo, entry);
      switch(entry.getDataType()){
        case DGMAttribute.DT_DOUBLE:
          dType = FilterMessages.getString("BRuleEditor.tipType.double");
          break;
        case DGMAttribute.DT_LONG:
          dType = FilterMessages.getString("BRuleEditor.tipType.int");
          break;
        case DGMAttribute.DT_BOOLEAN:
          dType = FilterMessages.getString("BRuleEditor.tipType.boolean");
          break;
        case DGMAttribute.DT_CHARACTER:
          dType = FilterMessages.getString("BRuleEditor.tipType.char");
          break;
        case DGMAttribute.DT_STRING:
          dType = FilterMessages.getString("BRuleEditor.tipType.string");
          break;
        case DGMAttribute.DT_DATE:
          dType = FilterMessages.getString("BRuleEditor.tipType.date");
          break;
        default:
          dType = null;
          break;
      }
      if (_helpArea!=null){
        _helpArea.setText(entry.getHelpMsg()!=null?entry.getHelpMsg():"");
      }
      if (dType!=null)
        _value.setToolTipText(dType);
    }
  }
  private class HelpFocusListener implements FocusListener{
    public void focusGained(FocusEvent e){
      BAccessorEntry entry = (BAccessorEntry) _accessors.getSelectedItem();
      String         hlpMsg, ope;

      if (_helpArea==null)
        return;
      if (entry==null){
        _helpArea.setText("");
        return;
      }
      hlpMsg = entry.getHelpMsg()!=null?entry.getHelpMsg():"";
      ope = (String )_operators.getSelectedItem();
      if (ope!=null){
        ope = _filterModel.getOperatorForText(ope);
        if (ope.equals(BDataAccessors.OPE_InRangeExclusive)||
            ope.equals(BDataAccessors.OPE_InRangeInclusive)){
          hlpMsg +=" ";
          hlpMsg +=BDataAccessors.InRangeHlpMsg;
        }
      }
      _helpArea.setText(hlpMsg);
    }
    public void focusLost(FocusEvent e){
      if (_helpArea==null)
        return;
      _helpArea.setText("");
    }
  }
}
