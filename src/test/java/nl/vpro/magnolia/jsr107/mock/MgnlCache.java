package nl.vpro.magnolia.jsr107.mock;

import info.magnolia.module.cache.Cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *  A mock Magnolia Cache implementations
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MgnlCache implements Cache  {
    private final Map<Object, Object> backing = new HashMap<>();

    private final String name;

    public MgnlCache(String name) {
        this.name = name;
    }

    @Override
    public boolean hasElement(Object key) {
        return backing.containsKey(key);
    }

    @Override
    public void put(Object key, Object value) {
        backing.put(key, value);
    }

    @Override
    public void put(Object key, Object value, int timeToLiveInSeconds) {
        backing.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return backing.get(key);
    }

    @Override
    public Object getQuiet(Object key) {
        return backing.get(key);
    }

    @Override
    public void remove(Object key) {
        backing.remove(key);
    }

    @Override
    public void clear() {
        backing.clear();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSize() {
        return backing.size();

    }

    @Override
    public Collection<Object> getKeys() {
        return backing.keySet();
    }
}
