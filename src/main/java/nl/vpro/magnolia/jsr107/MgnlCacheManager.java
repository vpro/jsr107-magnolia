package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.BlockingCache;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.inject.CacheFactoryProvider;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import javax.inject.Inject;

import org.jsr107.ri.annotations.guice.CacheLookupUtil;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@Slf4j
public class MgnlCacheManager implements CacheManager {

    private final CacheFactoryProvider factory;

    private final CacheLookupUtil cacheLookupUtil;

    @Inject
    public MgnlCacheManager(CacheFactoryProvider factory, CacheLookupUtil util) {
        this.factory = factory;
        this.cacheLookupUtil = util;
    }

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
        log.info("Creating cache {}", cacheName);
        info.magnolia.module.cache.Cache mgnlCache = get().getCache(cacheName);
        return new AdaptedCache<>(mgnlCache, this, configuration);

    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
        return getCache(cacheName);
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) {
        return new AdaptedCache<>(get().getCache(cacheName), this, MgnlCacheConfiguration.INSTANCE);
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

    /**
     * Gets a value from the cache (without blocking it)
     *//*

    public Object getValue(Method method, Object key) {
        StaticCacheInvocationContext<? extends Annotation> methodDetails = cacheLookupUtil.getMethodDetails(method, method.getClass());
        final CacheResolver cacheResolver = methodDetails.getCacheResolver();

        final Cache<Object, Object> cache = cacheResolver.resolveCache(cacheLookupUtil.getCacheInvocationContext(method);


        info.magnolia.module.cache.Cache mgnlCache = get().getCache(methodDetails.getCacheName());
        if (mgnlCache== null) {
            throw new IllegalArgumentException();
        }
        Object value = mgnlCache.get(key);
        if (mgnlCache instanceof BlockingCache) {
            ((BlockingCache) mgnlCache).unlock(key);
        }
        return ReturnCacheValueUnInterceptor.unwrap(value);
    }
*/
    /**
     * Gets a value from the cache (without blocking it)
     */

    public Object getValue(String cacheName, Object key) {
        info.magnolia.module.cache.Cache mgnlCache = get().getCache(cacheName);
        if (mgnlCache == null) {
            throw new IllegalArgumentException();
        }
        Object value = mgnlCache.get(key);
        if (mgnlCache instanceof BlockingCache) {
            ((BlockingCache) mgnlCache).unlock(key);
        }
        if (value instanceof CacheValue) {
            value = ((CacheValue) value).orNull();
        }
        return ReturnCacheValueUnInterceptor.unwrap(value);
    }

/*
    public Object getValue(String cacheName, Object key) {
        Cache cache = getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException();
        }
        return ReturnCacheValueUnInterceptor.unwrap(cache.get(key));
    }*/

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(factory.get().getClass())) {
            return (T) factory.get();
        }
        throw new IllegalArgumentException(factory + "  is not a " + clazz + " but a " + factory.get().getClass());

    }
}
