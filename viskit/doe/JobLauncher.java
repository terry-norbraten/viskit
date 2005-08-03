/*
Copyright (c) 1995-2005 held by the author(s).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer
      in the documentation and/or other materials provided with the
      distribution.
    * Neither the names of the Naval Postgraduate School (NPS)
      Modeling Virtual Environments and Simulation (MOVES) Institute
      (http://www.nps.edu and http://www.MovesInstitute.org)
      nor the names of its contributors may be used to endorse or
      promote products derived from this software without specific
      prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

/**
 * MOVES Institute
 * Naval Postgraduate School, Monterey, CA
 * www.nps.edu
 * @author Mike Bailey
 * @since Jul 21, 2005
 * @since 12:29:08 PM
 */

package viskit.doe;

import org.apache.xmlrpc.XmlRpcClientLite;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;
import viskit.SpringUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Vector;

public class JobLauncher extends JFrame implements Runnable
{
  String file;
  File f;
  FileReader fr;
  PrintWriter out;
  BufferedReader br;
  XmlRpcClientLite rpc;
  private JTextArea ta;
  JFrame mom;

  String clusterDNS = "cluster.moves.nps.navy.mil";
  private JButton canButt;
  private JButton runButt;
  private JButton closeButt;
  private int designPts,numRuns;
  private Document doc;
  private JTextField samps;
  private JTextField runs;
  private JTextField tmo;

  private Thread thread;
  private boolean outputDirty = false;

  public JobLauncher(String file, JFrame mainFrame)
  {
    super("Job " + file);
    this.file = file;
    f = new File(file);

    this.mom = mainFrame;
    this.designPts = designPts;
    this.numRuns = numRuns;

    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

    JPanel topPan = new JPanel(new SpringLayout());
    ta = new JTextArea(20, 30);
    JScrollPane jsp = new JScrollPane(ta);
    JPanel botBar = new JPanel();
    botBar.setLayout(new BoxLayout(botBar, BoxLayout.X_AXIS));

    JTextField cluster = new JTextField(clusterDNS);
    JLabel clusLab = new JLabel("Target grid engine");
    samps = new JTextField(6);
    JLabel sampLab = new JLabel("Number of samples");
    runs = new JTextField(6);
    JLabel runLab = new JLabel("Number of runs");
    tmo = new JTextField(6);
    JLabel tmoLab = new JLabel("Time out (ms)");

    cluster.setEditable(false);
    cluster.setBackground(samps.getBackground());

    try {
      getParams();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    topPan.add(clusLab);
    topPan.add(cluster);
    topPan.add(sampLab);
    topPan.add(samps);
    topPan.add(runLab);
    topPan.add(runs);
    topPan.add(tmoLab);
    topPan.add(tmo);

    SpringUtilities.makeCompactGrid(topPan, 4, 2, 10, 10, 5, 5);
    topPan.setMaximumSize(topPan.getPreferredSize());

    canButt = new JButton("Cancel job");
    canButt.setEnabled(false);
    runButt = new JButton("Run job");
    closeButt = new JButton("Close");
    botBar.add(Box.createHorizontalGlue());
    botBar.add(canButt);
    botBar.add(runButt);
    botBar.add(Box.createHorizontalStrut(20));
    botBar.add(closeButt);

    p.add(topPan);
    p.add(jsp);
    p.add(Box.createVerticalStrut(8));
    p.add(botBar);
    p.setBorder(new EmptyBorder(10, 10, 10, 10));
    setContentPane(p);

    pack();
    Dimension d = this.getSize();
    d.width+=50;
    this.setSize(d);
    centerMe();

    setVisible(true);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        canButt.doClick();
      }
    });
    doListeners();
  }

  private void getParams() throws Exception
  {
    doc = FileHandler.unmarshallJdom(f);
    Element root = doc.getRootElement();

    Element exp = root.getChild("Experiment");

    String att = exp.getAttributeValue("totalSamples");
    samps.setText(att);
    designPts = Integer.parseInt(att);
    att = exp.getAttributeValue("runsPerDesignPoint");
    runs.setText(att);
    numRuns = Integer.parseInt(att);
    att = exp.getAttributeValue("timeout");
    tmo.setText(att);
  }
  private void setParams() throws Exception
  {
    Element root = doc.getRootElement();
    Element exp = root.getChild("Experiment");
    Attribute att;
    att = exp.getAttribute("totalSamples");
    att.setValue(samps.getText().trim());
    att = exp.getAttribute("runsPerDesignPoint");
    att.setValue(runs.getText().trim());
    att = exp.getAttribute("timeout");
    att.setValue(tmo.getText().trim());

    FileHandler.marshallJdom(f,doc);
  }

  private void doListeners()
  {
    canButt.setActionCommand("cancel");
    runButt.setActionCommand("run");
    closeButt.setActionCommand("x");
    ActionListener al = new ButtListener();
    canButt.addActionListener(al);
    runButt.addActionListener(al);
    closeButt.addActionListener(al);
  }

  class ButtListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      switch (e.getActionCommand().charAt(0)) {
        case 'r':
          runButt.setEnabled(false);
          canButt.setEnabled(true);
          closeButt.setEnabled(false);
          thread = new Thread(JobLauncher.this);
          thread.start();
          break;
        case 'c':
          stopRun();
          break;
        case 'x':
          runButt.setEnabled(true);  // for next time (probably not used)
          canButt.setEnabled(false);
          if(outputDirty ) {
            if (JOptionPane.showConfirmDialog(JobLauncher.this, "Save output?") == JOptionPane.YES_OPTION) {
              JFileChooser jfc = new JFileChooser();
              jfc.setSelectedFile(new File("DOEOutput.txt"));
              jfc.showSaveDialog(JobLauncher.this);
              if (jfc.getSelectedFile() != null) {
                File f = jfc.getSelectedFile();
                try {
                  FileWriter fw = new FileWriter(f);
                   fw.write(ta.getText());
                  fw.close();
                }
                catch (IOException e1) {
                  e1.printStackTrace();
                }
              }
            }
            outputDirty = false;
          }

          setVisible(false);
          break;
        default:
          assert false:"Program error JobLauncher.java";
      }
    }
  }

  private void centerMe()
  {
    Rectangle meR = getBounds();
    if(mom != null) {
      Rectangle r = mom.getBounds();
      meR.x = r.x + r.width / 2 - meR.width / 2;
      meR.y = r.y + r.height / 2 - meR.height / 2;
    }
    setBounds(meR);
  }

  private void stopRun()
  {
    canButt.setEnabled(false);
    closeButt.setEnabled(true);
    runButt.setEnabled(true);

    writeStatus("Stopping run.");
    try {
      Vector parms = new Vector();
      //o = rpc.execute("experiment.flushQueue",parms);
      Object o = rpc.execute("experiment.clear", parms);
      writeStatus("flushQueue = " + o);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    if(thread != null) {
      Thread t = thread;
      thread = null;
      t.interrupt();
    }
  }

  private void writeStatus(final String s)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        ta.append(s);
        ta.append("\n");
      }
    });
  }

  StringWriter data;

  public void run()
  {
    outputDirty = true;
    try {
      createOutputDir();
      setParams(); // runs, sampls, tmo

      writeStatus("Building XML-RPC client to " + clusterDNS + ".");
      rpc = new XmlRpcClientLite(clusterDNS, 4444);

      fr = new FileReader(f);
      br = new BufferedReader(fr);
      data = new StringWriter();
      out = new PrintWriter(data);
      String line;
      while ((line = br.readLine()) != null) {
        out.println('\t' + line);
        //System.out.println('\t' + line);
      }
      out.close();

      Vector parms = new Vector();
      parms.add(data.toString());
      writeStatus("Sending job file to " + clusterDNS);
      Object o = rpc.execute("experiment.setAssembly", parms);
      writeStatus("experiment.setAssembly returned " + o);

    }
    catch (Exception e) {
      writeStatus("Error connecting to server: "+e.getMessage());
      thread = null;
      return;
    }

    writeStatus("10 second wait before getting results.");
    try {Thread.sleep(10000);}catch (InterruptedException e) {e.printStackTrace();}
    writeStatus("Getting results:");

    Vector parms = new Vector();
    Object o = null;
    int i=0;
    int n = designPts*numRuns;
    for (int dp = 0; dp < designPts; dp++) {
      for (int nrun = 0; nrun < numRuns; nrun++,i++) {
        try {
          parms.clear();
          parms.add(new Integer(dp));
          parms.add(new Integer(nrun));
          o = rpc.execute("experiment.getResult", parms);
          writeStatus("gotResult " + dp + "," + nrun + " ("+i+" of "+n+")");
          saveOutput((String) o, dp, nrun);
        }
        catch (Exception e) {
          writeStatus("Error from experiment.getResult(): " + e.getMessage());
          if(thread == null)
            return;
        }

      }
    }

    stopRun();
  }

  HashMap outputs = new HashMap();

  private void createOutputDir() throws Exception
  {
    outDir = File.createTempFile("DoeRun","");
    outDir.delete();
    outDir.mkdir();
  }
  File outDir;
  private void saveOutput(String o, int dp, int nrun)
  {
    try {
      File f = File.createTempFile("DoeResults", ".xml", outDir);
      f.deleteOnExit();
      FileWriter fw = new FileWriter(f);
      fw.write(o);
      fw.close();
      writeStatus("Result saved to "+f.getAbsolutePath());
      outputs.put("" + dp + "," + nrun, f);
    }
    catch (IOException e) {
      writeStatus("error saving output for run " + dp + ", " + nrun + ": " + e.getMessage());
    }

  }

  public static void main(String[] args)
  {
    if(args.length != 1)
      System.out.println("Give .grd file as argument");
    else
      new JobLauncher(args[0],null);
  }

}
