package nl.vpro.magnolia.jsr107;

import javax.cache.configuration.Configuration;

/**
 * @author Michiel Meeuwissen
 * @since 1.0
 */
public class MgnlCacheConfiguration implements Configuration<Object, Object> {

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
