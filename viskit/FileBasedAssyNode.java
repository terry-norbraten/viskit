package viskit;

import java.io.File;

/**
 * OPNAV N81 - NPS World Class Modeling (WCM)  2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * By:   Mike Bailey
 * Date: Jul 21, 2004
 * Time: 3:26:42 PM
 */

public class FileBasedAssyNode
{
  public static final String delim = "<fbasdelim>";
  public String loadedClass;
  public File  xmlSource;
  public File  classFile;
  public boolean isXML;
  public String pkg;

  public FileBasedAssyNode(File classFile, String loadedClass, File xml, String pkg)
  {
    this.xmlSource = xml;
    this.classFile = classFile;
    this.loadedClass = loadedClass;
    this.pkg = pkg;
    isXML = true;
  }
  public FileBasedAssyNode(File classFile, String loadedClass, String pkg)
  {
    this.xmlSource = null;
    this.classFile = classFile;
    this.loadedClass = loadedClass;
    this.pkg = pkg;
    isXML = false;
  }
  public String toString()
  {
    if(isXML)
      return classFile.getPath() + delim + loadedClass + delim + xmlSource.getPath() + delim + pkg;
    else
      return classFile.getPath() + delim + loadedClass + delim + pkg;
  }
  public static FileBasedAssyNode fromString(String s) throws FileBasedAssyNode.exception 
  {
    try {
      String[] sa = s.split(delim);
      if(sa.length == 3)
        return new FileBasedAssyNode(new File(sa[0]),sa[1],sa[2]);
      else if(sa.length == 4)
        return new FileBasedAssyNode(new File(sa[0]),sa[1],new File(sa[2]),sa[3]);
    }
    catch (Exception e) {}
    throw new FileBasedAssyNode.exception();
  }

  static class exception extends Exception
  {

  }
}
