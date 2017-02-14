package nl.vpro.magnolia.jsr107.mock;

import info.magnolia.module.cache.BlockingCache;
import info.magnolia.module.cache.exception.MgnlLockTimeoutException;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;

import java.util.List;

/**
 * @author Michiel Meeuwissen
 * @since 1.5
 */
@Slf4j
public class EHCacheWrapper  implements BlockingCache {

    private final net.sf.ehcache.constructs.blocking.BlockingCache ehcache;
    private final String name;

    public EHCacheWrapper(net.sf.ehcache.constructs.blocking.BlockingCache ehcache) {
                this.ehcache = ehcache;
        this.name = ehcache.getName();
    }


    @Override
    public Object get(Object key) {
        Object value;
        try {
            final Element element = ehcache.get(key);
            value = element != null ? element.getObjectValue() : null;
        } catch (LockTimeoutException e) {
            throw new MgnlLockTimeoutException(e);
        }
        return value;
    }

    @Override
    public Object getQuiet(Object key) {
        Element element = ehcache.getQuiet(key);
        return element == null ? null : element.getObjectValue();
    }

    @Override
    public boolean hasElement(Object key) {
        boolean hasElement;
        // we can't use isKeyInCache(), as it does not check for the element's expiration
        // which may lead to unexpected results.
        // return ehcache.isKeyInCache(key);
        try {
            // get() and getQuiet() do check for expiration and return null if the element was expired.
            // we can't use getQuiet, as it's non-blocking which could lead to multiple copies of same page to be generated
            // if page is requested while previous request for same page is still being processed by different thread
            hasElement = ehcache.get(key) != null;
        } catch (LockTimeoutException e) {
            // FYI: in case you want to return some value instead of re-throwing exception: this is a dilemma ... obviously resource does not exist yet, but being stuck here for while means that it is either being generated or it takes time to generate.
            // returning false would mean server attempts to generate the response again, possibly loosing another thread in the process
            // returning true means server will assume resource exists and will try to retrieve it later, possibly failing with the same error
            throw new MgnlLockTimeoutException(e);
        }
        return hasElement;
    }

    @Override
    public void put(Object key, Object value) {
        final Element element = new Element(key, value);
        ehcache.put(element);

    }

    @Override
    public void put(Object key, Object value, int timeToLiveInSeconds) {
        final Element element = new Element(key, value);
        element.setTimeToLive(timeToLiveInSeconds);
        ehcache.put(element);

    }

    @Override
    public void remove(Object key) {
        ehcache.remove(key);
    }

    @Override
    public void clear() {
        ehcache.removeAll();

    }

    @Override
    public void unlock(Object key) {
        if (ehcache.getQuiet(key) == null) {
            put(key, null);
            remove(key);
        }
    }

    @Override
    public int getBlockingTimeout() {
        return ehcache.getTimeoutMillis();
    }

    public Ehcache getWrappedEhcache() {
        return ehcache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getSize() {
        return ehcache.getSize();
    }

    @Override
    public List<Object> getKeys() {
        return ehcache.getKeys();
    }



}
