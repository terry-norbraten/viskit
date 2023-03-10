package viskit.view.dialog;

import viskit.model.vParameter;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import viskit.VGlobals;

/**
 * A dialog class that lets the user add a new parameter to the document.
 * After the user clicks "OK", "Cancel", or closes the dialog via the
 * close box, the caller retrieves the "buttonChosen" variable from
 * the object to determine the choice. If the user clicked "OK", the
 * caller can retrieve various choices from the object.
 *
 * @author DMcG
 */
public class ParameterDialog extends JDialog {

    private static ParameterDialog dialog;
    private static boolean modified = false;
    public static String newName,  newType,  newComment;
    private static int count = 0;

    private JTextField parameterNameField;    // Text field that holds the parameter name
    private JTextField expressionField;       // Text field that holds the expression
    private JTextField commentField;          // Text field that holds the comment
    private JComboBox parameterTypeCombo;    // Editable combo box that lets us select a type
    private vParameter param;
    private JButton okButt, canButt;

    public static boolean showDialog(JFrame f, vParameter parm) {
        if (dialog == null) {
            dialog = new ParameterDialog(f, parm);
        } else {
            dialog.setParams(f, parm);
        }

        dialog.setVisible(true);
        // above call blocks

        return modified;
    }

    private ParameterDialog(JFrame parent, vParameter param) {
        super(parent, "Parameter Inspector", true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new myCloseListener());

        Container cont = getContentPane();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));

        JPanel con = new JPanel();
        con.setLayout(new BoxLayout(con, BoxLayout.Y_AXIS));
        con.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),
                new EmptyBorder(10, 10, 10, 10)));

        con.add(Box.createVerticalStrut(5));
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));

        JLabel nameLab = new JLabel("name");
        JLabel initLab = new JLabel("initial value");
        JLabel typeLab = new JLabel("type");
        JLabel commLab = new JLabel("description");
        int w = OneLinePanel.maxWidth(new JComponent[]{nameLab, initLab, typeLab, commLab});

        parameterNameField = new JTextField(15);
        setMaxHeight(parameterNameField);
        expressionField = new JTextField(25);
        setMaxHeight(expressionField);
        commentField = new JTextField(25);
        setMaxHeight(commentField);

        parameterTypeCombo = VGlobals.instance().getTypeCB();
        setMaxHeight(parameterTypeCombo);

        fieldsPanel.add(new OneLinePanel(nameLab, w, parameterNameField));
        fieldsPanel.add(new OneLinePanel(typeLab, w, parameterTypeCombo));
        fieldsPanel.add(new OneLinePanel(commLab, w, commentField));
        con.add(fieldsPanel);
        con.add(Box.createVerticalStrut(5));

        JPanel buttPan = new JPanel();
        buttPan.setLayout(new BoxLayout(buttPan, BoxLayout.X_AXIS));
        canButt = new JButton("Cancel");
        okButt = new JButton("Apply changes");
        buttPan.add(Box.createHorizontalGlue());     // takes up space when dialog is expanded horizontally
        buttPan.add(canButt);
        buttPan.add(okButt);
        con.add(buttPan);
        con.add(Box.createVerticalGlue());    // takes up space when dialog is expanded vertically
        cont.add(con);

        // attach listeners
        canButt.addActionListener(new cancelButtonListener());
        okButt.addActionListener(new applyButtonListener());

        enableApplyButtonListener lis = new enableApplyButtonListener();
        parameterNameField.addCaretListener(lis);
        commentField.addCaretListener(lis);
        expressionField.addCaretListener(lis);
        parameterTypeCombo.addActionListener(lis);

        setParams(parent, param);
    }

    private void setMaxHeight(JComponent c) {
        Dimension d = c.getPreferredSize();
        d.width = Integer.MAX_VALUE;
        c.setMaximumSize(d);
    }

    public final void setParams(Component c, vParameter p) {
        param = p;

        fillWidgets();

        okButt.setEnabled((p == null));

        getRootPane().setDefaultButton(canButt);
        pack();
        setLocationRelativeTo(c);
    }

    private void fillWidgets() {
        if (param != null) {
            parameterNameField.setText(param.getName());
            parameterTypeCombo.setSelectedItem(param.getType());
            commentField.setText(param.getComment());
        } else {
            parameterNameField.setText("param_" + count++);
            commentField.setText("");
        }
    }

    private void unloadWidgets() {
        String ty = (String) parameterTypeCombo.getSelectedItem();
        ty = VGlobals.instance().typeChosen(ty);
        String nm = parameterNameField.getText();
        nm = nm.replaceAll("\\s", "");
        if (param != null) {
            param.setName(nm);
            //
            if (ty.equals("String") || ty.equals("Double") || ty.equals("Integer")) {
                ty = "java.lang." + ty;
            }
            param.setType(ty);
            param.setComment(commentField.getText());
        } else {
            newName = nm;
            newType = ty;
            newComment = commentField.getText().trim();
        }
    }

    class cancelButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            modified = false;    // for the caller
            dispose();
        }
    }

    class applyButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (modified) {
                unloadWidgets();
            }
            dispose();
        }
    }

    class enableApplyButtonListener implements CaretListener, ActionListener {

        @Override
        public void caretUpdate(CaretEvent event) {
            modified = true;
            okButt.setEnabled(true);
            getRootPane().setDefaultButton(okButt);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            caretUpdate(null);
        }
    }

    class myCloseListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            if (modified) {
                int ret = JOptionPane.showConfirmDialog(ParameterDialog.this, "Apply changes?",
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
