/*
 * StatsGraph.java
 *
 * Created on April 20, 2006, 11:19 AM
 */
package viskit.doe;

import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.IntervalBarRenderer;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.util.SortOrder;
import java.util.Hashtable;

/**
 * Shows a JFreeChart Statistics Graph for each named property.
 * Vertical axis bounded by maxObjs and minObs.
 * Horozontal each DesignPoint Statistic as a vertical bar, as these come in
 * the maxObjs and minObs scales are adjusted dynamically.
 * Each vertical bar shows standard deviation.
 * To consider, maybe clicking on a bar brings up replication stats for 
 * that design point with horizontal line across the mean. 
 * @author Patrick Sullivan
 * @version $Id: StatsGraph.java 1662 2007-12-16 19:44:04Z tdnorbra $
 */
public class StatsGraph extends JPanel {
    String[] properties;
    int designPoints;
    int samples;
    Hashtable<String, StatisticalCategoryDataset> meanAndStandardDeviations;
    Hashtable<String, IntervalCategoryDataset> minMaxs;
    Hashtable<String, ChartPanel> chartPanels;
    JTabbedPane tabbedPane;
    ChartPanel chartPanel;
    
    public StatsGraph() {
        tabbedPane = new JTabbedPane();
        meanAndStandardDeviations = new Hashtable<String, StatisticalCategoryDataset>();
        minMaxs = new Hashtable<String, IntervalCategoryDataset>();
        chartPanels = new Hashtable<String, ChartPanel>();
        
        add(tabbedPane);
        setVisible(true);
        reset();
    }
    
    public void setProperties(String[] properties, int designPoints, int samples) {
        this.designPoints = designPoints;
        this.samples = samples;
        tabbedPane.removeAll();
        for (int i = 0; i < properties.length; i++) {
            if (viskit.Vstatics.debug) System.out.println("StatsGraph: createDataSets for "+properties[i]);
            createDataSets(properties[i]);
            tabbedPane.add(properties[i],(ChartPanel)chartPanels.get(properties[i]));  
        }
    }
    /**
     *
     *
     */
    public void reset() {
        setProperties(new String[]{"Viskit DOE Results"},0,0);
    }

    // add SampleStatistic to datasets at designPoint d and sample s
    public void addSampleStatistic(viskit.xsd.bindings.assembly.SampleStatistics sample, int d, int s) {
        String name = sample.getName();
        DefaultStatisticalCategoryDataset statsData = (DefaultStatisticalCategoryDataset)meanAndStandardDeviations.get(name);
        DefaultIntervalCategoryDataset minMax = (DefaultIntervalCategoryDataset)minMaxs.get(name);
        if (viskit.Vstatics.debug) System.out.println("SampleStatisticType name: "+sample.getName());
        statsData.add(Double.parseDouble(sample.getMean()),Double.parseDouble(sample.getStandardDeviation()),"Design Point "+d,"Sample "+s);
        minMax.setStartValue(d,"Sample "+s,Double.valueOf(sample.getMinObs()));
        minMax.setEndValue(d,"Sample "+s,Double.valueOf(sample.getMaxObs()));
        ((ChartPanel)chartPanels.get(name)).repaint();
    }
    
    public void createDataSets(String name) {
        
        meanAndStandardDeviations.put(name,createStatisticalDataset());
        minMaxs.put(name,createMinMaxDataset());
        
        JFreeChart chart = new JFreeChart("Statistics for "+name, 
                new Font("Helvetica",Font.BOLD, 14),
                new CategoryPlot(),
                false);
       
        chartPanel = new ChartPanel(chart);
        chartPanels.put(name,chartPanel);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
   
   
        NumberAxis numAx = new NumberAxis("Value of "+name); 
        CategoryItemLabelGenerator generator 
            = new StandardCategoryItemLabelGenerator();
        CategoryItemRenderer renderer;
        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        
        plot.setDomainAxis(new CategoryAxis("Latin Hypersquare Samples of Parameters from Design of Experiment Panel"));
        renderer = new StatisticalBarRenderer();
        ((StatisticalBarRenderer)renderer).setItemMargin(.27);
        renderer.setItemLabelsVisible(true);
        renderer.setItemLabelGenerator(generator);
        numAx = new NumberAxis("Value of "+name);
        numAx.setAutoRange(true);
        numAx.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(numAx);
        plot.setDataset((StatisticalCategoryDataset)meanAndStandardDeviations.get(name));
        plot.setRenderer(renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(true);
        
        // now minmax
        
        renderer = new IntervalBarRenderer();
        ((IntervalBarRenderer)renderer).setDrawBarOutline(true);
        ((IntervalBarRenderer)renderer).setMaximumBarWidth(1.0);
        renderer.setShape(new java.awt.geom.RoundRectangle2D.Double());
        plot.setDataset(1,(IntervalCategoryDataset)minMaxs.get(name));
        plot.setRenderer(1,renderer);
        //plot.setRowRenderingOrder(SortOrder.ASCENDING);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);
    }
    
    /**
     * Creates a sample dataset.
     *
     * @return The dataset.
     */
    private StatisticalCategoryDataset createStatisticalDataset() {

        DefaultStatisticalCategoryDataset result = new DefaultStatisticalCategoryDataset();
        
        for ( int dp = 0; dp < designPoints; dp ++) {
            for ( int s = 0; s < samples; s++) {
                result.add(0.0,0.0,"Design Point "+dp,"Sample "+s);
            }
        }

        return result;

    }

    private IntervalCategoryDataset createMinMaxDataset() {
        
        String[] seriesKeys = new String[designPoints];
        String[] categoryKeys = new String[samples];
        Double[][] starts = new Double[designPoints][];
        Double[][] ends = new Double[designPoints][];
        for ( int dp = 0; dp < designPoints; dp ++) {
            seriesKeys[dp] = "Design Point "+dp;
            starts[dp] = new Double[samples];
            ends[dp] = new Double[samples];
            for ( int s = 0; s < samples; s++) {
                starts[dp][s] = new Double(0.0);
                ends[dp][s] = new Double(0.001);
                categoryKeys[s] = "Sample "+s; // could de-loop
            }
        }
        DefaultIntervalCategoryDataset result = new DefaultIntervalCategoryDataset(seriesKeys,categoryKeys,starts,ends);
        return result;

    }
    
}