package nl.vpro.magnolia.jsr107;

import javax.cache.Cache;
import javax.cache.annotation.GeneratedCacheKey;

import org.jsr107.ri.annotations.guice.CacheResultInterceptor;

/**
 * For the exception cache, we don't want to block!
 * @author Michiel Meeuwissen
 * @since 1.8
 */
public class NonBlockingCacheResultInterceptor extends CacheResultInterceptor {

    @Override
    protected void checkForCachedException(final Cache<Object, Throwable> exceptionCache, final GeneratedCacheKey cacheKey)
        throws Throwable {
        if (exceptionCache == null) {
            return;
        }
        if (exceptionCache.containsKey(cacheKey)) {
            final Throwable throwable = exceptionCache.get(cacheKey);
            if (throwable != null) {
                //Found exception, re-throw
                throw throwable;
            }
        }
    }
}
