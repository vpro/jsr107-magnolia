package nl.vpro.magnolia.jsr107;

import javax.cache.configuration.Configuration;

/**
 * The {@link Configuration} implementation associated with caches in Magnolia.
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MgnlCacheConfiguration implements Configuration<Object, Object> {

    static final MgnlCacheConfiguration INSTANCE = new MgnlCacheConfiguration();

    @Override
    public Class<Object> getKeyType() {
        return Object.class;
    }

    @Override
    public Class<Object> getValueType() {
        return Object.class;
    }

    @Override
    public boolean isStoreByValue() {
        return true;
    }
}
