package viskit;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

/**
 * OPNAV N81-NPS World-Class-Modeling (WCM) 2004 Projects
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * User: mike
 * Date: Feb 23, 2004
 * Time: 8:26:50 AM
 */

/**
 * This class will put up a single image as a splash screen in the center of the default screen.
 * Waits for 2 seconds, then loads simkit.viskit.Main, handing off the arguments which the
 * entry point in the class was given.
 */

public class Splash extends JFrame
{
  JLabel label;

  public Splash()
  {
    super();
    this.setUndecorated(true);
    this.setBackground(new Color(255,255,255,0)); //128));
    ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("viskit/images/ViskitSplash2.png"));
    label = new JLabel(icon);
    label.setOpaque(false);
    this.getContentPane().add(label, BorderLayout.CENTER);

    this.pack();
  }

  /**
   * Do this to put all GUI access in Event thread.  Play the array game to deal with final var restrictions.
   * @return
   */
  private static Splash makeSplash()
  {
    final Splash[] spl=new Splash[1];
    try {
      SwingUtilities.invokeAndWait(new Runnable()
      {
        public void run()
        {
          spl[0]=new Splash();
          Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
          spl[0].setLocation((d.width-spl[0].getWidth())/2,(d.height-spl[0].getHeight())/2);
          spl[0].setVisible(true);
        }
      });
    }
    catch (Exception ex){}
    return spl[0];
  }

  public static void main(String[] args)
  {
    Splash spl = makeSplash();

    try{Thread.sleep(2000);}catch(Exception e){}  // this is used to give us some min splash viewing

    try {
      // Call the main() method of the application using reflection
      Object[] arguments = new Object[]{args};
      Class[] parameterTypes = new Class[]{args.getClass()};

      Class mainClass = Class.forName("viskit.Main");

      Method mainMethod = mainClass.getMethod("main",parameterTypes);
      mainMethod.invoke(null, arguments);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    spl.dispose();
  }
}
