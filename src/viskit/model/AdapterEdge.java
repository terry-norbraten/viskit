package viskit.model;

import java.util.ArrayList;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey CA
 * www.nps.navy.mil
 * @author Mike Bailey
 * @since Mar 8, 2004
 * @since 9:04:09 AM
 * @version $Id: AdapterEdge.java 1662 2007-12-16 19:44:04Z tdnorbra $
 */
public class AdapterEdge extends AssemblyEdge {

    private String targetEvent;
    private String sourceEvent;
    private String name;
    private String type;
    private ArrayList<String> descriptionArray = new ArrayList<String>();

    AdapterEdge() // package-limited
    {
    }

    public String getTargetEvent() {
        return targetEvent;
    }

    public void setTargetEvent(String ev) {
        targetEvent = ev;
    }

    public String getSourceEvent() {
        return sourceEvent;
    }

    public void setSourceEvent(String ev) {
        sourceEvent = ev;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String n) {
        name = n;
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
    public ArrayList<String> getDescriptionArray() {
        return descriptionArray;
    }

    @Override
    public void setDescriptionArray(ArrayList<String> descriptionArray) {
        this.descriptionArray = descriptionArray;
    }
}