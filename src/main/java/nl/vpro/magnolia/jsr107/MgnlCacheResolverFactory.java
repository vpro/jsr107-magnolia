package nl.vpro.magnolia.jsr107;

import java.lang.annotation.Annotation;

import javax.cache.annotation.CacheMethodDetails;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResolverFactory;
import javax.cache.annotation.CacheResult;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * The {@link CacheResolverFactory} that factories {@link MgnlCacheResolver}s.
 *
 * @author Michiel Meeuwissen
 * @since 1.0
 */
class MgnlCacheResolverFactory implements CacheResolverFactory {

    private final Provider<MgnlCacheManager> manager;

    @Inject
    public MgnlCacheResolverFactory(Provider<MgnlCacheManager> manager) {
        this.manager = manager;
    }

    @Override
    public CacheResolver getCacheResolver(CacheMethodDetails<? extends Annotation> cacheMethodDetails) {
        return new MgnlCacheResolver(manager, false);
    }

    @Override
    public CacheResolver getExceptionCacheResolver(CacheMethodDetails<CacheResult> cacheMethodDetails) {
        return new MgnlCacheResolver(manager, true);
    }
}
