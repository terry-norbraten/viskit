package viskit.view;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;

import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.*;
import viskit.VGlobals;
import viskit.VStatics;

/**
 * A class to present an XML file in a JTree widget
 * @author Mike Bailey
 * @since 27 Aug 2004
 * @version $Id$
 */
public class XTree extends JTree {

    DefaultTreeModel mod;

    static XTreePanel getTreeInPanel(File xmlF) throws Exception {
        return new XTreePanel(xmlF);
    }

    public XTree(File xmlF) throws Exception {
        super();
        setFile(xmlF);
    }
    XMLOutputter xmlOut;
    Document doc = null;

    public final void setFile(File xmlF) throws Exception {
        SAXBuilder builder;
        Format form;
        try {
            builder = new SAXBuilder();
            doc = builder.build(xmlF);
            xmlOut = new XMLOutputter();
            form = Format.getPrettyFormat();
            xmlOut.setFormat(form);
        } catch (JDOMException | IOException e) {
            doc = null;
            xmlOut = null;

            throw new Exception("Error parsing or finding file " + xmlF.getAbsolutePath());
        }

        // throw existing away here?
        // this.removeAll();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        root.setUserObject(new nElement(0, doc.getRootElement()));
        mod = new DefaultTreeModel(root);
        //addChildren(root);
        addRoot(root);
        setModel(mod);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        MyRenderer rendr = new MyRenderer();
        setCellRenderer(rendr);

        //   collapseRow(1);

        setToolTipText("XML Tree View");  // needs to be done first to enable tt below
        setRootVisible(true);
        setShowsRootHandles(true);
        setVisibleRowCount(100);    // means always fill a normal size panel
        revalidate();
        this.expandAll(this, true);
    }

    public String getXML() {
        if (xmlOut != null) {
            return xmlOut.outputString(doc);
        }
        return "";
    }

    private void addAttributes(DefaultMutableTreeNode node) {
        Element elm = ((nElement) node.getUserObject()).elem;
        java.util.List lis = elm.getAttributes();
        if (lis.isEmpty()) {
            return;
        }
        for (Iterator itr = lis.iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            String attrs = "<font color='black'>" + att.getName() + " =</font> " + att.getValue();
            node.add(new DefaultMutableTreeNode(attrs));
        }
    }

    private void addRoot(DefaultMutableTreeNode node) {
        addAttributes(node);         // root attributes
        addContent(node);
    }

    private void addContent(DefaultMutableTreeNode node) {
        Element elm = ((nElement) node.getUserObject()).elem;
        int level = ((nElement) node.getUserObject()).n;

        java.util.List lis = elm.getContent();
        if (lis.isEmpty()) {
            return;
        }
        for (Iterator itr = lis.iterator(); itr.hasNext();) {
            Object o = itr.next();
            if (o instanceof Element) {
                DefaultMutableTreeNode dmt = new DefaultMutableTreeNode(new nElement(level + 1, (Element) o));
                node.add(dmt);
                addAttributes(dmt);
                addContent(dmt);
            } else if (o instanceof Text) {
                String s = ((Text) o).getTextTrim();
                if (s.length() > 0) {
                    DefaultMutableTreeNode dmt = new DefaultMutableTreeNode(s);
                    node.add(dmt);
                }
            } else {
                
                if (o != null) {
                    DefaultMutableTreeNode dmt = new DefaultMutableTreeNode(o.toString());
                    node.add(dmt);
                }
            }
        }

    }

    public void removeSelected() {
        TreePath[] selections;
        while ((selections = getSelectionPaths()) != null) {
            TreePath currentSelection = selections[0];
            if (currentSelection != null) {
                DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
                MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
                if (parent != null) {
                    mod.removeNodeFromParent(currentNode);
                }
            }
        }
    }

    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    private void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }

    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
    Color[] colors;

    class MyRenderer extends DefaultTreeCellRenderer {

        Icon[] icons = new XTreeIcon[8];
        float startR = 1.f;
        float startG = 51.f / 255.f;
        float startB = 51.f / 255.f;
        float endR = 51.f / 255.f;
        float endG = 1.f;
        float endB = 215.f / 255.f;

        MyRenderer() {
            colors = new Color[icons.length];
            float rDelta = (endR - startR) / (icons.length - 1);
            float gDelta = (endG - startG) / (icons.length - 1);
            float bDelta = (endB - startB) / (icons.length - 1);
            for (int i = 0; i < icons.length; i++) {
                colors[i] = new Color(startR + rDelta * i, startG + gDelta * i, startB + bDelta * i);
                icons[i] = new XTreeIcon(colors[i]);
            }
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object o = ((DefaultMutableTreeNode) value).getUserObject();

            if (o instanceof nElement) {
                int idx = ((nElement) o).n;
                Element el = ((nElement) o).elem;
                value = "<html>" + wrap(el.getName());
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                String tt = el.toString();
                if (tt.length() < 100) {
                    setToolTipText(tt);
                }
                if (idx == 0) {
                    setToolTipText(((XTree) tree).doc.toString());
                }
                setIcon(icons[idx % icons.length]);
            } else {
                value = "<html><font color='maroon' style='bold'>" + wrap(value.toString());
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                setIcon(null);
                setToolTipText(value.toString());
            }
            return this;
        //return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        }
    }
//    static int wrapSiz = 50;//100;
//    static String nl = System.getProperty("line.separator");
    static boolean isWindows = VStatics.OPERATING_SYSTEM.toLowerCase().contains("windows");

    static private String wrap(String s) {
        if (isWindows) {
            return s;
        }     // can't get it to work
        StringBuilder sb = new StringBuilder();
        String[] sa = new String[]{"", ""};
        sa[1] = s;
        do {
            sa = _wrap(sa[1]);
            sb.append(sa[0]);
            sb.append("<br>");
        } while (sa[1].length() > 0);
        sb.setLength(sb.length() - 4);  //lose last <br>
        return sb.toString().trim();

    // return s;
    }

    static private String[] _wrap(String s) {
        String[] sa = {"", ""};
        if (s.length() < 100) {
            sa[0] = s;
        } else {
            int idx = s.lastIndexOf(' ', 100);
            if (idx != -1) {
                sa[0] = s.substring(0, idx);
                sa[1] = s.substring(idx + 1);
            }
        }
        return sa;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            
            JFrame f = new JFrame("XML Tree Widget Test");
            
            JFileChooser jfc = new JFileChooser();
            jfc.showOpenDialog(f);
            File fil = jfc.getSelectedFile();
            if (fil == null) {
                VGlobals.instance().sysExit(0);
            }
            
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Container c = f.getContentPane();
            c.setLayout(new BorderLayout());
            
            //XTree xt = new XTree(fil);
            //c.add(new JScrollPane(xt), BorderLayout.CENTER);
            XTreePanel p = null;
            try {
                p = XTree.getTreeInPanel(fil);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            
            if (p != null)
                System.out.println(p.xtree.getXML());
            
            c.add(p, BorderLayout.CENTER);
            f.setSize(500, 400);
            f.setLocation(300, 300);
            f.setVisible(true);
            
            // xt.setFile(fil);
        });
    }

    class nElement {

        public int n;
        public Element elem;

        nElement(int n, Element e) {
            this.n = n;
            this.elem = e;
        }
    }
}

class XTreePanel extends JPanel {

    public XTree xtree;
    public JTextArea srcXML;

    XTreePanel(File xmlF) throws Exception {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        try {
            xtree = new XTree(xmlF);
        } catch (Exception e) {
            xtree = null;
            throw (e);
        }

        srcXML = new JTextArea("raw XML here");
        srcXML.setWrapStyleWord(true);
        srcXML.setLineWrap(true);
        srcXML.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        Font oldF = srcXML.getFont();
        srcXML.setFont(new Font("Monospaced", oldF.getStyle(), oldF.getSize()));
        srcXML.setText(getElementText((DefaultMutableTreeNode) xtree.mod.getRoot()));
        srcXML.setCaretPosition(0);

        JScrollPane treeJsp = new JScrollPane(xtree);
        JScrollPane taJsp = new JScrollPane(srcXML);
        taJsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // because we wrap

        JSplitPane jspt = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeJsp, taJsp);
        jspt.setOneTouchExpandable(false);
        jspt.setResizeWeight(0.75);

        Dimension d1 = xtree.getPreferredSize();
        Dimension d2 = srcXML.getPreferredSize();
        jspt.setPreferredSize(new Dimension(d1.width, d1.height + d2.height));
        add(jspt);
        add(Box.createVerticalGlue());

        xtree.getSelectionModel().addTreeSelectionListener((TreeSelectionEvent e) -> {
            DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) xtree.getLastSelectedPathComponent();
            if (dmt == null) {
                return;
            }
            srcXML.setText(getElementText(dmt));
            srcXML.revalidate();
            srcXML.setCaretPosition(0);
        });
    }

    final String getElementText(DefaultMutableTreeNode dmt) {
        Object o = dmt.getUserObject();
        if (o instanceof XTree.nElement) {
            Element elm = ((XTree.nElement) o).elem;
            return xtree.xmlOut.outputString(elm);
        } else {
            return "";
        }
    }
}

class XTreeIcon implements Icon {

    Color myColor;

    XTreeIcon(Color c) {
        super();

        myColor = c;
    }

    @Override
    public int getIconHeight() {
        return 12;
    }

    @Override
    public int getIconWidth() {
        return 12;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        Insets ins = new Insets(0, 0, 0, 0);
        if (c instanceof JComponent) {
            ins = ((Container) c).getInsets();
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(ins.left, ins.top);

        g2d.setColor(myColor);
        g2d.fillOval(1, 1, 10, 10);
        g2d.setColor(Color.black);
        g2d.drawOval(1, 1, 10, 10);
    }
}