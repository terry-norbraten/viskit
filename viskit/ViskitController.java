package viskit;

import viskit.model.*;

import java.awt.*;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: mike
 * Date: Mar 19, 2004
 * Time: 9:00:57 AM
 * To change this template use File | Settings | File Templates.
 */

public interface ViskitController
{
  /* start app */
  void begin();

  /* user has clicked a button or menu item: */
  void newNode();
  void newSimParameter();
  void newStateVariable();

  /* user has established some entity parameters, model can create objects */
  void newNode         (Point p);
  void newSimParameter (String name, String type, String initVal, String comment);
  void newStateVariable(String name, String type, String initVal, String comment);
  void newArc          (Object[] nodes);
  void newCancelArc    (Object[] nodes);

  void editGraphMetaData  ();
  
  void newEventGraph();

  void runAssemblyEditor();
  
  /* requests to the controller to perform editing operations on existing entities */
  void nodeEdit         (EventNode node);
  void arcEdit          (SchedulingEdge ed);
  void canArcEdit       (CancellingEdge ed);
  void simParameterEdit (vParameter param);
  void stateVariableEdit(vStateVariable var);

  /* menu selections */
  void copy();
  void cut();        // to remove nodes and edges
  void open();
  void paste();
  void quit();
  void save();
  void saveAs();
  void selectNodeOrEdge(Vector v);

  void deleteSimParameter(vParameter p);
  void deleteStateVariable(vStateVariable var);

  void eventList();
  void generateJavaClass();
  boolean syntaxCheck(EventNode node);
  boolean syntaxCheck(Edge ed);


}
