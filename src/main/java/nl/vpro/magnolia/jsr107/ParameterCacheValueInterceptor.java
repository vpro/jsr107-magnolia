package nl.vpro.magnolia.jsr107;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Michiel Meeuwissen
 * @since 1.5
 */
@Slf4j
class ParameterCacheValueInterceptor implements  MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        int i = 0;
        for (Annotation[] annotations : invocation.getMethod().getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof  javax.cache.annotation.CacheValue) {
                    invocation.getArguments()[i] = CacheValue.of(invocation.getArguments()[i]);
                }
            }
            i++;
        }
        return invocation.proceed();
    }
}
