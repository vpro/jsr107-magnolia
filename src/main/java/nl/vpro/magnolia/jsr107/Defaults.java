package nl.vpro.magnolia.jsr107;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Using this wrapper, you can define default cache settings for both the normal cache as the associated cache for exception.
 * @author Michiel Meeuwissen
 * @since 1.6
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Defaults {
    /**
     * If <code>true</code> Then the values default by cacheSettings/exceptionCacheSetting will even be applied when existing already.
     */
    boolean overrideOnUpdate() default false;
    DefaultCacheSettings cacheSettings() default @DefaultCacheSettings();
    DefaultCacheSettings exceptionCacheSettings() default @DefaultCacheSettings();
}
