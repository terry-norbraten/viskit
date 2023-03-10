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
package viskit.view.dialog;

import edu.nps.util.LogUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.configuration.XMLConfiguration;
import viskit.control.EventGraphController;
import viskit.VGlobals;
import viskit.ViskitConfig;
import viskit.ViskitProject;
import viskit.VStatics;
import viskit.control.AssemblyController;

/**
 * <p>MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu</p>
 * @author Mike Bailey
 * @since Nov 2, 2005
 * @since 11:24:06 AM
 * @version $Id$
 */
public class SettingsDialog extends JDialog {

    private static SettingsDialog dialog;
    private static boolean modified = false;
    private JButton canButt;
    private JButton okButt;
    private JTabbedPane tabbedPane;
    private JList<String> classPathJlist;
    private JCheckBox evGrCB;
    private JCheckBox assyCB;
    private JCheckBox runCB;
    private JCheckBox doeCB;
    private JCheckBox clusterRunCB;
    private JCheckBox analystReportCB;
    private JCheckBox debugMsgsCB;

    private JRadioButton defaultLafRB;
    private JRadioButton platformLafRB;
    private JRadioButton otherLafRB;
    private JTextField otherTF;

    public static boolean showDialog(JFrame mother) {
        if (dialog == null) {
            dialog = new SettingsDialog(mother);
        } else {
            dialog.setParams();
        }
        dialog.setVisible(true);
        // above call blocks
        return modified;
    }

    private SettingsDialog(JFrame mother) {
        super(mother, "Viskit Application Settings", true);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.addWindowListener(new myCloseListener());
        initConfigs();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(content);

        tabbedPane = new JTabbedPane();
        buildWidgets();

        JPanel buttPan = new JPanel();
        buttPan.setLayout(new BoxLayout(buttPan, BoxLayout.X_AXIS));
        canButt = new JButton("Cancel");
        okButt = new JButton("Close");
        buttPan.add(Box.createHorizontalGlue());
        //buttPan.add(canButt);
        buttPan.add(okButt);
        //buttPan.add(Box.createHorizontalGlue());

        content.add(tabbedPane);
        content.add(Box.createVerticalStrut(5));
        content.add(buttPan);

        // attach listeners
        canButt.addActionListener(new cancelButtonListener());
        okButt.addActionListener(new applyButtonListener());
        VisibilityHandler vis = new VisibilityHandler();
        evGrCB.addActionListener(vis);
        assyCB.addActionListener(vis);
        runCB.addActionListener(vis);
        doeCB.addActionListener(vis);
        clusterRunCB.addActionListener(vis);
        analystReportCB.addActionListener(vis);
        debugMsgsCB.addActionListener(vis);

        setParams();
    }

    private void setParams() {
        fillWidgets();
        getRootPane().setDefaultButton(canButt);

        modified = false;

        pack();
        Dimension d = getSize();
        d.width = Math.max(d.width, 500);
        setSize(d);
        setLocationRelativeTo(getParent());
    }

    private void buildWidgets() {
        JPanel classpathP = new JPanel();
        classpathP.setLayout(new BoxLayout(classpathP, BoxLayout.Y_AXIS));
        classPathJlist = new JList<>(new DefaultListModel<>());
        JScrollPane jsp = new JScrollPane(classPathJlist);
        jsp.setPreferredSize(new Dimension(70, 70));  // don't want it to control size of dialog
        classpathP.add(jsp);
        JPanel bPan = new JPanel();
        bPan.setLayout(new BoxLayout(bPan, BoxLayout.X_AXIS));
        bPan.add(Box.createHorizontalGlue());
        JButton upCPButt = new JButton(new ImageIcon(ClassLoader.getSystemResource("viskit/images/upArrow.png")));
        upCPButt.setBorder(null);
        upCPButt.addActionListener(new upCPhandler());
        JButton addCPButt = new JButton(new ImageIcon(ClassLoader.getSystemResource("viskit/images/plus.png")));
        addCPButt.addActionListener(new addCPhandler());
        JButton removeCPButt = new JButton(new ImageIcon(ClassLoader.getSystemResource("viskit/images/minus.png")));
        removeCPButt.addActionListener(new delCPhandler());
        JButton dnCPButt = new JButton(new ImageIcon(ClassLoader.getSystemResource("viskit/images/downArrow.png")));
        dnCPButt.setBorder(null);
        dnCPButt.addActionListener(new downCPhandler());
        bPan.add(upCPButt);
        bPan.add(addCPButt);
        bPan.add(removeCPButt);
        bPan.add(dnCPButt);
        bPan.add(Box.createHorizontalGlue());
        classpathP.add(bPan);

        tabbedPane.addTab("Additional classpath entries", classpathP);

        JPanel recentP = new JPanel();
        recentP.setLayout(new BoxLayout(recentP, BoxLayout.Y_AXIS));

        JButton clearEGRecent = new JButton("Clear recent event graphs list");
        clearEGRecent.addActionListener(new clearEGHandler());
        clearEGRecent.setAlignmentX(Box.CENTER_ALIGNMENT);
        JButton clearAssRecent = new JButton("Clear recent assemblies list");
        clearAssRecent.addActionListener(new clearAssHandler());
        clearAssRecent.setAlignmentX(Box.CENTER_ALIGNMENT);
        recentP.add(Box.createVerticalGlue());
        recentP.add(clearEGRecent);
        recentP.add(clearAssRecent);
        recentP.add(Box.createVerticalGlue());

        tabbedPane.addTab("Recent files", recentP);

        JPanel visibleP = new JPanel();
        visibleP.setLayout(new BoxLayout(visibleP, BoxLayout.Y_AXIS));
        visibleP.add(Box.createVerticalGlue());
        JPanel innerP = new JPanel();
        innerP.setLayout(new BoxLayout(innerP, BoxLayout.Y_AXIS));
        innerP.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        evGrCB = new JCheckBox("Event Graph Editor");
        innerP.add(evGrCB);
        assyCB = new JCheckBox("Assembly Editor");
        innerP.add(assyCB);
        runCB = new JCheckBox("Assembly Run");
        innerP.add(runCB);
        doeCB = new JCheckBox("Design Of Experiments");
        innerP.add(doeCB);
        clusterRunCB = new JCheckBox("Cluster Run");
        innerP.add(clusterRunCB);
        analystReportCB = new JCheckBox("Analyst Report");
        innerP.add(analystReportCB);
        debugMsgsCB = new JCheckBox("Verbose debug messages");
        innerP.add(debugMsgsCB);
        innerP.setBorder(new CompoundBorder(new LineBorder(Color.black), new EmptyBorder(3, 3, 3, 3)));

        visibleP.add(innerP, BorderLayout.CENTER);
        visibleP.add(Box.createVerticalStrut(3));
        JLabel lab = new JLabel("Changes are in effect at next Viskit launch.", JLabel.CENTER);
        lab.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        visibleP.add(lab);
        visibleP.add(Box.createVerticalGlue());

        tabbedPane.addTab("Tab visibility", visibleP);

        JPanel lookAndFeelP = new JPanel();
        lookAndFeelP.setLayout(new BoxLayout(lookAndFeelP, BoxLayout.Y_AXIS));
        lookAndFeelP.add(Box.createVerticalGlue());
        JPanel lAndFeelInnerP = new JPanel();
        lAndFeelInnerP.setLayout(new BoxLayout(lAndFeelInnerP, BoxLayout.Y_AXIS));
        lAndFeelInnerP.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        defaultLafRB = new JRadioButton("Default");
        defaultLafRB.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        lAndFeelInnerP.add(defaultLafRB);
        platformLafRB = new JRadioButton("Platform");
        platformLafRB.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        lAndFeelInnerP.add(platformLafRB);
        otherLafRB = new JRadioButton("Other");
        JPanel otherPan = new JPanel();
        otherPan.setLayout(new BoxLayout(otherPan,BoxLayout.X_AXIS));
        otherPan.add(otherLafRB);
        otherPan.add(Box.createHorizontalStrut(5));
        otherTF = new JTextField();
        VStatics.clampHeight(otherTF);
        otherPan.add(otherTF);
        otherPan.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        lAndFeelInnerP.add(otherPan);
        lAndFeelInnerP.setBorder(new CompoundBorder(new LineBorder(Color.black), new EmptyBorder(3,3,3,3)));
        VStatics.clampHeight(lAndFeelInnerP);
        lookAndFeelP.add(lAndFeelInnerP);
        lookAndFeelP.add(Box.createVerticalStrut(3));
        lab = new JLabel("Changes are in effect at next Viskit launch.", JLabel.CENTER);
        lab.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        lookAndFeelP.add(lab);
        lookAndFeelP.add(Box.createVerticalGlue());

        tabbedPane.addTab("Look and Feel",lookAndFeelP);

        ButtonGroup bg = new ButtonGroup();
        defaultLafRB.setSelected(true);
        otherTF.setEnabled(false);
        bg.add(defaultLafRB);
        bg.add(platformLafRB);
        bg.add(otherLafRB);
        ActionListener lis = new lafListener();
        platformLafRB.addActionListener(lis);
        defaultLafRB.addActionListener(lis);
        otherLafRB.addActionListener(lis);
        otherTF.addActionListener(lis);
     }

    class lafListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == otherTF) {
                guiConfig.setProperty(ViskitConfig.LOOK_AND_FEEL_KEY, otherTF.getText().trim());
            } else {
                if (defaultLafRB.isSelected()) {
                    guiConfig.setProperty(ViskitConfig.LOOK_AND_FEEL_KEY, ViskitConfig.LAF_DEFAULT);
                    otherTF.setEnabled(false);
                } else if (platformLafRB.isSelected()) {
                    guiConfig.setProperty(ViskitConfig.LOOK_AND_FEEL_KEY, ViskitConfig.LAF_PLATFORM);
                    otherTF.setEnabled(false);
                } else if (otherLafRB.isSelected()) {
                    guiConfig.setProperty(ViskitConfig.LOOK_AND_FEEL_KEY, otherTF.getText().trim());
                    otherTF.setEnabled(true);
                }
            }
        }
    }
    private static XMLConfiguration appConfig;
    private static XMLConfiguration projectConfig;
    private static XMLConfiguration guiConfig;

    private static void initConfigs() {
        appConfig = ViskitConfig.instance().getViskitAppConfig();
        projectConfig = ViskitConfig.instance().getProjectXMLConfig();
        guiConfig = ViskitConfig.instance().getViskitGuiConfig();
    }

    class VisibilityHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox src = (JCheckBox) e.getSource();
            if (src == evGrCB) {
                appConfig.setProperty(ViskitConfig.EG_EDIT_VISIBLE_KEY, evGrCB.isSelected());
            } else if (src == assyCB) {
                appConfig.setProperty(ViskitConfig.ASSY_EDIT_VISIBLE_KEY, assyCB.isSelected());
            } else if (src == runCB) {
                if (runCB.isSelected()) {
                    // if we turn on the assembly runner, we also need the assy editor
                    if (!assyCB.isSelected()) {
                        assyCB.doClick();
                    } // reenter here
                }
                appConfig.setProperty(ViskitConfig.ASSY_RUN_VISIBLE_KEY, runCB.isSelected());
            } else if (src == debugMsgsCB) {
                appConfig.setProperty(ViskitConfig.DEBUG_MSGS_KEY, debugMsgsCB.isSelected());
                VStatics.debug = debugMsgsCB.isSelected();
            } else if (src == analystReportCB) {
                appConfig.setProperty(ViskitConfig.ANALYST_REPORT_VISIBLE_KEY, analystReportCB.isSelected());
            } else if (src == doeCB) {
                appConfig.setProperty(ViskitConfig.DOE_EDIT_VISIBLE_KEY, doeCB.isSelected());
            } else if (src == clusterRunCB) {
                appConfig.setProperty(ViskitConfig.CLUSTER_RUN_VISIBLE_KEY, clusterRunCB.isSelected());
            }
        }
    }

    class clearEGHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            EventGraphController ctrlr = (EventGraphController) VGlobals.instance().getEventGraphController();
            ctrlr.clearRecentEGFileSet();
        }
    }

    class clearAssHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            AssemblyController aCtrlr = (AssemblyController) VGlobals.instance().getAssemblyController();
            aCtrlr.clearRecentAssyFileList();
        }
    }

    private static void clearClassPathEntries() {
        // Always reinitialize the config instances.  We may have changed projects
        initConfigs();
        projectConfig.clearTree(ViskitConfig.X_CLASS_PATHS_CLEAR_KEY);
    }

    static JDialog progressDialog;
    static JProgressBar progress = new JProgressBar(0, 100);

    /** Method to facilitate putting project/lib entries on the classpath
     * @param lis a list of classpath (jar) entries to include on the classpath
     */
    public static void saveExtraClassPathEntries(String[] lis) {
        clearClassPathEntries();

        int ix = 0;
        if (lis != null) {
            for (String s : lis) {
                s = s.replaceAll("\\\\", "/");
                LogUtils.getLogger(SettingsDialog.class).debug("lis[" + ix + "]: " + s);
                projectConfig.setProperty(ViskitConfig.X_CLASS_PATHS_PATH_KEY + "(" + ix + ")[@value]", s);
                ix++;
            }
        }

        if (dialog != null) {
            progressDialog = new JDialog(dialog);
            progressDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            progress.setIndeterminate(true);
            progress.setString("Loading Libraries");
            progress.setStringPainted(true);
            progressDialog.add(progress);
            progressDialog.pack();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            progressDialog.setLocation((d.width - progressDialog.getWidth()) / 2, (d.height - progressDialog.getHeight()) / 2);
            progressDialog.setVisible(true);
            progressDialog.setResizable(false);
        }
        Task t = new Task();
        t.execute();
    }

    static class Task extends SwingWorker<Void, Void> {

        @Override
        public Void doInBackground() {
            if (dialog != null) {
                progressDialog.setVisible(true);
                progressDialog.toFront();
            }

            // Incase we have custom jars, need to add these to the ClassLoader
            VGlobals.instance().resetWorkClassLoader();
            VGlobals.instance().rebuildLEGOTreePanels();
            return null;
        }

        @Override
        public void done() {
            if (dialog != null && progressDialog != null) {
                progress.setIndeterminate(false);
                progress.setValue(100);
                progressDialog.dispose();
            }
        }
    }

    private void fillWidgets() {
        DefaultListModel<String> mod = (DefaultListModel<String>) classPathJlist.getModel();
        mod.clear();
        if (getExtraClassPath() != null) {
            String[] sa = getExtraClassPath();
            for (String s : sa) {
                s = s.replaceAll("\\\\", "/");
                if (!mod.contains(s)) {
                    mod.addElement(s);
                }
            }
            classPathJlist.setModel(mod);
        }

        evGrCB.setSelected(isEventGraphEditorVisible());
        assyCB.setSelected(isAssemblyEditorVisible());
        runCB.setSelected(isAssemblyRunVisible());
        doeCB.setSelected(isDOEVisible());
        clusterRunCB.setSelected(isClusterRunVisible());
        analystReportCB.setSelected(isAnalystReportVisible());
        debugMsgsCB.setSelected(VStatics.debug = isVerboseDebug());

        String laf = getLookAndFeel();
        if(null == laf) {
            platformLafRB.setSelected(true);
        } else switch (laf) {
            case ViskitConfig.LAF_PLATFORM:
                platformLafRB.setSelected(true);
                break;
            case ViskitConfig.LAF_DEFAULT:
                defaultLafRB.setSelected(true);
                break;
            default:
                otherLafRB.setSelected(true);
                otherTF.setEnabled(true);
                otherTF.setText(laf);
                break;
        }
    }

    private void unloadWidgets() {
      // most everything gets instantly updated;  check for pending text entry
      if(otherLafRB.isSelected()) {
          guiConfig.setProperty(ViskitConfig.LOOK_AND_FEEL_KEY, otherTF.getText().trim());
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
            unloadWidgets();
            dispose();
        }
    }

    class myCloseListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            if (modified) {
                int ret = JOptionPane.showConfirmDialog(SettingsDialog.this, "Apply changes?",
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
    JFileChooser addChooser;

    class addCPhandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (addChooser == null) {
                addChooser = new JFileChooser(ViskitProject.MY_VISKIT_PROJECTS_DIR);
                addChooser.setMultiSelectionEnabled(false);
                addChooser.setAcceptAllFileFilterUsed(false);
                addChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                addChooser.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }
                        String nm = f.getName();
                        int idx = nm.lastIndexOf('.');
                        if (idx != -1) {
                            String extension = nm.substring(idx).toLowerCase();
                            if (extension != null && (extension.equals(".jar") ||
                                    extension.equals(".zip"))) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return "Directories, jars and zips";
                    }
                });
            }

            int retv = addChooser.showOpenDialog(SettingsDialog.this);
            if (retv == JFileChooser.APPROVE_OPTION) {
                File selFile = addChooser.getSelectedFile();
                String absPath = selFile.getAbsolutePath();
                ((DefaultListModel<String>) classPathJlist.getModel()).addElement(absPath.replaceAll("\\\\", "/"));
                installExtraClassPathIntoConfig();
            }
        }
    }

    private void installExtraClassPathIntoConfig() {
        Object[] oa = ((DefaultListModel) classPathJlist.getModel()).toArray();
        String[] sa = new String[oa.length];

        System.arraycopy(oa, 0, sa, 0, oa.length);

        saveExtraClassPathEntries(sa);
    }

    class delCPhandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selected = classPathJlist.getSelectedIndices();
            if (selected == null || selected.length <= 0) {
                return;
            }
            for (int i = selected.length - 1; i >= 0; i--) {
                ((DefaultListModel) classPathJlist.getModel()).remove(selected[i]);
            }
            installExtraClassPathIntoConfig();
        }
    }

    class upCPhandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selected = classPathJlist.getSelectedIndices();
            if (selected == null || selected.length <= 0 || selected[0] <= 0) {
                return;
            }
            moveLine(selected[0], -1);
        }
    }

    private void moveLine(int idx, int polarity) {
        classPathJlist.clearSelection();
        DefaultListModel<String> mod = (DefaultListModel<String>) classPathJlist.getModel();
        Object o = mod.get(idx);
        mod.remove(idx);
        mod.add(idx + polarity, (String) o);
        installExtraClassPathIntoConfig();
        classPathJlist.setSelectedIndex(idx + polarity);
    }

    class downCPhandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selected = classPathJlist.getSelectedIndices();
            int listLen = classPathJlist.getModel().getSize();

            if (selected == null || selected.length <= 0 || selected[0] >= (listLen - 1)) {
                return;
            }
            moveLine(selected[0], +1);
        }
    }

    /** @return a String array containing the extra classpaths to consider */
    public static String[] getExtraClassPath() {
        if ((appConfig == null) || (projectConfig == null)) {
            initConfigs();
        }
        return projectConfig.getStringArray(ViskitConfig.X_CLASS_PATHS_KEY);
    }

    /** @return a URL[] of the extra classpaths, to include a path to event graphs */
    public static URL[] getExtraClassPathArraytoURLArray() {
        String[] extClassPaths = getExtraClassPath();
        if (extClassPaths == null) {return null;}
        URL[] extClassPathsUrls = new URL[extClassPaths.length];
        int i = 0;
        File file;
        for (String path : extClassPaths) {
            file = new File(path);
            if (!file.exists()) {

                // Allow a relative path for Diskit-Test (Diskit)
                if (path.contains("..")) {
                    file = new File(VGlobals.instance().getCurrentViskitProject().getProjectRoot().getParent() + "/" + path.replaceFirst("../", ""));
                }
            }
            if (file.exists()) {
                try {
                    extClassPathsUrls[i++] = file.toURI().toURL();
                } catch (MalformedURLException ex) {
                    LogUtils.getLogger(SettingsDialog.class).error(ex);
                }
            }
        }
        return extClassPathsUrls;
    }

    /**
     * Return the value for the platform look and feel
     * @return the value for the platform look and feel
     */
    public static String getLookAndFeel() {
        return ViskitConfig.instance().getVal(ViskitConfig.LOOK_AND_FEEL_KEY);
    }

    /**
     * Return the value for tab visibility
     * @param prop the tab of interest
     * @return the value for tab visibility
     */
    public static boolean getVisibilitySense(String prop) {
        return appConfig.getBoolean(prop);
    }

    /**
     * Return if the EG Editor is to be visible
     * @return if the EG Editor is to be visible
     */
    public static boolean isEventGraphEditorVisible() {
        return getVisibilitySense(ViskitConfig.EG_EDIT_VISIBLE_KEY);
    }

    /**
     * Return if the Assy Editor is to be visible
     * @return if the Assy Editor is to be visible
     */
    public static boolean isAssemblyEditorVisible() {
        return getVisibilitySense(ViskitConfig.ASSY_EDIT_VISIBLE_KEY);
    }

    /**
     * Return if the Assy Runner is to be visible
     * @return if the Assy Runner is to be visible
     */
    public static boolean isAssemblyRunVisible() {
        return getVisibilitySense(ViskitConfig.ASSY_RUN_VISIBLE_KEY);
    }

    /**
     * Return if the Analyst Report Editor is to be visible
     * @return if the Analyst Report Editor is to be visible
     */
    public static boolean isAnalystReportVisible() {
        return getVisibilitySense(ViskitConfig.ANALYST_REPORT_VISIBLE_KEY);
    }

    /**
     * Return if verbose debug message are to be printed
     * @return if verbose debug message are to be printed
     */
    public static boolean isVerboseDebug() {
        return getVisibilitySense(ViskitConfig.DEBUG_MSGS_KEY);
    }

    /**
     * Return if the Design of Experiments Editor is to be visible
     * @return if the Design of Experiments Editor is to be visible
     */
    public static boolean isDOEVisible() {
        return getVisibilitySense(ViskitConfig.DOE_EDIT_VISIBLE_KEY);
    }

    /**
     * Return if the Cluster Runner is to be visible
     * @return if the Cluster Runner is to be visible
     */
    public static boolean isClusterRunVisible() {
        return getVisibilitySense(ViskitConfig.CLUSTER_RUN_VISIBLE_KEY);
    }
}
