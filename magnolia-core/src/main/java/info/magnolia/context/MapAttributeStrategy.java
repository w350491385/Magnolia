package info.magnolia.context;

import java.util.Hashtable;
import java.util.Map;

public class MapAttributeStrategy implements AttributeStrategy {

	private static final long serialVersionUID = 222L;

	private Map map = new Hashtable();
	
	public MapAttributeStrategy() {	
	}
	
	/**
     * Use the Map.put()
     */
    public void setAttribute(String name, Object value, int scope) {
        this.map.put(name, value);
    }

    /**
     * Use the Map.get()
     */
    public Object getAttribute(String name, int scope) {
        return this.map.get(name);
    }

    /**
     * use the Map.remove()
     */
    public void removeAttribute(String name, int scope) {
        this.map.remove(name);
    }

    /**
     * Ignore scope and return the inner map
     */
    public Map getAttributes(int scope) {
        return this.getAttributes();
    }

    /**
     * Returns the inner map
     */
    public Map getAttributes() {
        return this.map;
    }

    
    public Map getMap() {
        return map;
    }

    
    public void setMap(Map map) {
        this.map = map;
    }

}
