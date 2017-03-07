package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.BlockingCache;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.inject.CacheFactoryProvider;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Properties;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.GeneratedCacheKey;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import javax.inject.Inject;

import org.aopalliance.intercept.MethodInvocation;
import org.jsr107.ri.annotations.CacheResultMethodDetails;
import org.jsr107.ri.annotations.InternalCacheInvocationContext;
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

    /**
     * Caches in magnolia are always blocking. Sometimes this is asking for trouble.
     */
    public <K, V> Cache<K, V> getUnblockingCache(String cacheName) {
        return new UnblockingCache<K, V>(
            new AdaptedCache<>(get().getCache(cacheName), this, MgnlCacheConfiguration.INSTANCE)
        );
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
     */
    public Object getValue(Class<?> clazz, Object instance, String methodName, Object... key) {

        Method method = null;
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }
        CacheResultMethodDetails methodDetails = (CacheResultMethodDetails) cacheLookupUtil.getMethodDetails(method, instance.getClass());
        final CacheResolver cacheResolver = methodDetails.getCacheResolver();
        MethodInvocation invocation = new SimpleMethodInvocation(instance, method,  key);
        InternalCacheInvocationContext<? extends Annotation> cacheInvocationContext = cacheLookupUtil.getCacheInvocationContext(invocation);
        CacheKeyInvocationContext<? extends Annotation>  cacheKeyInvocationContext = cacheLookupUtil.getCacheKeyInvocationContext(invocation);
        AdaptedCache<Object, Object> cache = (AdaptedCache) cacheResolver.resolveCache(cacheInvocationContext);
        final CacheKeyGenerator cacheKeyGenerator = methodDetails.getCacheKeyGenerator();
        final GeneratedCacheKey cacheKey = cacheKeyGenerator.generateCacheKey(cacheKeyInvocationContext);
        return ReturnCacheValueUnInterceptor.unwrap(cache.getUnblocking(cacheKey));
    }



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

    private static class SimpleMethodInvocation implements MethodInvocation {
        private final Object instance;
        private final Method method;
        private final Object[] key;

        private SimpleMethodInvocation(Object instance, Method method, Object... key) {
            this.instance = instance;
            this.method = method;
            this.key = key;
        }

        @Override
        public Method getMethod() {
            return method;

        }

        @Override
        public Object[] getArguments() {
            return key;

        }

        @Override
        public Object proceed() throws Throwable {
            throw new UnsupportedOperationException();

        }

        @Override
        public Object getThis() {
            return instance;

        }

        @Override
        public AccessibleObject getStaticPart() {
            throw new UnsupportedOperationException();
        }
    }
}
