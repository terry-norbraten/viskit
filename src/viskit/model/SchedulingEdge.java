package viskit.model;

import java.util.ArrayList;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey CA
 * www.nps.edu
 * @author Mike Bailey
 * @since Mar 8, 2004
 * @since 9:04:09 AM
 * @version $Id$
 */
public class SchedulingEdge extends Edge {

    public String priority;
    
    /** Regex expression for simkit.Priority floating point values */
    public static final String DIGITS = "(\\p{Digit}+)";
    
    /** Regex expression for simkit.Priority exponential values */
    public static final String EXP = "[eE][+-]?";
    
    /** Adapted from JDK 1.6 Javadoc on java.lang.Double.valueOf(String s) */
    public static final String FLOATING_POINT_REGEX = 
            "([\\x00-\\x20]*" +  // Optional leading "whitespace"
             "[+-]?(" +          // Optional sign character
             "NaN|" +            // "NaN" string
             "Infinity|" +       // "Infinity" string

             // A decimal floating-point string representing a finite positive
             // number without a leading sign has at most five basic pieces:
             // Digits . Digits ExponentPart FloatTypeSuffix
             // 
             // Since this method allows integer-only strings as input
             // in addition to strings of floating-point literals, the
             // two sub-patterns below are simplifications of the grammar
             // productions from the Java Language Specification, 2nd 
             // edition, section 3.10.2.

             // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
             "((("+DIGITS+"(\\.)?("+DIGITS+"?)("+EXP+")?)" +

             // . Digits ExponentPart_opt FloatTypeSuffix_opt
             "(\\.("+DIGITS+")("+EXP+")?)" +
             "[\\x00-\\x20]*))))";   // Optional leading "whitespace"
    
    private String type;
    private ArrayList<String> descriptionArray = new ArrayList<String>();
    private String name;
    private boolean operation;
    private String operationOrAssignment;
    private String arrayType;
    private String indexingExpression;
    private String stateVarName;
    private String value;
    private String comment;
    private String stateVarType;

    /** package-limited constructor */
    SchedulingEdge() {
        parameters = new ArrayList<ViskitElement>();
    }

    Object copyShallow() {
        SchedulingEdge se = new SchedulingEdge();
        se.opaqueViewObject = opaqueViewObject;
        se.to = to;
        se.from = from;
        se.parameters = parameters;
        se.delay = delay;
        se.conditional = conditional;
        se.conditionalDescription = conditionalDescription;
        se.priority = priority;
        return se;
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
