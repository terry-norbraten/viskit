/*
 Copyright (c) 1995-2009 held by the author(s).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the
      distribution.
    * Neither the names of the Naval Postgraduate School (NPS)
      Modeling Virtual Environments and Simulation (MOVES) Institute
      (http://www.nps.edu and http://www.movesinstitute.org)
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
 */
package viskit;

import java.awt.Dialog;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import viskit.mvc.mvcController;

/**
 * Edit this GUI with Netbeans Matisse
 *
 * @author Mike Bailey
 * @since Aug 2008
 * @version $Id: $
 */
public class ViskitProjectButtonPanel extends javax.swing.JPanel {

    private static JDialog dialog;

    public static void showDialog() {
        ViskitProjectButtonPanel panel = new ViskitProjectButtonPanel();
        dialog = new JDialog((Dialog) null, true);  // modal
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Dialog will appear in center screen
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /** Creates new form ViskitProjectButtonPanel */
    public ViskitProjectButtonPanel() {
        initComponents();
    }

    private void setupViskitProject(File projFile) {
        if (projFile == null) return;
        ViskitProject.MY_VISKIT_PROJECTS_DIR = projFile.getParent().replaceAll("\\\\", "/");
        ViskitConfig.instance().setVal(ViskitConfig.PROJECT_PATH_KEY, ViskitProject.MY_VISKIT_PROJECTS_DIR);
        ViskitProject.DEFAULT_PROJECT_NAME = projFile.getName();
        ViskitConfig.instance().setVal(ViskitConfig.PROJECT_NAME_KEY, ViskitProject.DEFAULT_PROJECT_NAME);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    defaultButt = new javax.swing.JButton();
    existingButt = new javax.swing.JButton();
    createButt = new javax.swing.JButton();
    exitButt = new javax.swing.JButton();

    defaultButt.setText("Open default project");
    defaultButt.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        defaultButtActionPerformed(evt);
      }
    });

    existingButt.setText("Open existing project");
    existingButt.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        existingButtActionPerformed(evt);
      }
    });

    createButt.setText("Create new project");
    createButt.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        createButtActionPerformed(evt);
      }
    });

    exitButt.setText("Exit Viskit");
    exitButt.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exitButtActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(existingButt)
          .addComponent(createButt)
          .addComponent(exitButt))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(existingButt)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(createButt)
        .addGap(18, 18, 18)
        .addComponent(exitButt)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

    static boolean firstTime = true;
private void existingButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingButtActionPerformed
    File file = null;
    if (!firstTime) {
        mvcController vac = VGlobals.instance().getAssemblyController();
        if (vac != null) {

            AssemblyView vaw = (AssemblyView) vac.getView();

            if (vaw != null) {
                vaw.openProject();
                return;
            }
        }
    } else {
        file = ViskitProject.openProjectDir(null, ViskitProject.MY_VISKIT_PROJECTS_DIR);
        firstTime = !firstTime;
    }

    defaultButtActionPerformed(null);
    setupViskitProject(file);
}//GEN-LAST:event_existingButtActionPerformed

private void defaultButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultButtActionPerformed
    // TODO setup differently, but for now...
    dialog.dispose();
    dialog = null;
}//GEN-LAST:event_defaultButtActionPerformed

private void createButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtActionPerformed
    File projF;
    do {
        ViskitProjectGenerationDialog3.showDialog();
        if (ViskitProjectGenerationDialog3.cancelled) {
            return;
        }
        String projPath = ViskitProjectGenerationDialog3.projectPath;
        projF = new File(projPath);
        if (projF.exists() && (projF.isFile() || projF.list().length > 0)) {
            JOptionPane.showMessageDialog(this, "Chosen project name exists.");
        } else {
            break; // out of do
        }
    } while (true);

    // Since this dialog is modal, need to dispose() before we can move along in the startup
    defaultButtActionPerformed(null);
    setupViskitProject(projF);

    // The work directory will have already been created by default as VGlobals.init
    // was already called by the ${user.home}/.viskit which creates the directory
    // during constructor init
}//GEN-LAST:event_createButtActionPerformed

private void exitButtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtActionPerformed
    defaultButtActionPerformed(null);

    // I don't like the idea of a SysExit call right here, but the way each
    // frame component needs to develop while starting Viskit; each has to
    // finish before the VGlobals.instance().sysExit(0) call will work
    // properly, so, reluctantly...
    System.exit(0);
}//GEN-LAST:event_exitButtActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton createButt;
  private javax.swing.JButton defaultButt;
  private javax.swing.JButton existingButt;
  private javax.swing.JButton exitButt;
  // End of variables declaration//GEN-END:variables
}
