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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import bzh.plealog.bioinfo.api.filter.BDataAccessors;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.BFilterException;
import bzh.plealog.bioinfo.api.filter.BFilterFactory;
import bzh.plealog.bioinfo.api.filter.BRuleFactory;
import bzh.plealog.bioinfo.ui.filter.resources.FilterMessages;

import com.plealog.genericapp.api.EZEnvironment;

/**
 * This is a BFilter editor conveniently wrapped within a JDialog.
 * 
 * @author Patrick G. Durand
 */
public class BFilterEditorDialog extends JDialog implements ActionListener {
  private static final long serialVersionUID = -5714717217384269307L;
  private BFilterEditor    _editor;
  private BFilter          _filter;
  private Component        _parent;
  private JButton          _ok;
  private JButton          _cancel;
  private int              _answer;

  /**
   * Creates a modal BFilterEditorDialog.
   */
  public BFilterEditorDialog(Dialog dlg, String title, BDataAccessors fModel, BFilter filter){
    super(dlg, title, true);
    _parent = dlg;
    buildGUI(fModel, filter, null, null, false);
  }
  /**
   * Creates a modal BFilterEditorDialog.
   */
  public BFilterEditorDialog(Dialog dlg, String title, BDataAccessors fModel, BFilter filter, 
      BFilterFactory filterFactory, BRuleFactory ruleFactory){
    super(dlg, title, true);
    _parent = dlg;
    buildGUI(fModel, filter, filterFactory, ruleFactory, false);
  }
  /**
   * Creates a modal BFilterEditorDialog.
   */
  public BFilterEditorDialog(Frame frame, String title, BDataAccessors fModel, BFilter filter){
    super(frame, title, true);
    _parent = frame;
    buildGUI(fModel, filter, null, null, false);
  }
  /**
   * Creates a modal BFilterEditorDialog.
   */
  public BFilterEditorDialog(Frame frame, String title, BDataAccessors fModel, BFilter filter, 
      BFilterFactory filterFactory, BRuleFactory ruleFactory){
    super(frame, title, true);
    _parent = frame;
    buildGUI(fModel, filter, filterFactory, ruleFactory, false);
  }
  /**
   * Creates a modal BFilterEditorDialog.
   */
  public BFilterEditorDialog(Frame frame, String title, BDataAccessors fModel, BFilter filter, 
      BFilterFactory filterFactory, BRuleFactory ruleFactory, boolean showRulesOnly){
    super(frame, title, true);
    _parent = frame;
    buildGUI(fModel, filter, filterFactory, ruleFactory, showRulesOnly);
  }
  private void buildGUI(BDataAccessors fModel, BFilter filter, BFilterFactory filterFactory, BRuleFactory ruleFactory,
      boolean showRulesOnly){
    JPanel    buttonPanel;
    Container contentPane;
    boolean   macOS = EZEnvironment.getOSType()==EZEnvironment.MAC_OS;

    if (_parent!=null){
      this.setLocationRelativeTo(_parent);
    }
    //buttons
    _ok = new JButton(FilterMessages.getString("BFilterEditorDialog.ui.okBtn"));
    _ok.addActionListener(this);
    _cancel = new JButton(FilterMessages.getString("BFilterEditorDialog.ui.cancelBtn"));
    _cancel.addActionListener(this);
    this.getRootPane().setDefaultButton(_ok);

    //editor
    _editor = new BFilterEditor(fModel, filter, filterFactory, ruleFactory, showRulesOnly);
    _editor.setPreferredSize(new Dimension(780, 350));

    //Lay out the buttons from left to right.
    buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(macOS?_cancel:_ok);
    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPanel.add(macOS?_ok:_cancel);
    if (!macOS)
      buttonPanel.add(Box.createHorizontalGlue());

    //Put everything together
    contentPane = getContentPane();
    contentPane.add(_editor, BorderLayout.CENTER);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);
    this.pack();
    centerOnScreen();
    this.setVisible(true);
  }
  /**
   * Centers the frame on the screen. 
   */
  private void centerOnScreen(){
    Dimension screenSize = this.getToolkit().getScreenSize();
    Dimension dlgSize = this.getSize();

    this.setLocation(screenSize.width/2 - dlgSize.width/2,
        screenSize.height/2 - dlgSize.height/2);
  }

  /**
   * Implemantation of ActionListener interface.
   */
  public void actionPerformed(ActionEvent e) {
    if(_cancel == e.getSource()) {
      _filter = null;
      _answer = JOptionPane.CANCEL_OPTION;
    }
    else {
      try {
        _filter = _editor.getFilter();
      }
      catch (BFilterException e1) {
        JOptionPane.showMessageDialog(
            _parent, 
            e1.getMessage(), 
            FilterMessages.getString("BFilterEditorDialog.err.1"), 
            JOptionPane.ERROR_MESSAGE | JOptionPane.OK_CANCEL_OPTION) ;
        return;
      }
      _answer = JOptionPane.OK_OPTION;
    }
    setVisible(false);
  }

  /**
   * Returns either OK_OPTION or CANCEL_OPTION. These values are defined in
   * JOptionPane.
   */
  public int getAnswer(){
    return _answer;
  }

  /**
   * Returns the filter. May return null.
   */
  public BFilter getFilter(){
    return _filter;
  }
}
