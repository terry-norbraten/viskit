package viskit.model;

import viskit.xsd.bindings.Event;

import java.awt.*;
import java.util.ArrayList;
import java.util.Vector;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey CA
 * www.nps.navy.mil
 * By:   Mike Bailey
 * Date: Mar 8, 2004
 * Time: 9:08:08 AM
 */

/**
 * An event as seen by the model (not the view)
 */
public class EvGraphNode extends ViskitElement
{
  private String name;
  private String type;

  private Vector    connections = new Vector();
  private ArrayList constructorArguments = new ArrayList();
  private ArrayList comments = new ArrayList();
/*
  private ArrayList transitions = new ArrayList();
  private Vector    localVariables = new Vector();
  private ArrayList arguments = new ArrayList();
*/
  private Point     position = new Point(0,0);

  EvGraphNode(String name, String type)      // package access on constructor
  {
    this.name = name;
    this.type = type;
  }
  public String toString()
  {
    return name;
  }
  public EvGraphNode shallowCopy()
  {
    EvGraphNode en   = (EvGraphNode)super.shallowCopy(new EvGraphNode(name+"-copy",type));
    en.connections = connections;
    en.comments    = comments;
/*
    en.transitions = transitions;
    en.localVariables = localVariables;
    en.arguments   = arguments;
*/
    en.connections = connections;
    return en;
  }
  public String getName()
  {
    /*
    if(this.opaqueModelObject != null)
      return ((Event)opaqueModelObject).getName();
    else
    */
      return name;
  }
  public void setName(String s)
  {
    if(this.opaqueModelObject != null)
      ((Event)opaqueModelObject).setName(s);

    this.name = s;
  }
  public String getType()
  {
    return type;
  }
  public void setType(String typ)
  {
   //todo if(this.opaqueModelObject != null)
   //   ((Event)opaqueModelObject).setName(s);

    this.type = typ;

  }
  public ArrayList getConstructorArguments()
  {
    return constructorArguments;
  }
  public void setConstructorArguments(ArrayList lis)
  {
    constructorArguments = lis;
  }
 
  public ArrayList getComments()
  {
    return comments;
  }

  public void setComments(ArrayList comments)
  {
    this.comments = comments;
  }

  public Vector getConnections()
  {
    return connections;
  }

  public void setConnections(Vector connections)
  {
    this.connections = connections;
  }

  public Point getPosition()
  {
    return position;
  }

  public void setPosition(Point position)
  {
    this.position = position;
  }

}
