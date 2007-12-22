/*
Copyright (c) 1995-2007 held by the author(s).  All rights reserved.

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

/**
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * @author Mike Bailey
 * @since Nov 2, 2005
 * @since 11:24:06 AM
 * @version $Id: SettingsDialog.java 1667 2007-12-17 20:24:55Z tdnorbra $
 */
package viskit;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class SettingsDialog extends JDialog {

    static Logger log = Logger.getLogger(SettingsDialog.class);
    private static SettingsDialog dialog;
    private static boolean modified = false;
    private JFrame mother;
    private JButton canButt;
    private JButton okButt;
    private JTabbedPane tabbedPane;
    private JList classPathJlist;
    private JCheckBox evGrCB;
    private JCheckBox assyCB;
    private JCheckBox runCB;
    private JCheckBox analRptCB;
    private JCheckBox debugMsgsCB;

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
        this.mother = mother;

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new myCloseListener());
        initConfig();

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

        fillWidgets();     // put the data into the widgets

        getRootPane().setDefaultButton(canButt);

        pack();     // do this prior to next
        Dimension d = getSize();
        d.width = Math.max(d.width, 400);
        setSize(d);
        this.setLocationRelativeTo(mother);

        // attach listeners
        canButt.addActionListener(new cancelButtonListener());
        okButt.addActionListener(new applyButtonListener());
        VisibilityHandler vis = new VisibilityHandler();
        evGrCB.addActionListener(vis);
        assyCB.addActionListener(vis);
        runCB.addActionListener(vis);
        analRptCB.addActionListener(vis);
        debugMsgsCB.addActionListener(vis);

        // post process anything
        Vstatics.debug = isVerboseDebug();

    }

    private void setParams() {
        fillWidgets();

        modified = false;

        pack();
        Dimension d = getSize();
        d.width = Math.max(d.width, 600);
        setSize(d);
        this.setLocationRelativeTo(mother);
    }

    private void buildWidgets() {
        JPanel classpathP = new JPanel();
        classpathP.setLayout(new BoxLayout(classpathP, BoxLayout.Y_AXIS));
        classPathJlist = new JList(new DefaultListModel());
        JScrollPane jsp = new JScrollPane(classPathJlist);
        jsp.setPreferredSize(new Dimension(70, 70));  // don't want it to control size of dialog
        classpathP.add(jsp);
        JPanel bPan = new JPanel();
        bPan.setLayout(new BoxLayout(bPan, BoxLayout.X_AXIS));
        bPan.add(Box.createHorizontalGlue());
        JButton upCPButt = new JButton(new ImageIcon(ClassLoader.getSystemResource("viskit/images/upArrow.png")));
        upCPButt.setBorder(null);
        upCPButt.addActionListener(new upCPhandler());
        JButton addCPButt = new JButton("add");
        addCPButt.addActionListener(new addCPhandler());
        JButton removeCPButt = new JButton("remove");
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

        tabbedPane.addTab("Recent files lists", recentP);

        JPanel visibleP = new JPanel();
        visibleP.setLayout(new BoxLayout(visibleP, BoxLayout.Y_AXIS));
        visibleP.add(Box.createVerticalGlue());
        JPanel innerP = new JPanel();
        innerP.setLayout(new BoxLayout(innerP, BoxLayout.Y_AXIS));
        innerP.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        evGrCB = new JCheckBox("Event graph editor");
        innerP.add(evGrCB);
        assyCB = new JCheckBox("Assembly editor");
        innerP.add(assyCB);
        runCB = new JCheckBox("Assembly run");
        innerP.add(runCB);
        analRptCB = new JCheckBox("Analyst report");
        innerP.add(analRptCB);
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
    }
    private static XMLConfiguration vConfig;
    private static String xClassPathKey = "extraClassPath.path";
    private static String xClassPathClearKey = "extraClassPath";
    private static String recentEGClearKey = "history.EventGraphEditor.Recent";
    private static String recentAssyClearKey = "history.AssemblyEditor.Recent";
    private static String egEdVisibleKey = "application.tabs.EventGraphEditor[@visible]";
    private static String asEdVisibleKey = "application.tabs.AssemblyEditor[@visible]";
    private static String asRunVisibleKey = "application.tabs.AssemblyRun[@visible]";
    private static String anRptVisibleKey = "application.tabs.AnalystReport[@visible]";
    private static String debugMsgsKey = "application.debug";

    private static void initConfig() {
        try {
            vConfig = VGlobals.instance().getHistoryConfig();
        } catch (Exception e) {
            System.out.println("Error loading config file: " + e.getMessage());
            vConfig = null;
        }
    }

    class VisibilityHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JCheckBox src = (JCheckBox) e.getSource();
            if (src == evGrCB) {
                vConfig.setProperty(egEdVisibleKey, evGrCB.isSelected());
            } else if (src == assyCB) {
                vConfig.setProperty(asEdVisibleKey, assyCB.isSelected());
            } else if (src == runCB) {
                if (runCB.isSelected()) {
                    // if we turn on the assembly runner, we need also the assy editor
                    if (!assyCB.isSelected()) {
                        assyCB.doClick();
                    } // reenter here
                }
                vConfig.setProperty(asRunVisibleKey, runCB.isSelected());
            } else if (src == debugMsgsCB) {
                vConfig.setProperty(debugMsgsKey, debugMsgsCB.isSelected());
                Vstatics.debug = debugMsgsCB.isSelected();
            } else /* if(src == analRptCB) */ {
                vConfig.setProperty(anRptVisibleKey, analRptCB.isSelected());
            }
        }
    }

    class clearEGHandler implements ActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            vConfig.clearTree(recentEGClearKey);
        }
    }

    class clearAssHandler implements ActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            vConfig.clearTree(recentAssyClearKey);
        }
    }

    private void clearClassPathEntries() {
        vConfig.clearTree(xClassPathClearKey);
    }
    JDialog progressDialog = new JDialog(this);
    JProgressBar progress = new JProgressBar(0, 100);

    private void saveClassPathEntries(String[] lis) {
        clearClassPathEntries();

        for (int i = 0; i < lis.length; i++) {
            vConfig.setProperty(xClassPathKey + "(" + i + ")[@value]", lis[i]);
        }

        progressDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        //progressDialog.add(panel, BorderLayout.PAGE_START);
        progress.setIndeterminate(true);
        progress.setString("Loading Libraries");
        progress.setStringPainted(true);
        progressDialog.add(progress);
        //panel.add(progress);
        progressDialog.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        progressDialog.setLocation((d.width - progressDialog.getWidth()) / 2, (d.height - progressDialog.getHeight()) / 2);
        progressDialog.setVisible(true);
        progressDialog.setResizable(false);
        Task t = new Task();
        t.execute();
    }

    class Task extends SwingWorker<Void, Void> {

        public Void doInBackground() {
            progressDialog.setVisible(true);
            progressDialog.toFront();
            VGlobals.instance().rebuildTreePanels();
            return null;
        }

        @Override
        public void done() {
            progress.setIndeterminate(false);
            progress.setValue(100);
            progressDialog.setVisible(false);
        }
    }

    private void fillWidgets() {
        //classPathJlist.removeAll();
        DefaultListModel mod = new DefaultListModel();
        String[] sa = getExtraClassPath();
        for (int i = 0; i < sa.length; i++) {
            mod.addElement(sa[i]);
        }
        classPathJlist.setModel(mod);

        evGrCB.setSelected(isEventGraphEditorVisible());
        assyCB.setSelected(isAssemblyEditorVisible());
        runCB.setSelected(isAssemblyRunVisible());
        analRptCB.setSelected(isAnalystReportVisible());
        debugMsgsCB.setSelected(isVerboseDebug());
    }

    private void unloadWidgets() {

    }

    class cancelButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            modified = false;    // for the caller
            setVisible(false);
        }
    }

    class applyButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            if (modified) {
                unloadWidgets();
            }
            setVisible(false);
        }
    }

    class myCloseListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            if (modified == true) {
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

        public void actionPerformed(ActionEvent e) {
            if (addChooser == null) {
                addChooser = new JFileChooser(System.getProperty("user.dir"));
                addChooser.setMultiSelectionEnabled(false);
                addChooser.setAcceptAllFileFilterUsed(false);
                addChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                addChooser.setFileFilter(new FileFilter() {

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

                    public String getDescription() {
                        return "Directories, jars and zips";
                    }
                });
            }

            int retv = addChooser.showOpenDialog(SettingsDialog.this);
            if (retv == JFileChooser.APPROVE_OPTION) {
                File selFile = addChooser.getSelectedFile();
                String absPath = selFile.getAbsolutePath();
                String sep = System.getProperty("file.separator");
                if (selFile.isDirectory() && !absPath.endsWith(sep)) {
                    absPath = absPath + sep;
                }
                ((DefaultListModel) classPathJlist.getModel()).addElement(absPath);
                installClassPathIntoConfig();
            }
        }
    }

    private void installClassPathIntoConfig() {
        Object m = classPathJlist.getModel();
        Object[] oa = ((DefaultListModel) classPathJlist.getModel()).toArray();
        String[] sa = new String[oa.length];
        for (int j = 0; j < oa.length; j++) {
            sa[j] = (String) oa[j];
        }

        saveClassPathEntries(sa);
    }

    class delCPhandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int[] selected = classPathJlist.getSelectedIndices();
            if (selected == null || selected.length <= 0) {
                return;
            }
            for (int i = selected.length - 1; i >= 0; i--) {
                ((DefaultListModel) classPathJlist.getModel()).removeElementAt(selected[i]);
            }
            installClassPathIntoConfig();
        }
    }

    class upCPhandler implements ActionListener {

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
        DefaultListModel mod = (DefaultListModel) classPathJlist.getModel();
        Object o = mod.getElementAt(idx);
        mod.removeElementAt(idx);
        mod.insertElementAt(o, idx + polarity);
        classPathJlist.setSelectedIndex(idx + polarity);

    }

    class downCPhandler implements ActionListener {

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
        if (vConfig == null) {
            initConfig();
        }
        VGlobals.instance().resetWorkClassLoader();
        return vConfig.getStringArray(xClassPathKey + "[@value]");
    }

    /** @return a URL[] of the extra classpaths, to include a path to event graphs */
    public static URL[] getExtraClassPathArraytoURLArray() {
        String[] extClassPaths = getExtraClassPath();
        URL[] extClassPathsUrls = new URL[extClassPaths.length];
        int i = 0;
        for (String path : extClassPaths) {
            File extFile = new File(path);
            try {
                extClassPathsUrls[i++] = extFile.toURI().toURL();
            } catch (MalformedURLException ex) {
                log.error(ex);
            }
        }
        return extClassPathsUrls;
    }

    public static boolean getVisibilitySense(String prop) {
        if (vConfig == null) {
            initConfig();
        }

        boolean b = true; // by default
        try {
            b = vConfig.getBoolean(prop);
        } catch (Exception e) {
        // probably no-such-element
        }
        return b;
    }

    public static boolean isEventGraphEditorVisible() {
        return getVisibilitySense(egEdVisibleKey);
    }

    public static boolean isAssemblyEditorVisible() {
        return getVisibilitySense(asEdVisibleKey);
    }

    public static boolean isAssemblyRunVisible() {
        return getVisibilitySense(asRunVisibleKey);
    }

    public static boolean isAnalystReportVisible() {
        return getVisibilitySense(anRptVisibleKey);
    }

    public static boolean isVerboseDebug() {
        return getVisibilitySense(debugMsgsKey);
    }
}