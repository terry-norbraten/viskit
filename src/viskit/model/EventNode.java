package viskit.model;

import viskit.xsd.bindings.eventgraph.Event;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey CA
 * www.nps.edu
 * @author Mike Bailey
 * @since Mar 8, 2004
 * @since 9:08:08 AM
 * @version $Id$
 *
 * An event as seen by the model (not the view)
 */
public class EventNode extends ViskitElement {

    private String name;
    private Vector<ViskitElement> connections = new Vector<ViskitElement>();
    private Vector<ViskitElement> localVariables = new Vector<ViskitElement>();
    private List<String> comments = new ArrayList<String>();
    private List<ViskitElement> transitions = new ArrayList<ViskitElement>();
    private List<ViskitElement> arguments = new ArrayList<ViskitElement>();
    private List<String> descriptionArray = new ArrayList<String>();
    private Point position = new Point(0, 0);
    private String codeblock = "";
    private String type;
    private boolean operation;
    private String operationOrAssignment;
    private String arrayType;
    private String indexingExpression;
    private String stateVarName;
    private String value;
    private String comment;
    private String stateVarType;

    EventNode(String name) // package access on constructor
    {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public EventNode shallowCopy() {
        EventNode en = (EventNode) super.shallowCopy(new EventNode(name + "-copy"));
        en.connections = connections;
        en.comments = comments;
        en.transitions = transitions;
        en.localVariables = localVariables;
        en.arguments = arguments;
        en.codeblock = codeblock;
        return en;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        if (this.opaqueModelObject != null) {
            ((Event) opaqueModelObject).setName(s);
        }
        this.name = s;
    }

    public List<ViskitElement> getArguments() {
        return arguments;
    }

    public void setArguments(ArrayList<ViskitElement> arguments) {
        this.arguments = arguments;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public void setCodeBLock(String s) {
        this.codeblock = s;
    }

    public String getCodeBlock() {
        return codeblock;
    }

    public Vector<ViskitElement> getConnections() {
        return connections;
    }

    public void setConnections(Vector<ViskitElement> connections) {
        this.connections = connections;
    }

    public Vector<ViskitElement> getLocalVariables() {
        return localVariables;
    }

    public void setLocalVariables(Vector<ViskitElement> localVariables) {
        this.localVariables = localVariables;
    }

    public List<ViskitElement> getTransitions() {
        return transitions;
    }

    public void setTransitions(ArrayList<ViskitElement> transitions) {
        this.transitions = transitions;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public List<String> getDescriptionArray() {
        return descriptionArray;
    }

    @Override
    public void setDescriptionArray(ArrayList<String> descriptionArray) {
        this.descriptionArray = descriptionArray;
    }

    @Override
    public String getArrayType() {
        return arrayType;
    }

    @Override
    public String getIndexingExpression() {
        return indexingExpression;
    }

    @Override
    public String getStateVarName() {
        return stateVarName;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getOperationOrAssignment() {
        return operationOrAssignment;
    }

    @Override
    public boolean isOperation() {
        return operation;
    }

    @Override
    public String getStateVarType() {
        return stateVarType;
    }
}
