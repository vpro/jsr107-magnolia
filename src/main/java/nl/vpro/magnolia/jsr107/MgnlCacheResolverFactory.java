package nl.vpro.magnolia.jsr107;

import java.lang.annotation.Annotation;

import javax.cache.annotation.CacheMethodDetails;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResolverFactory;
import javax.cache.annotation.CacheResult;
import javax.inject.Inject;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MgnlCacheResolverFactory implements CacheResolverFactory {

    @Inject
    MgnlCacheResolver resolver;

	@Override
	public CacheResolver getCacheResolver(CacheMethodDetails<? extends Annotation> cacheMethodDetails) {
		return resolver;
	}

	@Override
	public CacheResolver getExceptionCacheResolver(CacheMethodDetails<CacheResult> cacheMethodDetails) {
		throw new UnsupportedOperationException();

	}
}
