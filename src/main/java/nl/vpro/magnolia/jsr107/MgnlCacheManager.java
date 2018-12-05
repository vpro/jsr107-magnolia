package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.BlockingCache;
import info.magnolia.module.cache.CacheFactory;
import info.magnolia.module.cache.inject.CacheFactoryProvider;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

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
import org.jsr107.ri.annotations.*;
import org.jsr107.ri.annotations.guice.CacheLookupUtil;

/**
 * Adapts a magnolia {@link CacheFactoryProvider} to a {@link CacheManager}. This is needed for cache-annotations-ri-guice, but
 * it can be used more genericly for code which desires such a cache manager.
 * @author Michiel Meeuwissen
 * @since 1.0
 */
@SuppressWarnings("unchecked")
@Slf4j
@ToString
public class MgnlCacheManager implements CacheManager {

    private final CacheFactoryProvider factory;

    private final CacheLookupUtil cacheLookupUtil;

    private static final Map<Class<? extends CacheKeyGenerator>, Function<GeneratedCacheKey, Object[]>>
    PARAMETER_GETTER = new HashMap<>();

    static {
        PARAMETER_GETTER.put(MgnlObjectsAwareCacheKeyGenerator.class,
            createGetter(SerializableGeneratedCacheKey.class, "parameters"));

        PARAMETER_GETTER.put(DefaultCacheKeyGenerator.class,
            createGetter(DefaultGeneratedCacheKey.class, "parameters"));
    }

    private static Function<GeneratedCacheKey, Object[]> createGetter(Class<?> keyClass, String field) {
        Field parameters;
        try {
            parameters = keyClass.getDeclaredField(field);
            parameters.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return o -> {
            try {
                return (Object[]) parameters.get(o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

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
        return new UnblockingCache<>(
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
     * Gets a value from the cache (without blocking it). Point to the method with the {@link javax.cache.annotation.CacheResult} annotation,
     * and give its key parameters.
     * @param clazz The class containing the @CacheResult annotation
     * @param instance The instance on which the cache method will be called
     * @param methodName The method for which the cache must be used
     * @param key The arguments of that method that make up the key in the cache (considering the cache key generator and the {@link javax.cache.annotation.CacheKey} annotations.
     */
    public Object getValue(Class<?> clazz, Object instance, String methodName, Object... key) {
        Class<?>[] keyClasses = key == null ? null : Arrays.stream(key).map(k -> k == null ? Object.class : k.getClass()).toArray((IntFunction<Class<?>[]>) Class[]::new);
        return getValueGetter(clazz, instance, methodName, keyClasses)
            .get(key);
    }

    public Getter getValueGetter(Class<?> clazz, Object instance, String methodName, Class<?>... keyClasses) {
        CacheResultMethodDetails methodDetails = getMethodDetails(clazz, methodName, keyClasses);
        final CacheResolver cacheResolver = methodDetails.getCacheResolver();
        final CacheKeyGenerator cacheKeyGenerator = methodDetails.getCacheKeyGenerator();
        return key -> {
            MethodInvocation invocation = new SimpleMethodInvocation(instance, methodDetails, key);
            InternalCacheInvocationContext<? extends Annotation> cacheInvocationContext = cacheLookupUtil.getCacheInvocationContext(invocation);
            CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext = cacheLookupUtil.getCacheKeyInvocationContext(invocation);
            AdaptedCache<Object, Object> cache = (AdaptedCache) cacheResolver.resolveCache(cacheInvocationContext);
            final GeneratedCacheKey cacheKey = cacheKeyGenerator.generateCacheKey(cacheKeyInvocationContext);
            final Object value = cache.getUnblocking(cacheKey);
            return ReturnCacheValueUnInterceptor.unwrap(value);
        };
    }

    public Iterator<Object[]> getKeys(Class<?> clazz, Object instance, String methodName, Class<?>... keys) {
        CacheResultMethodDetails methodDetails = getMethodDetails(clazz, methodName, keys);
        final CacheResolver cacheResolver = methodDetails.getCacheResolver();
        MethodInvocation invocation = new SimpleMethodInvocation(instance, methodDetails, keys);
        InternalCacheInvocationContext<? extends Annotation> cacheInvocationContext = cacheLookupUtil.getCacheInvocationContext(invocation);
        AdaptedCache<Object, Object> cache = (AdaptedCache) cacheResolver.resolveCache(cacheInvocationContext);
        Iterator<Cache.Entry<Object, Object>> iterator = cache.iterator();
        final Function<GeneratedCacheKey, Object[]> objectGetter = PARAMETER_GETTER.get(methodDetails.getCacheKeyGenerator().getClass());

        return new Iterator<Object[]>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Object[] next() {
                GeneratedCacheKey key = (GeneratedCacheKey) iterator.next().getKey();
                return objectGetter.apply(key);
            }
        };
    }

    protected CacheResultMethodDetails getMethodDetails(Class<?> clazz, String methodName, Class<?>... keyClasses) {

        Method method = getMethod(clazz, methodName, keyClasses);
        return  (CacheResultMethodDetails) cacheLookupUtil.getMethodDetails(method, clazz);
    }

    protected Method getMethod(Class<?> clazz, String methodName, Class<?>... arguments) {
        Method method;

        List<Method> candidates = new ArrayList<>();
        OUTER:
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                if (arguments.length > 0) {
                    if (arguments.length != m.getParameterTypes().length) {
                        continue;
                    }
                    for (int i = 0; i < m.getParameterTypes().length; i++) {
                        if (!m.getParameterTypes()[i].isAssignableFrom(arguments[i])) {
                            continue OUTER;
                        }
                    }

                }

                m.setAccessible(true);
                candidates.add(m);
            }
        }
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("Cannot find method " + methodName + "[" + Arrays.asList(arguments) + "] in " + clazz);
        }
        if (candidates.size() == 1) {
            method = candidates.get(0);
        } else {
            throw new IllegalArgumentException("Multiple methods " + methodName + "[" + Arrays.asList(arguments) + "] found " + clazz + " " + candidates);
        }

        return method;
    }


    public interface Getter extends Function<Object[], Object> {

        default Object get(Object... objects) {
            return apply(objects);
        }

    }


    /**
     * Gets a value from the cache (without blocking it)
     * This method requires that you know exactly what the key is in the underlying cache.
     * {@link #getValue(Class, Object, String, Object...)} does not have that requirement.
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

        private SimpleMethodInvocation(Object instance, CacheResultMethodDetails method, Object... key) {
            this.instance = instance;
            this.method = method.getMethod();
            List<CacheParameterDetails> keyParameters = method.getKeyParameters();
            if (key == null) {
                key = new Object[this.method.getParameterCount()];
            }
            for (int i = 0; i < keyParameters.size(); i++) {
                Object keyEntry = key[i];
                if (keyEntry != null && !keyParameters.get(i).getRawType().isInstance(keyEntry)) {
                    throw new IllegalArgumentException(keyEntry + " (parameter " + i + ") is not compatible with " + keyParameters);
                }
            }
            this.key = key;
        }

        private SimpleMethodInvocation(Object instance, CacheResultMethodDetails method, Class<?>... keyClasses) {
            this(instance, method, new Object[method.getMethod().getParameterCount()]);
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
        public Object proceed() {
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
