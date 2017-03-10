package nl.vpro.magnolia.jsr107;

import java.util.Objects;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Michiel Meeuwissen
 * @since 1.5
 */
class ReturnCacheValueUnInterceptor implements  MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        return unwrap(invocation.proceed());
    }

    public static Object unwrap(Object value) {
        if (Objects.equals(value, AdaptedCache.NULL)) {
            value = null;
        }
        if (Objects.equals(value, AdaptedCache.EXCEPTION)) {
            value = null;
        }
        return value;
    }

}
