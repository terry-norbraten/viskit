package viskit.jgraph;

import org.jgraph.graph.GraphCellEditor;
import org.jgraph.graph.GraphConstants;
import org.jgraph.plaf.basic.BasicGraphUI;
import viskit.ViskitController;
import viskit.model.CancellingEdge;
import viskit.model.Edge;
import viskit.model.EventNode;
import viskit.model.SchedulingEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Map;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey CA
 * www.nps.navy.mil
 * By:   Mike Bailey
 * Date: Mar 8, 2004
 * Time: 3:17:59 PM
 */

/**
 * BasicGraphUI must be overridden to allow in node and edge editting.
 * This code is a copy of the appropriate part of EditorGraph.java, which is
 * part of JGraph examples.
 */
public class vGraphAssemblyUI extends BasicGraphUI
{
  private JDialog editDialog;

  private vGraphAssemblyComponent parent;

  public vGraphAssemblyUI(vGraphAssemblyComponent parent)
  {
    this.parent = parent;
  }

  protected boolean startEditing(Object cell, MouseEvent event)
  {
    completeEditing();
    if (graph.isCellEditable(cell) && editDialog == null) {
        createEditDialog(cell, event);
     }

    return false;
  }


  protected void createEditDialog(Object c, MouseEvent event)
  {
    final Object cell = c;
    //MouseEvent ev = event;

    SwingUtilities.invokeLater( new Runnable()
    {
      public void run()
      {
        ViskitController cntl = (ViskitController) vGraphAssemblyUI.this.parent.parent.getController();     // todo fix this
        if (cell instanceof vEdgeCell) {
          Edge e = (Edge) ((vEdgeCell) cell).getUserObject();
          if (e instanceof SchedulingEdge)
            cntl.arcEdit((SchedulingEdge)e);
          else //if(e instanceof CancellingEdge)
            cntl.canArcEdit((CancellingEdge)e);
        }
        else if (cell instanceof CircleCell) {
          EventNode en = (EventNode) ((CircleCell) cell).getUserObject();
          cntl.nodeEdit(en);
        }
      }
    });
  }

  /**
   * Stops the editing session. If messageStop is true the editor
   * is messaged with stopEditing, if messageCancel is true the
   * editor is messaged with cancelEditing. If messageGraph is true
   * the graphModel is messaged with valueForCellChanged.
   */
  protected void completeEditing(boolean messageStop,
                                 boolean messageCancel,
                                 boolean messageGraph)
  {
    if (stopEditingInCompleteEditing
        && editingComponent != null
        && editDialog != null) {
      Component oldComponent = editingComponent;
      Object oldCell = editingCell;
      GraphCellEditor oldEditor = cellEditor;
      Object newValue = oldEditor.getCellEditorValue();
      Rectangle editingBounds = graph.getCellBounds(editingCell);
      boolean requestFocus =
          (graph != null
          && (graph.hasFocus() || editingComponent.hasFocus()));
      editingCell = null;
      editingComponent = null;
      if (messageStop)
        oldEditor.stopCellEditing();
      else if (messageCancel)
        oldEditor.cancelCellEditing();
      editDialog.dispose();
      if (requestFocus)
        graph.requestFocus();
      if (messageGraph) {
        Map map = GraphConstants.createMap();
        GraphConstants.setValue(map, newValue);
        Map nested = new Hashtable();
        nested.put(oldCell, map);
        graphLayoutCache.edit(nested, null, null, null);
      }
      updateSize();
      // Remove Editor Listener
      if (oldEditor != null && cellEditorListener != null)
        oldEditor.removeCellEditorListener(cellEditorListener);
      cellEditor = null;
      editDialog = null;
    }
  }
}
