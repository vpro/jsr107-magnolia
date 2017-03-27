package nl.vpro.magnolia.jsr107;

import lombok.ToString;

import java.io.Serializable;
import java.util.Arrays;

import javax.cache.annotation.GeneratedCacheKey;

import org.jsr107.ri.annotations.DefaultGeneratedCacheKey;

/**
 * Like {@link DefaultGeneratedCacheKey} but with toString, also it only accepts serializable parameters.
 */
@ToString
public class CacheKey implements GeneratedCacheKey {
    private static final long serialVersionUID = 1L;

    private final Serializable[] parameters;
    private final int hashCode;

    /**
     * Constructs a default cache key
     *
     * @param parameters the paramters to use
     */
    public CacheKey(Serializable... parameters) {
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
