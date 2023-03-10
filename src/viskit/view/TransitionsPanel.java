package viskit.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import viskit.VGlobals;
import viskit.control.EventGraphController;
import viskit.model.EventStateTransition;
import viskit.model.ViskitElement;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey CA
 * www.nps.edu
 * @author Mike Bailey
 * @since Mar 8, 2004
 * @since 2:56:21 PM
 * @version $Id$
 */
public class TransitionsPanel extends JPanel {

    private JList<String> lis;
    private JButton minusButt, plusButt;
    private myListModel model;
    private JButton edButt;

    public TransitionsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        JLabel instructions = new JLabel("Double click a row to ");
        int bigSz = getFont().getSize();
        instructions.setFont(getFont().deriveFont(Font.ITALIC, (float) (bigSz - 2)));
        p.add(instructions);
        edButt = new JButton("edit.");
        edButt.setFont(getFont().deriveFont(Font.ITALIC, (float) (bigSz - 2)));
        edButt.setBorder(null);
        edButt.setEnabled(false);
        p.add(edButt);
        p.add(Box.createHorizontalGlue());
        add(p);

        model = new myListModel();
        model.addElement("1");
        model.addElement("2");
        model.addElement("3");
        lis = new JList<>(model);
        lis.setVisibleRowCount(3);

        JScrollPane jsp = new JScrollPane(lis);
        Dimension dd = jsp.getPreferredSize();
        dd.width = Integer.MAX_VALUE;
        jsp.setMinimumSize(dd);
        add(jsp);

        add(Box.createVerticalStrut(5));

        JPanel buttPan = new JPanel();
        buttPan.setLayout(new BoxLayout(buttPan, BoxLayout.X_AXIS));
        buttPan.add(Box.createHorizontalGlue());
        plusButt = new JButton(new ImageIcon(ClassLoader.getSystemResource("viskit/images/plus.png")));
        plusButt.setBorder(null);
        plusButt.setText(null);
        Dimension d = plusButt.getPreferredSize();
        plusButt.setMinimumSize(d);
        plusButt.setMaximumSize(d);
        buttPan.add(plusButt);
        minusButt = new JButton(new ImageIcon(ClassLoader.getSystemResource("viskit/images/minus.png")));
        minusButt.setDisabledIcon(new ImageIcon(ClassLoader.getSystemResource("viskit/images/minusGrey.png")));
        d = plusButt.getPreferredSize();
        minusButt.setMinimumSize(d);
        minusButt.setMaximumSize(d);
        minusButt.setBorder(null);
        minusButt.setText(null);
        minusButt.setActionCommand("m");
        minusButt.setEnabled(false);
        buttPan.add(minusButt);
        buttPan.add(Box.createHorizontalGlue());
        add(buttPan);

        add(Box.createVerticalStrut(5));

        dd = this.getPreferredSize();
        this.setMinimumSize(dd);

        plusButt.addActionListener(new plusButtonListener());
        minusButt.addActionListener(new minusButtonListener());

        lis.addListSelectionListener(new myListSelectionListener());
        lis.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    if (myMouseLis != null) {
                        int idx = lis.getSelectedIndex();

                        // Don't fail on ArrayIndexOutOfBoundsException
                        if (idx == -1) {
                            return;
                        }
                        ViskitElement est = arLis.get(idx);
                        event.setSource(est);
                        myMouseLis.mouseClicked(event);
                    }
                }
            }
        });
    }

    // TODO: combine with list model.  List has no clone method implemented
    ArrayList<ViskitElement> arLis = new ArrayList<>();

    public void setTransitions(List<? extends ViskitElement> tLis) {
        clearTransitions();
        for (ViskitElement est : tLis) {
            addTransition(est);
        }
    }

    // We know this to be an ArrayList<ViskitElement> clone
    @SuppressWarnings("unchecked")
    public List<ViskitElement> getTransitions() {
        return (List<ViskitElement>) arLis.clone();
    }

    private void addTransition(ViskitElement est) {
        model.addElement(transitionString(est));
        arLis.add(est);
    }

    private String transitionString(ViskitElement est) {
        return est.toString();
    }

    public void clearTransitions() {
        model.removeAllElements();
        arLis.clear();
    }

    /** Used to determine whether to show or hide StateTransitions
     *
     * @return a state transitions string
     */
    public String getString() {
        String s = "";
        for (Enumeration en = model.elements(); en.hasMoreElements();) {
            s += (String) en.nextElement();
            s += "\n";
        }

        // lose last cr
        if (s.length() > 0) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
    private ActionListener myPlusListener;

    public void addPlusListener(ActionListener al) {
        myPlusListener = al;
    }
    private ActionListener myMinusListener;

    public void addMinusListener(ActionListener al) {
        myMinusListener = al;
    }

    public void addDoubleClickedListener(MouseListener ml) {
        myMouseLis = ml;
    }
    private MouseListener myMouseLis;

    public void updateTransition(EventStateTransition est) {
        int idx = arLis.indexOf(est);
        model.setElementAt(transitionString(est), idx);
    }

    class myListModel extends DefaultListModel<String> {

        myListModel() {
            super();
        }
    }

    class plusButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (VGlobals.instance().getStateVarsCBModel().getSize() <= 0) {
                ((EventGraphController)VGlobals.instance().getEventGraphController()).messageUser(
                    JOptionPane.ERROR_MESSAGE,
                    "Alert",
                    "No state variables have been defined," +
                        "\ntherefore no state transitions are possible.");
                return;
            }
            model.addElement("double click to edit");
            lis.setVisibleRowCount(Math.max(model.getSize(), 3));
            lis.ensureIndexIsVisible(model.getSize() - 1);
            lis.setSelectedIndex(model.getSize() - 1);
            TransitionsPanel.this.invalidate();
            minusButt.setEnabled(true);
            EventStateTransition est = new EventStateTransition();
            arLis.add(est);
            if (myPlusListener != null) {
                myPlusListener.actionPerformed(event);
            }

            // This does an edit immediately, and doesn't require a separate double click
            if (myMouseLis != null) {
                MouseEvent me = new MouseEvent(plusButt, 0, 0, 0, 0, 0, 2, false);   // plusButt temporarily used
                me.setSource(est);
                myMouseLis.mouseClicked(me);

                // If they cancelled, kill it
                String result = model.get(model.getSize() - 1);
                if ("double click to edit".equals(result) || result.isEmpty()) {  // remove it
                    ActionEvent ae = new ActionEvent(minusButt, 0, "delete");  // dummy
                    minusButt.getActionListeners()[0].actionPerformed(ae);
                }
            }
        }
    }

    class minusButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            if (lis.getSelectionModel().getValueIsAdjusting()) {
                return;
            }

            int reti = JOptionPane.showConfirmDialog(TransitionsPanel.this, "Are you sure?", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (reti != JOptionPane.YES_OPTION) {
                return;
            }

            int[] sel = lis.getSelectedIndices();
            if (sel.length != 0) {
                for (int i = 0; i < sel.length; i++) {
                    model.remove(sel[i] - i);
                    arLis.remove(sel[i] - i);
                }
            }
            if (lis.getModel().getSize() <= 0) {
                minusButt.setEnabled(false);
            }
            lis.setVisibleRowCount(Math.max(3, model.getSize()));
            TransitionsPanel.this.invalidate();

            if (myMinusListener != null) {
                myMinusListener.actionPerformed(event);
            }
        }
    }

    class myListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            if (lis.getModel().getSize() != 0) {
                boolean b = lis.getSelectedValue() != null;
                minusButt.setEnabled(b);
            }
        }
    }
}
