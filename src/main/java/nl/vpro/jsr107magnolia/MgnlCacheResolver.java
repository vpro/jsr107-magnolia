package nl.vpro.jsr107magnolia;

import info.magnolia.module.cache.CacheFactory;
import info.magnolia.objectfactory.Components;

import javax.cache.Cache;
import javax.cache.annotation.CacheInvocationContext;
import javax.cache.annotation.CacheResolver;
import java.lang.annotation.Annotation;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class MgnlCacheResolver implements CacheResolver {

	@Override
	public <K, V> Cache<K, V> resolveCache(CacheInvocationContext<? extends Annotation> cacheInvocationContext) {
		return new AdaptedCache<>(Components.getComponent(CacheFactory.class).getCache(cacheInvocationContext.getCacheName()));
	}
}
