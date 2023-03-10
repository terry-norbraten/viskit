package viskit.view.dialog;

import viskit.model.GraphMetaData;

import edu.nps.util.SpringUtilities;
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
 * @author Mike Bailey
 * @since Apr 12, 2004
 * @since 3:52:05 PM
 * @version $Id$
 */
abstract public class MetaDataDialog extends JDialog {

    protected static boolean modified = false;
    protected JComponent runtimePanel;
    private JButton canButt;
    private JButton okButt;
    GraphMetaData param;
    JTextField nameTf, packageTf, authorTf, versionTf, extendsTf, implementsTf;
    JTextField stopTimeTf;
    JCheckBox verboseCb;
    JTextArea descriptionTextArea;

    public MetaDataDialog(JFrame f, GraphMetaData gmd) {
        this(f, gmd, "Event Graph Properties");
    }

    public MetaDataDialog(JFrame f, GraphMetaData gmd, String title) {
        super(f, title, true);
        this.param = gmd;
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new myCloseListener());

        //Create and populate the panel.
        JPanel c = new JPanel();
        setContentPane(c);
        c.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

        JPanel textFieldPanel = new JPanel(new SpringLayout());
        textFieldPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JLabel nmLab = new JLabel("name", JLabel.TRAILING);
        nameTf = new JTextField(20);
        nmLab.setLabelFor(nameTf);
        textFieldPanel.add(nmLab);
        textFieldPanel.add(nameTf);

        JLabel pkgLab = new JLabel("package", JLabel.TRAILING);
        packageTf = new JTextField(20);
        packageTf.setToolTipText("Use standard Java dot notation for package naming");
        pkgLab.setLabelFor(packageTf);
        textFieldPanel.add(pkgLab);
        textFieldPanel.add(packageTf);

        JLabel authLab = new JLabel("author", JLabel.TRAILING);
        authorTf = new JTextField(20);
        authLab.setLabelFor(authorTf);
        textFieldPanel.add(authLab);
        textFieldPanel.add(authorTf);

        JLabel versLab = new JLabel("version", JLabel.TRAILING);
        versionTf = new JTextField(20);
        versLab.setLabelFor(versionTf);
        textFieldPanel.add(versLab);
        textFieldPanel.add(versionTf);

        JLabel extendsLab = new JLabel("extends", JLabel.TRAILING);
        extendsTf = new JTextField(20);
        extendsLab.setLabelFor(extendsTf);
        textFieldPanel.add(extendsLab);
        textFieldPanel.add(extendsTf);

        JLabel implementsLab = new JLabel("implements", JLabel.TRAILING);
        implementsTf = new JTextField(20);
        implementsLab.setLabelFor(implementsTf);
        textFieldPanel.add(implementsLab);
        textFieldPanel.add(implementsTf);

        // Lay out the panel.
        SpringUtilities.makeCompactGrid(textFieldPanel,
                6, 2,   //rows, cols
                0, 0,   //initX, initY
                6, 6);  //xPad, yPad

        Dimension d = textFieldPanel.getPreferredSize();
        textFieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
        c.add(textFieldPanel);

        runtimePanel = new JPanel(new SpringLayout());
        runtimePanel.setBorder(BorderFactory.createTitledBorder("Runtime defaults"));

        JLabel stopTimeLab = new JLabel("stop time", JLabel.TRAILING);
        stopTimeTf = new JTextField(20);
        stopTimeLab.setLabelFor(stopTimeTf);
        runtimePanel.add(stopTimeLab);
        runtimePanel.add(stopTimeTf);

        JLabel verboseLab = new JLabel("verbose output", JLabel.TRAILING);
        verboseCb = new JCheckBox();
        verboseLab.setLabelFor(verboseCb);
        runtimePanel.add(verboseLab);
        runtimePanel.add(verboseCb);

        SpringUtilities.makeCompactGrid(runtimePanel,
                2, 2, 6, 6, 6, 6);
        d = runtimePanel.getPreferredSize();
        runtimePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
        runtimePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        c.add(runtimePanel);

        JLabel descriptionLabel = new JLabel("Description");
        descriptionLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        c.add(descriptionLabel);
        c.add(Box.createVerticalStrut(5));

        descriptionTextArea = new JTextArea(6, 40);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setBorder(BorderFactory.createEmptyBorder());
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionTextArea);
        descriptionScrollPane.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        descriptionScrollPane.setBorder(authorTf.getBorder());

        c.add(descriptionScrollPane);
        c.add(Box.createVerticalStrut(5));

        JPanel buttPan = new JPanel();
        buttPan.setLayout(new BoxLayout(buttPan, BoxLayout.X_AXIS));
        buttPan.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        canButt = new JButton("Cancel");
        okButt = new JButton("Apply changes");
        getRootPane().setDefaultButton(okButt);
        buttPan.add(Box.createHorizontalGlue());
        buttPan.add(canButt);
        buttPan.add(okButt);
        c.add(buttPan);

        // attach listeners
        canButt.addActionListener(new cancelButtonListener());
        okButt.addActionListener(new applyButtonListener());

        setParams(f, gmd);
    }

    public final void setParams(Component c, GraphMetaData gmd) {
        param = gmd;

        fillWidgets();

        modified = (gmd == null);
        pack();
        setLocationRelativeTo(c);
    }

    private void fillWidgets() {
        if (param == null) {
            param = new GraphMetaData();
        }
        nameTf.setText(param.name);
        packageTf.setText(param.packageName);
        authorTf.setText(param.author);
        versionTf.setText(param.version);
        descriptionTextArea.setText(param.description);
        extendsTf.setText(param.extendsPackageName);
        implementsTf.setText(param.implementsPackageName);
        stopTimeTf.setText(param.stopTime);
        verboseCb.setSelected(param.verbose);
        nameTf.selectAll();
    }

    private void unloadWidgets() {
        param.author = authorTf.getText().trim();
        param.description = descriptionTextArea.getText().trim();

        if (this instanceof AssemblyMetaDataDialog) {

            // The default names are AssemblyName, or EventGraphName
            if (!param.name.contains("Assembly") || param.name.equals("AssemblyName"))

                // Note: we need to force "Assembly" in the file name for special recognition
                param.name = nameTf.getText().trim() + "Assembly";
            else
                param.name = nameTf.getText().trim();
        } else {
            param.name = nameTf.getText().trim();
        }
        param.packageName = packageTf.getText().trim();
        param.version = versionTf.getText().trim();
        param.extendsPackageName = extendsTf.getText().trim();
        param.implementsPackageName = implementsTf.getText().trim();
        param.stopTime = stopTimeTf.getText().trim();
        param.verbose = verboseCb.isSelected();
    }

    class cancelButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            modified = false;
            dispose();
        }
    }

    class applyButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            // In this class, if the user hits the apply button, it is assumed that the data has been changed,
            // so the model is marked dirty.  A different, more controlled scheme would be to have change listeners
            // for all the widgets, and only mark dirty if data has been changed.  Else do a string compare between
            // final data and ending data and set modified only if something had actually changed.
            modified = true;
            if (modified) {
                if (nameTf.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(MetaDataDialog.this, "Must have a non-zero length name.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    nameTf.requestFocus();
                    return;
                }

                // OK, we're good....
                unloadWidgets();
            }
            dispose();
        }
    }

    class myCloseListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            if (modified) {
                int ret = JOptionPane.showConfirmDialog(MetaDataDialog.this, "Apply changes?",
                        "Question", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (ret == JOptionPane.YES_OPTION) {
                    okButt.doClick();
                } else {
                    canButt.doClick();
                }
            } else {
                canButt.doClick();
            }
        }
    }
}
