//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.2-b15-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2004.04.15 at 03:17:15 PDT 
//


package viskit.xsd.bindings;


/**
 * Java content class for Schedule element declaration.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/home/rmgold/CVS/Simkit/simkit/xml/simkit.xsd line 101)
 * <p>
 * <pre>
 * &lt;element name="Schedule">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element ref="{}EdgeParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;attribute name="condition" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *         &lt;attribute name="event" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *         &lt;attribute name="delay" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" default="0.0" />
 *         &lt;attribute name="priority" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" default="0.0" />
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 */
public interface Schedule
    extends javax.xml.bind.Element, simkit.xsd.bindings.ScheduleType
{


}
