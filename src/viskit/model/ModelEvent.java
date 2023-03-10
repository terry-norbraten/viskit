package viskit.model;

import viskit.mvc.mvcModelEvent;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey CA
 * www.nps.edu
 * @author Mike Bailey
 * @since Mar 2, 2004
 * @since 1:04:35 PM
 * @version $Id$
 *
 * This defines every event with which the application Model informs its listeners. Typically
 * this is the view.
 */
public class ModelEvent extends mvcModelEvent
{
  public static final int NEWMODEL = 0;

  public static final int SIMPARAMETERADDED = 1;
  public static final int SIMPARAMETERDELETED = 2;
  public static final int SIMPARAMETERCHANGED = 3;

  public static final int STATEVARIABLEADDED = 4;
  public static final int STATEVARIABLEDELETED = 5;
  public static final int STATEVARIABLECHANGED = 6;

  public static final int EVENTADDED = 7;
  public static final int EVENTDELETED = 8;
  public static final int EVENTCHANGED = 9;

  public static final int EDGEADDED = 10;
  public static final int EDGEDELETED = 11;
  public static final int EDGECHANGED = 12;

  public static final int CANCELINGEDGEADDED = 13;
  public static final int CANCELINGEDGEDELETED = 14;
  public static final int CANCELINGEDGECHANGED = 15;

  public static final int CODEBLOCKCHANGED = 16;

  public static final int REDO_CANCELING_EDGE = 34;
  public static final int REDO_SCHEDULING_EDGE = 35;
  public static final int REDO_EVENT_NODE = 36;

  public static final int UNDO_CANCELING_EDGE = 37;
  public static final int UNDO_SCHEDULING_EDGE = 38;
  public static final int UNDO_EVENT_NODE = 39;

  // assembly editor:
  public static final int NEWASSEMBLYMODEL = 17;

  public static final int EVENTGRAPHADDED = 18;
  public static final int EVENTGRAPHDELETED = 19;
  public static final int EVENTGRAPHCHANGED = 20;

  public static final int PCLADDED = 21;
  public static final int PCLDELETED = 22;
  public static final int PCLCHANGED = 23;

  public static final int ADAPTEREDGEADDED = 24;
  public static final int ADAPTEREDGEDELETED = 25;
  public static final int ADAPTEREDGECHANGED = 26;

  public static final int SIMEVLISTEDGEADDED = 27;
  public static final int SIMEVLISTEDGEDELETED = 28;
  public static final int SIMEVLISTEDGECHANGED = 29;

  public static final int PCLEDGEADDED = 30;
  public static final int PCLEDGEDELETED = 31;
  public static final int PCLEDGECHANGED = 32;

  public static final int METADATA_CHANGED = 33;

  public static final int UNDO_EVENT_GRAPH = 40;
  public static final int REDO_EVENT_GRAPH = 41;
  public static final int UNDO_PCL = 42;
  public static final int REDO_PCL = 43;
  public static final int UNDO_ADAPTER_EDGE = 44;
  public static final int REDO_ADAPTER_EDGE = 45;
  public static final int UNDO_SIM_EVENT_LISTENER_EDGE = 46;
  public static final int REDO_SIM_EVENT_LISTENER_EDGE = 47;
  public static final int UNDO_PCL_EDGE = 48;
  public static final int REDO_PCL_EDGE = 49;

  public ModelEvent(Object obj, int id, String message)
  {
    super(obj,id,message);
  }
}
