package viskit.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/** This class was written by CDR Duane Davis for the AUV Workbench.
 * It was copied to this application to perform handy XSLT conversions.
 *
 * @author Duane Davis
 * @since March 11, 2004, 4:55 PM
 * @version $Id$
 */
public class XsltUtility {

    /**
     * Runs an XSL Transformation on an XML file and writes the result to another file
     *
     * @param inFile XML file to be transformed
     * @param outFile output file for transformation results
     * @param xslFile XSLT to utilize for transformation
     *
     * @return the resulting transformed XML file
     */
    public static boolean runXslt(String inFile, String outFile, String xslFile) {

        try {
            // Force Xalan for this TransformerFactory
            System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
            TransformerFactory factory = TransformerFactory.newInstance();

            // Look in the viskit.jar file for this XSLT
            Templates template = factory.newTemplates(new StreamSource(XsltUtility.class.getClassLoader().getResourceAsStream(xslFile)));
            Transformer xFormer = template.newTransformer();
            Source source = new StreamSource(new FileInputStream(inFile));
            Result result = new StreamResult(new FileOutputStream(outFile));
            xFormer.transform(source, result);
        } catch (FileNotFoundException e) {
            System.err.println("Unable to load file for XSL Transformation\n" +
                    "   Input file : " + inFile + "\n" +
                    "   Output file: " + outFile + "\n" +
                    "   XSLT file  : " + xslFile);
            return false;
        } catch (TransformerConfigurationException e) {
            System.err.println("Unable to configure transformer for XSL Transformation");
            return false;
        } catch (TransformerException e) {
            System.err.println("Exception during XSL Transformation");
            return false;
        }
        return true;
    }

} // end class file XsltUtility.java