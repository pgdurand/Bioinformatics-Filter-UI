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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import bzh.plealog.bioinfo.api.filter.BDataAccessors;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.BFilterException;
import bzh.plealog.bioinfo.api.filter.BFilterFactory;
import bzh.plealog.bioinfo.api.filter.BRule;
import bzh.plealog.bioinfo.api.filter.BRuleException;
import bzh.plealog.bioinfo.api.filter.BRuleFactory;
import bzh.plealog.bioinfo.api.filter.config.FilterSystemConfigurator;
import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.plealog.genericapp.api.EZEnvironment;

/**
 * This is a BFilter editor.
 * 
 * @author Patrick G. Durand
 */
public class BFilterEditor extends JPanel {
  private static final long serialVersionUID = -1828188339669036903L;
  private JTextField       _filterName;
  private JTextField       _filterDescription;
  private JComboBox<ExpressionTypeItem>        _exprType;
  private JPanel           _rulesPanel;
  private JTextArea        _helpArea;
  private ArrayList<BRulerEditorContainer>        _rulesContainer;
  private BDataAccessors   _filterModel;
  private BFilterFactory   _filterFactory;
  private BRuleFactory     _ruleFactory;
  private boolean          _showRulesOnly;

  private ExpressionTypeItem[] _exprItems = new ExpressionTypeItem[]{
      new ExpressionTypeItem("any", false),
      new ExpressionTypeItem("all", true)
  };

  private static final MessageFormat INVALID_RULE_FORMATTER  = new MessageFormat(
      FilterMessages.getString("BFilterEditor.err.2"));

  private static final int ANY_EXPR_ITEM = 0;
  private static final int ALL_EXPR_ITEM = 1;

  public BFilterEditor(BDataAccessors fModel){
    this(fModel, null);
  }

  public BFilterEditor(BDataAccessors fModel, BFilter filter){
    this(fModel, filter, null, null, false);
  }

  public BFilterEditor(BDataAccessors fModel, BFilter filter, BFilterFactory filterFactory, BRuleFactory ruleFactory){
    this(fModel, filter, filterFactory, ruleFactory, false);
  }
  public BFilterEditor(BDataAccessors fModel, BFilter filter, BFilterFactory filterFactory, BRuleFactory ruleFactory,
      boolean showRulesOnly){
    _filterModel = fModel;
    _rulesContainer = new ArrayList<BRulerEditorContainer>();
    _filterFactory = filterFactory==null?FilterSystemConfigurator.getFilterFactory():filterFactory;
    _ruleFactory = ruleFactory==null?FilterSystemConfigurator.getRuleFactory():ruleFactory;
    _showRulesOnly = showRulesOnly;
    createGUI();
    if (filter==null){
      addBRuleEditor();
    }
    else{
      setFilter(filter);
      _helpArea.setText("");
    }
  }
  /**
   * Returns a valid BFilter given the parameters entered by the users.
   */
  public BFilter getFilter() throws BFilterException{
    ExpressionTypeItem eti;
    BFilter            filter;
    String             fName, dName;
    int                i, size;

    if (!_showRulesOnly){
      fName = _filterName.getText();
      if (fName==null || fName.length()<1)
        throw new BFilterException(FilterMessages.getString("BFilterEditor.err.1")+".");
    }
    else{
      fName="No name";
    }

    filter = _filterFactory.createFilter(_filterModel, fName);

    if (!_showRulesOnly){
      dName = _filterDescription.getText();
      if (dName!=null && dName.length()>1)
        filter.setDescription(dName);
    }
    else{
      dName="No description";
    }
    size = _rulesContainer.size();
    i=0;
    try {
      for(;i<size;i++){
        filter.add(((BRulerEditorContainer)_rulesContainer.get(i)).getRule());
      }
    } catch (BRuleException e) {
      Object[] args = {new Integer(i+1)};
      throw new BFilterException(
          INVALID_RULE_FORMATTER.format(args)+": "+e.getMessage());
    }
    eti = (ExpressionTypeItem) _exprType.getSelectedItem();
    filter.setExclusive(eti.isExclusive());
    return filter;
  }

  /**
   * Initializes this editor with an existing filter.
   */
  public void setFilter(BFilter filter){
    if (filter!=null){
      setFilter(filter, true);
    }
    else{
      reset();
    }
  }

  /**
   * Adds to this editor the content of an existing filter.
   */
  public void addFilter(BFilter filter){
    setFilter(filter, false);
  }

  private void setFilter(BFilter filter, boolean reset){
    Iterator<BRule> iter;

    if (filter==null)
      return;
    if (reset)
      clear();
    iter = filter.getRules();
    while(iter.hasNext()){
      addBRuleEditor((BRule) iter.next());
    }
    if (!_showRulesOnly){
      _filterName.setText(filter.getName());
      _filterDescription.setText(filter.getDescription());
    }
    if (filter.getExclusive())
      _exprType.setSelectedIndex(ALL_EXPR_ITEM);
    else
      _exprType.setSelectedIndex(ANY_EXPR_ITEM);
  }

  private void clear(){
    BRulerEditorContainer rEditor;
    int                   i, size;

    size = _rulesContainer.size();
    for(i=0;i<size; i++){
      rEditor = (BRulerEditorContainer) _rulesContainer.get(i);
      _rulesPanel.remove(rEditor);
    }
    _rulesContainer.clear();
    _rulesPanel.updateUI();
    if (!_showRulesOnly){
      _filterName.setText("");
      _filterDescription.setText("");
    }
    _helpArea.setText("");
  }

  /**
   * Reset this filter editor.
   */
  public void reset(){
    clear();
    addBRuleEditor();
  }

  /**
   * Helper method to create a JTextField with custom settings.
   */
  private JTextField createTextField(){
    JTextField tf;

    tf = new JTextField();
    //tf.setEditable(false);
    //tf.setBorder(null);
    return tf;
  }
  private JTextArea createHelper(){
    _helpArea = new JTextArea();
    _helpArea.setRows(2);
    _helpArea.setLineWrap(true);
    _helpArea.setWrapStyleWord(true);
    _helpArea.setEditable(false);
    _helpArea.setOpaque(false);
    _helpArea.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
    //_helpArea.setForeground(DDResources.getSystemTextColor());
    return _helpArea;
  }

  private void createGUI(){
    DefaultFormBuilder builder;
    FormLayout         layout;
    JScrollPane        scroller;
    JPanel             top, expr, exprMain, rulesContainer;

    layout = new FormLayout("right:max(50dlu;p), 4dlu, 150dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    if (!_showRulesOnly){
      _filterName = createTextField();
      _filterDescription = createTextField();
      builder.appendSeparator(FilterMessages.getString("BFilterEditor.ui.lbl.1"));
      builder.append(new JLabel(FilterMessages.getString("BFilterEditor.ui.lbl.2")+": "),
          _filterName);
      builder.nextLine();
      builder.append(new JLabel(FilterMessages.getString("BFilterEditor.ui.lbl.3")+": "),
          _filterDescription);
      builder.nextLine();
    }

    _rulesPanel = new JPanel();

    _rulesPanel.setLayout(new BoxLayout(_rulesPanel, BoxLayout.Y_AXIS));

    expr = new JPanel();
    expr.add(new JLabel(FilterMessages.getString("BFilterEditor.ui.lbl.4")));
    _exprType = new JComboBox<ExpressionTypeItem>();
    _exprType.addItem(_exprItems[ANY_EXPR_ITEM]);//or
    _exprType.addItem(_exprItems[ALL_EXPR_ITEM]);//and
    _exprType.setSelectedIndex(ALL_EXPR_ITEM);
    expr.add(_exprType);
    expr.add(new JLabel(FilterMessages.getString("BFilterEditor.ui.lbl.5")+":"));
    exprMain = new JPanel(new BorderLayout());
    exprMain.add(expr, BorderLayout.WEST);
    rulesContainer = new JPanel(new BorderLayout());
    rulesContainer.add(_rulesPanel, BorderLayout.NORTH);
    scroller = new JScrollPane(rulesContainer);
    this.setLayout(new BorderLayout());
    top = new JPanel(new BorderLayout());
    if (!_showRulesOnly){
      top.add(builder.getContainer(), BorderLayout.CENTER);
    }
    top.add(exprMain, BorderLayout.SOUTH);
    this.add(top, BorderLayout.NORTH);
    this.add(scroller, BorderLayout.CENTER);
    this.add(createHelper(), BorderLayout.SOUTH);
  }

  private void updateRuleEditor(){
    BRulerEditorContainer rEditor;
    int                   i, size;

    size = _rulesContainer.size();
    for (i=0;i<size;i++){
      rEditor = (BRulerEditorContainer) _rulesContainer.get(i);
      rEditor.updateState(i, size);
    }
  }
  private void addBRuleEditor(BRule rule){
    BRulerEditorContainer rEditor;

    rEditor = new BRulerEditorContainer(_rulesContainer.size(), _rulesContainer.size()+1);
    rEditor.setRule(rule);
    _rulesContainer.add(rEditor);
    updateRuleEditor();
    _rulesPanel.add(rEditor);
    _rulesPanel.updateUI();


  }

  private void addBRuleEditor(){
    BRulerEditorContainer rEditor;

    rEditor = new BRulerEditorContainer(_rulesContainer.size(), _rulesContainer.size()+1);
    _rulesContainer.add(rEditor);
    updateRuleEditor();
    _rulesPanel.add(rEditor);
    _rulesPanel.updateUI();
  }

  private void removeBRuleEditor(int rankOfEmitter){
    BRulerEditorContainer rEditor;

    if (_rulesContainer.size()==1)
      return;
    rEditor = (BRulerEditorContainer) _rulesContainer.get(rankOfEmitter);
    _rulesContainer.remove(rEditor);
    _rulesPanel.remove(rEditor);
    _rulesPanel.updateUI();
    updateRuleEditor();
  }

  private class ExpressionTypeItem {
    private String  _label;
    private boolean _isExclusive;

    public ExpressionTypeItem(String lbl, boolean exclusive){
      _label = lbl;
      _isExclusive = exclusive;
    }
    protected boolean isExclusive(){
      return _isExclusive;
    }
    public String toString(){
      return _label;
    }
  }
  private class BRulerEditorContainer extends JPanel{
    private static final long serialVersionUID = 6048362952182894464L;
    private int         _rank;
    private BRuleEditor _rEditor;
    private JButton     _addRule;
    private JButton     _removeRule;

    public BRulerEditorContainer(int rank, int nbRules){
      createGUI();
      updateState(rank, nbRules);
    }

    public void updateState(int rank, int nbRules){
      _rank = rank;
      if (_rank>0){
        _removeRule.setVisible(true);
        nbRules--;
        if (rank==nbRules){
          _addRule.setVisible(true);
        }
        else{
          _addRule.setVisible(false);
        }
      }
      else{
        if (nbRules!=1){
          _addRule.setVisible(false);
          _removeRule.setVisible(true);
        }
        else{
          _addRule.setVisible(true);
          _removeRule.setVisible(false);
        }
      }
    }

    public BRule getRule() throws BRuleException{
      return _rEditor.getRule();
    }

    public void setRule(BRule rule){
      _rEditor.setRule(rule);
    }

    private void createGUI(){
      JPanel  pnl, btnPanel;
      ImageIcon icon;

      btnPanel = new JPanel(new BorderLayout());
      icon = EZEnvironment.getImageIcon("add.png");
      if (icon==null)
        _addRule = new JButton("+");
      else
        _addRule = new JButton(icon);
      _addRule.addActionListener(new AddBtnActionListener());
      icon = EZEnvironment.getImageIcon("remove.png");
      if (icon==null)
        _removeRule = new JButton("-");
      else
        _removeRule = new JButton(icon);
      _removeRule.addActionListener(new RemoveBtnActionListener());
      btnPanel.add(_removeRule, BorderLayout.EAST);
      btnPanel.add(_addRule, BorderLayout.WEST);

      _rEditor = new BRuleEditor(_filterModel, _ruleFactory, _addRule, _removeRule);
      _rEditor.setHelperArea(_helpArea);
      pnl = new JPanel();
      pnl.setLayout(new BorderLayout());
      pnl.add(_rEditor, BorderLayout.CENTER);
      pnl.add(btnPanel, BorderLayout.EAST);
      this.add(pnl);
    }

    private class AddBtnActionListener implements ActionListener{
      public void actionPerformed(ActionEvent event){
        addBRuleEditor();
      }
    }

    private class RemoveBtnActionListener implements ActionListener{
      public void actionPerformed(ActionEvent event){
        removeBRuleEditor(_rank);
      }
    }
  }
}
