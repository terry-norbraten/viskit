package viskit;

import javax.swing.*;
import java.awt.*;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM)  2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * By:   Mike Bailey
 * Date: May 14, 2004
 * Time: 9:44:55 AM
 */

public class LegosPanel extends JPanel
{
  JButton plus,minus;
  public LegosPanel(LegosTree tree)
  {
    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    JLabel lab = new JLabel("Event Graphs");
    lab.setAlignmentX(Box.CENTER_ALIGNMENT);
    add(lab);
    JScrollPane jsp = new JScrollPane(tree);
    jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    add(jsp);
      JPanel buttPan = new JPanel();
      buttPan.setLayout(new BoxLayout(buttPan,BoxLayout.X_AXIS));
      buttPan.add(Box.createHorizontalGlue());
        plus = new JButton(new ImageIcon(ClassLoader.getSystemResource("viskit/images/plus.png")));
        plus.setBorder(null);
        plus.setText(null);
        plus.setToolTipText("Add event graph class file or directory root to this list");
        Dimension dd = plus.getPreferredSize();
        plus.setMinimumSize(dd);
        plus.setMaximumSize(dd);
      buttPan.add(plus);
      buttPan.add(Box.createHorizontalStrut(10));

        minus = new JButton(new ImageIcon(ClassLoader.getSystemResource("viskit/images/minus.png")));
        minus.setDisabledIcon(new ImageIcon(ClassLoader.getSystemResource("viskit/images/minusGrey.png")));
        minus.setBorder(null);
        minus.setText(null);
        minus.setToolTipText("Remove event graph class file or directory from this list");
        dd = minus.getPreferredSize();
        minus.setMinimumSize(dd);
        minus.setMaximumSize(dd);
        minus.setActionCommand("m");
        minus.setEnabled(false);
      buttPan.add(minus);
      buttPan.add(Box.createHorizontalGlue());
    add(buttPan);

  }

}
