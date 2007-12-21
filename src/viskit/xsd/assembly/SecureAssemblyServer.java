/*
 * SecureAssemblyServer.java
 *
 * Created on January 26, 2006, 2:41 PM
 *
 */

package viskit.xsd.assembly;
import org.apache.xmlrpc.secure.SecureWebServer;
import org.apache.xmlrpc.secure.SecurityTool;

/**
 *
 * @author rmgold
 */
   
 class SecureAssemblyServer extends SecureWebServer {
     SimkitAssemblyXML2Java inst;
     
     SecureAssemblyServer(SimkitAssemblyXML2Java inst, int port, String keyStore, String keyPass, String trustStore, String trustStorePassword) {
         super(port);
         this.inst = inst;
         SecurityTool.setKeyStore(keyStore);
         SecurityTool.setKeyStorePassword(keyPass);
         SecurityTool.setTrustStore(trustStore);
         SecurityTool.setTrustStorePassword(trustStorePassword);
         addHandler("experiment",inst);
     }
 }
