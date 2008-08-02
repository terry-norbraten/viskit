package viskit.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.log4j.Logger;

/**
 * @version $Id: BrowserLauncher.java 1860 2008-06-17 23:48:42Z ahbuss $
 * @author abuss
 */
public class BrowserLauncher implements HyperlinkListener {

    public static final Logger logger = Logger.getLogger(BrowserLauncher.class);
    
    public static final String WINDOWS = "Windows";
    public static final String MAC = "Mac OS X";
    
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == ACTIVATED) {
            URL url = e.getURL();
            try {
                String osName = System.getProperty("os.name");
                if (osName.startsWith(WINDOWS)) {
                    Runtime.getRuntime().exec(
                            "rundll32 url.dll,FileProtocolHandler " + 
                            url);
                } else if (osName.equals(MAC)) {
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(
                            "com.apple.eio.FileManager");
                    Method method = clazz.getMethod("openURL", String.class);
                    method.invoke(null, url.toString());
                }
            } 
            catch (IOException ex ){
                logger.info(ex);
                throw new RuntimeException(ex);
            }
            catch (ClassNotFoundException ex) {
                logger.error(ex);
                throw new RuntimeException(ex);
            }
            catch (NoSuchMethodException ex) {
                logger.error(ex);
                throw new RuntimeException(ex);
            }
            catch (IllegalAccessException ex) {
                logger.error(ex);
                throw new RuntimeException(ex);
            }
            catch (InvocationTargetException ex) {
                logger.error(ex.getTargetException());
                throw new RuntimeException(ex.getTargetException());
            }

        }
    }

}