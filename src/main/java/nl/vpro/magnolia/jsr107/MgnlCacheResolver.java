package nl.vpro.magnolia.jsr107;

import java.lang.annotation.Annotation;

import javax.cache.Cache;
import javax.cache.annotation.CacheInvocationContext;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResult;
import javax.inject.Provider;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
class MgnlCacheResolver implements CacheResolver {


    private final Provider<MgnlCacheManager> cacheManager;
    private final boolean exceptions;

    public MgnlCacheResolver(Provider<MgnlCacheManager> cacheManager, boolean exceptions) {
        this.cacheManager = cacheManager;
        this.exceptions = exceptions;
    }

    @Override
    public <K, V> Cache<K, V> resolveCache(CacheInvocationContext<? extends Annotation> cacheInvocationContext) {
        if (exceptions) {
            String cacheName = ((CacheResult) cacheInvocationContext.getCacheAnnotation()).exceptionCacheName();
            return cacheManager.get().getCache(cacheName);
        } else {
            return cacheManager.get().getCache(cacheInvocationContext.getCacheName());
        }
    }
}
