package viskit.model;

import simkit.viskit.ModelEvent;
import simkit.viskit.mvc.mvcAbstractModel;
import simkit.xsd.bindings.*;
import simkit.xsd.bindings.Event;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey CA
 * www.nps.navy.mil
 * By:   Mike Bailey
 * Date: Mar 2, 2004
 * Time: 1:09:38 PM
 */

/**
 * This is the "master" model of an event graph.  It should hold the node, edge and assembly
 * information.  What hasn't been done is to put in accessor methods for the view to
 * read pieces that it needs, say after it receives a "new model" event.
 */
public class Model extends mvcAbstractModel implements ViskitModel
{
  JAXBContext jc;
  ObjectFactory oFactory;

  SimkitModule jaxbRoot;
  File currentFile;

  public static final String schemaLoc = "http://diana.gl.nps.navy.mil/Simkit/simkit.xsd";
  HashMap evNodeCache = new HashMap();
  HashMap edgeCache = new HashMap();
  Vector stateVariables = new Vector();
  public void init()
  {
    try {
      jc = JAXBContext.newInstance("simkit.xsd.bindings");
      oFactory = new ObjectFactory();
      jaxbRoot = oFactory.createSimkitModule(); // to start with empty graph
    }
    catch (JAXBException e) {
      //assert false : "Model.java -- error on JAXBContext instantiation";
      System.out.println("assert false : Model.java -- error on JAXBContext instantiation");
    }
  }

  /**
   * Boolean to signify whether the model has been changed since last disk save.
   *
   * @return true means changes have been made and it needs to be flushed.
   */
  public boolean isDirty()
  {
    return modelDirty;
  }
  private boolean modelDirty = false;

  /**
   * Replace existing model with one contained in the existing file.
   *
   * @param f
   */
  public void newModel(File f)
  {
    tempRemoveAllNodes();   // this should not be require...removeAll() should work
    tempRemoveAllEdges();

    if (f == null) {
      try {
        jaxbRoot = oFactory.createSimkitModule(); // to start with empty graph
      }
      catch (JAXBException e) {
        //assert false : "Model.java -- error on JAXBContext instantiation";
        System.out.println("assert false : Model.java -- error on JAXBContext instantiation");
      }
      this.notifyChanged(new ModelEvent(this, ModelEvent.NEWMODEL, "New empty model"));
    }
    else {
      this.notifyChanged(new ModelEvent(this, ModelEvent.NEWMODEL, "New model loaded from file"));

      try {
        Unmarshaller u = jc.createUnmarshaller();
        // u.setValidating(true); can't do this, the unmarshaller needs to have this capability..
        // see u.isValidating()
        // Unmarshaller does NOT validate by default
        jaxbRoot = (SimkitModule) u.unmarshal(f);

        buildEventsFromJaxb(jaxbRoot.getEvent());
        buildParametersFromJaxb(jaxbRoot.getParameter());
        buildStateVariablesFromJaxb(jaxbRoot.getStateVariable());

      }
      catch (JAXBException e) {
        //assert false : "Model.java -- error on JAXBContext instantiation";
        System.out.println("assert false : Model.java -- error unmarshalling " + f.getName());
        return;
      }
    }
    currentFile = f;
    modelDirty = false;
  }

  public void saveModel(File f)
  {
    if(f == null)
      f = currentFile;
     try {
       FileWriter fw = new FileWriter(f);
       Marshaller m = jc.createMarshaller();
       m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,new Boolean(true));
       m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,schemaLoc);

       String nm = f.getName();
       int dot=-1;
       if((dot=nm.indexOf('.')) != -1)
         nm = nm.substring(0,dot);
       
       jaxbRoot.setName(nm);
       jaxbRoot.setVersion("0.1");
       m.marshal(jaxbRoot,fw);
       fw.close();

       modelDirty = false;
     }
     catch (JAXBException e) {
       //assert false : "Model.java -- error on JAXBContext instantiation";
       System.err.println("assert false : Model.java -- error marshalling "+f.getName()+"\n"+e);
       modelDirty = true;
     }
     catch (IOException ex) {
       System.err.println("Error writing the file...");   // todo, access view and put up error dialog
       modelDirty = true;
     }
  }

  private void buildEventsFromJaxb(List lis)
  //----------------------------------------
  {
   // xdim = ydim = 1; // init...temp
    for(Iterator itr = lis.iterator(); itr.hasNext();) {
      Event ev = (Event)itr.next();
      EventNode en = buildNodeFromJaxbEvent(ev);

      buildEdgesFromJaxb(en,ev.getScheduleOrCancel());
    }
  }

  private EventNode buildNodeFromJaxbEvent(Event ev)
  {
    EventNode en = (EventNode)evNodeCache.get(ev);
    if(en != null) {
      return en;
    }
    en = new EventNode(ev.getName());
    jaxbEvToNode(ev,en);
    en.opaqueModelObject = ev;
    evNodeCache.put(ev,en);   // key = ev

    //notifyChanged(new ModelEvent(new Object[]{en,p},ModelEvent.EVENTADDED, "Event added"));
    notifyChanged(new ModelEvent(en,ModelEvent.EVENTADDED, "Event added"));

    return en;
  }
  private String concatStrings(List lis)
  {
    StringBuffer sb = new StringBuffer();
    for(Iterator itr = lis.iterator(); itr.hasNext();) {
      sb.append(((String) itr.next()));
      if(sb.length() > 0 && sb.charAt(sb.length()-1) != ' ')
        sb.append(' ');
    }
    return sb.toString().trim();
  }

  private void jaxbEvToNode(Event ev, EventNode node)
  {
    node.setName(ev.getName());

    CoordinateType coor = ev.getCoordinate();
    if(coor != null)        //todo lose this after all xmls updated
      node.setPosition(new Point(Integer.parseInt(coor.getX()),
                               Integer.parseInt(coor.getY())));

    node.getComments().clear();
    node.getComments().addAll(ev.getComment());

    node.getLocalVariables().clear();
    for(Iterator itr = ev.getLocalVariable().iterator(); itr.hasNext();) {
      LocalVariable lv = (LocalVariable)itr.next();
      EventLocalVariable elv = new EventLocalVariable(
                  lv.getName(),lv.getType(),lv.getValue());
      elv.setComment(concatStrings(lv.getComment()));
      elv.opaqueModelObject = lv;
      node.getLocalVariables().add(elv);
    }

    node.getArguments().clear();
    for(Iterator itr = ev.getArgument().iterator(); itr.hasNext();) {
      Argument arg = (Argument)itr.next();
      EventArgument ea = new EventArgument();
      ea.setName(arg.getName());
      ea.setType(arg.getType());
      ArrayList com = new ArrayList();
      com.addAll(arg.getComment());
      ea.setComments(com);
      ea.opaqueModelObject = arg;
      node.getArguments().add(ea);
    }

    node.getTransitions().clear();
    for(Iterator itr = ev.getStateTransition().iterator(); itr.hasNext();) {
      EventStateTransition est = new EventStateTransition();
      StateTransition st = (StateTransition)itr.next();
      StateVariable   sv = (StateVariable)st.getState();
      est.setStateVarName(sv.getName());
      est.setStateVarType(sv.getType());
      est.setOperation(st.getOperation() != null);
      if(est.isOperation())
        est.setOperationOrAssignment(st.getOperation().getMethod());
      else
        est.setOperationOrAssignment(st.getAssignment().getValue());
      ArrayList cmt = new ArrayList();
      cmt.addAll(sv.getComment());  // jmb here
      est.setComments(cmt);
      est.opaqueModelObject = st;
      node.getTransitions().add(est);
    }
  }

/*
  int xdim = 1, ydim=1;
  int factor = 50;
  private Point tempPositionPoint(String name)
  {
    if(name.equalsIgnoreCase("run"))
      return new Point(20,20);
    else if(name.equalsIgnoreCase("arrival"))
      return new Point(120,20);
    else if(name.equalsIgnoreCase("startservice"))
      return new Point(250,20);
    else if(name.equalsIgnoreCase("renege"))
      return new Point(120,150);
    else if(name.equalsIgnoreCase("endservice"))
      return new Point(250,150);

    return new Point(factor*xdim++,factor*ydim++);
  }
*/
  private void buildEdgesFromJaxb(EventNode src,List lis)
  {

    for(Iterator itr = lis.iterator(); itr.hasNext();) {
      Object o = itr.next();
      if( o instanceof Schedule) {
        buildScheduleEdgeFromJaxb(src,(Schedule)o);
      }
      else {
        buildCancelEdgeFromJaxb(src,(Cancel)o);
      }
    }
  }
  private SchedulingEdge buildScheduleEdgeFromJaxb(EventNode src, Schedule ed)
  {
    SchedulingEdge se = new SchedulingEdge();
    se.opaqueModelObject = ed;

    se.from = src;
    EventNode target = buildNodeFromJaxbEvent((Event)ed.getEvent());
    se.to = target;
    src.getConnections().add(se);
    target.getConnections().add(se);

    se.conditional = ed.getCondition();
    se.delay = ed.getDelay();
    se.parameters = buildEdgeParmsFromJaxb(ed.getEdgeParameter());
    edgeCache.put(ed,se);

    modelDirty = true;

    this.notifyChanged(new ModelEvent(se, ModelEvent.EDGEADDED, "Edge added"));

    return se;
  }

  private CancellingEdge buildCancelEdgeFromJaxb(EventNode src, Cancel ed)
  {
    CancellingEdge ce = new CancellingEdge();
    ce.opaqueModelObject = ed;

    ce.conditional = ed.getCondition();
    ce.parameters = buildEdgeParmsFromJaxb(ed.getEdgeParameter());

    ce.from = src;
    EventNode target = buildNodeFromJaxbEvent((Event)ed.getEvent());
    ce.to = target;
    src.getConnections().add(ce);
    target.getConnections().add(ce);

    edgeCache.put(ed,ce);
    modelDirty = true;

    notifyChanged(new ModelEvent(ce, ModelEvent.CANCELLINGEDGEADDED, "Cancelling edge added"));
    return ce;
  }
  private ArrayList buildEdgeParmsFromJaxb(List lis)
  {
    ArrayList alis = new ArrayList(3);
    for(Iterator itr = lis.iterator();itr.hasNext();) {
      EdgeParameter ep = (EdgeParameter)itr.next();

      vEdgeParameter vep = new vEdgeParameter((String)ep.getValue()); //,ep.getType());
      alis.add(vep);
    }
    return alis;
  }
  private void buildStateVariablesFromJaxb(List lis)
  {
    for(Iterator itr = lis.iterator(); itr.hasNext();) {
      StateVariable var = (StateVariable)itr.next();
      List varCom = var.getComment();
      String c = " ";
      for(Iterator ii=varCom.iterator();ii.hasNext();) {
        c += (String)ii.next();
        c += " ";
      }

      vStateVariable v = new vStateVariable(var.getName(),var.getType(),c.trim());
      v.opaqueModelObject = var;
      this.stateVariables.add(v);
      notifyChanged(new ModelEvent(v,ModelEvent.STATEVARIABLEADDED,"New state variable"));
    }
  }
  private void buildParametersFromJaxb(List lis)
  {
    for(Iterator itr = lis.iterator(); itr.hasNext();) {
      Parameter p = (Parameter)itr.next();
      List pCom = p.getComment();
      String c = " ";
      for(Iterator ii=pCom.iterator();ii.hasNext();) {
        c += (String)ii.next();
        c += " ";
      }
      vParameter vp = new vParameter(p.getName(), p.getType(),c.trim());
      vp.opaqueModelObject = p;
      notifyChanged(new ModelEvent(vp,ModelEvent.SIMPARAMETERADDED,"New sim parameter"));
    }

  }



  /**
   * Replace existing model with one contained in the existing reader source.
   *
   * //@param reader
   */
  public void newModel()//EventGraphXMLReader reader, boolean pIsEditable)
  {
    // put code to do it here

    modelDirty = false;
    this.notifyChanged(new ModelEvent(this, ModelEvent.NEWMODEL, "New model loaded from file"));
  }

  public Vector getAllNodes()
  {
    return new Vector(evNodeCache.values());
  }

  public Vector getStateVariables()
  {
    return (Vector)stateVariables.clone();
  }

  public ArrayList getSimParameters()
  {
    return new ArrayList(jaxbRoot.getParameter());
  }

  // parameter mods
  // --------------
  public void newSimParameter(String nm, String typ, String xinitVal, String comment)
  {
    modelDirty = true;

    vParameter vp = new vParameter(nm,typ,comment);
    //p.setValue(initVal);
    Parameter p = null;
    try {p = this.oFactory.createParameter(); } catch(JAXBException e){ System.out.println("newParmJAXBEX"); }
    p.setName(nm);
    p.setShortName(nm);
    p.setType(typ);
    p.getComment().add(comment);

    vp.opaqueModelObject = p;
    jaxbRoot.getParameter().add(p);

    this.notifyChanged(new ModelEvent(vp, ModelEvent.SIMPARAMETERADDED, "vParameter added"));
  }
  
  public void newSimParameter(vParameter p)
  {
    // put code to do it here

    modelDirty = true;
    this.notifyChanged(new ModelEvent(p, ModelEvent.SIMPARAMETERADDED, "vParameter added"));
  }

  public void deleteSimParameter(vParameter vp)
  {
    // remove jaxb variable
    jaxbRoot.getParameter().remove(vp.opaqueModelObject);
    modelDirty = true;
    this.notifyChanged(new ModelEvent(vp, ModelEvent.SIMPARAMETERDELETED, "vParameter deleted"));
  }

  public void changeSimParameter(vParameter vp)
  {
    // fill out jaxb variable
    Parameter p = (Parameter)vp.opaqueModelObject;
    p.setName(vp.getName());
    p.setShortName(vp.getName());
    p.setType(vp.getType());
    p.getComment().clear();
    p.getComment().add(vp.getComment());

    modelDirty = true;
    this.notifyChanged(new ModelEvent(vp, ModelEvent.SIMPARAMETERCHANGED, "vParameter changed"));
  }

  // State variable mods
  // -------------------

  public void newStateVariable(String name, String type, String xinitVal, String comment)
  {
    // put code to do it here

    modelDirty = true;
    // get the new one here and show it around
    vStateVariable vsv = new vStateVariable(name,type,comment);
    StateVariable s = null;
    try {s = this.oFactory.createStateVariable(); } catch(JAXBException e){ System.out.println("newStVarJAXBEX"); }
    s.setName(name);
    s.setType(type);
    s.getComment().add(comment);

    vsv.opaqueModelObject = s;
    jaxbRoot.getStateVariable().add(s);
    this.stateVariables.add(vsv);
    this.notifyChanged(new ModelEvent(vsv, ModelEvent.STATEVARIABLEADDED, "State variable added"));

  }

  public void deleteStateVariable(vStateVariable vsv)
  {
    // remove jaxb variable
    jaxbRoot.getStateVariable().remove(vsv.opaqueModelObject);
    this.stateVariables.remove(vsv);
    modelDirty = true;
    this.notifyChanged(new ModelEvent(vsv, ModelEvent.STATEVARIABLEDELETED, "State variable deleted"));
  }

  public void changeStateVariable(vStateVariable vsv)
  {
    // fill out jaxb variable
    StateVariable sv = (StateVariable)vsv.opaqueModelObject;
    sv.setName(vsv.getName());
    sv.setShortName(vsv.getName());
    sv.setType(vsv.getType());
    sv.getComment().clear();
    sv.getComment().add(vsv.getComment());

    modelDirty = true;
    this.notifyChanged(new ModelEvent(vsv, ModelEvent.STATEVARIABLECHANGED, "State variable changed"));
  }

  // Event (node) mods
  // -----------------
  /**
   * Add a new event to the graph with the given label, at the given point
   */
  public void newEvent(String nodeName, Point p)
  {
    EventNode node = new EventNode(nodeName);
    if (p == null)
      node.setPosition(new Point(100,100));
    else
      node.setPosition(p);

    Event jaxbEv = null;
    try {
      jaxbEv = oFactory.createEvent();
    }
    catch (JAXBException e) {
      //assert false : "Model.newEvent, error creating simkit.xsd.bindings.Event.";
      System.err.println("Model.newEvent, error creating simkit.xsd.bindings.Event.");
      return;
    }

    jaxbEv.setName(nodeName);
    node.opaqueModelObject = jaxbEv;
    evNodeCache.put(jaxbEv,node);   // key = ev
    jaxbRoot.getEvent().add(jaxbEv);

    modelDirty = true;
    notifyChanged(new ModelEvent(node,ModelEvent.EVENTADDED, "Event added"));
  }
  /**
   * Delete the referenced event, also deleting attached edges.
   *
   * @param node
   */
  public void deleteEvent(EventNode node)
  {
    Event jaxbEv = (Event)node.opaqueModelObject;
    evNodeCache.remove(jaxbEv);
    jaxbRoot.getEvent().remove(jaxbEv);

    modelDirty = true;
    this.notifyChanged(new ModelEvent(node, ModelEvent.EVENTDELETED, "Event deleted"));
  }
  private StateVariable findStateVariable(String nm)
  {
    List lis = jaxbRoot.getStateVariable();
    for(Iterator itr = lis.iterator(); itr.hasNext(); ) {
      StateVariable st = (StateVariable)itr.next();
      if(st.getName().equals(nm))
        return st;
    }
    //assert false : Model.findStateVariable, missing stateVar
    return null;
  }
  private void cloneTransitions(List targ, ArrayList local)
  {
    try {
      targ.clear();
      for(Iterator itr = local.iterator(); itr.hasNext();) {
        EventStateTransition est = (EventStateTransition)itr.next();
        StateTransition st =  oFactory.createStateTransition();
         StateVariable sv = findStateVariable(est.getStateVarName());
        st.setState(sv);
        if(est.isOperation()) {
          Operation o = oFactory.createOperation();
          o.setMethod(est.getOperationOrAssignment());
          st.setOperation(o);
        }
        else {
          Assignment a = oFactory.createAssignment();
          a.setValue(est.getOperationOrAssignment());
          st.setAssignment(a);
        }
        est.opaqueModelObject = st; //replace
        targ.add(st);
      }
    }
    catch (JAXBException e) {
      e.printStackTrace();
    }
  }
  private void cloneComments(List targ, ArrayList local)
  {
    targ.clear();
    targ.addAll(local);
  }
  private void cloneArguments(List targ, ArrayList local)
  {
    try {
      targ.clear();
      for(Iterator itr = local.iterator(); itr.hasNext();) {
        EventArgument ea = (EventArgument)itr.next();
        Argument arg = oFactory.createArgument();
        arg.setName(ea.getName());
        arg.setType(ea.getType());
        arg.getComment().clear();
        arg.getComment().addAll(ea.getComments());
        ea.opaqueModelObject = arg; // replace
        targ.add(arg);
      }
    }
    catch(JAXBException e) {
      System.err.println("Exc Model.cloneArguments() "+e);
    }
  }
  private void cloneLocalVariables(List targ, Vector local)
  {
    try {
      targ.clear();
      for(Iterator itr = local.iterator(); itr.hasNext();) {
        EventLocalVariable elv = (EventLocalVariable)itr.next();
        LocalVariable lvar = oFactory.createLocalVariable();
        lvar.setName(elv.getName());
        lvar.setType(elv.getType());
        lvar.setValue(elv.getValue());
        lvar.getComment().clear();
        lvar.getComment().add(elv.getComment());
        elv.opaqueModelObject = lvar; //replace
        targ.add(lvar);
      }
    }
    catch(JAXBException e) {
      System.err.println("Exc Model.cloneLocalVariables() "+e);
    }

  }
  public void changeEvent(EventNode node)
  {
    Event jaxbEv = (Event)node.opaqueModelObject;

    jaxbEv.setName(node.getName());

    Coordinate coor = null;
    try {
      coor = oFactory.createCoordinate();
    } catch(JAXBException e) {
      System.err.println("Exc Model.changeEvent()");
    }
    coor.setX(""+node.getPosition().x);
    coor.setY(""+node.getPosition().y);
    jaxbEv.setCoordinate(coor);

    cloneTransitions(jaxbEv.getStateTransition(),node.getTransitions());
    cloneComments(jaxbEv.getComment(),node.getComments());
    cloneArguments(jaxbEv.getArgument(),node.getArguments());
    cloneLocalVariables(jaxbEv.getLocalVariable(),node.getLocalVariables());

    // todo connections?

    modelDirty = true;
    this.notifyChanged(new ModelEvent(node, ModelEvent.EVENTCHANGED, "Event changed"));
  }

  // Edge mods
  // ---------
  public void newEdge(EventNode src, EventNode target)
  {
    SchedulingEdge se = new SchedulingEdge();
    se.from = src;
    se.to = target;
    src.getConnections().add(se);
    target.getConnections().add(se);

    Schedule sch;
    try {
      sch = oFactory.createSchedule();
    }
    catch (JAXBException e) {
      //assert false : "Model.newEdge, error createing simkit.xsd.bindings.Schedule.";
      System.err.println("Model.newEdge, error createing simkit.xsd.bindings.Schedule.");
      return;
    }
    se.opaqueModelObject = sch;
    Event targEv = (Event)target.opaqueModelObject;
    sch.setEvent(targEv);
    Event srcEv = (Event)src.opaqueModelObject;
    srcEv.getScheduleOrCancel().add(sch);

    // Put in dummy edge parameters to match the target arguments
    ArrayList args = target.getArguments();
    if(args.size() > 0) {
      ArrayList eps = new ArrayList(args.size());
      for(int i = 0;i<args.size();i++)
        eps.add(new vEdgeParameter(""));
      se.parameters = eps;
    }

    this.edgeCache.put(sch,se);
    modelDirty = true;

    this.notifyChanged(new ModelEvent(se, ModelEvent.EDGEADDED, "Edge added"));
  }

  public void newCancelEdge(EventNode src, EventNode target)
  {
    CancellingEdge ce = new CancellingEdge();
    ce.from = src;
    ce.to = target;
    src.getConnections().add(ce);
    target.getConnections().add(ce);

    Cancel can;
    try {
      can = oFactory.createCancel();
    }
    catch (JAXBException e) {
      //assert false : "Model.newEdge, error createing simkit.xsd.bindings.Cancel.";
      System.err.println("Model.newEdge, error createing simkit.xsd.bindings.Cancel.");
      return;
    }
    ce.opaqueModelObject = can;
    Event targEv = (Event)target.opaqueModelObject;
    can.setEvent(targEv);
    Event srcEv = (Event)src.opaqueModelObject;
    srcEv.getScheduleOrCancel().add(can);

    // Put in dummy edge parameters to match the target arguments
    ArrayList args = target.getArguments();
    if(args.size() > 0) {
      ArrayList eps = new ArrayList(args.size());
      for(int i = 0;i<args.size();i++)
        eps.add(new vEdgeParameter(""));
      ce.parameters = eps;
    }


    this.edgeCache.put(can,ce);
    modelDirty = true;

    this.notifyChanged(new ModelEvent(ce, ModelEvent.CANCELLINGEDGEADDED, "Edge added"));
  }

  public void deleteEdge(SchedulingEdge edge)
  {
    _commonEdgeDelete(edge);

    modelDirty = true;
    this.notifyChanged(new ModelEvent(edge, ModelEvent.EDGEDELETED, "Edge deleted"));
  }

  private void _commonEdgeDelete(Edge edg)
  {
    Object jaxbEdge = edg.opaqueModelObject;

    List nodes = jaxbRoot.getEvent();
    for(Iterator itr = nodes.iterator(); itr.hasNext();) {
      Event ev = (Event)itr.next();
      List edges = ev.getScheduleOrCancel();
      edges.remove(jaxbEdge);
    }

    edgeCache.remove(edg);
  }

  public void deleteCancelEdge(CancellingEdge edge)
  {
    _commonEdgeDelete(edge);

    modelDirty = true;
    this.notifyChanged(new ModelEvent(edge, ModelEvent.CANCELLINGEDGEDELETED, "Cancelling edge deleted"));
  }

  public void changeEdge(Edge e)
  {
    Schedule sch = (Schedule)e.opaqueModelObject;
    sch.setCondition(e.conditional);
    sch.setDelay(""+e.delay);
    sch.setEvent((Event)e.to.opaqueModelObject);
    sch.setPriority("0");  // todo implement priority

    sch.getEdgeParameter().clear();
    for(Iterator itr = e.parameters.iterator(); itr.hasNext();) {
      vEdgeParameter vp = (vEdgeParameter)itr.next();
      EdgeParameter p = null;
      try {
        p = oFactory.createEdgeParameter();
      }
      catch (JAXBException e1) {
        //assert false : "Model.changeEdge, jaxb error createing EdgeParameter";
        System.err.println("Model.changeEdge, jaxb error createing EdgeParameter");
        return;
      }
      //p.setType(vp.getType());
      p.setValue(vp.getValue());
      sch.getEdgeParameter().add(p);
    }

    modelDirty = true;
    this.notifyChanged(new ModelEvent(e, ModelEvent.EDGECHANGED, "Edge changed"));
  }

  public void changeCancelEdge(Edge e)
  {
    Cancel can = (Cancel)e.opaqueModelObject;
    can.setCondition(e.conditional);
    can.setEvent((Event)e.to.opaqueModelObject);

    can.getEdgeParameter().clear();
    for(Iterator itr = e.parameters.iterator(); itr.hasNext();) {
      vEdgeParameter vp = (vEdgeParameter)itr.next();
      EdgeParameter p = null;
      try {
        p = oFactory.createEdgeParameter();
      }
      catch (JAXBException e1) {
        //assert false : "Model.changeEdge, jaxb error createing EdgeParameter";
        System.err.println("Model.changeEdge, jaxb error createing EdgeParameter");
        return;
      }
      //p.setType(vp.getType());
      p.setValue(vp.getValue());
      can.getEdgeParameter().add(p);
    }


    modelDirty = true;
    this.notifyChanged(new ModelEvent(e, ModelEvent.CANCELLINGEDGECHANGED, "Cancelling edge changed"));
  }

  private Vector testNodes = new Vector();
  private Vector testEdges = new Vector();

  private void tempRemoveAllNodes()
  {
    for(Iterator itr = testNodes.iterator(); itr.hasNext();) {
      EventNode en = (EventNode)itr.next();
      notifyChanged(new ModelEvent(en, ModelEvent.EVENTDELETED, "Event deleted"));
    }
    testNodes.clear();
  }
  private void tempRemoveAllEdges()
  {
    for(Iterator itr = testEdges.iterator(); itr.hasNext();) {
      Edge en = (Edge)itr.next();
      if(en instanceof CancellingEdge)
        notifyChanged(new ModelEvent(en, ModelEvent.CANCELLINGEDGEDELETED, "CanEdge deleted"));
      else
        notifyChanged(new ModelEvent(en, ModelEvent.EDGEDELETED,"Edge deleted"));
    }
    testEdges.clear();
  }
  
  public void changeCancelEdge(simkit.viskit.model.Edge e) {
  }
  
  public void changeEdge(simkit.viskit.model.Edge e) {
  }
  
  public void changeEvent(simkit.viskit.model.EventNode ev) {
  }
  
  public void changeSimParameter(simkit.viskit.model.vParameter p) {
  }
  
  public void changeStateVariable(simkit.viskit.model.vStateVariable st) {
  }
  
  public void deleteCancelEdge(simkit.viskit.model.CancellingEdge edge) {
  }
  
  public void deleteEdge(simkit.viskit.model.SchedulingEdge edge) {
  }
  
  public void deleteEvent(simkit.viskit.model.EventNode node) {
  }
  
  public void deleteSimParameter(simkit.viskit.model.vParameter p) {
  }
  
  public void deleteStateVariable(simkit.viskit.model.vStateVariable sv) {
  }
  
  public void newCancelEdge(simkit.viskit.model.EventNode src, simkit.viskit.model.EventNode target) {
  }
  
  public void newEdge(simkit.viskit.model.EventNode src, simkit.viskit.model.EventNode target) {
  }
  
  /*
  private void initTestData()
  {
    modelDirty = true;
    EventNode run_en = new EventNode("Run");  testNodes.add(run_en);
    announceNewNode(run_en,new Point(20,50));

    EventNode arr_en = new EventNode("Arrival"); testNodes.add(arr_en);
    arr_en.stateTrans = "Q++";
    announceNewNode(arr_en,new Point(120,50));

    EventNode st_en = new EventNode("Start\nService");  testNodes.add(st_en);
    st_en.stateTrans = "Q--, S--";
    announceNewNode(st_en,new Point(270,50));

    EventNode endSvc_en = new EventNode("End\nService");  testNodes.add(endSvc_en);
    endSvc_en.stateTrans = "S++";
    announceNewNode(endSvc_en,new Point(420,50));

    EventNode endR_en = new EventNode("End\nRepair");    testNodes.add(endR_en);
    endR_en.stateTrans = "F--";
    announceNewNode(endR_en,new Point(270,200));

    EventNode fail_en = new EventNode("Failure");       testNodes.add(fail_en);
    fail_en.stateTrans = "F++,<br>Q += 1-S,<br>S = 0";
    announceNewNode(fail_en,new Point(420,200));

    SchedulingEdge sEdge           = tempNewEdge(run_en,arr_en);      testEdges.add(sEdge);
    SchedulingEdge seArrToSvc      = tempNewEdge(arr_en,st_en);       testEdges.add(seArrToSvc);
    SchedulingEdge seSTStoEnds     = tempNewEdge(st_en,endSvc_en);    testEdges.add(seSTStoEnds);
    SchedulingEdge seEndToSt       = tempNewEdge(endSvc_en,st_en);    testEdges.add(seEndToSt);
    SchedulingEdge seEndRprToStSvc = tempNewEdge(endR_en,st_en);      testEdges.add(seEndRprToStSvc);
    CancellingEdge cEdge           = tempNewCancelEdge(fail_en,endSvc_en);  testEdges.add(cEdge);
    SchedulingEdge seEndRprToFail  = tempNewEdge(endR_en,fail_en);    testEdges.add(seEndRprToFail);
    SchedulingEdge seFailToEndRpt  = tempNewEdge(fail_en,endR_en);    testEdges.add(seFailToEndRpt);

    nodes.put(run_en.getModelKey(),    run_en);
    nodes.put(arr_en.getModelKey(),    arr_en);
    nodes.put(st_en.getModelKey(),     st_en);
    nodes.put(endSvc_en.getModelKey(), endSvc_en);
    nodes.put(endR_en.getModelKey(),   endR_en);
    nodes.put(fail_en.getModelKey(),   fail_en);

    edges.put(sEdge.getModelKey(),sEdge);
    edges.put(seArrToSvc.getModelKey(),seArrToSvc);
    edges.put(seSTStoEnds.getModelKey(),seSTStoEnds);
    edges.put(seEndToSt.getModelKey(),seEndToSt);
    edges.put(seEndRprToStSvc.getModelKey(),seEndRprToStSvc);
    edges.put(cEdge.getModelKey(),cEdge);
    edges.put(seEndRprToFail.getModelKey(),seEndRprToFail);
    edges.put(seFailToEndRpt.getModelKey(),seFailToEndRpt);

    announceNewEdge(sEdge);
    announceNewEdge(seArrToSvc);
    announceNewEdge(seSTStoEnds);
    announceNewEdge(seEndToSt);
    announceNewEdge(seEndRprToStSvc);
    announceNewEdge(seEndRprToFail);
    announceNewEdge(seFailToEndRpt);

    announceCancelEdge(cEdge);
  }
*/
}
