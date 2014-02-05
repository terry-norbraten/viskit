package viskit.xsd.cli;

import edu.nps.util.LogUtils;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import org.apache.log4j.Logger;
import viskit.AssemblyController;

public class Launcher extends Thread implements Runnable {

    static ClassLoader cloader;
    static Logger log = LogUtils.getLogger(Launcher.class);
    String assembly = null;
    String assemblyName;
    Hashtable<String, String> eventGraphs = new Hashtable<String, String>();
    private static final boolean debug = true;
    private boolean compiled = true;
    private boolean inGridlet = false;

    /**
     *  args -A ssemblyFile [-E ventGraphFile]
     */
    public Launcher() {
    }

    public void setup() {

        try {
            URL u;

            cloader = Thread.currentThread().getContextClassLoader();
            InputStream configIn = cloader.getResourceAsStream("config.properties");
            Properties p = new Properties();
            p.load(configIn);

            try {
                // first load any of the event-graphs in the BehaviorLibraries
                // while this could be handled by EventGraphs property, getting
                // a specific directory is more automated
                String eventGraphDir = p.getProperty("EventGraphDir");
                eventGraphDir = (eventGraphDir == null) ? "?" : eventGraphDir;
                if (Thread.currentThread().getContextClassLoader() instanceof Boot) {
                    Boot b = (Boot) Thread.currentThread().getContextClassLoader();
                    u = b.baseJarURL;
                    URLConnection urlc = u.openConnection();
                    JarInputStream jis = new JarInputStream(urlc.getInputStream());
                    JarEntry je;
                    while ((je = jis.getNextJarEntry()) != null) {
                        String name = je.getName();
                        System.out.println("Loading EventGraph from: " + name);
                        // nb: BehaviorLibraries is sort of hard coded here,
                        // better to have a property tbd
                        if (name.indexOf(eventGraphDir) >= 0 && name.endsWith("xml")) {
                            addEventGraph(jis);
                        }
                    }
                }

                compiled = (p.getProperty("Beanshell") == null);
                // check that were not starting in SGE mode to boot up a gridlet
                // in this case we don't want to have to recompile any of the BehaviorLibraries
                // all over again each time.
                // if were not in a Gridlet booter, then either we're booting the AssemblyServer
                // or running local mode, and the execCompiled actually happens, as it gets
                // side stepped during setup to launchGridkit otherwise.

                /* This may break on Microsoft Windows
                 * due to illegal chars in the system env
                 * at this stage could be an instance of local run on windows
                 * so, do something else.
                Process pr = Runtime.getRuntime().exec("env");
                InputStream is = pr.getInputStream();
                Properties p = System.getProperties();
                p.load(is);
                 */
                Process pr = Runtime.getRuntime().exec("env");
                InputStream is = pr.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String SGE = null;
                String line;
                while ((line = br.readLine()) != null) {
                    if (debug) {
                        System.out.println(line);
                    }
                    if (line.indexOf("SGE_TASK_ID") > -1) {
                        SGE = line;
                        break;
                    }
                }
                inGridlet = (SGE != null);


                if (compiled && !inGridlet) {
                    // only case where BehaviorLibraries should get compiled
                    compile();
                //eventGraphs.clear();
                } else {
                    eventGraphs.clear();
                }


                if (p.getProperty("Port") != null) {
                    launchGridkit(p.getProperty("Port"));
                } else if (p.getProperty("Assembly") != null) {
                    if (debug) {
                        System.out.println("Running Assembly " + p.getProperty("Assembly"));
                    }
                    u = cloader.getResource(p.getProperty("Assembly"));
                    if (debug) {
                        System.out.println("From URL: " + u);
                    }
                    setAssembly(u);
                    // EventGraphs can also be dropped onto the top-level dir
                    // as long as they are enumerated
                    StringTokenizer st = new StringTokenizer(p.getProperty("EventGraphs"));
                    while (st.hasMoreTokens()) {
                        u = cloader.getResource(st.nextToken());
                        addEventGraph(u);
                    }
                } else if (p.getProperty("Viskit") != null) {
                    launchGUI();
                }
            } catch (Exception e) {
                log.error(e);
            }

        } catch (IOException e) {
            log.error(e);
        }
    }

    public Launcher(String[] args) {
        int a = 0;
        // I'm in command line mode TBD
        try {
            URL u;
            while (a < args.length - 1) {
                if (args[a].equals("-A")) {
                    u = new URL(args[a + 1]);
                    setAssembly(u);
                    a += 2;
                } else if (args[a].equals("-E")) {
                    u = new URL(args[a + 1]);
                    addEventGraph(u);
                    a += 2;
                } else {
                    throw new IllegalArgumentException(args[a] + " not a valid arg, exiting");
                }

            }

            if (assemblyName == null) {
                throw new IllegalArgumentException("Must supply fully qualified class name to run");
            }
            if (assembly == null) {
                throw new IllegalArgumentException("Must supply at least one assembly URL");
            }

        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * read in XML to String
     * @param assemblyURL
     * @throws java.lang.Exception
     */
    public final void setAssembly(URL assemblyURL) throws Exception {
        URLConnection urlc = assemblyURL.openConnection();
        InputStream is = urlc.getInputStream();
        int c;
        StringBuilder xml = new StringBuilder();
        while ((c = is.read()) > -1) {
            xml.append((char) c);
        }
        assembly = xml.toString();

        log.debug("Assembly XML" + xml.toString());

        // get the class name as defined in the XML
        log.debug("Trying ClassLoader " + cloader);

        ByteArrayInputStream bais = new ByteArrayInputStream(xml.toString().getBytes());

        if (cloader instanceof java.net.URLClassLoader) {
            URL[] usz = ((java.net.URLClassLoader) cloader).getURLs();
            for (URL usz1 : usz) {
                log.debug("URL " + usz1.toString());
            }
        }

        // create a jaxb context to obtain the name field from the assembly
        Class<?> jclz = cloader.loadClass("javax.xml.bind.JAXBContext");
        Method m = jclz.getDeclaredMethod("newInstance", new Class<?>[]{String.class, ClassLoader.class});
        Object jco = m.invoke(null, new Object[]{"viskit.xsd.bindings.assembly", cloader});
        m = jclz.getDeclaredMethod("createUnmarshaller", new Class<?>[]{});
        Object umo = m.invoke(jco, new Object[]{});

        // unmarshal the xml to java bindings
        jclz = cloader.loadClass("javax.xml.bind.Unmarshaller");
        m = jclz.getDeclaredMethod("unmarshal", new Class<?>[]{InputStream.class});
        Object assemblyJaxb = m.invoke(umo, new Object[]{bais});

        // get the root node of the assembly and obtain name of assembly
        jclz = cloader.loadClass("viskit.xsd.bindings.assembly.SimkitAssemblyType");
        m = jclz.getDeclaredMethod("getPackage", new Class<?>[]{});
        assemblyName = (String) m.invoke(assemblyJaxb, new Object[]{});
        m = jclz.getDeclaredMethod("getName", new Class<?>[]{});
        assemblyName += "." + (String) m.invoke(assemblyJaxb, new Object[]{});

    }

    /**
     * read in XML to String
     * @param is
     * @throws java.lang.Exception
     */
    public void addEventGraph(InputStream is) throws Exception {
        int c;
        StringBuilder xml = new StringBuilder();
        // for some reason, maybe OS dependent, reading from
        // a jar stream marker sometimes doesn't work for reads
        // greater than one char at a time, particularly signed
        // jars?
        while ((c = is.read()) > -1) {
            xml.append((char) c);
        }

        try {
            // get the class name as defined in the XML
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.toString().getBytes());
            Class<?> jclz = cloader.loadClass("javax.xml.bind.JAXBContext");
            Method m = jclz.getDeclaredMethod("newInstance", new Class<?>[]{String.class, ClassLoader.class});
            Object jco = m.invoke(null, new Object[]{"viskit.xsd.bindings.eventgraph", cloader});
            m = jclz.getDeclaredMethod("createUnmarshaller", new Class<?>[]{});
            Object umo = m.invoke(jco, new Object[]{});

            jclz = cloader.loadClass("javax.xml.bind.Unmarshaller");
            m = jclz.getDeclaredMethod("unmarshal", new Class<?>[]{InputStream.class});
            Object eventgraph = m.invoke(umo, new Object[]{bais});

            jclz = cloader.loadClass("viskit.xsd.bindings.eventgraph.SimEntityType");
            m = jclz.getDeclaredMethod("getPackage", new Class<?>[]{});
            String eventGraphName = (String) m.invoke(eventgraph, new Object[]{});
            m = jclz.getDeclaredMethod("getName", new Class<?>[]{});
            eventGraphName += "." + m.invoke(eventgraph, new Object[]{});

            eventGraphs.put(eventGraphName, xml.toString());
            if (debug) {
                System.out.println(eventGraphName + "EventGraph XML");
                System.out.println(xml.toString());
            }

        // silent this may not be an event-graph xml
        } catch (ClassNotFoundException e) {
            log.error(e);
        } catch (NoSuchMethodException e) {
            log.error(e);
        } catch (SecurityException e) {
            log.error(e);
        } catch (IllegalAccessException e) {
            log.error(e);
        } catch (IllegalArgumentException e) {
            log.error(e);
        } catch (InvocationTargetException e) {
            log.error(e);
        }

    }

    public final void addEventGraph(URL eventGraphURL) throws Exception {
        URLConnection urlc = eventGraphURL.openConnection();
        addEventGraph(urlc.getInputStream());
    }

    @Override
    public void run() {

        try {
            setup();
            if (assemblyName != null) {
                exec();
            }

        } catch (Exception e) {
            log.error(e);
        }
    }

    public void exec() {
        try {
            if (this.compiled) {
                execCompiled();
            } else {
                execInterpreted();
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void execCompiled() {
        try {
            compile();
            if (assemblyName != null) {
                // run it
                Class<?> asmz = Thread.currentThread().getContextClassLoader().loadClass(assemblyName);
                Constructor<?> c = asmz.getConstructor(new Class<?>[]{});
                Object out = c.newInstance(new Object[]{});
                Thread t = new Thread((Runnable) out);
                t.start();
                t.join();
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    // make java source from xml and add to classpath
    public void compile() throws Exception {
        Class<?> xml2jz;
        Class<?> axml2jz;
        Class<?> tempDirz;
        Class<?> assyContlr;

        Object out;
        Method m;
        Constructor<?> c;
        ByteArrayInputStream bais;
        String assemblyJava;
        File tempDir = null;

        xml2jz = cloader.loadClass("viskit.xsd.translator.SimkitXML2Java");
        axml2jz = cloader.loadClass("viskit.xsd.assembly.SimkitAssemblyXML2Java");
        tempDirz = cloader.loadClass("edu.nps.util.TempFileManager");
        assyContlr = cloader.loadClass("viskit.AssemblyController");

        try {
            m = tempDirz.getDeclaredMethod("createTempFile", new Class<?>[]{String.class, String.class});
            tempDir = (File) (m.invoke(null, new Object[]{"gridkit", "doe"}));

            // jam the classpath with the new directory
            String systemClassPath = System.getProperty("java.class.path");
            System.setProperty("java.class.path", systemClassPath + File.pathSeparator + tempDir.getCanonicalPath());
            // hopefully don't have to addURL, but here just in case


            Enumeration<String> e = eventGraphs.keys();
            while (e.hasMoreElements()) {
                String eventGraphName = e.nextElement();
                String eventGraph = eventGraphs.get(eventGraphName);
                String eventGraphJava;

                try {
                    // unmarshal eventGraph xml
                    bais = new ByteArrayInputStream(eventGraph.getBytes());
                    c = xml2jz.getConstructor(new Class<?>[]{InputStream.class});
                    out = c.newInstance(new Object[]{bais});
                    m = out.getClass().getDeclaredMethod("unmarshal", new Class<?>[]{});
                    m.invoke(out, new Object[]{});

                    // translate xml to generate java source
                    m = out.getClass().getDeclaredMethod("translate", new Class<?>[]{});
                    out = m.invoke(out, new Object[]{});
                    eventGraphJava = (String) out;

                    log.debug(eventGraphJava);

                    m = assyContlr.getDeclaredMethod("compileJavaClassFromString", new Class<?>[]{String.class});

                    log.info("Generating Java Bytecode...");
                    if (m.invoke(null, new Object[]{eventGraphJava}) != null) {
                        log.info("Compilation of " + eventGraphName + " complete.");
                    }

                    //normal to have exception as eventGraphs
                    //may have been blindly loaded with all .xml
                    //under BehaviorLibraries which could also
                    //include assemblies. must be caught though
                    //and ingored
                } catch (NoSuchMethodException ex) {
                    log.error(ex);
                } catch (SecurityException ex) {
                    log.error(ex);
                } catch (InstantiationException ex) {
                    log.error(ex);
                } catch (IllegalAccessException ex) {
                    log.error(ex);
                } catch (IllegalArgumentException ex) {
                    log.error(ex);
                } catch (InvocationTargetException ex) {
                    log.error(ex);
                }
            }

            if (assembly != null) {
                // unmarshal Assembly xml
                bais = new ByteArrayInputStream(assembly.getBytes());
                c = axml2jz.getConstructor(new Class<?>[]{InputStream.class});
                out = c.newInstance(new Object[]{bais});
                m = out.getClass().getDeclaredMethod("unmarshal", new Class<?>[]{});
                m.invoke(out, new Object[]{});

                // translate xml to generate java source
                m = out.getClass().getDeclaredMethod("translate", new Class<?>[]{});
                out = m.invoke(out, new Object[]{});
                assemblyJava = (String) out;

                log.debug(assemblyJava);

                log.info("Generating Java Bytecode...");
                if (m.invoke(null, new Object[]{assemblyJava}) != null) {
                    log.info("Compilation of assembly complete.");
                }
            }

            // finally jar up the classes and add them to the Boot ClassLoader

            File jarFromDir = makeJarFileFromDir(tempDir);
            URL url = new URL("file:" + File.separator + File.separator + jarFromDir.getCanonicalPath() + File.separator);
            // if these path-jammers don't work, then will need to define classes directly in the classloader
            // via bytes. url here is the file:\/\/+ canonical path from above to tempDir
            ((Boot) (Thread.currentThread().getContextClassLoader())).addURL(url);
            // jam the classpath with the new directory
            systemClassPath = System.getProperty("java.class.path");
            System.setProperty("java.class.path", systemClassPath + File.pathSeparator + jarFromDir.getCanonicalPath());

        } catch (NoSuchMethodException e) {
            log.error(e);
        } catch (SecurityException e) {
            log.error(e);
        } catch (IllegalAccessException e) {
            log.error(e);
        } catch (IllegalArgumentException e) {
            log.error(e);
        } catch (InvocationTargetException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } catch (InstantiationException e) {
            log.error(e);
        }
    }

    public void execInterpreted() throws Exception {
        Class<?> xml2jz;
        Class<?> axml2jz;
        Class<?> bshz;
        Class<?> bshcmz;
        Object bsh;
        Object bshcm;
        Object out;
        Method m;
        Constructor<?> c;
        ByteArrayInputStream bais;
        String assemblyJava;

        xml2jz = cloader.loadClass("viskit.xsd.translator.SimkitXML2Java");
        axml2jz = cloader.loadClass("viskit.xsd.assembly.SimkitAssemblyXML2Java");
        bshz = cloader.loadClass("bsh.Interpreter");
        bshcmz = cloader.loadClass("bsh.BshClassManager");

        c = bshz.getConstructor(new Class<?>[]{});
        bsh = c.newInstance(new Object[]{});

        m = bshz.getDeclaredMethod("getClassManager", new Class<?>[]{});
        bshcm = m.invoke(bsh, new Object[]{});
        m = bshcmz.getDeclaredMethod("classExists", new Class<?>[]{String.class});
        out = m.invoke(bshcm, new Object[]{"viskit.xsd.assembly.BasicAssembly"});
        if (debug) {
            System.out.println("Checking if viskit.xsd.assembly.BasicAssembly exists... " + ((Boolean) out).toString());
        }

        try {
            Enumeration<String> e = eventGraphs.keys();
            while (e.hasMoreElements()) {
                String eventGraphName = e.nextElement();
                String eventGraph = eventGraphs.get(eventGraphName);
                String eventGraphJava;

                // unmarshal eventGraph xml
                bais = new ByteArrayInputStream(eventGraph.getBytes());
                c = xml2jz.getConstructor(new Class<?>[]{InputStream.class});
                out = c.newInstance(new Object[]{bais});
                m = out.getClass().getDeclaredMethod("unmarshal", new Class<?>[]{});
                m.invoke(out, new Object[]{});

                // translate xml to generate java source
                m = out.getClass().getDeclaredMethod("translate", new Class<?>[]{});
                out = m.invoke(out, new Object[]{});
                eventGraphJava = (String) out;
                if (debug) {
                    System.out.println(eventGraphJava);
                }

                // bsh eval generated java source
                m = bshz.getDeclaredMethod("eval", new Class<?>[]{String.class});
                m.invoke(bsh, new Object[]{eventGraphJava});

                // sanity check bsh if eventGraph class exists
                m = bshcmz.getDeclaredMethod("classExists", new Class<?>[]{String.class});
                out = m.invoke(bshcm, new Object[]{eventGraphName});
                System.out.println("Checking if " + eventGraphName + " exists... " + ((Boolean) out).toString());
            }

            // unmarshal assembly xml
            bais = new ByteArrayInputStream(assembly.getBytes());
            c = axml2jz.getConstructor(new Class<?>[]{InputStream.class});
            out = c.newInstance(new Object[]{bais});
            m = axml2jz.getDeclaredMethod("unmarshal", new Class<?>[]{});
            m.invoke(out, new Object[]{});

            // translate xml to java source
            m = axml2jz.getDeclaredMethod("translate", new Class<?>[]{});
            out = m.invoke(out, new Object[]{});
            assemblyJava = (String) out;
            if (debug) {
                System.out.println(assemblyJava);
            }

            // bsh eval the generated source
            m = bshz.getDeclaredMethod("eval", new Class<?>[]{String.class});
            if (debug) {
                m.invoke(bsh, new Object[]{"debug();"});
            }
            m.invoke(bsh, new Object[]{assemblyJava});

            // sanity check the bsh class loaders if assembly exists
            m = bshcmz.getDeclaredMethod("classExists", new Class<?>[]{String.class});
            out = m.invoke(bshcm, new Object[]{assemblyName});
            if (debug) {
                System.out.println("Checking if " + assemblyName + " exists... " + ((Boolean) out).toString());
            }
            out = m.invoke(bshcm, new Object[]{"simkit.random.RandomVariateFactory"});
            if (debug) {
                System.out.println("Checking if simkit.random.RandomVariateFactory exists... " + ((Boolean) out).toString());
            }

            // get the assembly class, create instance and thread it
            m = bshcmz.getDeclaredMethod("classForName", new Class<?>[]{String.class});
            Class<?> asmz = (Class) m.invoke(bshcm, new Object[]{assemblyName});
            c = asmz.getConstructor(new Class<?>[]{});
            out = c.newInstance(new Object[]{});
            Thread t = new Thread((Runnable) out);
            t.start();
            t.join();
        // another one of many ways to do run the resulting assembly
        //m = bshz.getDeclaredMethod("eval", new Class<?>[]{ String.class });
        //m.invoke(bsh,new Object[]{ "Thread t = new Thread(new "+assemblyName+"());"});
        //m = bshz.getDeclaredMethod("get", new Class<?>[]{ String.class });
        //out = m.invoke(bsh, new Object[]{ "t" });
        //((Thread)out).start();

        } catch (NoSuchMethodException e) {
            log.error(e);
        } catch (SecurityException e) {
            log.error(e);
        } catch (InstantiationException e) {
             log.error(e);
        } catch (IllegalAccessException e) {
             log.error(e);
        } catch (IllegalArgumentException e) {
             log.error(e);
        } catch (InvocationTargetException e) {
             log.error(e);
        } catch (InterruptedException e) {
             log.error(e);
        }

    /*
     * Above is mostly introspection of below, which only works in command line mode
     * (ie with classpath set normally)
     *
     * to run within a jar of jars it has to be done via introspection.
     *
    if (bsh.getClassManager().classExists("simkit.BasicAssembly")) System.out.println("simkit.BasicAssembly found");
    else System.out.println("simkit.BasicAssembly not found");
    try {
    bsh.eval("debug();");
    // load eventGraph XML as classes
    // since we've overridden the default bsh ClassLoader, need to
    // add the defined class in this ClassLoader
    Enumeration e = eventGraphs.keys();
    while( e.hasMoreElements() ){
    String eventGraphName = (String) (e.nextElement());
    String eventGraph = (String)eventGraphs.get(eventGraphName);
    bais = new ByteArrayInputStream(eventGraph.getBytes());
    Class clz = Thread.currentThread().getContextClassLoader().loadClass("viskit.xsd.translator.SimkitXML2Java");
    Constructor cnstr = clz.getConstructor( new Class<?>[] { InputStream.class } );
    //xml2j = new SimkitXML2Java(bais);
    xml2j = (SimkitXML2Java)cnstr.newInstance( new Object[] { bais } );
    xml2j.unmarshal();
    bsh.eval(xml2j.translate());
    if (debug) {
    System.out.println("Bsh eval of:");
    System.out.println(xml2j.translate());
    }
    Class evgc = bsh.getClassManager().classForName(eventGraphName);
    // add these, since this is going to be the class loader,
    // and could conflict with the one beanshell last used to
    // eval the string (?)
    eventGraphs.put(eventGraphName, evgc);
    if (debug) System.out.println("Eventgraph class added: "+eventGraphName+ " "+evgc);
    }
    // now any and all dependencies are loaded for the assembly
    bais = new ByteArrayInputStream(assembly.getBytes());
    Class clz = Thread.currentThread().getContextClassLoader().loadClass("viskit.xsd.assembly.SimkitAssemblyXML2Java");
    Constructor cnstr = clz.getConstructor( new Class<?>[] { InputStream.class } );
    axml2j = (SimkitAssemblyXML2Java)cnstr.newInstance( new Object[] { bais } );
    axml2j.unmarshal();
    bsh.eval(axml2j.translate());
    if (debug) {
    System.out.println("Bsh eval of:");
    System.out.println(axml2j.translate());
    }
    // run the assembly
    BshClassManager bcm = bsh.getClassManager();
    bcm.dump(new java.io.PrintWriter(System.out));
    // according to bsh docs, can't reload classes
    // if not using the addClassPath method, since
    // Launcher overrides the default bsh loader,
    // don't do the following:
    // bcm.reloadAllClasses();
    // instead, relaunch.
    if (bcm.classExists(assemblyName)) {
    System.out.println("Running Assembly "+assemblyName);
    //bsh.eval(assemblyName+".main(new String[0])");
    bsh.eval("Thread t = new Thread( new "+assemblyName+"() );");
    Thread t = (Thread) bsh.get("t");
    t.start();
    } else {
    System.out.println("Can't find Assembly "+assemblyName);
    }
    } catch (Exception e) {
    e.printStackTrace();
    }
     */
    }

    void launchGUI() {
        try {
            Class<?> viskitz = cloader.loadClass("viskit.Splash2");
            Method m = viskitz.getDeclaredMethod("main", new Class<?>[]{String[].class});
            m.invoke(null, new Object[]{new String[]{"viskit.EventGraphAssemblyComboMain"}});
        } catch (ClassNotFoundException e) {
            log.error(e);
            System.exit(1);
        } catch (NoSuchMethodException e) {
            log.error(e);
            System.exit(1);
        } catch (SecurityException e) {
            log.error(e);
            System.exit(1);
        } catch (IllegalAccessException e) {
            log.error(e);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            log.error(e);
            System.exit(1);
        } catch (InvocationTargetException e) {
            log.error(e);
            System.exit(1);
        }
    }

    void launchGridkit(String port) {
        try {

            if (inGridlet) {
                launchGridlet();
            } else {
                launchAssemblyServer(Integer.parseInt(port));
            }
        } catch (NumberFormatException e) {
            log.error(e);
        }
    }

    // this would be invoked as a result of an SGE qsub
    // Gridlet actually does use the Boot class loader for
    // appending 3rd pty jars.
    // Gridlets also do their own to get PORT and other env
    void launchGridlet() {
        try {
            Class<?> gridletz = cloader.loadClass("viskit.xsd.assembly.Gridlet");
            Method m = gridletz.getDeclaredMethod("main", new Class<?>[]{String[].class});
            m.invoke(null, new Object[]{new String[]{}});
        } catch (ClassNotFoundException e) {
            log.error(e);
            System.exit(1);
        } catch (NoSuchMethodException e) {
            log.error(e);
            System.exit(1);
        } catch (SecurityException e) {
            log.error(e);
            System.exit(1);
        } catch (IllegalAccessException e) {
            log.error(e);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            log.error(e);
            System.exit(1);
        } catch (InvocationTargetException e) {
            log.error(e);
            System.exit(1);
        }
    }

    void launchAssemblyServer(int port) {
        try {
            Class<?> serverz = cloader.loadClass("viskit.xsd.assembly.AssemblyServer");
            Method m = serverz.getDeclaredMethod("main", new Class<?>[]{String[].class});
            m.invoke(null, new Object[]{new String[]{"-p", "" + port}});
        } catch (ClassNotFoundException e) {
            log.error(e);
            System.exit(1);
        } catch (NoSuchMethodException e) {
            log.error(e);
            System.exit(1);
        } catch (SecurityException e) {
            log.error(e);
            System.exit(1);
        } catch (IllegalAccessException e) {
            log.error(e);
            System.exit(1);
        } catch (IllegalArgumentException e) {
            log.error(e);
            System.exit(1);
        } catch (InvocationTargetException e) {
            log.error(e);
            System.exit(1);
        }
    }

    private File makeJarFileFromDir(File dir2jar) {
        File jarOut = dir2jar;
        try {
            jarOut = File.createTempFile("bhvr", ".jar", new File(System.getProperty("user.dir")));
            FileOutputStream fos = new FileOutputStream(jarOut);
            JarOutputStream jos = new JarOutputStream(fos);
            if (dir2jar.isDirectory()) {
                makeJarFileFromDir(dir2jar, dir2jar, jos);
            }
            jos.flush();
            jos.close();

        } catch (IOException ex) {
            log.error(ex);
        }
        return jarOut;
    }

    private void makeJarFileFromDir(File baseDir, File newDir, JarOutputStream jos) {
        File[] dirList = newDir.listFiles();
        FileInputStream fis;
        JarEntry je;
        for (File dirList1 : dirList) {
            if (dirList1.isDirectory()) {
                makeJarFileFromDir(baseDir, dirList1, jos);
            } else {
                try {
                    je = new JarEntry(dirList1.getCanonicalPath().substring(baseDir.getCanonicalPath().length() + 1));
                    jos.putNextEntry(je);
                    fis = new FileInputStream(dirList1);
                    byte[] buf = new byte[256];
                    int c;
                    while ((c = fis.read(buf)) > 0) {
                        jos.write(buf, 0, c);
                    }
                    jos.closeEntry();
                } catch (IOException ex) {
                    log.error(ex);
                }
            }
        }
    }
}