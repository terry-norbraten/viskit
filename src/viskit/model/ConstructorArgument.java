package viskit.model;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * @author Mike Bailey
 * @since Apr 1, 2004
 * @since 3:57:26 PM
 * @version $Id: ConstructorArgument.java 1662 2007-12-16 19:44:04Z tdnorbra $
 * 
 * To change this template use File | Settings | File Templates.
 */
public class ConstructorArgument extends ViskitElement {
    private ArrayList<String> descriptionArray = new ArrayList<String>();
    private String name;

    private String type;
    private String value;
    private ArrayList<String> comments = new ArrayList<String>();

    public ArrayList getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String name) {
        this.value = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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