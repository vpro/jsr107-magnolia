package nl.vpro.magnolia.jsr107;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be added next to the @CacheResult to specify default for the {@link nl.vpro.magnolia.jsr107.CreateConfigurationTasks}
 * See also {@link CacheSettings#of(DefaultCacheSettings)}
 * @author Michiel Meeuwissen
 * @since 1.4
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultCacheSettings  {

    boolean copyOnRead() default false;
    boolean copyOnWrite() default false;
    boolean eternal() default false;
    int maxElementsInMemory() default 500;
    int maxElementsOnDisk() default 0;
    EvictionPolicy memoryStoreEvictionPolicy() default EvictionPolicy.LRU;
    boolean overflowToDisk() default true;
    int timeToIdleSeconds() default 300;
    int timeToLiveSeconds() default 300;
    int diskExpiryThreadIntervalSeconds() default 3600;
    int diskSpoolBufferSizeMB() default 50;

    /**
     * This is a ehcache setting. It seems not to work?
     */
    int blockingTimeout() default 10000;
}
