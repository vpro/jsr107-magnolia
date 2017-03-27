package nl.vpro.magnolia.jsr107;

import info.magnolia.jcr.util.NodeUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import javax.jcr.Node;

/**
 * A {@link CacheKeyGenerator} that maps every {@link Node} argument to its path, which makes it {@link java.io.Serializable}
 * @author Michiel Meeuwissen
 * @since 1.13
 */
@Slf4j
public class NodeAwareCacheKeyGenerator implements CacheKeyGenerator {

    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
        List<Serializable> result = new ArrayList<>();
        for (CacheInvocationParameter cacheInvocationParameter : cacheKeyInvocationContext.getKeyParameters()) {
            Object value = cacheInvocationParameter.getValue();
            if (value instanceof Node) {
                Node node = (Node) value;
                result.add(NodeUtil.getPathIfPossible(node));
            } else if (value instanceof Serializable) {
                result.add((Serializable) value);
            } else {
                throw new IllegalArgumentException("Not serializable " + value);
            }
        }

        return new CacheKey(result.toArray(new Serializable[result.size()]));
    }


}

