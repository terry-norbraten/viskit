package viskit;

import edu.nps.util.LogUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.configuration.*;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.logging.log4j.Logger;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import viskit.doe.FileHandler;

/**
 * <p>Viskit Discrete Event Simulation (DES) Tool
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu</p>
 *
 * @author Mike Bailey
 * @since Mar 8, 2005
 * @since 11:09:07 AM
 * @version $Id$
 */
public class ViskitConfig {
    
    public static final String VISKIT_SHORT_APPLICATION_NAME = "Visual Simkit";
    public static final String VISKIT_FULL_APPLICATION_NAME  = "Visual Simkit (Viskit) Analyst Tool for Discrete Event Simulation (DES)";

    public static final File VISKIT_CONFIG_DIR = new File(System.getProperty("user.home"), ".viskit");
    public static final File V_CONFIG_FILE = new File(VISKIT_CONFIG_DIR, "vconfig.xml");
    public static final File C_APP_FILE = new File(VISKIT_CONFIG_DIR, "c_app.xml");
    public static final File C_GUI_FILE = new File(VISKIT_CONFIG_DIR, "c_gui.xml");
    public static final File V_DEBUG_LOG = new File(VISKIT_CONFIG_DIR, "debug.log");

    public static final String GUI_BEANSHELL_ERROR_DIALOG = "gui.beanshellerrordialog";
    public static final String BEANSHELL_ERROR_DIALOG_TITLE = GUI_BEANSHELL_ERROR_DIALOG + ".title";
    public static final String BEANSHELL_ERROR_DIALOG_LABEL = GUI_BEANSHELL_ERROR_DIALOG + ".label";
    public static final String BEANSHELL_ERROR_DIALOG_QUESTION = GUI_BEANSHELL_ERROR_DIALOG + ".question";
    public static final String BEANSHELL_ERROR_DIALOG_SESSIONCHECKBOX = GUI_BEANSHELL_ERROR_DIALOG + ".sessioncheckbox";
    public static final String BEANSHELL_ERROR_DIALOG_PREFERENCESCHECKBOX = GUI_BEANSHELL_ERROR_DIALOG + ".preferencescheckbox";
    public static final String BEANSHELL_ERROR_DIALOG_PREFERENCESTOOLTIP = GUI_BEANSHELL_ERROR_DIALOG + ".preferencestooltip";
    public static final String BEANSHELL_WARNING = "app.beanshell.warning";
    public static final String PROJECT_HOME_CLEAR_KEY = "app.projecthome";
    public static final String PROJECT_PATH_KEY = PROJECT_HOME_CLEAR_KEY + ".path[@dir]";
    public static final String PROJECT_NAME_KEY = PROJECT_HOME_CLEAR_KEY + ".name[@value]";
    public static final String X_CLASS_PATHS_CLEAR_KEY = "extraClassPaths";
    public static final String X_CLASS_PATHS_PATH_KEY = X_CLASS_PATHS_CLEAR_KEY + ".path";
    public static final String X_CLASS_PATHS_KEY = X_CLASS_PATHS_PATH_KEY + "[@value]";
    public static final String RECENT_EG_CLEAR_KEY = "history.EventGraphEditor.Recent";
    public static final String RECENT_ASSY_CLEAR_KEY = "history.AssemblyEditor.Recent";
    public static final String RECENT_PROJ_CLEAR_KEY = "history.ProjectEditor.Recent";
    public static final String EG_HISTORY_KEY = RECENT_EG_CLEAR_KEY + ".EventGraphFile";
    public static final String ASSY_HISTORY_KEY = RECENT_ASSY_CLEAR_KEY + ".AssemblyFile";
    public static final String PROJ_HISTORY_KEY = RECENT_PROJ_CLEAR_KEY + ".Project";
    public static final String EG_EDIT_VISIBLE_KEY = "app.tabs.EventGraphEditor[@visible]";
    public static final String ASSY_EDIT_VISIBLE_KEY = "app.tabs.AssemblyEditor[@visible]";
    public static final String ASSY_RUN_VISIBLE_KEY = "app.tabs.AssemblyRun[@visible]";
    public static final String DOE_EDIT_VISIBLE_KEY = "app.tabs.DesignOfExperiments[@visible]";
    public static final String CLUSTER_RUN_VISIBLE_KEY = "app.tabs.ClusterRun[@visible]";
    public static final String ANALYST_REPORT_VISIBLE_KEY = "app.tabs.AnalystReport[@visible]";
    public static final String DEBUG_MSGS_KEY = "app.debug";

    /** A cached path to satisfactorily compiled, or not, XML EventGraphs and their respective .class versions */
    public static final String CACHED_CLEAR_KEY = "Cached";
    public static final String CACHED_DIGEST_KEY = CACHED_CLEAR_KEY + ".EventGraphs[@digest]";
    public static final String CACHED_EVENTGRAPHS_KEY = CACHED_CLEAR_KEY + ".EventGraphs[@xml]";
    public static final String CACHED_EVENTGRAPHS_CLASS_KEY = CACHED_CLEAR_KEY + ".EventGraphs[@class]";
    public static final String CACHED_MISS_FILE_KEY = CACHED_CLEAR_KEY + ".Miss[@file]";
    public static final String CACHED_MISS_DIGEST_KEY = CACHED_CLEAR_KEY + ".Miss[@digest]";

    public static final String APP_MAIN_BOUNDS_KEY = "app.mainframe.size";
    public static final String LOOK_AND_FEEL_KEY = "gui.lookandfeel";
    public static final String PROJECT_TITLE_NAME = "gui.projecttitle.name[@value]";
    public static final String LAF_DEFAULT = "default";
    public static final String LAF_PLATFORM = "platform";

    private static ViskitConfig me;

    static final Logger LOG = LogUtils.getLogger(ViskitConfig.class);

    private final Map<String, XMLConfiguration> xmlConfigurations;
    private final Map<String, String> sessionHM;
    private CombinedConfiguration cc;
    private DefaultConfigurationBuilder builder;
    private XMLConfiguration projectXMLConifg = null;

    static {
        LOG.info("Welcome to the Visual Discrete Event Simulation (DES) toolkit (Viskit)");
        LOG.debug("VISKIT_CONFIG_DIR: " + VISKIT_CONFIG_DIR + " " + VISKIT_CONFIG_DIR.exists() + "\n");
    }

    public static synchronized ViskitConfig instance() {
        if (me == null) {
            me = new ViskitConfig();
        }
        return me;
    }

    private ViskitConfig() {
        try {
            if (!VISKIT_CONFIG_DIR.exists()) {
                VISKIT_CONFIG_DIR.mkdirs();
                LOG.info("Created dir: " + VISKIT_CONFIG_DIR);
            }
            File vconfigSrc = new File("configuration/" + V_CONFIG_FILE.getName());
            if (!V_CONFIG_FILE.exists()) {
                Files.copy(vconfigSrc.toPath(), V_CONFIG_FILE.toPath());
            }
            File cAppSrc = new File("configuration/" + C_APP_FILE.getName());
            if (!C_APP_FILE.exists()) {
                Files.copy(cAppSrc.toPath(), C_APP_FILE.toPath());
            }
            File cGuiSrc;
            if (VStatics.OPERATING_SYSTEM.toLowerCase().contains("os x"))
                cGuiSrc = new File("configuration/c_gui_mac.xml");
            else
                cGuiSrc = new File("configuration/" + C_GUI_FILE.getName());

            if (!C_GUI_FILE.exists()) {
                Files.copy(cGuiSrc.toPath(), C_GUI_FILE.toPath());
            }
        } catch (IOException ex) {
            LOG.error(ex);
        }
        xmlConfigurations = new HashMap<>();
        sessionHM = new HashMap<>();
        setDefaultConfig();
    }

    /** Builds, or rebuilds a default configuration */
    private void setDefaultConfig() {
        try {
            builder = new DefaultConfigurationBuilder(V_CONFIG_FILE);
            try {
                cc = builder.getConfiguration(true);
            } catch (ConfigurationException e) {
                LOG.error(e);
            }

            // Save off the indiv XML config for each prefix so we can write back
            int numConfigs = cc.getNumberOfConfigurations();
            Object obj;
            XMLConfiguration xc;
            HierarchicalConfiguration.Node n;
            for (int i = 0; i < numConfigs; i++) {
                obj = cc.getConfiguration(i);
                if (!(obj instanceof XMLConfiguration)) {
                    continue;
                }
                xc = (XMLConfiguration) obj;
                xc.setAutoSave(true);
                n = xc.getRoot();
                for (Object o : n.getChildren()) {
                    xmlConfigurations.put(((ConfigurationNode) o).getName(), xc);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
//            e.printStackTrace();
        }
    }

    /**
     * Rather screwy.  A decent design would allow the CombinedConfiguration obj
     * to do the saving, but it won't.
     *
     * @param key the ViskitConfig named key to set
     * @param val the value of this key
     */
    public void setVal(String key, String val) {
        String cfgKey = key.substring(0, key.indexOf('.'));
        XMLConfiguration xc = xmlConfigurations.get(cfgKey);
        xc.setProperty(key, val);
    }

    public void setSessionVal(String key, String val) {
        sessionHM.put(key, val);
    }

    public String getVal(String key) {
        String retS = sessionHM.get(key);
        if (retS != null && retS.length() > 0) {
            return retS;
        }

        return cc.getString(key);
    }

    public String[] getConfigValues(String key) {
        return cc.getStringArray(key);
    }

    /** @param f a Viskit project file */
    public void setProjectXMLConfig(String f) {
        try {
            projectXMLConifg = new XMLConfiguration(f);
        } catch (ConfigurationException ce) {
            LOG.error(ce);
        }
        projectXMLConifg.setAutoSave(true);
        cc.addConfiguration(projectXMLConifg, "proj");
        xmlConfigurations.put("proj", projectXMLConifg);
    }

    /** @return the XMLConfiguration for Viskit project */
    public XMLConfiguration getProjectXMLConfig() {
        return projectXMLConifg;
    }

    /** Remove a project's XML configuration upon closing a Viskit project
     * @param projConfig the project configuration to remove
     */
    public void removeProjectXMLConfig(XMLConfiguration projConfig) {
        cc.removeConfiguration(projConfig);
        xmlConfigurations.remove("proj");
    }

    /** @return the XMLConfiguration for Viskit app */
    public XMLConfiguration getViskitAppConfig() {
        return (XMLConfiguration) cc.getConfiguration("app");
    }

    /** @return the XMLConfiguration for Viskit gui */
    public XMLConfiguration getViskitGuiConfig() {
        return (XMLConfiguration) cc.getConfiguration("gui");
    }

    /** Used to clear all Viskit Configuration information to create a new
     * Viskit Project
     */
    public void clearViskitConfig() {
        setVal(ViskitConfig.PROJECT_PATH_KEY, "");
        setVal(ViskitConfig.PROJECT_NAME_KEY, "");
        getViskitAppConfig().clearTree(ViskitConfig.RECENT_EG_CLEAR_KEY);
        getViskitAppConfig().clearTree(ViskitConfig.RECENT_ASSY_CLEAR_KEY);

        // TODO: Other clears?
    }

    public void resetViskitConfig() {
        me = null;
    }

    public void cleanup() {
        // Lot of hoops to pretty-fy config xml files
        Document doc;
        Format form = Format.getPrettyFormat();
        XMLOutputter xout = new XMLOutputter(form);
        try {

            // For c_app.xml
            doc = FileHandler.unmarshallJdom(C_APP_FILE);
            xout.output(doc, new FileWriter(C_APP_FILE));

            // For c_gui.xml
            doc = FileHandler.unmarshallJdom(C_GUI_FILE);
            xout.output(doc, new FileWriter(C_GUI_FILE));

            // For vconfig.xml
            doc = FileHandler.unmarshallJdom(V_CONFIG_FILE);
            xout.output(doc, new FileWriter(V_CONFIG_FILE));

            // For the current Viskit project file
            doc = FileHandler.unmarshallJdom(VGlobals.instance().getCurrentViskitProject().getProjectFile());
            xout.output(doc, new FileWriter(VGlobals.instance().getCurrentViskitProject().getProjectFile()));
        } catch (Exception e) {
            LOG.error("Bad jdom op: " + e.getMessage());
        }
    }
}
