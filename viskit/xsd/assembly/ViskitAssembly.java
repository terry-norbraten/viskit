/*
 * ViskitAssembly.java
 *
 * Created on September 25, 2005, 1:44 PM
 *
 * BasicAssembly doesn't provide Viskit users with ability to 
 * reference a known SimEntity within a constructor. This class 
 * provides hooks into the BasicAssembly and enables named references
 * to instances within the design tool. 
 */

package viskit.xsd.assembly;

import simkit.*;
import simkit.stat.*;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import viskit.xsd.bindings.assembly.PropertyChangeListenerConnection;

/**
 * @version $Id$
 * @author Rick Goldberg
 */
public class ViskitAssembly extends BasicAssembly { 
    
    public LinkedHashMap entities = new LinkedHashMap();
    public LinkedHashMap replicationStatistics;
    public LinkedHashMap designPointStatistics;
    public LinkedHashMap propertyChangeListeners;
    public LinkedHashMap propertyChangeListenerConnections;
    public LinkedHashMap designPointStatsListenerConnections;
    public LinkedHashMap replicationStatsListenerConnections;
    public LinkedHashMap simEventListenerConnections;
    public LinkedHashMap adapters;
    
    /** Creates a new instance of ViskitAssembly */
    public ViskitAssembly() {

        
    }
    
    public void createObjects() {
        entities = new LinkedHashMap();
        replicationStatistics = new LinkedHashMap();
        designPointStatistics = new LinkedHashMap();
        propertyChangeListeners = new LinkedHashMap();
        simEventListenerConnections = new LinkedHashMap();
        propertyChangeListenerConnections = new LinkedHashMap();
        simEventListenerConnections = new LinkedHashMap();
        designPointStatsListenerConnections = new LinkedHashMap();
        replicationStatsListenerConnections = new LinkedHashMap();
        adapters = new LinkedHashMap();
        super.createObjects();
    }
    
    public void hookupReplicationListeners() {
        String[] listeners = (String[]) replicationStatsListenerConnections.keySet().toArray(new String[0]);
        for ( int i = 0; i < listeners.length; i++ ) {
            PropertyConnector pc = (PropertyConnector)replicationStatsListenerConnections.get(listeners[i]);
            connectReplicationStat(listeners[i], pc.source);
        }
        
    }
    
    public void hookupSimEventListeners() {
        String[] listeners = (String[]) simEventListenerConnections.keySet().toArray(new String[0]);
        for ( int i = 0; i < listeners.length; i++ ) {
            String source = (String) simEventListenerConnections.get(listeners[i]);
            connectSimEventListener(listeners[i], source);
        }
        
    }
    
    public void hookupPropertyChangeListeners() {
        String[] listeners = (String[]) propertyChangeListenerConnections.keySet().toArray(new String[0]);
        for ( int i = 0; i < listeners.length; i++ ) {
            PropertyConnector pc = (PropertyConnector)propertyChangeListenerConnections.get(listeners[i]);
            connectPropertyChangeListener(listeners[i], pc.source);
        }
    }
    
    public void hookupDesignPointListeners() {
        String[] listeners = (String[]) designPointStatsListenerConnections.keySet().toArray(new String[0]);
        for ( int i = 0; i < listeners.length; i++ ) {
            PropertyConnector pc = (PropertyConnector)designPointStatsListenerConnections.get(listeners[i]);
            connectDesignPointStat(listeners[i], pc.source);
        }
        
    }
    
    void connectSimEventListener(String listener, String source) {
        getSimEntityByName(source).addSimEventListener(getSimEntityByName(listener));
    } 
    
    void connectPropertyChangeListener(String listener, String source) {
        getSimEntityByName(source).addPropertyChangeListener(getPropertyChangeListenerByName(listener));
    }
    
    void connectReplicationStat(String listener, String source) {
        getSimEntityByName(source).addPropertyChangeListener(getReplicationStatByName(listener));
    }
    
    void connectDesignPointStat(String listener, String source) {
        getSimEntityByName(source).addPropertyChangeListener(getDesignPointStatByName(listener));
    }
    

    /** to be called after all entities have been added as a super() */
    /*  note not using template version of ArrayList... */
    public void createSimEntities() {
        simEntity = 
                (SimEntity[]) entities.values().toArray(new SimEntity[0]);
    }
    
    public void createDesignPointStats() {
        designPointStats = 
                (SampleStatistics[]) designPointStatistics.values().toArray(new SampleStatistics[0]);
        if ( designPointStats.length == 0 ) {
            super.createDesignPointStats();
        }
    }
    
    public void createReplicationStats() {
        replicationStats = 
                (SampleStatistics[]) replicationStatistics.values().toArray(new SampleStatistics[0]);
    }
    
    public void createPropertyChangeListeners() {
        propertyChangeListener = 
                (PropertyChangeListener[]) propertyChangeListeners.values().toArray(new PropertyChangeListener[0]);
    }
    
    public void addSimEntity(String name, SimEntity entity) {
        entity.setName(name);
        entities.put(name,entity);
    }
    
    public void addDesignPointStat(String listenerName, PropertyChangeListener pcl) {
        designPointStatistics.put(listenerName,pcl);
    }
   
    public void addReplicationStat(String listenerName, PropertyChangeListener pcl) {
        replicationStatistics.put(listenerName,pcl);
    }
    
    public void addPropertyChangeListener(String listenerName, PropertyChangeListener pcl) {
        propertyChangeListeners.put(listenerName,pcl);
    }
    
    public void addPropertyChangeListenerConnection(String listener, String property, String source) {
        propertyChangeListenerConnections.put(listener,new PropertyConnector(property,source));
    }
    
    public void addDesignPointStatListenerConnection(String listener, String property, String source) {
        designPointStatsListenerConnections.put(listener,new PropertyConnector(property,source));
    }
        
    public void addReplicationStatsListenerConnection(String listener, String property, String source) {
        propertyChangeListenerConnections.put(listener,new PropertyConnector(property,source));
    } 
    
    public void addSimEventListenerConnection(String listener, String source) {
        simEventListenerConnections.put(listener,source);
    }
    
    public void addAdapter(String name, String heard, String sent, String from, String to) {
        Adapter a = new Adapter(heard,sent);
        // Adapters always follow SimEntities in the Schema,
        // it is safe to connect now.
        a.connect(getSimEntityByName(from),getSimEntityByName(to));
        adapters.put(name,a);
        entities.put(name,a);
    }
    
    public PropertyChangeListener getPropertyChangeListenerByName(String name) {
        return (PropertyChangeListener) propertyChangeListeners.get(name);
    }
    
    public SampleStatistics getDesignPointStatByName(String name) {
        return (SampleStatistics) designPointStatistics.get(name);
    }
    
    public SampleStatistics getReplicationStatByName(String name) {
        return (SampleStatistics) designPointStatistics.get(name);
    }
    
    public SimEntity getSimEntityByName(String name) {
        return (SimEntity) entities.get(name);
    }
    
    /* not exactly sure if this is needed when a property is set as in the constructor
     * of a SampleStatisics type 
     */
    class PropertyConnector {
        String property;
        String source;
        
        PropertyConnector(String p, String s) {
            this.property = p;
            this.source = s;
        }
    }
}
