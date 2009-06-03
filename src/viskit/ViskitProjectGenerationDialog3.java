/*
 * ViskitProjectGenerationDialog3.java
 *
 * Created on August 19, 2008, 2:49 PM
 */
package viskit;

import java.awt.Dialog;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author  mike
 */
public class ViskitProjectGenerationDialog3 extends javax.swing.JPanel {

    public static boolean cancelled = true;  // by default
    public static String projectPath = "";
    private static JDialog dialog;

    public static void showDialog() {
        ViskitProjectGenerationDialog3 panel = new ViskitProjectGenerationDialog3();
        dialog = new JDialog((Dialog) null, true);  // modal
        dialog.setTitle("Viskit Project Creation");
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE); //WindowConstants.DO_NOTHING_ON_CLOSE);
        // Dialog will appear in center screen
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /** Creates new form ViskitProjectGenerationDialog3 */
    public ViskitProjectGenerationDialog3() {
        initComponents();

        DocumentListener lis = new myDocListener();
        nameTF.getDocument().addDocumentListener(lis);
        locationTF.getDocument().addDocumentListener(lis);

        nameTF.setText(ViskitProject.DEFAULT_PROJECT_NAME);
        locationTF.setText(ViskitProject.DEFAULT_VISKIT_PROJECTS_DIR);
    }

    class myDocListener implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            common();
        }

        public void insertUpdate(DocumentEvent e) {
            common();
        }

        public void removeUpdate(DocumentEvent e) {
            common();
        }
        StringBuffer sb = new StringBuffer();

        private void common() {
            sb.setLength(0);

            sb.append(locationTF.getText().trim());
            int len;
            while ((len = sb.length()) > 0) {
                if (sb.charAt(len - 1) != '/') {
                    break;
                }
                sb.setLength(len - 1);
            }

            sb.append("/");
            sb.append(nameTF.getText().trim());
            pathTF.setText(sb.toString().replaceAll("\\\\", "/"));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jLabel1 = new javax.swing.JLabel();
    nameTF = new javax.swing.JTextField();
    jLabel2 = new javax.swing.JLabel();
    locationTF = new javax.swing.JTextField();
    browseButt = new javax.swing.JButton();
    jLabel3 = new javax.swing.JLabel();
    pathTF = new javax.swing.JTextField();
    cancelButt = new javax.swing.JButton();
    createButt = new javax.swing.JButton();
    jLabel4 = new javax.swing.JLabel();

    setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setLayout(new java.awt.GridBagLayout());

    jLabel1.setText("Project Name:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
    add(jLabel1, gridBagConstraints);

    nameTF.setColumns(30);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
    add(nameTF, gridBagConstraints);

    jLabel2.setText("Project Location:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
    add(jLabel2, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    add(locationTF, gridBagConstraints);

    browseButt.setText("Browse...");
    browseButt.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        browseButtActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
    add(browseButt, gridBagConstraints);

    jLabel3.setText("Project Path:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
    add(jLabel3, gridBagConstraints);

    pathTF.setColumns(30);
    pathTF.setEditable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
    add(pathTF, gridBagConstraints);

    cancelButt.setText("Cancel");
    cancelButt.setPreferredSize(new java.awt.Dimension(65, 22));
    cancelButt.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 5);
    add(cancelButt, gridBagConstraints);

    createButt.setText("Create");
    createButt.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        createButtActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(10, 5, 0, 0);
    add(createButt, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.weighty = 1.0;
    add(jLabel4, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

private void cancelButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtActionPerformed
    cancelled = true;
    dialog.dispose();
}//GEN-LAST:event_cancelButtActionPerformed

private void createButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtActionPerformed
    cancelled = false;
    projectPath = pathTF.getText().trim().replaceAll("\\\\", "/");
    dialog.dispose();
}//GEN-LAST:event_createButtActionPerformed

JFileChooser chooser;
private void browseButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtActionPerformed
    if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Project Location");
        File f = new File(locationTF.getText().trim());
        if (f.exists() && f.isDirectory()) {
            chooser.setCurrentDirectory(f);
        }
    }

    int ret = chooser.showOpenDialog(this);
    if (ret == JFileChooser.CANCEL_OPTION) {
        return;
    }

    locationTF.setText(chooser.getSelectedFile().getAbsolutePath().replaceAll("\\\\", "/"));
}//GEN-LAST:event_browseButtActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton browseButt;
  private javax.swing.JButton cancelButt;
  private javax.swing.JButton createButt;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JTextField locationTF;
  private javax.swing.JTextField nameTF;
  private javax.swing.JTextField pathTF;
  // End of variables declaration//GEN-END:variables
}
