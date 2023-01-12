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

import edu.nps.util.LogUtils;
import java.awt.Desktop;

import java.awt.EventQueue;
import java.awt.Taskbar;

import java.io.File;

import java.lang.reflect.InvocationTargetException;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;

import viskit.view.MainFrame;
import viskit.view.dialog.SettingsDialog;

/**
 * <p>MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu</p>
 * @author Mike Bailey
 * @since Sep 22, 2005 : 3:23:52 PM
 * @version $Id$
 */
public class EventGraphAssemblyComboMain {

    private static ImageIcon aboutIcon = null;

    /**
     * Viskit entry point from the command line, or introspection
     * @param args command line arguments if any
     */
    public static void main(final String[] args) {

        // Launch all GUI stuff on, or within the EDT
        try {
//            throw new InvocationTargetException(new Throwable("mail this error"));

            if (!EventQueue.isDispatchThread()) {
                SwingUtilities.invokeAndWait(() -> {
                    createGUI(args);
                });
            } else {
                SwingUtilities.invokeLater(() -> {
                    createGUI(args);
                });
            }

        } catch (InterruptedException | InvocationTargetException e) {
            LogUtils.getLogger(EventGraphAssemblyComboMain.class).error(e);

            if (e instanceof InvocationTargetException) {

                // not convinced we need to do this anymore.  A corrupted
                // viskitProject can cause an InvocationTargetException.  The
                // Apache Commons config files have behaved rather well and don't
                // need to be nuked as of late: 03 DEC 2014.
//                nukeDotViskit();

                // If we encounter this case, then uncomment printStackTrace() to
                // drill down on the cause.  Easier than setting a breakpoint and
                // debugging!
                e.printStackTrace(System.err);
            }

            try {
                URL url = new URL("mailto:" + VStatics.VISKIT_MAILING_LIST +
                        "?subject=Viskit%20startup%20error&body=log%20output:");
                
                String msg = "Viskit has experienced a startup glitch.  <br/>Please "
                        + "navigate to " + ViskitConfig.V_DEBUG_LOG.getPath() + " and "
                        + "email the log to "
                        + "<b><a href=\"" + url.toString() + "\">" + VStatics.VISKIT_MAILING_LIST + "</a></b>"
                        + "<br/><br/>Click the link to open up an email form, then copy and paste the log's contents";

                VStatics.showHyperlinkedDialog(null, e.toString(), url, msg, true);
            } catch (MalformedURLException ex) {
                LogUtils.getLogger(EventGraphAssemblyComboMain.class).error(ex);
            }
        }
    }

    /** Draconian process for restoring from a possibly corrupt, or out if synch
     * .viskit config directory in the user's profile space
     */
    public static void nukeDotViskit() {
        File dotViskit = ViskitConfig.VISKIT_CONFIG_DIR;
        if (dotViskit.exists()) {

            // Can't delete .viskit dir unless it's empty
            File[] files = dotViskit.listFiles();
            for (File file : files) {
                file.delete();
            }
            if (dotViskit.delete())
                LogUtils.getLogger(EventGraphAssemblyComboMain.class).info(dotViskit.getName() + " was found and deleted from your system.");

            LogUtils.getLogger(EventGraphAssemblyComboMain.class).info("Please restart Viskit");
        }
    }

    private static void createGUI(String[] args) {

        boolean isMac = VStatics.OPERATING_SYSTEM.toLowerCase().startsWith("mac os x");
        String initialFile = null;

        if (args.length > 0) {
            initialFile = args[0];
        }

        if (viskit.VStatics.debug) {
            LogUtils.getLogger(EventGraphAssemblyComboMain.class).info("***Inside EventGraphAssembly main: " + args.length);
        }
        setLandFandFonts();

        // Leave tooltips on the screen until mouse movement causes removal
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setDismissDelay(Integer.MAX_VALUE);  // never remove automatically

        JFrame mainFrame = new MainFrame(initialFile);
        VGlobals.instance().setMainAppWindow(mainFrame);

        if (isMac) {
            aboutIcon = new ImageIcon(EventGraphAssemblyComboMain.class.getResource("/viskit/images/ViskitLogo.gif"));
            setupMacGUI();
        }

        mainFrame.setVisible(true);
    }

    private static void setLandFandFonts() {
        String s = SettingsDialog.getLookAndFeel();
        try {
            if (s == null || s.isEmpty() || s.equalsIgnoreCase("default")) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } else if (s.equalsIgnoreCase("platform")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(s);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            LogUtils.getLogger(EventGraphAssemblyComboMain.class).error("Error setting Look and Feel to " + s);
        }
    }

    private static void setupMacGUI() {
        
        Desktop.getDesktop().setAboutHandler(e -> {
            Help help = VGlobals.instance().getHelp();
            help.aboutEventGraphEditor();
        });
        
        if (aboutIcon != null)
            Taskbar.getTaskbar().setIconImage(aboutIcon.getImage());
    }

} // end class file EventGraphAssemblyComboMain.java