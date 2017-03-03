package nl.vpro.magnolia.jsr107;

import java.lang.annotation.Annotation;

import org.jsr107.ri.annotations.CachePutMethodDetails;
import org.jsr107.ri.annotations.InternalCacheKeyInvocationContext;
import org.jsr107.ri.annotations.guice.CachePutInterceptor;

/**
 * A specialization of {@link CachePutInterceptor} which arranges putting of <code>null</code> in the cache
 * (in cooperation with {@link ReturnCacheValueUnInterceptor})
 *
 * @author Michiel Meeuwissen
 * @since 1.6
 */
class CachePutOrNullInterceptor extends CachePutInterceptor {

    @Override
    protected void cacheValue(
        final InternalCacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext,
        final CachePutMethodDetails methodDetails, Object value) {
        if (value == null) {
            value = AdaptedCache.NULL;
        }
        super.cacheValue(cacheKeyInvocationContext, methodDetails, value);
    }
}
