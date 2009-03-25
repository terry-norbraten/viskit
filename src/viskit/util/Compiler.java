package viskit.util;

import edu.nps.util.LogUtils;
import viskit.*;
import java.io.File;
import java.util.Arrays;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/** Using the java compiler now part of javax, we no longer have to 
 * either ship tools.jar or require a jdk environment variable.
 * This class was taken from viskit.SourceWindow.
 * TBD refactor out from SourceWindow, the main difference being Compiler actually
 * creates .class Files, where SourceWindow did in memory.
 *
 * @author Rick Goldberg
 * @version $Id$
 */
public class Compiler {
    
    /** Call the java compiler to test compile our event graph java source
     * 
     * @param pkg 
     * @param className
     * @param src
     * @return Diagnostic messages from compiler
     */
    public static String invoke(String pkg, String className, String src) {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StringBuffer diagnosticMessages = new StringBuffer();
        StringBuilder sb = new StringBuilder();
        CompilerDiagnosticsListener diag = new CompilerDiagnosticsListener(diagnosticMessages);
        JavaObjectFromString jofs = null;
        StringBuilder classPaths = null;
        String cp = null;

        try {
            jofs = new JavaObjectFromString(pkg + "." + className, src);
            File workDir = VGlobals.instance().getWorkDirectory();
            StandardJavaFileManager sjfm = compiler.getStandardFileManager(diag, null, null);
            Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(jofs);
            String workDirPath = workDir.getCanonicalPath();
            
            // This is would be the first instance of obtaining a LBL if
            // beginning fresh, so, it is reset on the first instantiation
            String[] workClassPath = ((viskit.doe.LocalBootLoader) (VGlobals.instance().getWorkClassLoader())).getClassPath();
            int wkpLength = workClassPath.length;
            classPaths = new StringBuilder(wkpLength);

            for (String cPath : workClassPath) {
                classPaths.append(cPath + File.pathSeparator);
            }

            // Get rid of the last ";" on the cp
            classPaths = classPaths.deleteCharAt(classPaths.lastIndexOf(File.pathSeparator));
            cp = classPaths.toString();
            LogUtils.getLogger().debug("cp is: " + cp);
            String[] options = {"-Xlint:unchecked",
                    "-Xlint:deprecation",
                    "-cp", 
                    cp,
                    "-d", 
                    workDirPath};
            java.util.List<String> optionsList = Arrays.asList(options);
            compiler.getTask(null, sjfm, diag, optionsList, null, fileObjects).call();
            sb.append(diag.messageString);
        } catch (Exception ex) {
            LogUtils.getLogger().error("JavaObjectFromString " + pkg + "." + className + "  " + jofs.toString());
            LogUtils.getLogger().info("Classpath is: " + cp);
            ex.printStackTrace();
        }
        return sb.toString();
    }
} // end class file Compiler.java