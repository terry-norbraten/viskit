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

package viskit.view;

import viskit.jgraph.VgraphAssemblyComponentWrapper;
import actions.ActionIntrospector;
import actions.ActionUtilities;
import edu.nps.util.LogUtils;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import viskit.control.AssemblyController;
import viskit.control.AssemblyControllerImpl;
import viskit.util.FileBasedAssyNode;
import viskit.Help;
import viskit.model.ModelEvent;
import viskit.VGlobals;
import viskit.VStatics;
import viskit.ViskitProject;
import viskit.control.RecentProjFileSetListener;
import viskit.doe.LocalBootLoader;
import viskit.images.AdapterIcon;
import viskit.images.PropChangListenerImageIcon;
import viskit.images.PropChangeListenerIcon;
import viskit.images.SimEventListenerIcon;
import viskit.jgraph.vGraphAssemblyModel;
import viskit.model.*;
import viskit.mvc.mvcAbstractJFrameView;
import viskit.mvc.mvcController;
import viskit.mvc.mvcModel;
import viskit.mvc.mvcModelEvent;
import viskit.mvc.mvcRecentFileListener;
import viskit.util.AssemblyFileFilter;
import viskit.view.dialog.EventGraphNodeInspectorDialog;
import viskit.view.dialog.RecentFilesDialog;
import viskit.view.dialog.SimEventListenerConnectionInspectorDialog;
import viskit.view.dialog.SettingsDialog;
import viskit.view.dialog.PclNodeInspectorDialog;
import viskit.view.dialog.AdapterConnectionInspectorDialog;
import viskit.view.dialog.PclEdgeInspectorDialog;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM)  2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * @author Mike Bailey
 * @since May 10, 2004
 * @since 2:07:37 PM
 * @version $Id$
 */
public class AssemblyViewFrame extends mvcAbstractJFrameView implements AssemblyView, DragStartListener {

    /** Modes we can be in--selecting items, adding nodes to canvas, drawing arcs, etc. */
    public static final int SELECT_MODE = 0;
    public static final int ADAPTER_MODE = 1;
    public static final int SIMEVLIS_MODE = 2;
    public static final int PCL_MODE = 3;

    // The view needs access to this
    public JButton runButt;

    JMenu openRecentAssyMenu, openRecentProjMenu;

    private final static String FRAME_DEFAULT_TITLE = " Viskit Assembly Editor";

    private final String FULLPATH = VStatics.FULL_PATH;
    private final String CLEARPATHFLAG = VStatics.CLEAR_PATH_FLAG;
    private final Color background = new Color(0xFB, 0xFB, 0xE5);

    /** Toolbar for dropping icons, connecting, etc. */
    private JTabbedPane tabbedPane;
    private JToolBar toolBar;
    private JToggleButton selectMode;
    private JToggleButton adapterMode,  simEventListenerMode,  propChangeListenerMode;
    private LegoTree lTree,  pclTree;
    private JMenuBar myMenuBar;
    private JMenuItem quitMenuItem;
    private RecentProjFileSetListener recentProjFileSetListener;

    private int untitledCount = 0;

    public AssemblyViewFrame(mvcController controller) {
        super(FRAME_DEFAULT_TITLE);
        initMVC(controller);   // set up mvc linkages
        initUI();   // build widgets
    }

    public JComponent getContent() {
        return (JComponent) getContentPane();
    }

    public JMenuBar getMenus() {
        return myMenuBar;
    }

    public JMenuItem getQuitMenuItem() {
        return quitMenuItem;
    }

    /**
     * Initialize the MVC connections
     * @param ctrl the controller for this view
     */
    private void initMVC(mvcController ctrl) {
        setController(ctrl);
    }

    /**
     * Initialize the user interface
     */
    private void initUI() {

        buildMenus();
        buildToolbar();

        // Build here to prevent NPE from EGContrlr
        buildTreePanels();

        // Set up a assemblyEditorContent level pane that will be the content pane. This
        // has a border layout, and contains the toolbar on the assemblyEditorContent and
        // the main splitpane underneath.

        // assemblyEditorContent level panel
        getContent().setLayout(new BorderLayout());
        getContent().add(getToolBar(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new TabSelectionHandler());

        getContent().add(tabbedPane, BorderLayout.CENTER);
        getContent().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    public VgraphAssemblyComponentWrapper getCurrentVgacw() {
        JSplitPane jsplt = (JSplitPane) tabbedPane.getSelectedComponent();
        if (jsplt == null) {
            return null;
        }

        JScrollPane jSP = (JScrollPane) jsplt.getRightComponent();
        return (VgraphAssemblyComponentWrapper) jSP.getViewport().getComponent(0);
    }

    public Component getCurrentJgraphComponent() {
        VgraphAssemblyComponentWrapper vcw = getCurrentVgacw();
        if (vcw == null || vcw.drawingSplitPane == null) {return null;}
        return vcw.drawingSplitPane.getRightComponent();
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    public void setToolBar(JToolBar toolBar) {
        this.toolBar = toolBar;
    }

    /**
     * @return the recentProjFileSetListener
     */
    public RecentProjFileSetListener getRecentProjFileSetListener() {
        return recentProjFileSetListener;
    }

    /** Tab switch: this will come in with the newly selected tab in place */
    class TabSelectionHandler implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            VgraphAssemblyComponentWrapper myVgacw = getCurrentVgacw();

            if (myVgacw == null) {     // last tab has been closed
                setSelectedAssemblyName(null);
                return;
            }

            // Key to getting the LEGOs tree panel in each tab view
            myVgacw.drawingSplitPane.setLeftComponent(myVgacw.trees);

            setModel((mvcModel) myVgacw.assyModel); // hold on locally
            getController().setModel(getModel()); // tell controller
            AssemblyModelImpl mod = (AssemblyModelImpl) getModel();

            if (mod.getLastFile() != null) {
                ((AssemblyControllerImpl) getController()).initOpenAssyWatch(mod.getLastFile(), mod.getJaxbRoot());
            }

            GraphMetaData gmd = mod.getMetaData();
            if (gmd != null) {
                setSelectedAssemblyName(gmd.name);
            } else if (viskit.VStatics.debug) {
                System.err.println("error: AssemblyViewFrame gmd null..");
            }
        }
    }

    class RecentAssyFileListener implements mvcRecentFileListener {

        @Override
        public void listChanged() {
            AssemblyController acontroller = (AssemblyController) getController();
            Set<File> lis = acontroller.getRecentAssyFileSet();
            openRecentAssyMenu.removeAll();
            lis.stream().filter(fullPath -> !(!fullPath.exists())).map(fullPath -> {
                String nameOnly = fullPath.getName();
                Action act = new ParameterizedAssyAction(nameOnly);
                act.putValue(FULLPATH, fullPath);
                JMenuItem mi = new JMenuItem(act);
                mi.setToolTipText(fullPath.getPath());
                return mi;
            }).forEachOrdered(mi -> {
                openRecentAssyMenu.add(mi);
            });
            if (!lis.isEmpty()) {
                openRecentAssyMenu.add(new JSeparator());
                Action act = new ParameterizedAssyAction("clear");
                act.putValue(FULLPATH, CLEARPATHFLAG);  // flag
                JMenuItem mi = new JMenuItem(act);
                mi.setToolTipText("Clear this list");
                openRecentAssyMenu.add(mi);
            }
        }
    }

    class ParameterizedAssyAction extends javax.swing.AbstractAction {

        ParameterizedAssyAction(String s) {
            super(s);
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            AssemblyController acontroller = (AssemblyController) getController();

            File fullPath;
            Object obj = getValue(VStatics.FULL_PATH);
            if (obj instanceof String)
                fullPath = new File((String) obj);
            else
                fullPath = (File) obj;

            if (fullPath.getPath().equals(CLEARPATHFLAG)) {
                acontroller.clearRecentAssyFileList();
            } else {
                acontroller.openRecentAssembly(fullPath);
            }
        }
    }

    private void buildMenus() {
        AssemblyController controller = (AssemblyController) getController();

        controller.addRecentAssyFileSetListener(new RecentAssyFileListener());

        int accelMod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        // Set up file menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        fileMenu.add(buildMenuItem(controller, "newProject", "New Viskit Project", KeyEvent.VK_V,
                KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_DOWN_MASK)));
        fileMenu.add(buildMenuItem(controller, "zipAndMailProject", "Zip/Mail Viskit Project", KeyEvent.VK_Z,
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.ALT_DOWN_MASK)));
        fileMenu.add(buildMenuItem(controller, "newAssembly", "New Assembly", KeyEvent.VK_N,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, accelMod)));
        fileMenu.addSeparator();

        fileMenu.add(buildMenuItem(controller, "open", "Open", KeyEvent.VK_O,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, accelMod)));
        fileMenu.add(openRecentAssyMenu = buildMenu("Open Recent Assembly"));
        fileMenu.add(buildMenuItem(this, "openProject", "Open Project", KeyEvent.VK_P,
                KeyStroke.getKeyStroke(KeyEvent.VK_P, accelMod)));
        fileMenu.add(openRecentProjMenu = buildMenu("Open Recent Project"));

        // The EGViewFrame will get this listener for it's menu item of the same
        recentProjFileSetListener = new RecentProjFileSetListener();
        getRecentProjFileSetListener().addMenuItem(openRecentProjMenu);
        controller.addRecentProjFileSetListener(getRecentProjFileSetListener());

        // Bug fix: 1195
        fileMenu.add(buildMenuItem(controller, "close", "Close", null,
                KeyStroke.getKeyStroke(KeyEvent.VK_W, accelMod)));
        fileMenu.add(buildMenuItem(controller, "closeAll", "Close All", null, null));
        fileMenu.add(buildMenuItem(controller, "save", "Save", KeyEvent.VK_S,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMod)));
        fileMenu.add(buildMenuItem(controller, "saveAs", "Save as...", KeyEvent.VK_A, null));
        fileMenu.addSeparator();

        fileMenu.add(buildMenuItem(controller, "showXML", "View Saved XML", KeyEvent.VK_X, null));
        fileMenu.add(buildMenuItem(controller, "generateJavaSource", "Generate Java Source", KeyEvent.VK_J,
                KeyStroke.getKeyStroke(KeyEvent.VK_J, accelMod)));
        fileMenu.add(buildMenuItem(controller, "captureWindow", "Save Screen Image", KeyEvent.VK_I,
                KeyStroke.getKeyStroke(KeyEvent.VK_I, accelMod)));
        fileMenu.add(buildMenuItem(controller, "compileAssemblyAndPrepSimRunner", "Initialize Assembly", KeyEvent.VK_C,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK)));

        // TODO: Unknown as to what this does exactly
        fileMenu.add(buildMenuItem(controller, "export2grid", "Export to Cluster Format", KeyEvent.VK_C, null));
        ActionIntrospector.getAction(controller, "export2grid").setEnabled(false);
        fileMenu.addSeparator();

        fileMenu.add(buildMenuItem(controller, "settings", "Settings", null, null));
        fileMenu.addSeparator();

        fileMenu.add(quitMenuItem = buildMenuItem(controller, "quit", "Exit", KeyEvent.VK_Q,
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, accelMod)));

        // Set up edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        editMenu.add(buildMenuItem(controller, "undo", "Undo", KeyEvent.VK_Z,
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, accelMod)));
        editMenu.add(buildMenuItem(controller, "redo", "Redo", KeyEvent.VK_Y,
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, accelMod)));

        ActionIntrospector.getAction(controller, "undo").setEnabled(false);
        ActionIntrospector.getAction(controller, "redo").setEnabled(false);
        editMenu.addSeparator();

        // the next four are disabled until something is selected
        editMenu.add(buildMenuItem(controller, "cut", "Cut", KeyEvent.VK_X,
                KeyStroke.getKeyStroke(KeyEvent.VK_X, accelMod)));
        editMenu.getItem(editMenu.getItemCount()-1).setToolTipText("Cut is not supported in Viskit.");
        editMenu.add(buildMenuItem(controller, "copy", "Copy", KeyEvent.VK_C,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, accelMod)));
        editMenu.add(buildMenuItem(controller, "paste", "Paste Events", KeyEvent.VK_V,
                KeyStroke.getKeyStroke(KeyEvent.VK_V, accelMod)));
        editMenu.add(buildMenuItem(controller, "remove", "Delete", KeyEvent.VK_DELETE,
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, accelMod)));

        // These start off being disabled, until something is selected
        ActionIntrospector.getAction(controller, "cut").setEnabled(false);
        ActionIntrospector.getAction(controller, "remove").setEnabled(false);
        ActionIntrospector.getAction(controller, "copy").setEnabled(false);
        ActionIntrospector.getAction(controller, "paste").setEnabled(false);
        editMenu.addSeparator();

        editMenu.add(buildMenuItem(controller, "newEventGraphNode", "Add Event Graph...", KeyEvent.VK_G, null));
        editMenu.add(buildMenuItem(controller, "newPropChangeListenerNode", "Add Property Change Listener...", KeyEvent.VK_L, null));
        editMenu.addSeparator();

        editMenu.add(buildMenuItem(controller, "editGraphMetaData", "Edit Properties...", KeyEvent.VK_E,
                KeyStroke.getKeyStroke(KeyEvent.VK_E, accelMod)));

        // Create a new menu bar and add the menus we created above to it
        myMenuBar = new JMenuBar();
        myMenuBar.add(fileMenu);
        myMenuBar.add(editMenu);

        Help help = VGlobals.instance().getHelp();
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        helpMenu.add(buildMenuItem(help, "doContents", "Contents", KeyEvent.VK_C, null));
        helpMenu.add(buildMenuItem(help, "doSearch", "Search", KeyEvent.VK_S, null));
        helpMenu.addSeparator();

        helpMenu.add(buildMenuItem(help, "doTutorial", "Tutorial", KeyEvent.VK_T, null));
        helpMenu.add(buildMenuItem(help, "aboutAssemblyEditor", "About...", KeyEvent.VK_A, null));

        myMenuBar.add(helpMenu);
    }

    private JMenu buildMenu(String name) {
        return new JMenu(name);
    }

    // Use the actions package
    private JMenuItem buildMenuItem(Object source, String method, String name, Integer mn, KeyStroke accel) {
        Action a = ActionIntrospector.getAction(source, method);

        Map<String, Object> map = new HashMap<>();
        if (mn != null) {
            map.put(Action.MNEMONIC_KEY, mn);
        }
        if (accel != null) {
            map.put(Action.ACCELERATOR_KEY, accel);
        }
        if (name != null) {
            map.put(Action.NAME, name);
        }
        if (!map.isEmpty()) {
            ActionUtilities.decorateAction(a, map);
        }

        return ActionUtilities.createMenuItem(a);
    }

    /**
     * @return the current mode--select, add, arc, cancelArc
     */
    public int getCurrentMode() {
        // Use the button's selected status to figure out what mode
        // we are in.

        if (selectMode.isSelected()) {
            return SELECT_MODE;
        }
        if (adapterMode.isSelected()) {
            return ADAPTER_MODE;
        }
        if (simEventListenerMode.isSelected()) {
            return SIMEVLIS_MODE;
        }
        if (propChangeListenerMode.isSelected()) {
            return PCL_MODE;
        }
        LogUtils.getLogger(AssemblyViewFrame.class).error("assert false : \"getCurrentMode()\"");
        return 0;
    }

    private void buildToolbar() {
        ButtonGroup modeButtonGroup = new ButtonGroup();
        setToolBar(new JToolBar());

        // Buttons for what mode we are in

        selectMode = makeJTButton(null, "viskit/images/selectNode.png",
                "Select items on the graph");
        Border defBor = selectMode.getBorder();
        selectMode.setBorder(BorderFactory.createCompoundBorder(defBor, BorderFactory.createLineBorder(Color.lightGray, 2)));

        adapterMode = makeJTButton(null, new AdapterIcon(24, 24),
                "Connect assemblies with adapter pattern");
        defBor = adapterMode.getBorder();
        adapterMode.setBorder(BorderFactory.createCompoundBorder(defBor, BorderFactory.createLineBorder(new Color(0xce, 0xce, 0xff), 2)));

        simEventListenerMode = makeJTButton(null, new SimEventListenerIcon(24, 24),
                "Connect assemblies through a SimEvent listener pattern");
        defBor = simEventListenerMode.getBorder();
        simEventListenerMode.setBorder(BorderFactory.createCompoundBorder(defBor, BorderFactory.createLineBorder(new Color(0xce, 0xce, 0xff), 2)));

        propChangeListenerMode = makeJTButton(null, new PropChangeListenerIcon(24, 24),
                "Connect a property change listener to a SimEntity");
        defBor = propChangeListenerMode.getBorder();
        propChangeListenerMode.setBorder(BorderFactory.createCompoundBorder(defBor, BorderFactory.createLineBorder(new Color(0xff, 0xc8, 0xc8), 2)));

        JButton zoomIn = makeButton(null, "viskit/images/ZoomIn24.gif",
                "Zoom in on the graph");

        JButton zoomOut = makeButton(null, "viskit/images/ZoomOut24.gif",
                "Zoom out on the graph");

        Action runAction = ActionIntrospector.getAction(getController(), "compileAssemblyAndPrepSimRunner");
        runButt = makeButton(runAction, "viskit/images/Play24.gif",
                "Compile, initialize the assembly and prepare the Simulation Runner");
        modeButtonGroup.add(selectMode);
        modeButtonGroup.add(adapterMode);
        modeButtonGroup.add(simEventListenerMode);
        modeButtonGroup.add(propChangeListenerMode);

        // Make selection mode the default mode
        selectMode.setSelected(true);

        getToolBar().add(new JLabel("Mode: "));

        getToolBar().add(selectMode);
        getToolBar().addSeparator(new Dimension(5, 24));
        getToolBar().add(adapterMode);
        getToolBar().addSeparator(new Dimension(5, 24));
        getToolBar().add(simEventListenerMode);
        getToolBar().addSeparator(new Dimension(5, 24));
        getToolBar().add(propChangeListenerMode);

        getToolBar().addSeparator(new Dimension(24, 24));
        getToolBar().add(new JLabel("Zoom: "));
        getToolBar().add(zoomIn);
        getToolBar().addSeparator(new Dimension(5, 24));
        getToolBar().add(zoomOut);
        getToolBar().addSeparator(new Dimension(24, 24));
        
        JLabel initializeLabel = new JLabel ("  Initialize assembly runner: ");
        initializeLabel.setToolTipText("First initialize assembly runner from Assembly tab");
        getToolBar().add(initializeLabel);
        getToolBar().add(runButt);

        // Let the opening of Assembliess make this visible
        getToolBar().setVisible(false);

        zoomIn.addActionListener((ActionEvent e) -> {
            getCurrentVgacw().setScale(getCurrentVgacw().getScale() + 0.1d);
        });
        zoomOut.addActionListener((ActionEvent e) -> {
            getCurrentVgacw().setScale(Math.max(getCurrentVgacw().getScale() - 0.1d, 0.1d));
        });

        // These buttons perform operations that are internal to our view class, and therefore their operations are
        // not under control of the application controller (Controller.java).  Small, simple anonymous inner classes
        // such as these have been certified by the Surgeon General to be only minimally detrimental to code health.

        class PortsVisibleListener implements ActionListener {

            private final boolean tOrF;

            PortsVisibleListener(boolean tOrF) {
                this.tOrF = tOrF;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                getCurrentVgacw().setPortsVisible(tOrF);
            }
        }

        PortsVisibleListener portsOn = new PortsVisibleListener(true);
        PortsVisibleListener portsOff = new PortsVisibleListener(false);
        selectMode.addActionListener(portsOff);
        adapterMode.addActionListener(portsOn);
        simEventListenerMode.addActionListener(portsOn);
        propChangeListenerMode.addActionListener(portsOn);
    }

    private JToggleButton makeJTButton(Action a, String icPath, String tt) {
        JToggleButton jtb;
        if (a != null) {
            jtb = new JToggleButton(a);
        } else {
            jtb = new JToggleButton();
        }
        return (JToggleButton) buttonCommon(jtb, icPath, tt);
    }

    private JToggleButton makeJTButton(Action a, Icon ic, String tt) {
        JToggleButton jtb;
        if (a != null) {
            jtb = new JToggleButton(a);
        } else {
            jtb = new JToggleButton();
        }
        jtb.setIcon(ic);
        return (JToggleButton) buttonCommon2(jtb, tt);
    }

    private JButton makeButton(Action a, String icPath, String tt) {
        JButton b;
        if (a != null) {
            b = new JButton(a);
        } else {
            b = new JButton();
        }
        return (JButton) buttonCommon(b, icPath, tt);
    }

    private AbstractButton buttonCommon(AbstractButton b, String icPath, String tt) {
        b.setIcon(new ImageIcon(VGlobals.instance().getWorkClassLoader().getResource(icPath)));
        return buttonCommon2(b, tt);
    }

    private AbstractButton buttonCommon2(AbstractButton b, String tt) {
        b.setToolTipText(tt);
        b.setBorder(BorderFactory.createEtchedBorder());
        b.setText(null);
        return b;
    }

    @Override
    public void addTab(AssemblyModel mod) {
        vGraphAssemblyModel vGAmod = new vGraphAssemblyModel();
        VgraphAssemblyComponentWrapper graphPane = new VgraphAssemblyComponentWrapper(vGAmod, this);
        vGAmod.setjGraph(graphPane);                               // todo fix this

        graphPane.assyModel = mod;
        graphPane.trees = treePanels;
        graphPane.trees.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        graphPane.trees.setMinimumSize(new Dimension(20, 20));
        graphPane.trees.setDividerLocation(250);

        // Split pane with the canvas on the right and a split pane with LEGO tree and PCLs on the left.
        JScrollPane jscrp = new JScrollPane(graphPane);

        graphPane.drawingSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPane.trees, jscrp);

        // This is the key to getting the jgraph half to come up appropriately
        // wide by giving the right component (JGraph side) most of the usable
        // extra space in this SplitPlane -> 25%
        graphPane.drawingSplitPane.setResizeWeight(0.25);
        graphPane.drawingSplitPane.setOneTouchExpandable(true);

        try {
            graphPane.getDropTarget().addDropTargetListener(new vDropTargetAdapter());
        } catch (TooManyListenersException tmle) {
            LogUtils.getLogger(AssemblyViewFrame.class).error(tmle);
        }

        // the view holds only one assyModel, so it gets overwritten with each tab
        // but this call serves also to register the view with the passed assyModel
        // by virtue of calling stateChanged()
        tabbedPane.add("untitled" + untitledCount++, graphPane.drawingSplitPane);
        tabbedPane.setSelectedComponent(graphPane.drawingSplitPane); // bring to front

        // Now expose the Assembly toolbar
        Runnable r = () -> {
            getToolBar().setVisible(true);
        };
        SwingUtilities.invokeLater(r);
    }

    @Override
    public void delTab(AssemblyModel mod) {
        Component[] ca = tabbedPane.getComponents();

        for (int i = 0; i < ca.length; i++) {
            JSplitPane jsplt = (JSplitPane) ca[i];
            JScrollPane jsp = (JScrollPane) jsplt.getRightComponent();
            VgraphAssemblyComponentWrapper vgacw = (VgraphAssemblyComponentWrapper) jsp.getViewport().getComponent(0);
            if (vgacw.assyModel == mod) {
                tabbedPane.remove(i);
                vgacw.isActive = false;

                // Don't allow operation of tools with no Assembly tab in view (NPEs)
                if (tabbedPane.getTabCount() == 0) {
                    Runnable r = () -> {
                        getToolBar().setVisible(false);
                    };
                    SwingUtilities.invokeLater(r);
                }
                return;
            }
        }
    }

    @Override
    public AssemblyModel[] getOpenModels() {
        Component[] ca = tabbedPane.getComponents();
        AssemblyModel[] vm = new AssemblyModel[ca.length];
        for (int i = 0; i < vm.length; i++) {
            JSplitPane jsplt = (JSplitPane) ca[i];
            JScrollPane jsp = (JScrollPane) jsplt.getRightComponent();
            VgraphAssemblyComponentWrapper vgacw = (VgraphAssemblyComponentWrapper) jsp.getViewport().getComponent(0);
            vm[i] = vgacw.assyModel;
        }
        return vm;
    }

    /** Rebuilds the Listener Event Graph Object (LEGO) tree view */
    public void rebuildLEGOTreePanels() {
        lTree.clear();
        JSplitPane treeSplit = buildTreePanels();
        getCurrentVgacw().drawingSplitPane.setTopComponent(treeSplit);
        treeSplit.setDividerLocation(250);
        lTree.repaint();
    }
    private JSplitPane treePanels;

    private JSplitPane buildTreePanels() {

        lTree = new LegoTree("simkit.BasicSimEntity", "viskit/images/assembly.png",
                this, "Drag an Event Graph onto the canvas to add it to the assembly");

        pclTree = new LegoTree("java.beans.PropertyChangeListener", new PropChangListenerImageIcon(20, 20),
                this, "Drag a PropertyChangeListener onto the canvas to add it to the assembly");

        String[] extraCP = SettingsDialog.getExtraClassPath();

        if (extraCP != null) {
            File file;
            for (String path : extraCP) { // tbd same for pcls
                file = new File(path);
                if (!file.exists()) {

                    // Allow a relative path for Diskit-Test (Diskit)
                    if (path.contains("..")) {
                        file = new File(VGlobals.instance().getCurrentViskitProject().getProjectRoot().getParent() + "/" + path.replaceFirst("../", ""));
                    }
                }

                if (file.exists()) {
                    addEventGraphsToLegoTree(file, file.isDirectory());
                }
            }
        }

        // Now add our EventGraphs path for LEGO tree inclusion of our SimEntities
        VGlobals vGlobals = VGlobals.instance();
        ViskitProject vkp = vGlobals.getCurrentViskitProject();

        // A fresh (reset) LocalBootLoader will be instantiated
        // here when compiling EGs for the first time, or when the
        // SimkitXML2Java translator attempts to resolve a ParameterMap
        addEventGraphsToLegoTree(vkp.getEventGraphsDir(), true);

        // Now load the simkit.jar and diskit.jar from where ever they happen to
        // be located on the classpath if present
        String[] classPath = ((LocalBootLoader) vGlobals.getWorkClassLoader()).getClassPath();
        for (String path : classPath) {
            if (path.contains("simkit.jar") || (path.contains("diskit.jar"))) {
                addEventGraphsToLegoTree(new File(path), false);
                addPCLsToLegoTree(new File(path), false);
            }
        }

        LegosPanel lPan = new LegosPanel(lTree);
        PropChangeListenersPanel pclPan = new PropChangeListenersPanel(pclTree);

        lTree.setBackground(background);
        pclTree.setBackground(background);

        treePanels = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lPan, pclPan);
        treePanels.setBorder(null);
        treePanels.setOneTouchExpandable(true);

        pclPan.setMinimumSize(new Dimension(20, 80));
        lPan.setMinimumSize(new Dimension(20, 80));
        lPan.setPreferredSize(new Dimension(20, 240)); // give it some height for the initial split

        lTree.setDragEnabled(true);
        pclTree.setDragEnabled(true);

        return treePanels;
    }

    @Override
    public void genericReport(int type, String title, String msg)
    {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }
    Transferable dragged;

    @Override
    public void startingDrag(Transferable trans) {
        dragged = trans;
    }

    /** Class to facilitate dragging new nodes onto the pallete */
    class vDropTargetAdapter extends DropTargetAdapter {

        @Override
        public void dragOver(DropTargetDragEvent e) {

            // NOTE: this action is very critical in getting JGraph 5.14 to
            // signal the drop method
            e.acceptDrag(e.getDropAction());
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            if (dragged != null) {
                try {
                    Point p = dtde.getLocation();

                    String s = dragged.getTransferData(DataFlavor.stringFlavor).toString();
                    String[] sa = s.split("\t");

                    // Check for XML-based node
                    FileBasedAssyNode xn = isFileBasedAssyNode(sa[1]);
                    if (xn != null) {
                        switch (sa[0]) {
                            case "simkit.BasicSimEntity":
                                ((AssemblyController) getController()).newFileBasedEventGraphNode(xn, p);
                                break;
                            case "java.beans.PropertyChangeListener":
                                ((AssemblyController) getController()).newFileBasedPropChangeListenerNode(xn, p);
                                break;
                        }
                    } else {
                        // Else class-based node
                        switch (sa[0]) {
                            case "simkit.BasicSimEntity":
                                ((AssemblyController) getController()).newEventGraphNode(sa[1], p);
                                break;
                            case "java.beans.PropertyChangeListener":
                                ((AssemblyController) getController()).newPropChangeListenerNode(sa[1], p);
                                break;
                        }
                    }

                    dragged = null;
                } catch (UnsupportedFlavorException | IOException e) {
                    LogUtils.getLogger(AssemblyViewFrame.class).error(e);
                }
            }
        }
    }

    private FileBasedAssyNode isFileBasedAssyNode(String s) {
        try {
            return FileBasedAssyNode.fromString(s);
        } catch (FileBasedAssyNode.exception e) {
            return null;
        }
    }

    @Override
    public void modelChanged(mvcModelEvent event) {
        switch (event.getID()) {
            default:
                getCurrentVgacw().viskitModelChanged((ModelEvent) event);
                break;
        }
    }

    @Override
    public boolean doEditEvGraphNode(EvGraphNode evNode) {
        return EventGraphNodeInspectorDialog.showDialog(this, evNode);
    }

    @Override
    public boolean doEditPclNode(PropChangeListenerNode pclNode) {
        return PclNodeInspectorDialog.showDialog(this, pclNode); // blocks
    }

    @Override
    public boolean doEditPclEdge(PropChangeEdge pclEdge) {
        return PclEdgeInspectorDialog.showDialog(this, pclEdge);
    }

    @Override
    public boolean doEditAdapterEdge(AdapterEdge aEdge) {
        return AdapterConnectionInspectorDialog.showDialog(this, aEdge);
    }

    @Override
    public boolean doEditSimEvListEdge(SimEvListenerEdge seEdge) {
        return SimEventListenerConnectionInspectorDialog.showDialog(this, seEdge);
    }

    private Object getLeafUO(JTree tree) {
        TreePath[] oa = tree.getSelectionPaths();
        if (oa != null) {
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) oa[0].getLastPathComponent();
            return dmtn.getUserObject();
        }
        return null;
    }

    @Override
    public Object getSelectedEventGraph() {
        return getLeafUO(lTree);
    }

    @Override
    public Object getSelectedPropChangeListener() {
        return getLeafUO(pclTree);
    }

    @Override
    public void addEventGraphsToLegoTree(File f, boolean b) {
        if (f.exists()) {
            lTree.addContentRoot(f, b);
        }
    }

    @Override
    public void addPCLsToLegoTree(File f, boolean b) {
        pclTree.addContentRoot(f, b);
    }

    @Override
    public void removeEventGraphFromLEGOTree(File f) {
        lTree.removeContentRoot(f);
    }

    // Not used
    @Override
    public void removePropChangeFromLEGOTree(File f) {
        pclTree.removeContentRoot(f);
    }

    @Override
    public int genericAsk(String title, String msg) {
        return JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_CANCEL_OPTION);
    }

    @Override
    public int genericAskYN(String title, String msg) {
        return JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION);
    }

    @Override
    public int genericAsk2Butts(String title, String msg, String butt1, String butt2) {
        return JOptionPane.showOptionDialog(this, msg, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{butt1, butt2}, butt1);
    }

    @Override
    public String promptForStringOrCancel(String title, String message, String initval) {
        return (String) JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE,
                null, null, initval);
    }    // ViskitView-required methods:

    private JFileChooser jfc;
    private JFileChooser buildOpenSaveChooser() {

        // Try to open in the current project directory for Assemblies
        if (VGlobals.instance().getCurrentViskitProject() != null) {
            return new JFileChooser(VGlobals.instance().getCurrentViskitProject().getAssembliesDir());
        } else {
            return new JFileChooser(new File(ViskitProject.MY_VISKIT_PROJECTS_DIR));
        }
    }

    @Override
    public File[] openFilesAsk() {
        jfc = buildOpenSaveChooser();
        jfc.setDialogTitle("Open Assembly Files");

        // Look for assembly in the filename, Bug 1247 fix
        FileFilter filter = new AssemblyFileFilter("assembly");
        jfc.setFileFilter(filter);

        jfc.setMultiSelectionEnabled(true);

        int returnVal = jfc.showOpenDialog(this);
        return (returnVal == JFileChooser.APPROVE_OPTION) ? jfc.getSelectedFiles() : null;
    }

    @Override
    public File openRecentFilesAsk(Collection<String> lis) {
        String fn = RecentFilesDialog.showDialog(this, lis);
        if (fn != null) {
            File f = new File(fn);
            if (f.exists()) {
                return f;
            } else {
                genericReport(JOptionPane.ERROR_MESSAGE, "File not found.", f + " does not exist");
            }
        }
        return null;
    }

    @Override
    public void setSelectedAssemblyName(String s) {
        boolean nullString = !(s != null && !s.isEmpty());
        if (!nullString) {
            tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), s);
        }
    }

    @Override
    public void openProject() {
        AssemblyControllerImpl aController = ((AssemblyControllerImpl) getController());

        if (!aController.handleProjectClosing()) {
            return;
        }

        File file = ViskitProject.openProjectDir(this, ViskitProject.MY_VISKIT_PROJECTS_DIR);
        if (file != null) {
            aController.openProject(file);
        }

        showProjectName();
    }

    @Override
    public void showProjectName() {
        super.showProjectName();
        VGlobals.instance().getEventGraphEditor().showProjectName();
    }

    private File getUniqueName(String suggName) {
        String appnd = "";
        String suffix = "";

        int lastDot = suggName.lastIndexOf('.');
        if (lastDot != -1) {
            suffix = suggName.substring(lastDot);
            suggName = suggName.substring(0, lastDot);
        }
        int count = -1;
        File fil = null;
        do {
            fil = new File(suggName + appnd + suffix);
            appnd = "" + ++count;
        } while (fil.exists());

        return fil;
    }

    @Override
    public File saveFileAsk(String suggName, boolean showUniqueName) {
        if (jfc == null) {
            jfc = buildOpenSaveChooser();
        }

        jfc.setDialogTitle("Save Assembly File");
        File fil = new File(VGlobals.instance().getCurrentViskitProject().getAssembliesDir(), suggName);
        if (!fil.getParentFile().isDirectory()) {
            fil.getParentFile().mkdirs();
        }
        if (showUniqueName) {
            fil = getUniqueName(suggName);
        }

        jfc.setSelectedFile(fil);
        int retv = jfc.showSaveDialog(this);
        if (retv == JFileChooser.APPROVE_OPTION) {
            if (jfc.getSelectedFile().exists()) {
                if (JOptionPane.YES_OPTION != genericAskYN("File Exists",  "Overwrite? Confirm")) {
                    return null;
                }
            }
            return jfc.getSelectedFile();
        }

        // We canceled
        deleteCanceledSave(fil.getParentFile());
        jfc = null;
        return null;
    }

    /** Handles a canceled new EG file creation
     *
     * @param file to candidate EG file
     */
    public void deleteCanceledSave(File file) {
        if (file.exists()) {
            if (file.delete()) {
                if (file.getParentFile().exists() && !file.getParentFile().equals(VGlobals.instance().getCurrentViskitProject().getEventGraphsDir())) {
                    deleteCanceledSave(file.getParentFile());
                }
            }
        }
    }

    @Override
    public void showAndSaveSource(String className, String s, String fileName) {
        final JFrame f = new SourceWindow(this, className, s);
        f.setTitle("Generated source from " + fileName);

        Runnable r = () -> {
            f.setVisible(true);
        };
        SwingUtilities.invokeLater(r);
    }

    @Override
    public void displayXML(File f) {
        JComponent xt;
        try {
            xt = XTree.getTreeInPanel(f);
        } catch (Exception e) {
            genericReport(JOptionPane.ERROR_MESSAGE, "XML Display Error", e.getMessage());
            return;
        }
        //xt.setVisibleRowCount(25);
        xt.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        final JFrame jf = new JFrame(f.getName());

        JPanel content = new JPanel();
        jf.setContentPane(content);

        content.setLayout(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        content.add(xt, BorderLayout.CENTER);
        JPanel buttPan = new JPanel();
        buttPan.setLayout(new BoxLayout(buttPan, BoxLayout.X_AXIS));
        buttPan.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        JButton closeButt = new JButton("Close");
        buttPan.add(Box.createHorizontalGlue());
        buttPan.add(closeButt);
        content.add(buttPan, BorderLayout.SOUTH);

        //jf.pack();
        jf.setSize(475, 500);
        jf.setLocationRelativeTo(this);

        Runnable r = () -> {
            jf.setVisible(true);
        };
        SwingUtilities.invokeLater(r);

        closeButt.addActionListener((ActionEvent e) -> {
            Runnable r1 = () -> {
                jf.dispose();
            };
            SwingUtilities.invokeLater(r1);
        });
    }

    void clampHeight(JComponent comp) {
        Dimension d = comp.getPreferredSize();
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
        comp.setMinimumSize(new Dimension(Integer.MAX_VALUE, d.height));
    }

    void clampSize(JComponent comp) {
        Dimension d = comp.getPreferredSize();
        comp.setMaximumSize(d);
        comp.setMinimumSize(d);
    }

} // end class file AssemblyViewFrame.java

/** Utility class to handle EventNode DnD operations */
interface DragStartListener {
    void startingDrag(Transferable trans);
}
