package nl.vpro.magnolia.jsr107.mock;

import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheFactory;
import net.sf.ehcache.CacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A mock Magnolia CacheFactory
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MockCacheFactory implements CacheFactory {

    public final Map<String, Cache> caches = new ConcurrentHashMap<>();

    private final boolean blocking;

    CacheManager cm = CacheManager.create();


    public MockCacheFactory(boolean blocking) {
        this.blocking = blocking;
        cm.clearAll();
    }

    @Override
    public Cache getCache(String name) {

        return caches.computeIfAbsent(name, (n) -> {
            if (blocking) {
                if (cm.getCache(name) == null) {
                    cm.addCache(name);
                }
                return EHCacheWrapper.getCache(cm.getCache(name));
            } else {
                return new MockCache(n);
            }
        });

    }

    @Override
    public void start(boolean isRestart) {

    }

    @Override
    public void stop(boolean isRestart) {

    }

    @Override
    public List<String> getCacheNames() {
        return new ArrayList<>(caches.keySet());

    }
}
