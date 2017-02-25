package nl.vpro.magnolia.jsr107;

/**
 * Using this wrapper, you can define default cache settings for both the normal cache as the associated cache for exception.
 * @author Michiel Meeuwissen
 * @since 1.6
 */
public @interface Defaults {
    DefaultCacheSettings defaults();
    DefaultCacheSettings exceptionDefaults();
}
