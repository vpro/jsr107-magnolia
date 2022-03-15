package nl.vpro.magnolia.jsr107;

import java.io.Serializable;

import javax.cache.configuration.Configuration;

/**
 * The {@link Configuration} implementation associated with caches in Magnolia.
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MgnlCacheConfiguration implements Configuration<Serializable, Serializable> {

    static final MgnlCacheConfiguration INSTANCE = new MgnlCacheConfiguration();

    @Override
    public Class<Serializable> getKeyType() {
        return Serializable.class;
    }

    @Override
    public Class<Serializable> getValueType() {
        return Serializable.class;
    }

    @Override
    public boolean isStoreByValue() {
        return true;
    }
}
