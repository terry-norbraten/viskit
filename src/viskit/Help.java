package viskit;

import edu.nps.util.LogUtils;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import javax.help.HelpBroker;
import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.SwingHelpUtilities;
import viskit.util.BrowserLauncher;
import viskit.util.Version;

/**
 * @version $Id$
 * @author  ahbuss
 */
public class Help {

    public static final Version VERSION = new Version("version.txt");
    public static final String VERSION_STRING = VERSION.getVersionString();
    public static final String CR = "<br>";
    public static final String ABOUT_EG_STRING =
            "Viskit Event Graph Editor" + CR + "   version " + VERSION_STRING + CR
            + "last modified: " + VERSION.getLastModified() + CR + CR;
    public static final String ABOUT_ASSEMBLY_STRING =
            "Viskit Assembly Editor" + CR + "   version " + VERSION_STRING + CR
            + "last modified: " + VERSION.getLastModified() + CR + CR;
    public static final String SIMKIT_URL = "https://github.com/ahbuss/Simkit/";
    public static final String VISKIT_URL = "https://gitlab.nps.edu/tdnorbra/viskit/";
    public static final String BUGZILLA_URL = "https://diana.nps.edu/bugzilla/";
    public static final String DEVELOPERS =
            "Copyright &copy; 2004-2022 under the Lesser GNU Public License (LGPL)" + CR + CR
            + "<b>Developers:</b>" + CR
            + "&nbsp;&nbsp;&nbsp;Arnold Buss" + CR
            + "&nbsp;&nbsp;&nbsp;Mike Bailey" + CR
            + "&nbsp;&nbsp;&nbsp;Rick Goldberg" + CR
            + "&nbsp;&nbsp;&nbsp;Don McGregor" + CR
            + "&nbsp;&nbsp;&nbsp;Don Brutzman" + CR
            + "&nbsp;&nbsp;&nbsp;Patrick Sullivan" + CR
            + "&nbsp;&nbsp;&nbsp;Terry Norbraten";
    public static final String SIMKIT_PAGE =
            CR
            + "Visit the Simkit home page at" + CR
            + LinkURLString(SIMKIT_URL) + CR;
    public static final String VISKIT_PAGE = CR
            + "Visit the Viskit home page at" + CR
            + LinkURLString(VISKIT_URL);
    public static final String VERSIONS =
            "<hr>Simkit Version: "
            + simkit.Version.getVersion()
            + CR + "Java version: "
            + System.getProperty("java.version");
    public static final String BUGZILLA_PAGE = CR
            + "Please register for the Viskit Issue tracker:" + CR
            + LinkURLString(BUGZILLA_URL);

    private HelpBroker hb;

    // A strange couple of things to support JavaHelp's rather strange design for CSH use:
    private Component TUTORIAL_COMPONENT;
    private ActionListener TUTORIAL_LISTENER_LAUNCHER;

    private Component parent;
    private Icon icon;
    private JEditorPane aboutEGEditorPane;
    private JEditorPane aboutAssemblyEditorPane;

    /** Creates a new instance of Help
     * @param parent main frame to center on
     */
    public Help(Component parent) {
        this.parent = parent;

        ClassLoader cl = viskit.Help.class.getClassLoader();
        URL helpSetURL = HelpSet.findHelpSet(cl, "viskit/javahelp/vHelpSet.hs");
        try {
            hb = new HelpSet(null, helpSetURL).createHelpBroker();
        } catch (HelpSetException e) {
//        e.printStackTrace();
            LogUtils.getLogger(Help.class).error(e);
        }

        // Here we're setting up the action event peripherals for the tutorial menu selection
        TUTORIAL_LISTENER_LAUNCHER = new CSH.DisplayHelpFromSource(hb);
        TUTORIAL_COMPONENT = new Button();

        CSH.setHelpIDString(TUTORIAL_COMPONENT, "hTutorial");

        icon = new ImageIcon(
                VGlobals.instance().getWorkClassLoader().getResource(
                "viskit/images/ViskitLogo.png"));

        BrowserLauncher bl = new BrowserLauncher(null);
        SwingHelpUtilities.setContentViewerUI("viskit.util.BrowserLauncher");

        aboutEGEditorPane = new JEditorPane();
        aboutEGEditorPane.addHyperlinkListener(bl);
        aboutEGEditorPane.setContentType("text/html");
        aboutEGEditorPane.setEditable(false);
        aboutEGEditorPane.setText(ABOUT_EG_STRING
                + DEVELOPERS + CR + VISKIT_PAGE //+ BUGZILLA_PAGE
                + SIMKIT_PAGE + VERSIONS);

        aboutAssemblyEditorPane = new JEditorPane();
        aboutAssemblyEditorPane.addHyperlinkListener(bl);
        aboutAssemblyEditorPane.setContentType("text/html");
        aboutAssemblyEditorPane.setEditable(false);
        aboutAssemblyEditorPane.setText(ABOUT_ASSEMBLY_STRING
                + DEVELOPERS + CR + VISKIT_PAGE //+ BUGZILLA_PAGE
                + SIMKIT_PAGE);
    }

    public void aboutEventGraphEditor() {
        JOptionPane.showMessageDialog(parent, aboutEGEditorPane,
                "About Viskit Event Graph Editor...",
                JOptionPane.OK_OPTION, icon);
    }

    public void aboutAssemblyEditor() {
        JOptionPane.showMessageDialog(parent, aboutAssemblyEditorPane,
                "About Viskit Assembly Editor...",
                JOptionPane.OK_OPTION, icon);
    }

    public void doContents() {
        hb.setDisplayed(true);
        hb.setCurrentView("TOC");
    }

    public void doSearch() {
        hb.setDisplayed(true);
        hb.setCurrentView("Search");
    }

    public void doTutorial() {
        ActionEvent ae = new ActionEvent(TUTORIAL_COMPONENT, 0, "tutorial");
        TUTORIAL_LISTENER_LAUNCHER.actionPerformed(ae);
    }

    public void mainFrameLocated(Rectangle bounds) {
        Point p = new Point(bounds.x, bounds.y);
        Dimension d = new Dimension(bounds.width, bounds.height);
        Dimension hd = new Dimension(1200, 700);
        hb.setSize(hd);
        p.x = p.x + d.width / 2 - hd.width / 2;
        p.y = p.y + d.height / 2 - hd.height / 2;
        hb.setLocation(p);
    }

    public static String LinkURLString(String urlString) {
        String linkString = "";
        try {
            URL url = new URL(urlString);
            linkString = "<a href = " + url + ">" + url + "</a>";
        } catch (MalformedURLException ex) {}
        return linkString;

    }

    public static void main(String[] args) {
        System.out.println("Viskit DES interface: " + VERSION);
    }
}
