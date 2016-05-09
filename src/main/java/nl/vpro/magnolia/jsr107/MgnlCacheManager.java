package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.inject.CacheFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import javax.inject.Inject;
import java.net.URI;
import java.util.Properties;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MgnlCacheManager implements CacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(MgnlCacheManager.class);

    @Inject
    private CacheFactoryProvider factory;

    private CacheFactory get() {
        return factory.get();
    }

    private Properties properties = new Properties();

    @Override
    public CachingProvider getCachingProvider() {
        return null;

    }

    @Override
    public URI getURI() {
        return null;

    }

    @Override
    public ClassLoader getClassLoader() {
        return MgnlCacheManager.class.getClassLoader();

    }

    @Override
    public Properties getProperties() {
        return properties;

    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName, C configuration) throws IllegalArgumentException {
        LOG.info("Creating cache {}", cacheName);
        info.magnolia.module.cache.Cache mgnlCache = get().getCache(cacheName);
        return new AdaptedCache<>(mgnlCache, this, configuration);

    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return getCache(cacheName);
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) {
        if (get().getCacheNames().contains(cacheName)) {
            LOG.debug("Getting cache {}", cacheName);
            return new AdaptedCache<>(get().getCache(cacheName), this, new MgnlCacheConfiguration());
        }
        return createCache(cacheName, null);
    }


    @Override
    public Iterable<String> getCacheNames() {
        return get().getCacheNames();
    }

    @Override
    public void destroyCache(String cacheName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableManagement(String cacheName, boolean enabled) {


    }

    @Override
    public void enableStatistics(String cacheName, boolean enabled) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;

    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(factory.get().getClass())) {
            return (T) factory.get();
        }
        throw new IllegalArgumentException(factory + "  is not a " + clazz + " but a " + factory.get().getClass());

    }
}
