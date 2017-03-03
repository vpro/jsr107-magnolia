package nl.vpro.magnolia.jsr107;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;

import javax.cache.Cache;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.GeneratedCacheKey;
import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jsr107.ri.annotations.*;

/**
 * @author Michiel Meeuwissen
 * @since 1.5
 */
@Slf4j
class ReturnCacheValueInterceptor extends AbstractCacheResultInterceptor<MethodInvocation> implements  MethodInterceptor {


    private CacheContextSource<MethodInvocation> cacheContextSource;


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            Object o = proceed(invocation);
            if (o == null) {
                o = AdaptedCache.NULL;
            }
            return o;
        } catch (Throwable t) {
            cacheException(invocation);
            throw t;

        }
    }

    @Override
    protected Object proceed(MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }

    @Inject
    public void setCacheContextSource(CacheContextSource<MethodInvocation> cacheContextSource) {
        this.cacheContextSource = cacheContextSource;
    }

    /**
     * Putting _something_ in the cache, otherwise Blocking timeout exceptions in magnolia....
     */
    protected void cacheException(MethodInvocation invocation) {

        final InternalCacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext = cacheContextSource.getCacheKeyInvocationContext(invocation);
        final CacheResultMethodDetails methodDetails = this.getStaticCacheKeyInvocationContext(cacheKeyInvocationContext, InterceptorType.CACHE_RESULT);
        final Cache<Object, Object> cache = methodDetails.getCacheResolver().resolveCache(cacheKeyInvocationContext);
        final CacheKeyGenerator cacheKeyGenerator = methodDetails.getCacheKeyGenerator();
        final GeneratedCacheKey cacheKey = cacheKeyGenerator.generateCacheKey(cacheKeyInvocationContext);
        log.debug("Marking as exception {} {}", cache, cacheKey);
        cache.put(cacheKey, AdaptedCache.EXCEPTION);
    }

}
