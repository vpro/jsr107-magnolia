package nl.vpro.magnolia.jsr107;

import java.lang.annotation.Annotation;

import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;

import org.jsr107.ri.annotations.DefaultGeneratedCacheKey;

/**
 * This Cache Key Generator can be used on a method without arguments.
 * <pre>
 {@literal @}CacheResult(cacheName = "constants", cacheKeyGenerator = nl.vpro.magnolia.jsr107.MethodKey.class)
   public Site getSite() {
       ...
}</pre>
 * @author Michiel Meeuwissen
 * @since 1.1
 */
public class MethodKey implements CacheKeyGenerator {
    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
        String key = cacheKeyInvocationContext.getMethod().toGenericString();
        return new DefaultGeneratedCacheKey(new Object[] { key });
    }
}
