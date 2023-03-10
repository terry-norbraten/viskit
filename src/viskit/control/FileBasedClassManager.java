package viskit.control;

import edu.nps.util.GenericConversion;
import edu.nps.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.math.BigInteger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.configuration.XMLConfiguration;

import org.apache.logging.log4j.Logger;

import viskit.util.FileBasedAssyNode;
import viskit.util.FindClassesForInterface;

import viskit.VGlobals;

import viskit.ViskitConfig;

import viskit.VStatics;

import viskit.xsd.bindings.eventgraph.SimEntity;
import viskit.xsd.translator.eventgraph.SimkitXML2Java;

/** A custom class manager to support finding EGs and PCLs in *.class form vice
 * XML.  Used to populate the LEGOs tree on the Assy Editor.
 *
 * <pre>
 * OPNAV N81 - NPS World Class Modeling (WCM)  2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * </pre>
 * @author Mike Bailey
 * @since Jul 23, 2004
 * @since 12:53:36 PM
 * @version $Id$
 */
public class FileBasedClassManager {

    static final Logger LOG = LogUtils.getLogger(FileBasedClassManager.class);

    // Singleton:
    protected static FileBasedClassManager me;
    private static XMLConfiguration projectConfig;
    private Map<String, FileBasedAssyNode> fileMap;
    private Map<String, Class<?>> classMap;

    public static synchronized FileBasedClassManager instance() {
        if (me == null) {
            me = new FileBasedClassManager();
        }

        // This requires reinitializing everytime this FBM is called
        projectConfig = ViskitConfig.instance().getProjectXMLConfig();
        return me;
    }

    private FileBasedClassManager() {
        classMap = new HashMap<>();
        fileMap = Collections.synchronizedMap(new HashMap<String, FileBasedAssyNode>());
    }

    public void addFileClass(Class<?> c) {
        classMap.put(c.getName(), c);
    }

    public void removeFileClass(Class<?> c) {
        removeFileClass(c.getName());
    }

    private void removeFileClass(String nm) {
        classMap.remove(nm);
    }

    public Class<?> getFileClass(String s) {
        return classMap.get(s);
    }

    public void unloadFile(FileBasedAssyNode fban) {
        removeFileClass(fban.loadedClass);
        fileMap.remove(fban.loadedClass);
    }
    FileBasedAssyNode fban = null;
    Class<?> fclass = null;
    JAXBContext jaxbCtx = null;
    Unmarshaller um = null;
    PkgAndFile paf = null;
    File fXml = null;
    SimEntity simEntity = null;

    /** Known path for EventGraph Compilation
     *
     * @param f an event graph to compile
     * @param implementsClass to test for extension of simkit.BasicSimEntity
     * @return a node tree for viewing in the Assembly Editor
     * @throws java.lang.Throwable for a problem finding a class
     */
    public FileBasedAssyNode loadFile(File f, Class<?> implementsClass) throws Throwable {

        // if it is cached, cacheXML directory exists and will be loaded on start
        if (f.getName().toLowerCase().endsWith(".xml")) {
            jaxbCtx = JAXBContext.newInstance(SimkitXML2Java.EVENT_GRAPH_BINDINGS);
            um = jaxbCtx.createUnmarshaller();

            // Did we cacheXML the EventGraph XML and Class?
            if (!isCached(f)) {

                // Make sure it's not a Cached Miss
                if (!isCacheMiss(f)) {

                    // This will compile first time found EGs
                    paf = ((AssemblyControllerImpl)VGlobals.instance().getAssemblyController()).createTemporaryEventGraphClass(f);

                    // Compile fail of an EventGraph, so just return here
                    if (paf == null) {
                        return null;
                    }

                    // Reset this so that the correct FBAN gets created
                    fXml = null;
                    setFileBasedAssemblyNode(f);

                    // TODO: work situtation where another build/classes gets added
                    // to the classpath as it won't readily be seen before the
                    // project's build/classes is.  This causes ClassNotFoundExceptions
                    addCache(f, fban.classFile);
                }

            // It's cached
            } else {
                f = getCachedClass(f);
                fXml = getCachedXML(f);
                setFileBasedAssemblyNode(f);
            }

        // Check, but don't cacheXML .class files
        } else if (f.getName().toLowerCase().endsWith(".class")) {
            fclass = FindClassesForInterface.classFromFile(f, implementsClass);   // Throwable from here possibly
            if (fclass != null) {
                String pkg = fclass.getName().substring(0, fclass.getName().lastIndexOf("."));
                fban = new FileBasedAssyNode(f, fclass.getName(), pkg);

                // If we have an annotated ParameterMap, then cacheXML it.  If not,
                // then treat the fclass as something that belongs on the
                // extra classpath
                List<Object>[] pMap = VStatics.resolveParameters(fclass);
                if (pMap != null && pMap.length > 0)
                    VStatics.putParameterList(fclass.getName(), pMap);
            }

        // TODO: Check if this is really necessary and should be dealt with
        // upstream
        } else if (!f.getName().toLowerCase().endsWith(".java")) {
            throw new Exception("Unsupported file type.");
        }

        if (fclass != null) {
            addFileClass(fclass);
            fileMap.put(fclass.getName(), fban);
        } else {
            fban = null;
        }
        return fban;
    }

    private void setFileBasedAssemblyNode(File f) {

        // bug fix 1407
        ClassLoader loader = VGlobals.instance().getWorkClassLoader();

        // since we're here, cacheXML the parameter names
        try {
            simEntity = (fXml == null) ? (SimEntity) um.unmarshal(f) : (SimEntity) um.unmarshal(fXml);

            // NOTE: If the project's build directory got nuked and we have
            // cached our EGs and classes with MD5 hash, we'll throw a
            // ClassNotFoundException.
            // TODO: Check for this and recompile the EGs before loading their classes
            fclass = loader.loadClass(simEntity.getPackage() + "." + simEntity.getName());

            fban =  (fXml == null) ?
                new FileBasedAssyNode(paf.f, fclass.getName(), f, paf.pkg) :
                new FileBasedAssyNode(f, fclass.getName(), fXml, simEntity.getPackage());

            List<Object>[] pa = GenericConversion.newListObjectTypeArray(List.class, 1);
            pa[0].addAll(simEntity.getParameter());
            VStatics.putParameterList(fclass.getName(), pa);

            LOG.debug("Put " + fclass.getName() + simEntity.getParameter());

        } catch (JAXBException | ClassNotFoundException | NoClassDefFoundError e) {
            LOG.error(e);
        }
    }

    /**
     * Cache the EG and it's .class file with good MD5 hash
     * @param xmlEg the EG to cacheXML
     * @param classFile the classfile of this EG
     */
    public void addCache(File xmlEg, File classFile) {
        // isCached ( itself checks isStale, if so update and return cached false ) if so don't bother adding the same cacheXML
        if (isCached(xmlEg)) {
            return;
        }
        try {
            List<String> cache = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_EVENTGRAPHS_KEY));
            if (viskit.VStatics.debug) {
                if (cache == null) {
                    LOG.debug("cache " + cache);
                } else {
                    LOG.debug("cache size " + cache.size());
                }
            }

            // TODO: Not used right now, but may be useful for other build/classes paths
//            if (cacheXML.isEmpty()) {
//                String s = VGlobals.instance().getWorkDirectory().getCanonicalPath().replaceAll("\\\\", "/");
//                if (viskit.VStatics.debug) {
//                    LOG.debug("Cache is empty, creating workDir entry at " + s);
//                }
//                projectConfig.setProperty(ViskitConfig.CACHED_WORKING_DIR_KEY, s);
//            }
            if (viskit.VStatics.debug) {
                LOG.debug("Adding cache " + xmlEg + " " + classFile);
            }

            if (cache != null) {
                projectConfig.setProperty("Cached.EventGraphs(" + cache.size() + ")[@xml]", xmlEg.getCanonicalPath().replaceAll("\\\\", "/"));
                projectConfig.setProperty("Cached.EventGraphs(" + cache.size() + ")[@class]", classFile.getCanonicalPath().replaceAll("\\\\", "/"));
                projectConfig.setProperty("Cached.EventGraphs(" + cache.size() + ")[@digest]", createMessageDigest(xmlEg, classFile));
            }
            // if used to miss, unmiss it
            removeCacheMiss(xmlEg);
        } catch (IOException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
        }
    }

    /** Creates an MD5 message digest composed of file contents.  If contents
     * change there will be a mismatch in the new digest, so delete cacheXML
     * etc.
     * @param files the varargs containing files to evaluate
     * @return a String representation of the message digest
     */
    public String createMessageDigest(File... files) {

        String retVal = "";

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            for (File file : files) {
                byte[] buf = new byte[(int) file.length()];
                try {
                    try (InputStream is = file.toURI().toURL().openStream()) {
                        is.read(buf);
                        md.update(buf);
                    }
                } catch (IOException ex) {

                    // This can happen if the build/classes directory got nuked
                    // so, ignore
//                    LOG.error(ex);
//                    ex.printStackTrace();
                }
            }
            byte[] hash = md.digest();
            if (viskit.VStatics.debug) {
                LOG.debug("hash " + new BigInteger(hash).toString(16) + " " + hash.length);
            }
            retVal = new BigInteger(hash).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
        }
        return retVal;
    }

    public boolean isCached(File file) {
        List<String> cacheXML = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_EVENTGRAPHS_KEY));
        try {
            String filePath = file.getCanonicalPath().replaceAll("\\\\", "/");
            LOG.debug("isCached() " + file + " of cacheSize " + cacheXML.size());
            LOG.debug("chached " + cacheXML.contains(filePath));
            if (cacheXML.contains(filePath)) {
                if (isStale(file)) {
                    deleteCache(file);
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } catch (IOException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
            return false;
        }
    }

    /**
     * @param file XML file cached with its class file
     * @return a cached class file given its cached XML file
     */
    public File getCachedClass(File file) {
        List<String> cacheXML = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_EVENTGRAPHS_KEY));
        List<String> cacheClass = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_EVENTGRAPHS_CLASS_KEY));
        int index = 0;
        try {
            index = cacheXML.lastIndexOf(file.getCanonicalPath().replaceAll("\\\\", "/"));
            if (viskit.VStatics.debug) {
                LOG.debug("getCached index at " + index);
                LOG.debug("will return " + cacheClass.get(index));
            }
        } catch (IOException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
        }
        File cachedFile = new File(cacheClass.get(index));
        if (viskit.VStatics.debug) {
            LOG.debug("cachedFile index at " + index);
            LOG.debug("will return " + cachedFile);
        }
        return cachedFile;
    }

    /**
     * @param file cached compiled class file of XML file
     * @return an XML file given its cached class file
     */
    public File getCachedXML(File file) {
        List<String> cacheXML = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_EVENTGRAPHS_KEY));
        List<String> cacheClass = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_EVENTGRAPHS_CLASS_KEY));
        int index = 0;
        try {
            index = cacheClass.lastIndexOf(file.getCanonicalPath().replaceAll("\\\\", "/"));
            if (viskit.VStatics.debug) {
                LOG.debug("getCachedXml index at " + index);
                LOG.debug("will return " + cacheXML.get(index));
            }
        } catch (IOException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
        }
        File cachedFile = new File(cacheXML.get(index));
        if (viskit.VStatics.debug) {
            LOG.debug("cachedFile index at " + index);
            LOG.debug("will return " + cachedFile);
        }
        return cachedFile;
    }

    /** Delete cacheXML given either xml or class file
     * @param file the XML, or class file to delete from the cacheXML
     */
    public void deleteCache(File file) {
        List<String> cacheXML = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_EVENTGRAPHS_KEY));
        List<String> cacheClass = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_EVENTGRAPHS_CLASS_KEY));
        String filePath;
        File deletedCache = null;
        try {
            filePath = file.getCanonicalPath().replaceAll("\\\\", "/");

            int index = - 1;
            if (cacheXML.contains(filePath)) {
                index = cacheXML.lastIndexOf(filePath);
                deletedCache = new File(cacheClass.get(index));
            } else if (cacheClass.contains(filePath)) {
                index = cacheClass.lastIndexOf(filePath);
                deletedCache = file;
            }
            if (index >= 0) {
                projectConfig.clearProperty("Cached.EventGraphs(" + index + ")[@xml]");
                projectConfig.clearProperty("Cached.EventGraphs(" + index + ")[@class]");
                projectConfig.clearProperty("Cached.EventGraphs(" + index + ")[@digest]");

                boolean didDelete = false;
                if (deletedCache != null)
                    didDelete = deletedCache.delete();
                if (viskit.VStatics.debug) {
                    LOG.debug(didDelete + ": cachedFile deleted index at " + index);
                }
            }
        } catch (IOException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
        }
    }

    /** Check if digests match as well as being on list, if no digest match
     * file changed since being a miss, so do updates etc.
     * @param file the file to evaluate
     * @return an indication of miss caching
     */
    public boolean isCacheMiss(File file) {
        List<String> cacheMisses = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_MISS_FILE_KEY));
        List<String> digests = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_MISS_DIGEST_KEY));
        int index;
        try {
            index = cacheMisses.lastIndexOf(file.getCanonicalPath().replaceAll("\\\\", "/"));
            if (index >= 0) {
                String digest = digests.get(index);
                String compare = createMessageDigest(file);
                return digest.equals(compare);
            }
        } catch (IOException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
        }
        return false;
    }

    public void addCacheMiss(File file) {
        deleteCache(file);
        removeCacheMiss(file); // remove any old ones
        int index = ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_MISS_FILE_KEY).length;
        try {
            projectConfig.addProperty("Cached.Miss(" + index + ")[@file]", file.getCanonicalPath().replaceAll("\\\\", "/"));
            projectConfig.addProperty("Cached.Miss(" + index + ")[@digest]", createMessageDigest(file));
        } catch (IOException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
        }
    }

    public void removeCacheMiss(File file) {
        List<String> cacheMisses = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_MISS_FILE_KEY));
        int index;
        try {
            if ((index = cacheMisses.lastIndexOf(file.getCanonicalPath().replaceAll("\\\\", "/"))) > -1) {
                projectConfig.clearProperty("Cached.Miss(" + index + ")[@file]");
                projectConfig.clearProperty("Cached.Miss(" + index + ")[@digest]");
            }
        } catch (IOException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
        }
    }

    /** If either the egFile changed, or the classFile, the cacheXML is stale
     * @param egFile the EventGraph file to compare digests with
     * @return an indication EG state change
     */
    public boolean isStale(File egFile) {
        File classFile = getCachedClass(egFile);
        List<String> cacheDigest = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_DIGEST_KEY));
        List<String> cacheXML = Arrays.asList(ViskitConfig.instance().getConfigValues(ViskitConfig.CACHED_EVENTGRAPHS_KEY));
        String filePath;
        try {
            filePath = egFile.getCanonicalPath().replaceAll("\\\\", "/");
            int index = cacheXML.lastIndexOf(filePath);
            if (index >= 0) {
                String cachedDigest = cacheDigest.get(index);
                String compareDigest = createMessageDigest(egFile, classFile);
                return !cachedDigest.equals(compareDigest);
            }
        } catch (IOException ex) {
            LOG.error(ex);
//            ex.printStackTrace();
        }
        // if egFile not in cacheXML, it can't be stale
        return false;
    }

}
