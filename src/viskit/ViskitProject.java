/*
Copyright (c) 1995-2015 held by the author(s).  All rights reserved.

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

import edu.nps.util.LogUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.filechooser.FileView;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/** The base class for management of all Viskit Projects
 * @version $Id: ViskitProject.java 1916 2008-07-04 09:13:41Z tdnorbra $
 * @author abuss
 */
public class ViskitProject {

    /** This static variable will be set by the user upon first Viskit startup
     * to determine a project home space on the user's machine.  A default
     * home will be the user's working directory where Viskit is installed.
     */
    public static final String DEFAULT_VISKIT_PROJECTS_DIR =
            System.getProperty("user.home").replaceAll("\\\\", "/") + "/MyViskitProjects";

    public static String MY_VISKIT_PROJECTS_DIR = DEFAULT_VISKIT_PROJECTS_DIR;

    /** This static variable will be set by the user upon first Viskit startup
     * to determine a project location space on the user's machine.  A default
     * location will be in the user's profile, or home directory.
     */
    public static String DEFAULT_PROJECT_NAME = "DefaultProject";

    public static final String VISKIT_ROOT_NAME = "ViskitProject";
    public static final String PROJECT_FILE_NAME = "viskitProject.xml";
    public static final String ASSEMBLIES_DIRECTORY_NAME = "Assemblies";
    public static final String EVENT_GRAPHS_DIRECTORY_NAME = "EventGraphs";

    public static final String ANALYST_REPORTS_DIRECTORY_NAME = "AnalystReports";
    public static final String VISKIT_ICON_FILE_NAME = "Viskit.ico";
    public static final String VISKIT_CONFIG_DIR = "configuration";
    public static final String VISKIT_ICON_SOURCE = VISKIT_CONFIG_DIR + "/" + VISKIT_ICON_FILE_NAME;
    public static final String ANALYST_REPORT_CHARTS_DIRECTORY_NAME = "charts";
    public static final String ANALYST_REPORT_IMAGES_DIRECTORY_NAME = "images";
    public static final String ANALYST_REPORT_ASSEMBLY_IMAGES_DIRECTORY_NAME = ASSEMBLIES_DIRECTORY_NAME;
    public static final String ANALYST_REPORT_EVENT_GRAPH_IMAGES_DIRECTORY_NAME = EVENT_GRAPHS_DIRECTORY_NAME;
    public static final String ANALYST_REPORT_STATISTICS_DIRECTORY_NAME = "statistics";

    public static final String BUILD_DIRECTORY_NAME = "build";
    public static final String CLASSES_DIRECTORY_NAME = "classes";
    public static final String SOURCE_DIRECTORY_NAME = "src";
    public static final String DIST_DIRECTORY_NAME = "dist";
    public static final String LIB_DIRECTORY_NAME = "lib";

    static Logger log = LogUtils.getLogger(ViskitProject.class);

    private File projectRoot;
    private File projectFile;
    private File analystReportsDir;
    private File analystReportChartsDir;
    private File analystReportImagesDir;
    private File analystReportAssemblyImagesDir;
    private File analystReportEventGraphImagesDir;
    private File analystReportStatisticsDir;
    private File assembliesDir;
    private File eventGraphsDir;
    private File buildDir;
    private File classesDir;
    private File srcDir;
    private File distDir;
    private File libDir;
    private boolean projectFileExists = false;
    private boolean dirty;
    private boolean projectOpen = false;
    private Document projectDocument;

    public ViskitProject(File projectRoot) {
        if (projectRoot.exists() && !projectRoot.isDirectory()) {
            throw new IllegalArgumentException(
                    "Project root must be directory: " +
                    projectRoot);
        }
        setProjectRoot(projectRoot);
    }

    public boolean initProject() {

        if (!projectRoot.exists()) {
            projectRoot.mkdir();
        }

        setAnalystReportsDir(new File(projectRoot, ANALYST_REPORTS_DIRECTORY_NAME));
        if (!analystReportsDir.exists()) {
            getAnalystReportsDir().mkdirs();
            try {
                Files.copy(new File(VISKIT_ICON_SOURCE).toPath(), new File(getAnalystReportsDir(), VISKIT_ICON_FILE_NAME).toPath());
            } catch (IOException ex) {
                log.error(ex);
            }
        }

        setAnalystReportChartsDir(new File(getAnalystReportsDir(), ANALYST_REPORT_CHARTS_DIRECTORY_NAME));
        if (!analystReportChartsDir.exists()) {
            getAnalystReportChartsDir().mkdirs();
        }

        setAnalystReportImagesDir(new File(getAnalystReportsDir(), ANALYST_REPORT_IMAGES_DIRECTORY_NAME));

        setAnalystReportAssemblyImagesDir(new File(getAnalystReportImagesDir(), ANALYST_REPORT_ASSEMBLY_IMAGES_DIRECTORY_NAME));
        if (!analystReportAssemblyImagesDir.exists()) {
            getAnalystReportAssemblyImagesDir().mkdirs();
        }

        setAnalystReportEventGraphImagesDir(new File(getAnalystReportImagesDir(), ANALYST_REPORT_EVENT_GRAPH_IMAGES_DIRECTORY_NAME));
        if (!analystReportEventGraphImagesDir.exists()) {
            getAnalystReportEventGraphImagesDir().mkdirs();
        }

        setAnalystReportStatisticsDir(new File(getAnalystReportsDir(), ANALYST_REPORT_STATISTICS_DIRECTORY_NAME));
        if (!analystReportStatisticsDir.exists()) {
            getAnalystReportStatisticsDir().mkdirs();
        }

        setAssembliesDir(new File(projectRoot, ASSEMBLIES_DIRECTORY_NAME));
        if (!assembliesDir.exists()) {
            getAssembliesDir().mkdir();
        }

        setEventGraphsDir(new File(projectRoot, EVENT_GRAPHS_DIRECTORY_NAME));
        if (!eventGraphsDir.exists()) {
            getEventGraphsDir().mkdir();
        }

        setBuildDir(new File(projectRoot, BUILD_DIRECTORY_NAME));

        // Start with a fresh build directory
//        if (getBuildDir().exists()) {
//            clean();
//        }

        // NOTE: If the project's build directory got nuked and we have
        // cached our EGs and classes with MD5 hash, we'll throw a
        // ClassNotFoundException.  Caching of EGs is a convenience for large
        // directories of EGs that take time to compile the first time

        setSrcDir(new File(getBuildDir(), SOURCE_DIRECTORY_NAME));
        if (!srcDir.exists()) {
            getSrcDir().mkdirs();
        }

        setClassesDir(new File(getBuildDir(), CLASSES_DIRECTORY_NAME));
        if (!classesDir.exists()) {
            getClassesDir().mkdirs();
        }

        setLibDir(new File(projectRoot, LIB_DIRECTORY_NAME));
        if (!libDir.exists()) {
            getLibDir().mkdir();
        }

        // If we already have a project file, then load it.  If not, create it
        setProjectFile(new File(projectRoot, PROJECT_FILE_NAME));
        if (!projectFile.exists()) {
            try {
                getProjectFile().createNewFile();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            projectDocument = createProjectDocument();
            writeProjectFile();
        } else {
            loadProjectFromFile(getProjectFile());
        }
        ViskitConfig.instance().setProjectXMLConfig(getProjectFile().getAbsolutePath());
        setProjectOpen(projectFileExists);
        return projectFileExists;
    }

    private Document createProjectDocument() {
        Document document = new Document();

        Element root = new Element(VISKIT_ROOT_NAME);
        root.setAttribute("name", projectRoot.getName());
        document.setRootElement(root);

        Element element = new Element(ANALYST_REPORTS_DIRECTORY_NAME);
        element.setAttribute("name", ANALYST_REPORTS_DIRECTORY_NAME);
        root.addContent(element);

        element = new Element("AssembliesDirectory");
        element.setAttribute("name", ASSEMBLIES_DIRECTORY_NAME);
        root.addContent(element);

        element = new Element("EventGraphsDirectory");
        element.setAttribute("name", EVENT_GRAPHS_DIRECTORY_NAME);
        root.addContent(element);

        element = new Element("BuildDirectory");
        element.setAttribute("name", BUILD_DIRECTORY_NAME);
        root.addContent(element);

        Element subElement = new Element("ClassesDirectory");
        subElement.setAttribute("name", CLASSES_DIRECTORY_NAME);
        element.addContent(subElement);

        subElement = new Element("SourceDirectory");
        subElement.setAttribute("name", SOURCE_DIRECTORY_NAME);
        element.addContent(subElement);

        element = new Element("DistDirectory");
        element.setAttribute("name", DIST_DIRECTORY_NAME);
        root.addContent(element);

        element = new Element("LibDirectory");
        element.setAttribute("name", LIB_DIRECTORY_NAME);
        root.addContent(element);

        return document;
    }

    private void writeProjectFile() {
        FileOutputStream fileOutputStream = null;
        try {
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            fileOutputStream = new FileOutputStream(getProjectFile());
            xmlOutputter.output(projectDocument, fileOutputStream);
            projectFileExists = true;
        } catch (IOException ex) {
            log.error(ex);
        } finally {
            try {
                if (fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    /**
     * Load a Viskit project file
     * @param inputProjectFile a Viskit project file
     */
    private void loadProjectFromFile(File inputProjectFile) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            projectDocument = saxBuilder.build(inputProjectFile);
            Element root = projectDocument.getRootElement();
            if (!root.getName().equals(VISKIT_ROOT_NAME)) {
                projectDocument = null;
                throw new IllegalArgumentException("Not a Viskit Project File");
            }
            projectFileExists = true;
        } catch (JDOMException | IOException ex) {
            log.error(ex);
            throw new RuntimeException(ex);
        }
    }

    /** @return an array of a project's external resources */
    public String[] getProjectContents() {

        // Prevent duplicate entries
        Set<String> cp = new HashSet<>();

        // Find and list JARs and ZIPs, from the project's lib directory, in the extra classpath widget
        try {
            for (File f : getLibDir().listFiles()) {
                if ((f.getName().contains(".jar")) || (f.getName().contains(".zip"))) {
                    String file = f.getCanonicalPath().replaceAll("\\\\", "/");
                    log.debug(file);
                    cp.add(file);
                }
            }
            log.debug(getEventGraphsDir().getCanonicalPath());

            // Now list any paths outside of the project space, i.e. ${other path}/build/classes
            String[] classPaths = ViskitConfig.instance().getConfigValues(ViskitConfig.X_CLASS_PATHS_KEY);
            for (String classPath : classPaths) {
                cp.add(classPath.replaceAll("\\\\", "/"));
            }

        } catch (IOException ex) {
            log.error(ex);
        } catch (NullPointerException npe) {
            return null;
        }
        return cp.toArray(new String[cp.size()]);
    }

    public void clean() {
        if (getBuildDir().exists()) {
            deleteDirectoryContents(getBuildDir());
        }
    }

    public static void deleteDirectoryContents(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] contents = file.listFiles();
            for (File f : contents) {
                deleteDirectoryContents(f);
            }
            file.delete();
        }
    }

    public void generateSource() {
        if (!buildDir.exists()) {
            getBuildDir().mkdir();
        }
        if (!srcDir.exists()) {
            getSrcDir().mkdir();
        }
        System.out.println("Generate source into " + getSrcDir());
    }

    public void compileSource() {
        if (!buildDir.exists()) {
            generateSource();
        }
        if (!classesDir.exists()) {
            getClassesDir().mkdir();
        }
        System.out.println("Compile Source to " + getClassesDir());
    }

    public void deleteProject() {
        deleteDirectoryContents(projectRoot);
    }

    public void closeProject() {
        ViskitConfig vConfig = ViskitConfig.instance();
        vConfig.getViskitGuiConfig().setProperty(ViskitConfig.PROJECT_TITLE_NAME, "");
        vConfig.cleanup();
        vConfig.removeProjectXMLConfig(vConfig.getProjectXMLConfig());
        setProjectOpen(false);
    }

    /** @return the root directory of this ViskitProject */
    public File getProjectRoot() {
        return projectRoot;
    }

    public final void setProjectRoot(File projectRoot) {
        this.projectRoot = projectRoot;
        XMLConfiguration guiConfig = ViskitConfig.instance().getViskitGuiConfig();
        guiConfig.setProperty(ViskitConfig.PROJECT_TITLE_NAME, getProjectRoot().getName());
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public File getBuildDir() {
        return buildDir;
    }

    /**
     * @param buildDir the buildDir to set
     */
    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    /**
     * @return indication of the projectOpen
     */
    public boolean isProjectOpen() {
        return projectOpen;
    }

    /**
     * @param projectOpen the projectOpen to set
     */
    public void setProjectOpen(boolean projectOpen) {
        this.projectOpen = projectOpen;
    }

    /**
     * @return the analystReportsDir
     */
    public File getAnalystReportsDir() {
        return analystReportsDir;
    }

    /**
     * @param analystReportsDir the analystReportsDir to set
     */
    public void setAnalystReportsDir(File analystReportsDir) {
        this.analystReportsDir = analystReportsDir;
    }

    /** Retrieve the project's src directory (located in build)
     *
     * @return the project's src directory (located in build)
     */
    public File getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    public File getClassesDir() {
        return classesDir;
    }

    public void setClassesDir(File classDir) {
        this.classesDir = classDir;
    }

    public File getEventGraphsDir() {
        return eventGraphsDir;
    }

    public void setEventGraphsDir(File eventGraphDir) {
        this.eventGraphsDir = eventGraphDir;
    }

    public File getLibDir() {
        return libDir;
    }

    public void setLibDir(File libDir) {
        this.libDir = libDir;
    }

    public File getAssembliesDir() {
        return assembliesDir;
    }

    public void setAssembliesDir(File assemblyDir) {
        this.assembliesDir = assemblyDir;
    }

    /**
     * @return the projectFile
     */
    public File getProjectFile() {
        return projectFile;
    }

    /**
     * @param projectFile the projectFile to set
     */
    public void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }


    /**
     * @return the analystReportChartsDir
     */
    public File getAnalystReportChartsDir() {
        return analystReportChartsDir;
    }

    /**
     * @param analystReportChartsDir the analystReportChartsDir to set
     */
    public void setAnalystReportChartsDir(File analystReportChartsDir) {
        this.analystReportChartsDir = analystReportChartsDir;
    }

    /**
     * @return the analystReportImagesDir
     */
    public File getAnalystReportImagesDir() {
        return analystReportImagesDir;
    }

    /**
     * @param analystReportImagesDir the analystReportImagesDir to set
     */
    public void setAnalystReportImagesDir(File analystReportImagesDir) {
        this.analystReportImagesDir = analystReportImagesDir;
    }

    /**
     * @return the analystReportAssemblyImagesDir
     */
    public File getAnalystReportAssemblyImagesDir() {
        return analystReportAssemblyImagesDir;
    }

    /**
     * @param analystReportAssemblyImagesDir the analystReportAssemblyImagesDir to set
     */
    public void setAnalystReportAssemblyImagesDir(File analystReportAssemblyImagesDir) {
        this.analystReportAssemblyImagesDir = analystReportAssemblyImagesDir;
    }

    /**
     * @return the analystReportEventGraphImagesDir
     */
    public File getAnalystReportEventGraphImagesDir() {
        return analystReportEventGraphImagesDir;
    }

    /**
     * @param analystReportEventGraphImagesDir the analystReportEventGraphImagesDir to set
     */
    public void setAnalystReportEventGraphImagesDir(File analystReportEventGraphImagesDir) {
        this.analystReportEventGraphImagesDir = analystReportEventGraphImagesDir;
    }

    /**
     * @return the analystReportStatisticsDir
     */
    public File getAnalystReportStatisticsDir() {
        return analystReportStatisticsDir;
    }

    @Override
    public String toString() {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        StringWriter stringWriter = new StringWriter();
        try {
            xmlOutputter.output(projectDocument, stringWriter);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return stringWriter.toString();
    }

    /** A static JFileChooser that can be used at Viskit init from a clean install */
    private static JFileChooser projectChooser;

    /** When a user selects an iconized Viskit Project directory, then load it */
    private static PropertyChangeListener myChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {

                File file = projectChooser.getSelectedFile();

                if (((ViskitProjectFileView) projectChooser.getFileView()).isViskitProject(file)) {
                    projectChooser.approveSelection();
                }
            }
        }
    };

    private static void initializeProjectChooser(String startPath) {
        if (projectChooser == null) {
            projectChooser = new JFileChooser(startPath);

            projectChooser.addPropertyChangeListener(myChangeListener);

            // show spec. icon for viskit projects
            projectChooser.setFileView(new ViskitProjectFileView());

            // allow only dirs for selection
            projectChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            projectChooser.setMultiSelectionEnabled(false);
            projectChooser.setFileFilter(new ProjectFilter());
            projectChooser.setApproveButtonToolTipText("Open selected project");
        } else {
            projectChooser.setCurrentDirectory(new File(startPath));
        }
    }

    /** Used to aid in new project path creation
     *
     * @param parent the component to center the FileChooser against
     * @param startingDirPath a path to start looking
     * @return a selected file
     */
    public static File newProjectPath(JComponent parent, String startingDirPath) {
        initializeProjectChooser(startingDirPath);

        projectChooser.setDialogTitle("New Viskit Project Directory");
        int ret = projectChooser.showSaveDialog(parent);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return null;
        }
        return projectChooser.getSelectedFile();
    }

    /** Utility method to aid in Viskit specific project directory selection
     *
     * @param parent the component parent for JOptionPane orientation
     * @param startingDirPath a path to start looking from in the chooser
     * @return a path to a valid project directory
     */
    public static File openProjectDir(JFrame parent, String startingDirPath) {
        File projectDir = null;
        initializeProjectChooser(startingDirPath);

        projectChooser.setDialogTitle("Open an Existing Viskit Project");
        boolean isProjectDir;

        do {
            int ret = projectChooser.showOpenDialog(parent);

            // User may have exited the chooser
            if (ret == JFileChooser.CANCEL_OPTION) {
                return null;
            }

            projectDir = projectChooser.getSelectedFile();
            isProjectDir = ((ViskitProjectFileView)projectChooser.getFileView()).isViskitProject(projectDir);

            // Give user a chance to select an iconized project directory
            if (!isProjectDir) {
                Object[] options = {"Select project", "Cancel"};
                int retrn = JOptionPane.showOptionDialog(parent, "Your selection is not a valid Viskit project.", "Please try another selection",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);

                if (retrn != 0) {
                    // 0th choice (Select project)
                    return null; // cancelled
                } // cancelled
            }

        } while (!isProjectDir);

        return projectDir;
    }

    /**
     * @param analystReportStatisticsDir the analystReportStatisticsDir to set
     */
    public void setAnalystReportStatisticsDir(File analystReportStatisticsDir) {
        this.analystReportStatisticsDir = analystReportStatisticsDir;
    }

    private static class ViskitProjectFileView extends FileView {

        Icon viskitProjIcon;

        public ViskitProjectFileView() {

            // Can't use VGlobals.instance().getWorkClassLoader() b/c it will
            // hang if this is the first frest startup of Viskit
            viskitProjIcon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("viskit/images/ViskitIcon.gif"));
        }

        @Override
        public Icon getIcon(File f) {
            return isViskitProject(f) ? viskitProjIcon : null;
        }

        /**
         * Report if given directory holds a Viskit Project
         *
         * @param fDir the project directory to test
         * @return true when a viskitProject.xml file is found
         */
        public boolean isViskitProject(File fDir) {

            if ((fDir == null) || !fDir.exists() || !fDir.isDirectory()) {
                return false;
            }

            // http://www.avajava.com/tutorials/lessons/how-do-i-use-a-filenamefilter-to-display-a-subset-of-files-in-a-directory.html
            File[] files = fDir.listFiles(new java.io.FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {

                    // configuration/ contains the template viskitProject.xml file
                    // so, don't show this directory as a potential Viskit project
                    if (dir.getName().equals(VISKIT_CONFIG_DIR)) {
                        return false;
                    }

                    // Be brutally specific to reduce looking for any *.xml
                    return name.equalsIgnoreCase(PROJECT_FILE_NAME);
                }
            });

            // This can happen on Win machines when parsing "My Computer" directory
            if (files == null) {
                return false;
            }

            // If this List is not empty, we found a project file
            return files.length > 0;
        }
    }

    private static class ProjectFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File pathname) {
            return !pathname.getName().contains("svn");
        }

        @Override
        public String getDescription() {
            return "Viskit projects";
        }
    }
}
