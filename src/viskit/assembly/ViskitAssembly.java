package viskit.assembly;

import edu.nps.util.GenericConversion;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import simkit.Adapter;
import simkit.Named;
import simkit.SimEntity;
import simkit.stat.SampleStatistics;

/** BasicAssembly doesn't provide Viskit users with ability to
 * reference a known SimEntity within a constructor. This class
 * provides hooks into the BasicAssembly and enables named references
 * to instances within the design tool.
 * @version $Id$
 * @author Rick Goldberg
 * @since September 25, 2005, 1:44 PM
 */
public class ViskitAssembly extends BasicAssembly {

    protected Map<String, SimEntity> entities;
    protected Map<String, PropertyChangeListener> replicationStatistics;
    protected Map<String, PropertyChangeListener> designPointStatistics;
    protected Map<String, PropertyChangeListener> propertyChangeListeners;
    protected Map<String, List<PropertyConnector>> propertyChangeListenerConnections;
    protected Map<String, List<PropertyConnector>> designPointStatsListenerConnections;
    protected Map<String, List<PropertyConnector>> replicationStatsListenerConnections;
    protected Map<String, List<String>> simEventListenerConnections;
    protected Map<String, Adapter> adapters;
    private static boolean debug = false;

    /** Creates a new instance of ViskitAssembly */
    public ViskitAssembly() {
        entities = new LinkedHashMap<>();
        replicationStatistics = new LinkedHashMap<>();
        designPointStatistics = new LinkedHashMap<>();
        propertyChangeListeners = new LinkedHashMap<>();
        propertyChangeListenerConnections = new LinkedHashMap<>();
        designPointStatsListenerConnections = new LinkedHashMap<>();
        replicationStatsListenerConnections = new LinkedHashMap<>();
        simEventListenerConnections = new LinkedHashMap<>();
        adapters = new LinkedHashMap<>();
    }

    @Override
    public void createObjects() {
        super.createObjects();

        /* After all PCLs have been created pass the LHMap to the super so that the
         * keys can be extracted for data output indexing. This method is used by
         * the ReportStatisticsConfig.
         */
        setStatisticsKeyValues(replicationStatistics);
    }

    @Override
    protected void createSimEntities() {
        if (entities != null) {
            if (entities.values() != null) {
                simEntity = GenericConversion.toArray(entities.values(), new SimEntity[0]);
            }
        }
    }

    @Override
    protected void createReplicationStats() {
        replicationStats = GenericConversion.toArray(replicationStatistics.values(), new PropertyChangeListener[0]);
        for (PropertyChangeListener sampleStats : replicationStats) {
            LOG.debug(((Named) sampleStats).getName() + " replicationStat created");
        }
    }

    @Override
    protected void createDesignPointStats() {

        super.createDesignPointStats();

        // the super.
        for (SampleStatistics sampleStats : designPointStats) {
//            LOG.debug(sampleStats.getName() + " designPointStat created");
            designPointStatistics.put(sampleStats.getName(), sampleStats);
        }
    }

    @Override
    protected void createPropertyChangeListeners() {
        propertyChangeListener = GenericConversion.toArray(propertyChangeListeners.values(), new PropertyChangeListener[0]);
        for (PropertyChangeListener pcl : propertyChangeListener) {
            LOG.debug(pcl + " propertyChangeListener created");
        }
    }

    @Override
    public void hookupSimEventListeners() {
        String[] listeners = GenericConversion.toArray(simEventListenerConnections.keySet(), new String[0]);
        if(debug) {
            LOG.info("hookupSimEventListeners called " + listeners.length);
        }
        for (String listener : listeners) {
            List<String> simEventListenerConnects = simEventListenerConnections.get(listener);
            if (simEventListenerConnects != null) {
                for(String source : simEventListenerConnects) {
                    connectSimEventListener(listener, source);
                    if (debug) {
                        LOG.info("hooking up SimEvent source " + source + " to listener " + listener);
                    }
                }
            }
        }
    }

    @Override
    public void hookupReplicationListeners() {
        String[] listeners = GenericConversion.toArray(replicationStatsListenerConnections.keySet(), new String[0]);
        for (String listener : listeners) {
            List<PropertyConnector> repStatsConnects = replicationStatsListenerConnections.get(listener);
            if (repStatsConnects != null) {
                for (PropertyConnector pc : repStatsConnects) {
                    connectReplicationStats(listener, pc);
                }
            } else if (debug) {
                LOG.info("No replicationListeners");
            }
        }
    }

    @Override
    protected void hookupDesignPointListeners() {
        super.hookupDesignPointListeners();
        String[] listeners = GenericConversion.toArray(designPointStatsListenerConnections.keySet(), new String[0]);
        // if not the default case, need to really do this with
        // a Class to create instances selected by each ReplicationStats listener.
        if (listeners.length > 0) {
            for (String listener : listeners) {
                List<PropertyConnector> designPointConnects = designPointStatsListenerConnections.get(listener);
                if ( designPointConnects != null ) {
                    for (PropertyConnector pc : designPointConnects) {
                        connectDesignPointStats(listener, pc);
                    }
                }
            }
        } else if (debug) {
            LOG.info("No external designPointListeners to add");
        }
    }

    @Override
    protected void hookupPropertyChangeListeners() {
        String[] listeners = GenericConversion.toArray(propertyChangeListenerConnections.keySet(), new String[0]);
        for (String listener : listeners) {
            List<PropertyConnector> propertyConnects = propertyChangeListenerConnections.get(listener);
            if (propertyConnects != null) {
                for (PropertyConnector pc : propertyConnects) {
                    connectPropertyChangeListener(listener, pc);
                }
            } else if (debug) {
                LOG.info("No propertyConnectors");
            }
        }
    }

    void connectSimEventListener(String listener, String source) {
        getSimEntityByName(source).addSimEventListener(getSimEntityByName(listener));
    }

    void connectPropertyChangeListener(String listener, PropertyConnector pc) {
        if ( "null".equals(pc.property) ) {
            pc.property = "";
        }
        if (pc.property.isEmpty()) {
            getSimEntityByName(pc.source).addPropertyChangeListener(getPropertyChangeListenerByName(listener));
        } else {
            getSimEntityByName(pc.source).addPropertyChangeListener(pc.property,getPropertyChangeListenerByName(listener));
        }
        if (debug) {
            LOG.info("connecting entity " + pc.source + " to " + listener + " property " + pc.property);
        }
    }

    void connectReplicationStats(String listener, PropertyConnector pc) {
        if ( "null".equals(pc.property) ) {
            pc.property = "";
        }
        if (debug) {
            LOG.info("Connecting entity " + pc.source + " to replicationStat " + listener + " property " + pc.property);
        }

        if (pc.property.isEmpty()) {
            pc.property = getReplicationStatsByName(listener).getName().trim();
            if (debug) {
                LOG.info("Property unspecified, attempting with lookup " + pc.property);
            }
        }

        if (pc.property.isEmpty()) {
            if (debug) {
                LOG.info("Null property, replicationStats connecting "+pc.source+" to "+listener);
            }
            getSimEntityByName(pc.source).addPropertyChangeListener(getReplicationStatsByName(listener));
        } else {
            if (debug) {
                LOG.info("Connecting replicationStats from "+pc.source+" to "+listener);
            }
            getSimEntityByName(pc.source).addPropertyChangeListener(pc.property,getReplicationStatsByName(listener));
        }
    }

    void connectDesignPointStats(String listener, PropertyConnector pc) {
        if ( pc.property.equals("null") ) {
            pc.property = "";
        }
        if ( "".equals(pc.property) ) {
            pc.property = getDesignPointStatsByName(listener).getName();
        }

        if ( "".equals(pc.property) ) {
            getSimEntityByName(pc.source).addPropertyChangeListener(getDesignPointStatsByName(listener));
        } else {
            getSimEntityByName(pc.source).addPropertyChangeListener(pc.property,getDesignPointStatsByName(listener));
        }
    }

    public void addSimEntity(String name, SimEntity entity) {
        entity.setName(name);
        entities.put(name, entity);

        // TODO: This will throw an IllegalArgumentException?
//        LOG.debug("entity is: " + entity);
    }

    public void addDesignPointStats(String listenerName, PropertyChangeListener pcl) {
        designPointStatistics.put(listenerName,pcl);
    }

    /** Called from the generated Assembly adding PCLs in order of calling
     * @param listenerName the given name of the PropertyChangeListener
     * @param pcl type of PropertyChangeListener
     */
    public void addReplicationStats(String listenerName, PropertyChangeListener pcl) {
        LOG.debug("Adding to replicationStatistics " + listenerName + " " + pcl);
        replicationStatistics.put(listenerName, pcl);
    }

    @Override
    public void addPropertyChangeListener(String listenerName, PropertyChangeListener pcl) {
        propertyChangeListeners.put(listenerName, pcl);
    }

    public void addPropertyChangeListenerConnection(String listener, String property, String source) {
        List<PropertyConnector> propertyConnects = propertyChangeListenerConnections.get(listener);
        if ( propertyConnects == null ) {
            propertyConnects = new LinkedList<>();
            propertyChangeListenerConnections.put(listener, propertyConnects);
        }
        propertyConnects.add(new PropertyConnector(property, source));
    }

    public void addDesignPointStatsListenerConnection(String listener, String property, String source) {
        List<PropertyConnector> designPointConnects = designPointStatsListenerConnections.get(listener);
        if ( designPointConnects == null ) {
            designPointConnects = new LinkedList<>();
            designPointStatsListenerConnections.put(listener, designPointConnects);
        }
        designPointConnects.add(new PropertyConnector(property,source));
    }

    public void addReplicationStatsListenerConnection(String listener, String property, String source) {
        List<PropertyConnector> repStatsConnects = replicationStatsListenerConnections.get(listener);
        if ( repStatsConnects == null ) {
            repStatsConnects = new LinkedList<>();
            replicationStatsListenerConnections.put(listener, repStatsConnects);
        }
        repStatsConnects.add(new PropertyConnector(property,source));
    }

    public void addSimEventListenerConnection(String listener, String source) {
        List<String> simEventListenerConnects = simEventListenerConnections.get(listener);
        if ( simEventListenerConnects == null ) {
            simEventListenerConnects = new LinkedList<>();
            simEventListenerConnections.put(listener, simEventListenerConnects);
        }
        if (debug) {
            LOG.info("addSimEventListenerConnection source " + source + " to listener " + listener );
        }
        simEventListenerConnects.add(source);
    }

    public void addAdapter(String name, String heard, String sent, String from, String to) {
        Adapter a = new Adapter(heard,sent);
        a.connect(getSimEntityByName(from),getSimEntityByName(to));
        adapters.put(name,a);
        entities.put(name,a);
    }

    public PropertyChangeListener getPropertyChangeListenerByName(String name) {
        return propertyChangeListeners.get(name);
    }

    public SampleStatistics getDesignPointStatsByName(String name) {
        return (SampleStatistics) designPointStatistics.get(name);
    }

    public SampleStatistics getReplicationStatsByName(String name) {
        return (SampleStatistics) replicationStatistics.get(name);
    }

    public SimEntity getSimEntityByName(String name) {
        if (debug) {
            LOG.info("getSimEntityByName for " + name + " " + entities.get(name));
        }
        return entities.get(name);
    }

    protected class PropertyConnector {
        String property;
        String source;

        PropertyConnector(String p, String s) {
            this.property = p;
            this.source = s;
        }
    }
}
