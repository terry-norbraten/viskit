package viskit;

import viskit.mvc.mvcAbstractJFrameView;
import viskit.mvc.mvcModelEvent;
import viskit.model.Model;
import viskit.model.AssemblyModel;
import viskit.model.ViskitAssemblyModel;
import viskit.jgraph.vGraphAssemblyModel;
import viskit.jgraph.vGraphAssemblyComponent;
import viskit.images.AdapterIcon;
import viskit.images.SimEventListenerIcon;
import viskit.images.PropChangeListenerIcon;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM)  2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * By:   Mike Bailey
 * Date: May 10, 2004
 * Time: 2:07:37 PM
 */

public class AssemblyViewFrame extends mvcAbstractJFrameView implements ViskitAssemblyView, DragStartListener
{
  private ViskitAssemblyModel model;
  private ViskitAssemblyController controller;
  private JSplitPane jsp;
  private Color background = new Color(0xFB,0xFB,0xE5);

  // Modes we can be in--selecting items, adding nodes to canvas, drawing arcs, etc.
  public static final int SELECT_MODE = 0;
  public static final int ADAPTER_MODE = 1;
  public static final int SIMEVLIS_MODE = 2;
  public static final int PCL_MODE = 3;


  /**
   * Toolbar for dropping icons, connecting, etc.
   */
  private JToolBar toolBar;

  /**
   * Button group that holds the mode buttons.
   */
  private ButtonGroup modeButtonGroup;

  private JToggleButton selectMode;
  private JToggleButton adapterMode, simEventListenerMode, propChangeListenerMode;
  private JButton zoomIn, zoomOut;

  private JPanel canvasPanel;
  private LegosTree lTree;

  public AssemblyViewFrame(AssemblyModel model, AssemblyController controller)
  {
    super("Viskit -- Simkit Assembly Editor");
    initMVC(model,controller);   // set up mvc linkages
    initUI();            // build widgets

    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(((d.width - 800) / 2)+30, ((d.height - 600) / 2)+30);
    setSize(800, 600);

/*   enable when finished with widget creation
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        ((AssemblyController)getController()).quit();
      }
    });
*/
  }

  /**
   * Initialize the MCV connections
   */
  private void initMVC(AssemblyModel mod, AssemblyController ctrl)
  {
    setModel(mod);
    setController(ctrl);
  }

  /**
   * Initialize the user interface
   */
  private void initUI()
  {
    Container cont = getContentPane();

    buildMenus();
    buildToolbar();
    buildVCRToolbar();

    // Set up a top level pane that will be the content pane. This
    // has a border layout, and contains the toolbar on the top and
    // the main splitpane underneath.

    // top level panel
    JPanel top = new JPanel();
    top.setLayout(new BorderLayout());
    //top.add(toolBar, BorderLayout.NORTH);

    JComponent canvas = buildCanvas();
    JComponent trees = buildTreePanels();
    trees.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    JScrollPane leftsp = new JScrollPane(trees);
    leftsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    //leftsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,trees,new JScrollPane(canvasPanel)); //canvas));
    jsp.setOneTouchExpandable(true);
    trees.setMinimumSize(new Dimension(20,20));
    canvas.setMinimumSize(new Dimension(20,20));
    //jsp.setDividerLocation(0.5d);
    top.add(jsp,BorderLayout.CENTER);
    top.add(vcrToolBar,BorderLayout.SOUTH);
    top.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));

    getContentPane().add(top);
  }



  private void buildMenus()
  {

  }
  /**
   * Returns the current mode--select, add, arc, cancelArc
   */
  public int getCurrentMode()
  {
    // Use the button's selected status to figure out what mode
    // we are in.

    if (selectMode.isSelected() == true) return SELECT_MODE;
    if (adapterMode.isSelected() == true) return ADAPTER_MODE;
    if (simEventListenerMode.isSelected() == true) return SIMEVLIS_MODE;
    if (propChangeListenerMode.isSelected() == true) return PCL_MODE;
    //assert false : "getCurrentMode()";
    System.err.println("assert false : \"getCurrentMode()\"");
    return 0;
  }


  private JPanel vcrToolBar;
  private JButton vcrStop, vcrPause, vcrPlay, vcrStep;
  private JCheckBox  vcrVerbose;
  private void buildVCRToolbar()
  {
    vcrToolBar = new JPanel();
    vcrToolBar.setLayout(new BoxLayout(vcrToolBar,BoxLayout.X_AXIS));
    vcrToolBar.add(Box.createHorizontalGlue());

    vcrStop = makeButton(null, "viskit/images/Stop24.gif", "Stop the simulation run");
    vcrStop.setEnabled(false);
    vcrToolBar.add(vcrStop);
    vcrPlay = makeButton(null, "viskit/images/Play24.gif", "Begin or resume the simulation run");
    vcrToolBar.add(vcrPlay);
    vcrPause = makeButton(null, "viskit/images/Pause24.gif", "Pause the simulation run");
    vcrToolBar.add(vcrPause);
    vcrPause.setEnabled(false);
    vcrStep = makeButton(null, "viskit/images/StepForward24.gif", "Step the simulation");
    vcrToolBar.add(vcrStep);

    vcrToolBar.add(Box.createHorizontalStrut(20));

    vcrVerbose = new JCheckBox("verbose output",false);
    vcrVerbose.setToolTipText("Enable or disable verbose simulation output");
    vcrToolBar.add(vcrVerbose);

    vcrToolBar.add(Box.createHorizontalGlue());
    vcrToolBar.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));


  }
  private void buildToolbar()
  {
    modeButtonGroup = new ButtonGroup();
    toolBar = new JToolBar();

    // Buttons for what mode we are in

    selectMode             = makeJTButton(null, "viskit/images/selectNode.png",
                                       "Select items on the graph");
    //adapterMode          = makeJTButton(null, "viskit/images/adapter.png",
    adapterMode            = makeJTButton(null,new AdapterIcon(24,24),
                                       "Connect assemblies with adapter pattern");

    //simEventListenerMode = makeJTButton(null, "viskit/images/bridge.png",
    simEventListenerMode   = makeJTButton(null,new SimEventListenerIcon(24,24),
                                       "Connect assemblies through a SimEvent listener pattern");
    //propChangeListenerMode = makeJTButton(null, "viskit/images/bridge.png",
    propChangeListenerMode = makeJTButton(null,new PropChangeListenerIcon(24,24),
                                       "Connect a property change listener to a SimEntity");

    zoomIn = makeButton(null, "viskit/images/ZoomIn24.gif",
                                        "Zoom in on the graph");

    zoomOut = makeButton(null, "viskit/images/ZoomOut24.gif",
                                        "Zoom out on the graph");

    modeButtonGroup.add(selectMode);
    modeButtonGroup.add(adapterMode);
    modeButtonGroup.add(simEventListenerMode);
    modeButtonGroup.add(propChangeListenerMode);

    // Make selection mode the default mode
    selectMode.setSelected(true);

    toolBar.add(new JLabel("Mode: "));

    toolBar.add(selectMode);
    toolBar.addSeparator(new Dimension(5,24));
    toolBar.add(adapterMode);
    toolBar.addSeparator(new Dimension(5,24));
    toolBar.add(simEventListenerMode);
    toolBar.addSeparator(new Dimension(5,24));
    toolBar.add(propChangeListenerMode);

    toolBar.addSeparator(new Dimension(24,24));
    toolBar.add(new JLabel("Zoom: "));
    toolBar.add(zoomIn);
    toolBar.addSeparator(new Dimension(5,24));
    toolBar.add(zoomOut);

    zoomIn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        graphPane.setScale(graphPane.getScale() + 0.1d);
      }
    });
    zoomOut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        graphPane.setScale(Math.max(graphPane.getScale() - 0.1d, 0.1d));
      }
    });

    // These buttons perform operations that are internal to our view class, and therefore their operations are
    // not under control of the application controller (Controller.java).  Small, simple anonymous inner classes
    // such as these have been certified by the Surgeon General to be only minimally detrimental to code health.

    selectMode.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        graphPane.setPortsVisible(false);
      }
    });
    adapterMode.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        graphPane.setPortsVisible(true);
      }
    });


  }

  private JToggleButton makeJTButton(Action a, String icPath, String tt)
  {
    JToggleButton jtb;
    if(a != null)jtb = new JToggleButton(a);
    else jtb = new JToggleButton();
    return (JToggleButton)buttonCommon(jtb,icPath,tt);
  }
  private JToggleButton makeJTButton(Action a, Icon ic, String tt)
  {
    JToggleButton jtb;
    if(a != null)jtb = new JToggleButton(a);
    else jtb = new JToggleButton();
    jtb.setIcon(ic);
    return (JToggleButton)buttonCommon2(jtb,tt);
  }
  private JButton makeButton(Action a, String icPath, String tt)
  {
    JButton b;
    if(a != null) b = new JButton(a);
    else b = new JButton();
    return (JButton)buttonCommon(b,icPath,tt);
  }
  private AbstractButton buttonCommon(AbstractButton b, String icPath, String tt)
  {
    b.setIcon(new ImageIcon(ClassLoader.getSystemResource(icPath)));
    return buttonCommon2(b,tt);
  }
  private AbstractButton buttonCommon2(AbstractButton b, String tt)
  {
    b.setToolTipText(tt);
    b.setBorder(BorderFactory.createEtchedBorder());
    b.setText(null);
    return b;
  }




  private vGraphAssemblyComponent graphPane;
  private JComponent buildCanvas()
  {
    // Set up the basic panes for the layouts
    vGraphAssemblyModel mod = new vGraphAssemblyModel();
    graphPane = new vGraphAssemblyComponent(mod,this);
    mod.graph = graphPane;                               // todo fix this

    graphPane.addMouseListener(new vCursorHandler());
    try{
      //DropTarget dt = graphPane.getDropTarget();
      //System.out.println("blub");
      graphPane.getDropTarget().addDropTargetListener(new vDropTargetAdapter());
    }
    catch(Exception e) {
      //assert false : "Drop target init. error";
      System.err.println("assert false : \"Drop target init. error\"");
    }

    canvasPanel = new JPanel();
    canvasPanel.setLayout(new BorderLayout());
    canvasPanel.add(graphPane,BorderLayout.CENTER);
    canvasPanel.add(toolBar,BorderLayout.NORTH);
    return graphPane;
  }
  private JSplitPane panJsp;

  private JComponent buildTreePanels()
  {
    lTree = new LegosTree(this);
    LegosPanel lPan = new LegosPanel(lTree);

    PropChangeListenersList pcList = new PropChangeListenersList(this);
    PropChangeListenersPanel pcPan = new PropChangeListenersPanel(pcList);

    lTree.setBackground(background);
    pcList.setBackground(background);

      panJsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,lPan,pcPan);
/*
  JScrollPane tsp = new JScrollPane(lPan,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  JScrollPane psp = new JScrollPane(pcPan,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  panJsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,tsp,psp);
  tsp.setMinimumSize(new Dimension(20,20));
  psp.setMinimumSize(new Dimension(20,20));
*/

      panJsp.setBorder(null);
      panJsp.setOneTouchExpandable(true);
      pcPan.setMinimumSize(new Dimension(20,80));
      lPan.setMinimumSize(new Dimension(20,80));

    lTree.setDragEnabled(true);
  //  lTree.setTransferHandler(new JTreeToJgraphTransferHandler());
   // lTree.addMouseListener( new DragMouseAdapter());
    return panJsp;
  }

  // Two classes to support dragging and dropping on the graph
  class xDragMouseAdapter extends MouseAdapter
  {
    public void mousePressed(MouseEvent e)
    {
      JComponent c = (JComponent) e.getSource();
      System.out.println(c);
/*
      if(c == EventGraphViewFrame.this.addSelfRef)
        dragger = SELF_REF_DRAG;
      else
        dragger = NODE_DRAG;
*/

   //   TransferHandler handler = c.getTransferHandler();
   //   handler.exportAsDrag(c, e, TransferHandler.COPY);
    }
  }
  Transferable dragged;
  public void startingDrag(Transferable trans)
  {
    dragged = trans;
  }

  class vDropTargetAdapter extends DropTargetAdapter
  {

    public void drop(DropTargetDropEvent dtde)
    {
      if(dragged != null) {
        try {
          String s = dragged.getTransferData(DataFlavor.stringFlavor).toString();
          Class uo = null;
          Class c = null;
          Class cc= null;
          try {
            uo = Class.forName(s);     // what we've drug
            c  = Class.forName("simkit.BasicSimEntity");   // what  we're checking for
            cc = Class.forName("java.beans.PropertyChangeListener");      // ditto
          }
          catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
          Point p = dtde.getLocation();

          if(((Class)cc).isAssignableFrom(uo))
            ((ViskitAssemblyController)getController()).newPropChangeListenerNode(s,p);
          else if(((Class)c).isAssignableFrom(uo))
            ((ViskitAssemblyController)getController()).newEventGraphNode(s,p);

          dragged = null;
          return;
        }
        catch (UnsupportedFlavorException e) {
          e.printStackTrace();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
      else {
/*
        // get the node in question from the graph
        Object o = graphPane.getViskitElementAt(p);
        if(o != null && o instanceof EventNode) {
          EventNode en = (EventNode)o;
          // We're making a self-referential arc
          ((ViskitController)getController()).newArc(new Object[]{en.opaqueViewObject,en.opaqueViewObject});
*/
        }
      }
    }

  private void buildToolBar()
  {

  }
  private boolean firstShown = false;

  public void setVisible(boolean b)
  {
    super.setVisible(b);
    if(firstShown == false) {
      firstShown = true;
      jsp.setDividerLocation(225);
      panJsp.setDividerLocation(0.5d);
    }
  }

  // Some private classes to implement dnd and dynamic cursor update
  class vCursorHandler extends MouseAdapter
  {
    Cursor select;
    Cursor arc;
    Cursor cancel;

    vCursorHandler()
    {
      super();
      select    = Cursor.getDefaultCursor();
      //select    = new Cursor(Cursor.MOVE_CURSOR);
      arc       = new Cursor(Cursor.CROSSHAIR_CURSOR);

      Image img = new ImageIcon(ClassLoader.getSystemResource("viskit/images/canArcCursor.png")).getImage();
      cancel    = Toolkit.getDefaultToolkit().createCustomCursor(img,new Point(0,0),"CancelArcCursor");
    }

    public void mouseEntered(MouseEvent e)
    {
/*
      switch(getCurrentMode()) {
        case SELECT_MODE:
        case SELF_REF_MODE:
          graphPane.setCursor(select);
          break;
        case ARC_MODE:
          graphPane.setCursor(arc);
          break;
        case CANCEL_ARC_MODE:
          graphPane.setCursor(cancel);
          break;
        default:
          //assert false : "vCursorHandler";
          System.err.println("assert false : \"vCursorHandler\"");
      }
*/
    }
  }

  /**
   * This is where the "master" model (simkit.viskit.model.Model) updates the view.
   * Not so much to do in this editor as in EventGraphViewFrame
   * @param event
   */
  public void modelChanged(mvcModelEvent event)
  //-------------------------------------------
  {
    System.out.println("AssView got "+event.toString());
    switch(event.getID())
    {
/*
      case ModelEvent.EVENTGRAPHADDED:
        break;
      case ModelEvent.NEWASSEMBLYMODEL:
        // fall through
*/

      // Changes the graph needs to know about
      default:
        this.graphPane.viskitModelChanged((ModelEvent)event);

    }
  }

}
interface DragStartListener
{
  public void startingDrag(Transferable trans);
}