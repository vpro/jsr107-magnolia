package nl.vpro.magnolia.jsr107;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.jsr107.ri.annotations.DefaultGeneratedCacheKey;

/**
 * A {@link CacheKeyGenerator} that maps every {@link Node} argument to its path, which makes it {@link java.io.Serializable}
 * @author Michiel Meeuwissen
 * @since 1.13
 */
@Slf4j
public class NodeAwareCacheKeyGenerator implements CacheKeyGenerator {

    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
        List<Object> result = new ArrayList<>();
        for (CacheInvocationParameter cacheInvocationParameter : cacheKeyInvocationContext.getKeyParameters()) {
            Object value = cacheInvocationParameter.getValue();
            if (value instanceof Node) {
                try {
                    result.add(((Node) value).getPath());
                } catch (RepositoryException e) {
                    log.error(e.getMessage(), e);
                    result.add(value);
                }
            } else {
                result.add(value);
            }
        }

        return new CacheKey(result.toArray(new Object[result.size()]));
    }


}

/**
 * Like {@link DefaultGeneratedCacheKey} but with toString
 */
@ToString
class CacheKey implements GeneratedCacheKey {
    private static final long serialVersionUID = 1L;

    private final Object[] parameters;
    private final int hashCode;

    /**
     * Constructs a default cache key
     *
     * @param parameters the paramters to use
     */
    public CacheKey(Object[] parameters) {
        this.parameters = parameters;
        this.hashCode = Arrays.deepHashCode(parameters);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (this.hashCode != obj.hashCode()) {
            return false;
        }
        CacheKey other = (CacheKey) obj;
        return Arrays.deepEquals(this.parameters, other.parameters);
    }
}
