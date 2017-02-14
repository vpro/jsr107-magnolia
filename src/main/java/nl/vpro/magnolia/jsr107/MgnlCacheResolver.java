package nl.vpro.magnolia.jsr107;

import java.lang.annotation.Annotation;

import javax.cache.Cache;
import javax.cache.annotation.CacheInvocationContext;
import javax.cache.annotation.CacheResolver;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MgnlCacheResolver implements CacheResolver {


    private final Provider<MgnlCacheManager> cacheManager;

    @Inject
    public MgnlCacheResolver(Provider<MgnlCacheManager> cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public <K, V> Cache<K, V> resolveCache(CacheInvocationContext<? extends Annotation> cacheInvocationContext) {
        return cacheManager.get().getCache(cacheInvocationContext.getCacheName());
    }
}
