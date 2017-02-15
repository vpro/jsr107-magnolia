package nl.vpro.magnolia.jsr107;

import lombok.extern.slf4j.Slf4j;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Michiel Meeuwissen
 * @since 1.5
 */
@Slf4j
class CacheValueInterceptor implements  MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return CacheValue.of(invocation.proceed());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CacheValue.of(new RuntimeException(e));
        }
    }
}
