package viskit;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Autonomous Underwater Vehicle Workbench
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * @author Mike Bailey
 * @since Mar 8, 2005
 * @since 11:09:07 AM
 * @version $Id: ViskitConfig.java 1662 2007-12-16 19:44:04Z tdnorbra $
 */

public class ViskitConfig
{
  public static String configFile = "vconfig.xml";
  private static ViskitConfig me;

  public static synchronized ViskitConfig instance()
  {
    if (me == null)
      me = new ViskitConfig(configFile);
    return me;
  }

  private HashMap<String, XMLConfiguration> configs = new HashMap<String, XMLConfiguration>();
  private HashMap<String, String> sessionHM = new HashMap<String, String>();
  private CompositeConfiguration config;

  private ViskitConfig(String cfile)
  {
      // TODO: Since this is carry over from AUV Workbench, may need some INFO
      // level detail for T/S
//    Logger.getRootLogger().setLevel(Level.FATAL);        // don't show everything
//    BasicConfigurator.configure();
    try {
      ConfigurationFactory factory = new ConfigurationFactory();
      factory.setConfigurationFileName(cfile);
      config = (CompositeConfiguration) factory.getConfiguration();

      // Save off the indiv XML config for each prefix so we can write back
      for (int i = 0; i < config.getNumberOfConfigurations(); i++) {
        Object obj = config.getConfiguration(i);
        if (!(obj instanceof XMLConfiguration))
          continue;
        XMLConfiguration xc = (XMLConfiguration) obj;
        xc.setAutoSave(true);
        HierarchicalConfiguration.Node n = xc.getRoot();
        List lis = n.getChildren();
        for (Iterator itr = lis.iterator(); itr.hasNext();) {
          Object o = itr.next();
          configs.put(((HierarchicalConfiguration.Node) o).getName(), xc);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Rather screwy.  A decent design would allow the CompositeConfiguration obj
   * to do the saving, but it won't.
   *
   * @param key
   * @param val
   */
  public void setVal(String key, String val)
  {
    String cfgKey = key.substring(0, key.indexOf('.'));
    XMLConfiguration xc = (XMLConfiguration) configs.get(cfgKey);
    xc.setProperty(key, val);
  }

  public void setSessionVal(String key, String val)
  {
    sessionHM.put(key, val);
  }

  public String getVal(String key)
  {
    String retS = (String) sessionHM.get(key);
    if (retS != null && retS.length() > 0)
      return retS;

    return config.getString(key);
  }

  public int getConfigValueCount(String key)
  {
    String[]sa = config.getStringArray(key);
    return sa.length;
  }

  public XMLConfiguration getIndividualXMLConfig(String f) throws Exception
  {
    XMLConfiguration config = new XMLConfiguration(f);
    config.setAutoSave(true);
    return config;
  }
}