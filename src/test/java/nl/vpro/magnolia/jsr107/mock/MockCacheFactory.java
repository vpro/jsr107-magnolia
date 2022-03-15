package nl.vpro.magnolia.jsr107.mock;

import info.magnolia.module.cache.*;
import info.magnolia.module.cache.ehcache3.EhCache3Wrapper;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.*;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.mockito.Mockito.mock;

/**
 * A mock Magnolia CacheFactory
 *
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MockCacheFactory implements CacheFactory {

    public final Map<String, Cache> caches = new ConcurrentHashMap<>();

    private final boolean blocking;

    CacheManager cm = CacheManagerBuilder.newCacheManagerBuilder().build();

    CacheModule cacheModule = mock(CacheModule.class);


    public MockCacheFactory(boolean blocking) {
        this.blocking = blocking;
        cm.init();;
        cm.getRuntimeConfiguration().getCacheConfigurations().keySet().forEach(n -> {
            cm.removeCache(n);
        });
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, (n) -> {
            if (blocking) {
                if (cm.getCache(name, Serializable.class, Serializable.class) == null) {
                    ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.heap(1024);
                    CacheConfigurationBuilder<Serializable, Serializable> builder = newCacheConfigurationBuilder(Serializable.class, Serializable.class, resourcePoolsBuilder);
                    cm.createCache(name, builder.build());
                }
                return new EhCache3Wrapper(name, cacheModule, 1000, cm.getCache(name, Serializable.class, Serializable.class));
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
