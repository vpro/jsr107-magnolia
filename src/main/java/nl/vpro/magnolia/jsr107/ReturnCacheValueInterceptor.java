package nl.vpro.magnolia.jsr107;

import lombok.extern.slf4j.Slf4j;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Michiel Meeuwissen
 * @since 1.5
 */
@Slf4j
class ReturnCacheValueInterceptor implements  MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return CacheValue.of(invocation.proceed());
        } catch(RuntimeException e) {
            log.error(e.getMessage(), e);
            return CacheValue.of(e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CacheValue.of(new RuntimeException(e.getMessage(), e));
        }
    }
}
