package viskit.model;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * @author Mike Bailey
 * @since Apr 7, 2004
 * @since 3:19:43 PM
 * @version $Id: vEdgeParameter.java 1662 2007-12-16 19:44:04Z tdnorbra $
 * To change this template use File | Settings | File Templates.
 */
public class vEdgeParameter extends ViskitElement {
    
    private String type;
    private String value;
    public String bogus; //todo fix
    private String name;
    private ArrayList<String> descriptionArray = new ArrayList<String>();
    
    public vEdgeParameter(String value)//, String type)
    {
        //this.type = type;
        this.value = value;
    }
    
    public String getType()
    {
    return type;
    }
    
    public void setType(String type)
    {
    this.type = type;
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
