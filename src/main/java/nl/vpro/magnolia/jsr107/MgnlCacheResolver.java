package nl.vpro.magnolia.jsr107;

import info.magnolia.module.cache.CacheFactory;
import info.magnolia.objectfactory.Components;

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

    @Inject
    Provider<CacheFactory> cacheFactory;

	@Override
	public <K, V> Cache<K, V> resolveCache(CacheInvocationContext<? extends Annotation> cacheInvocationContext) {

        //info.magnolia.module.cache.Cache mgnlCache = cacheFactory.get().getCache(cacheInvocationContext.getCacheName());
        info.magnolia.module.cache.Cache mgnlCache = Components.getComponent(CacheFactory.class).getCache(cacheInvocationContext.getCacheName());
		return new AdaptedCache<K, V>(mgnlCache);
	}
}
