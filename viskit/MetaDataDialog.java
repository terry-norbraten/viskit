package viskit;

import viskit.model.GraphMetaData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM)  2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * By:   Mike Bailey
 * Date: Apr 12, 2004
 * Time: 3:52:05 PM
 */

abstract public class MetaDataDialog extends JDialog
{
  protected static MetaDataDialog dialog;
  protected static boolean modified = false;

  protected JComponent runtimePanel;
  private JButton canButt;
  private JButton okButt;

  JFrame parentFrame;
  Component locationComp;
  GraphMetaData param;
  JTextField nameTf, packageTf, authorTf, versionTf, extendTf;
  JTextField stopTimeTf;
  JCheckBox verboseCb;
  JTextArea commentTa;

  public MetaDataDialog(JFrame f, Component comp, GraphMetaData gmd)
  {
    this(f,comp,gmd,"Event Graph Properties");
  }

  public MetaDataDialog(JFrame f, Component comp, GraphMetaData gmd, String title)
  {
    super(f,title, true);
    this.param = gmd;
    this.locationComp = comp;
    this.parentFrame = f;
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new myCloseListener());

    //Create and populate the panel.
    Container c = getContentPane();
    c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));

    JPanel textFieldPanel = new JPanel(new SpringLayout());

    JLabel nmLab = new JLabel("name",JLabel.TRAILING);
    nameTf = new JTextField(20);
    nmLab.setLabelFor(nameTf);
    textFieldPanel.add(nmLab);
    textFieldPanel.add(nameTf);

    JLabel pkgLab = new JLabel("package",JLabel.TRAILING);
    packageTf = new JTextField(20);
    pkgLab.setLabelFor(packageTf);
    textFieldPanel.add(pkgLab);
    textFieldPanel.add(packageTf);

    JLabel authLab = new JLabel("author",JLabel.TRAILING);
    authorTf = new JTextField(20);
    authLab.setLabelFor(authorTf);
    textFieldPanel.add(authLab);
    textFieldPanel.add(authorTf);

    JLabel versLab = new JLabel("version",JLabel.TRAILING);
    versionTf = new JTextField(20);
    versLab.setLabelFor(versionTf);
    textFieldPanel.add(versLab);
    textFieldPanel.add(versionTf);

    JLabel extendLab = new JLabel("extends",JLabel.TRAILING);
    extendTf = new JTextField(20);
    extendLab.setLabelFor(extendTf);
    textFieldPanel.add(extendLab);
    textFieldPanel.add(extendTf);

    //Lay out the panel.
    SpringUtilities.makeCompactGrid(textFieldPanel,
                                    5, 2, //rows, cols
                                    6, 6,        //initX, initY
                                    6, 6);       //xPad, yPad

    Dimension d = textFieldPanel.getPreferredSize();
    textFieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,d.height));
    c.add(textFieldPanel);

    runtimePanel = new JPanel(new SpringLayout());
    runtimePanel.setBorder(BorderFactory.createTitledBorder("Runtime defaults"));

      JLabel stopTimeLab = new JLabel("stop time",JLabel.TRAILING);
      stopTimeTf = new JTextField(20);
      stopTimeLab.setLabelFor(stopTimeTf);
      runtimePanel.add(stopTimeLab);
      runtimePanel.add(stopTimeTf);

      JLabel verboseLab = new JLabel("verbose output",JLabel.TRAILING);
      verboseCb = new JCheckBox();
      verboseLab.setLabelFor(verboseCb);
      runtimePanel.add(verboseLab);
      runtimePanel.add(verboseCb);

      SpringUtilities.makeCompactGrid(runtimePanel,
                                      2,2, 6,6, 6,6 );
      d = runtimePanel.getPreferredSize();
      runtimePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,d.height));
    c.add(runtimePanel);

    commentTa = new JTextArea(6,40);
    commentTa.setWrapStyleWord(true);
    commentTa.setLineWrap(true);
    commentTa.setBorder(authorTf.getBorder());
    JScrollPane csp = new JScrollPane(commentTa);
    csp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5,5,5,5),
                  "Comment"));
    c.add(csp);

    JPanel buttPan = new JPanel();
    buttPan.setLayout(new BoxLayout(buttPan, BoxLayout.X_AXIS));
    canButt = new JButton("Cancel");
    okButt = new JButton("Apply changes");
    buttPan.add(Box.createHorizontalGlue());
    buttPan.add(canButt);
    buttPan.add(okButt);
    buttPan.add(Box.createHorizontalStrut(5));
    c.add(buttPan);
    c.add(Box.createVerticalStrut(5));
    fillWidgets();

    pack();     // do this prior to next
    this.setLocationRelativeTo(locationComp);

    // attach listeners
    canButt.addActionListener(new cancelButtonListener());
    okButt.addActionListener(new applyButtonListener());

  }
  public void setParams(Component c, GraphMetaData gmd)
  {
    param = gmd;
    locationComp = c;

    fillWidgets();

    modified        = (gmd==null?true:false);
 //   okButt.setEnabled((gmd==null?true:false));

//    getRootPane().setDefaultButton(canButt);

    this.setLocationRelativeTo(c);
  }

  private void fillWidgets()
  {
    if(param == null)
      param = new GraphMetaData();
    nameTf.setText(param.name);
    packageTf.setText(param.pkg);
    authorTf.setText(param.author);
    versionTf.setText(param.version);
    commentTa.setText(param.comment);
    extendTf.setText(param.extend);
    stopTimeTf.setText(param.stopTime);
    verboseCb.setSelected(param.verbose);
    nameTf.selectAll();
  }
  private void unloadWidgets()
  {
    param.author = authorTf.getText().trim();
    param.comment = commentTa.getText().trim();
    param.name = nameTf.getText().trim();
    param.pkg = packageTf.getText().trim();
    param.version = versionTf.getText().trim();
    param.extend = extendTf.getText().trim();
    param.stopTime = stopTimeTf.getText().trim();
    param.verbose = verboseCb.isSelected();
  }
  class cancelButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent event)
    {
      modified = false;
      setVisible(false);
    }
  }

  class applyButtonListener implements ActionListener
  {
    public void actionPerformed(ActionEvent event)
    {
      // test
      modified = true;
      if (modified) {
        if (nameTf.getText().trim().length() <= 0) {
          JOptionPane.showMessageDialog(MetaDataDialog.this, "An event graph must have a non-zero length name.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
          nameTf.requestFocus();
          return;
        }

        // OK, we're good....
        unloadWidgets();
      }
      setVisible(false);
    }
  }

  class myCloseListener extends WindowAdapter
  {
    public void windowClosing(WindowEvent e)
    {
      if(modified == true) {
        int ret = JOptionPane.showConfirmDialog(MetaDataDialog.this,"Apply changes?",
            "Question",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(ret == JOptionPane.YES_OPTION)
          okButt.doClick();
        else
          canButt.doClick();
        }
      else
        canButt.doClick();
    }
  }

}
