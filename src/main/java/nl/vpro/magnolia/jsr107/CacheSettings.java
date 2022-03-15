package nl.vpro.magnolia.jsr107;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;

import org.apache.commons.lang3.ClassUtils;

import com.google.common.base.MoreObjects;

/**
 * See https://documentation.magnolia-cms.com/display/DOCS60/Ehcache+3+module
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@lombok.Builder(builderClassName = "Builder")
@Slf4j
public class CacheSettings {

    public static CacheSettings of(DefaultCacheSettings defaults) {
        CacheSettings.Builder builder = new CacheSettings.Builder();
        invoke(builder, defaults);
        CacheSettings settings = builder.build();
        boolean change = false;
        if (settings.isEternal()) {
            change = setToNullIfDefault(defaults, "timeToIdleSeconds", builder);
            change |= setToNullIfDefault(defaults, "timeToLiveSeconds", builder);
        }
        if (! settings.isOverflowToDisk()) {
            change |= setToNullIfDefault(defaults, "diskExpiryThreadIntervalSeconds", builder);
            change |= setToNullIfDefault(defaults, "diskSpoolBufferSizeMB", builder);
        }
        if (change) {
            settings = builder.build();
        }
        return settings;
    }
    protected static boolean setToNullIfDefault(DefaultCacheSettings defaults, String methodName, Builder builder) {
        return
            runIfDefault(defaults, methodName, () -> {
                Method method = Arrays.stream(Builder.class.getMethods())
                    .filter(m -> m.getName().equals(methodName)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Not found " + methodName));
                try {
                    method.invoke(builder, (Object) null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    protected static boolean runIfDefault(DefaultCacheSettings defaults, String methodName, Runnable runnable) {
        try {
            Method method =  DefaultCacheSettings.class.getMethod(methodName);
            Object defaultValue = method.getDefaultValue();
            if (Objects.equals(method.invoke(defaults), defaultValue)) {
                runnable.run();
                return true;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static CacheSettings.Builder builder() {
        CacheSettings.Builder builder = new CacheSettings.Builder();
        invoke(builder, null);
        return builder;
    }

    public static class Builder {
        Builder() {
            super();
        }

        @Deprecated
        public CacheSettings.Builder timeToIdle(Duration duration) {
            return timeToIdleSeconds(duration == null ? null : (int) duration.toMillis() / 1000);
        }

        public CacheSettings.Builder timeToLive(Duration duration) {
            return timeToLiveSeconds(duration == null ? null : (int) duration.toMillis() / 1000);
        }

        @Deprecated
        public CacheSettings.Builder diskExpiryThreadInterval(Duration duration) {
            return diskExpiryThreadIntervalSeconds(duration == null ? null : (int) duration.toMillis() / 1000);

        }
        public CacheSettings.Builder eternal(boolean eternal) {
            if (eternal) {
                timeToIdleSeconds(null);
                timeToLiveSeconds(null);
            }
            this.eternal = eternal;
            return this;
        }

        public CacheSettings.Builder blocking(Boolean blocking) {
            this.blocking = blocking != null && blocking;
            return this;
        }

        public CacheSettings.Builder overflowToDisk(boolean overflowToDisk) {
            if (! overflowToDisk) {
                diskExpiryThreadInterval(null);
                diskSpoolBufferSizeMB(null);
            }
            this.overflowToDisk = overflowToDisk;
            return this;
        }
    }

    /**
     * Copies all {@link DefaultCacheSettings} annotation values to a CacheSettings object.
     * Using reflection, considering default values.
     * This way we ensure that {@link CacheSettings} and {@link @DefaultCacheSetting} have effectively the
     * same fields and defaults.
     */
    private static void invoke(CacheSettings.Builder builder, DefaultCacheSettings defaults) {
        for (Method m : DefaultCacheSettings.class.getDeclaredMethods()) {
            try {
                Method tm;
                try {
                    tm = builder.getClass().getMethod(
                        m.getName(), m.getReturnType());
                } catch (NoSuchMethodException nsme) {
                    if (m.getReturnType().isPrimitive()) {
                        tm = builder.getClass().getMethod(
                            m.getName(), ClassUtils.primitiveToWrapper(m.getReturnType())
                        );
                    } else {
                        throw nsme;
                    }
                }
                    Object value;
                    if (defaults == null) {
                    value = m.getDefaultValue();
                } else {
                    value = m.invoke(defaults);
                }
                tm.invoke(builder, value);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    boolean copyOnRead;
    boolean copyOnWrite;
    /**
     * If elements are set to eternal, timeouts are ignored and the element is never expired.
     */
    boolean eternal;
    /**
     * Sets maximum number of objects that will be created in memory. 0 = no limit
     */
    int maxElementsInMemory;

    /**
     * Sets maximum number of objects maintained in the DiskStore. The default value of zero means unlimited.
     */
    @Deprecated
    int maxElementsOnDisk;

    int maxSizeOnDiskMB;
    /**
     * Policy is enforced upon reaching the maxElementsInMemory limit. Available policies:
     Least Recently Used (specified as LRU)
     First In First Out (specified as FIFO)
     Less Frequently Used (specified as LFU)
     */
    @Deprecated
    EvictionPolicy memoryStoreEvictionPolicy;

    /**
     * Permits elements to overflow to disk when the memory store has reached the maxInMemory limit.
     */
    boolean overflowToDisk;

    /**
     * Optional attribute. Sets max idle time between accesses for an element before it expires. Only used if the element is not eternal. A value of 0 means that an Element can idle indefinitely.
     */
    @Deprecated
    Integer timeToIdleSeconds;

    /**
     * Sets lifespan for an element. Only used if the element is not eternal. Optional attribute. A value of 0 means an Element can live for infinity
     */
    Integer timeToLiveSeconds;

    /**
     * Number of seconds between runs of the disk expiry thread.
     */
    @Deprecated
    Integer diskExpiryThreadIntervalSeconds;

    /**
     * Size to allocate to DiskStore for a spool buffer. Writes are made to this area and then asynchronously written to disk. Default: 30MB. Each spool buffer is used only by its cache. If OutOfMemory errors, you may need to lower this value. To improve DiskStore performance consider increasing it. Trace level logging in the DiskStore will show if put back-ups are occurring.
     */
    @Deprecated
    Integer diskSpoolBufferSizeMB;

    /**
     * Instructs Ehcache to wait the specified time in milliseconds before attempting to cache the request. Create the blockingTimeout property in the tree at the same level where the EhCacheFactory class is defined, not inside the defaultCacheConfiguration node.
     *
     * TODO: So this is created at the wrong level now. But this is a bit silly, I'd want to set it <em>per cache</em>
     */
    int blockingTimeout;


    /**
     * Whether the cache should be blocking
     * @since 1.20
     */
    boolean blocking;


    TrueFalseDefault overrideOnUpdate;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .omitNullValues()
            .add("copyOnRead", copyOnRead)
            .add("copyOnWrite", copyOnWrite)
            .add("eternal", eternal)
            .add("maxElementsInMemory", maxElementsInMemory)
            .add("maxElementsOnDisk", maxElementsOnDisk)
            .add("maxSizeOnDiskMB", maxSizeOnDiskMB)
            .add("memoryStoreEvictionPolicy", memoryStoreEvictionPolicy)
            .add("overflowToDisk", overflowToDisk)
            .add("timeToIdleSeconds", timeToIdleSeconds)
            .add("timeToLiveSeconds", timeToLiveSeconds)
            .add("diskExpiryThreadIntervalSeconds", diskExpiryThreadIntervalSeconds)
            .add("diskSpoolBufferSizeMB", diskSpoolBufferSizeMB)
            .add("blockingTimeout", blockingTimeout)
            .add("blocking", blocking)
            .toString();
    }
}
