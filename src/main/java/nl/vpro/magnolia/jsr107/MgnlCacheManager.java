package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.mbean.CacheMonitor;

import java.net.URI;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MgnlCacheManager implements CacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(MgnlCacheManager.class);


    @Inject
    private Provider<CacheFactory> factory;

    @Inject 
    private Provider<CacheMonitor> monitor;

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
        ensureMonitor(cacheName);
        return new AdaptedCache<K, V>(mgnlCache, this);

    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return getCache(cacheName);
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) {
        if (get().getCacheNames().contains(cacheName)) {
            LOG.debug("Getting cache {}", cacheName);
            ensureMonitor(cacheName);
            return new AdaptedCache<K, V>(get().getCache(cacheName), this);
        }
        return createCache(cacheName, null);
    }

    private void ensureMonitor(String cacheName) {
        if (!monitor.get().getAll().containsKey(cacheName)) {
            LOG.info("Adding for monitoring cache {}", cacheName);
            monitor.get().addCache(cacheName);  // it seems silly that we have to do this?
        }
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
        throw new UnsupportedOperationException();

    }
}
