package viskit;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM)  2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * By:   Mike Bailey
 * Date: June 2, 2004
 * Time: 9:19:41 AM
 */

import viskit.model.EvGraphNode;
import viskit.model.ConstructorArgument;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;


public class EvGraphNodeInspectorDialog extends JDialog
{
  private JLabel handleLab;
  private JLabel typeLab;
  private JTextField handleField;    // Text field that holds the parameter name
  private JTextField typeField;
  private InstantiationPanel ip;
  private Class myClass;
  private static EvGraphNodeInspectorDialog dialog;
  private static boolean modified = false;
  private EvGraphNode egNode;
  private Component locationComp;
  private JButton okButt, canButt;
  private enableApplyButtonListener lis;
  JPanel  buttPan;

  public static String newName, newConstrValue;

  public static boolean showDialog(JFrame f, Component comp, EvGraphNode parm)
  {
    if (dialog == null)
      dialog = new EvGraphNodeInspectorDialog(f, comp, parm);
    else
      dialog.setParams(comp, parm);

    dialog.setVisible(true);
    // above call blocks
    return modified;
  }

  private EvGraphNodeInspectorDialog(JFrame parent, Component comp, EvGraphNode lv)
  {
    super(parent, "Property Change Listener", true);
    this.egNode = lv;
    this.locationComp = comp;
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new myCloseListener());

    lis = new enableApplyButtonListener();

    JPanel content = new JPanel();
    setContentPane(content);
    content.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

    handleField = new JTextField();
    clampHeight(handleField);
    handleField.addCaretListener(lis);
    handleLab = new JLabel("handle",JLabel.TRAILING);
    handleLab.setLabelFor(handleField);

    typeLab = new JLabel("type",JLabel.TRAILING);
    typeField = new JTextField();
    clampHeight(typeField);
    typeField.setEditable(false);
    typeLab.setLabelFor(typeField);

    buttPan = new JPanel();
    buttPan.setLayout(new BoxLayout(buttPan, BoxLayout.X_AXIS));
    canButt = new JButton("Cancel");
    okButt = new JButton("Apply changes");
    buttPan.add(Box.createHorizontalGlue());     // takes up space when dialog is expanded horizontally
    buttPan.add(canButt);
    buttPan.add(okButt);

    fillWidgets();     // put the data into the widgets

    modified = (lv == null ? true : false);     // if it's a new egNode, they can always accept defaults with no typing
    okButt.setEnabled((lv == null ? true : false));

    getRootPane().setDefaultButton(canButt);

    pack();     // do this prior to next
    this.setLocationRelativeTo(locationComp);

    // attach listeners
    canButt.addActionListener(new cancelButtonListener());
    okButt.addActionListener(new applyButtonListener());
  }

  public void setParams(Component c, EvGraphNode p)
  {
    egNode = p;
    locationComp = c;

    fillWidgets();

    modified = (p == null ? true : false);
    okButt.setEnabled((p == null ? true : false));

    getRootPane().setDefaultButton(canButt);
    pack();
    this.setLocationRelativeTo(c);
  }

  private void fillWidgets()
  {
    if (egNode != null) {

      try {
        myClass = Class.forName(egNode.getType());
        Constructor[] cons = myClass.getConstructors();
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace();
      }

      handleField.setText(egNode.getName());
      typeField.setText(egNode.getType());

      ip = new InstantiationPanel(myClass,lis);
      setupIP();
      JPanel content = new JPanel();
      content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
      content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

      JPanel cont = new JPanel(new SpringLayout());
      cont.add(handleLab);
      cont.add(handleField);
      cont.add(typeLab);
      cont.add(typeField);
      SpringUtilities.makeCompactGrid(cont, 2 , 2, 10, 10, 5, 5);
      content.add(cont);

      content.add(ip);
      content.add(Box.createVerticalStrut(5));
      content.add(buttPan);
      setContentPane(content);
    }
    else {
      handleField.setText("egNode name");
      //commentField.setText("comments here");
    }
  }

  private void unloadWidgets()
  {
    String nm = handleField.getText();
    nm = nm.replaceAll("\\s", "");
    if (egNode != null) {
      egNode.setName(nm);
      ArrayList arl = ip.getData();
      egNode.setConstructorArguments(ip.getData());
    }
    else {
      newName = nm;
   //   newConstrValue = constrParmFields[0].getText().trim();
    }
  }

  /**
   * Initialize the InstantiationsPanel with the data from the pclnode
   */
  private void setupIP()
  {
    //if (egNode.getConstructorArguments().isEmpty())
    //  return;           must handle zero-arg constructors
    ArrayList ca = egNode.getConstructorArguments();

    ip.setData(ca);
  }
  class cancelButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent event)
    {
      if(checkBlankFields())
        return;
      modified = false;    // for the caller
      setVisible(false);
    }
  }

  class applyButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent event)
    {
      if(checkBlankFields())
        return;
      if (modified)
        unloadWidgets();
      setVisible(false);
    }
  }

  class enableApplyButtonListener implements CaretListener, ActionListener
  {
    public void caretUpdate(CaretEvent event)
    {
      modified = true;
      okButt.setEnabled(true);
      getRootPane().setDefaultButton(okButt);
    }

    public void actionPerformed(ActionEvent event)
    {
      caretUpdate(null);
    }
  }
  /**
   * Check for blank fields and return true if user wants to cancel close
   * @return true = cancel close
   */
  boolean checkBlankFields()
  {
    ArrayList constr = ip.getData();
    testLp:
    {
      if(handleField.getText().trim().length() <= 0)
        break testLp;
      if(typeField.getText().trim().length() <= 0)
        break testLp;
      if(!constr.isEmpty()) {
        for (Iterator itr = constr.iterator(); itr.hasNext();) {
          ConstructorArgument ca = (ConstructorArgument) itr.next();
          if(ca.getValue().trim().length() <= 0)
            break testLp;
        }
      }

      return false; // no blank fields , don't cancel close
    }   // testLp

    // Here if we found a problem
    int ret = JOptionPane.showConfirmDialog(EvGraphNodeInspectorDialog.this, "All fields must be completed. Close anyway?",
        "Question", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (ret == JOptionPane.YES_OPTION)
      return false;  // don't cancel
    else
      return true;  // cancel close
  }

  class myCloseListener extends WindowAdapter
  {
    public void windowClosing(WindowEvent e)
    {
      if (modified == true) {
        int ret = JOptionPane.showConfirmDialog(EvGraphNodeInspectorDialog.this, "Apply changes?",
            "Question", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ret == JOptionPane.YES_OPTION)
          okButt.doClick();
        else
          canButt.doClick();
      }
      else
        canButt.doClick();
    }
  }

  void clampHeight(JComponent comp)
  {
    Dimension d = comp.getPreferredSize();
    comp.setMaximumSize(new Dimension(Integer.MAX_VALUE,d.height));
    comp.setMinimumSize(new Dimension(Integer.MAX_VALUE,d.height));
  }
}


