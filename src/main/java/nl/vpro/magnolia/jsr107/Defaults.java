package nl.vpro.magnolia.jsr107;

/**
 * @author Michiel Meeuwissen
 * @since 1.6
 */
public @interface Defaults {
    DefaultCacheSettings defaults();
    DefaultCacheSettings exceptionDefaults();
}
