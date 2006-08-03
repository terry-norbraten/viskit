/*
* XsltUtility.java
*
* Created on March 11, 2004, 4:55 PM
*
* This class was written by CDR Duane Davis for work on the AUV Workbench.
* It was copied to this application to perform XSLT conversions.
*
*@author Duane Davis
*@version $Id$
*/

package viskit.xsd.assembly;



import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class XsltUtility
{
   
    /** Creates a new instance of XmlUtilities */
    public XsltUtility() {}



   
    /**
     * Runs an XSL Transformation on an XML file and writes the result to another file
     *
     * @param inFile XML file to be transformed
     * @param outFile output file for transformation results
     * @param xslFile XSLT to utilize for transformation
     *
     * @return the resulting transformed XML file
     */
    public static boolean runXslt(String inFile, String outFile, String xslFile)
    {
        try // FileNotFoundException, TransformerConfigurationException, TransformerException
        {
            TransformerFactory factory = TransformerFactory.newInstance();
            Templates template  = factory.newTemplates(new StreamSource(new FileInputStream(xslFile)));
            Transformer xFormer = template.newTransformer();
            Source source = new StreamSource(new FileInputStream(inFile));
            Result result = new StreamResult(new FileOutputStream(outFile));
            xFormer.transform(source, result);
        } // try
        catch (FileNotFoundException e)
        {
            System.out.println("Unable to load file for XSL Transformation\n" +
                               "   Input file : " + inFile + "\n" +
                               "   Output file: " + outFile + "\n" +
                               "   XSLT file  : " + xslFile);
            return false;
        } // catch (FileNotFoundException e)
        catch (TransformerConfigurationException e)
        {
            System.out.println("Unable to configure transformer for XSL Transformation");
            return false;
        } // catch (TransformerConfigurationException e)
        catch (TransformerException e)
        {
            System.out.println("Exception during XSL Transformation");
            return false;
        } // catch (TransformerException e)
       
        return true;
    } // runXslt   
   
} // XmlUtilities