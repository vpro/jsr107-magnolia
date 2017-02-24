package nl.vpro.magnolia.jsr107;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Michiel Meeuwissen
 * @since 1.5
 */
class ReturnCacheValueUnInterceptor implements  MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object v = invocation.proceed();
        if (v == AdaptedCache.NULL) {
            v = null;
        }
        return v;
    }
}
