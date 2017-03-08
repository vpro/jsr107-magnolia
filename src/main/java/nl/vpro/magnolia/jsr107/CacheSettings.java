package nl.vpro.magnolia.jsr107;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;

import org.apache.commons.lang3.ClassUtils;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(builderClassName = "Builder")
@Slf4j
public class CacheSettings {

    public static CacheSettings of(DefaultCacheSettings defaults) {
        CacheSettings.Builder builder = new CacheSettings.Builder();
        invoke(builder, defaults);
        CacheSettings settings = builder.build();
        if (settings.isEternal()) {
            boolean change = false;
            try {
                Integer defaultTimeToIdle = (Integer) DefaultCacheSettings.class.getMethod("timeToIdleSeconds").getDefaultValue();
                if (defaults.timeToIdleSeconds() == defaultTimeToIdle) {
                    change = true;
                    builder.timeToIdleSeconds(null);
                }
                Integer defaultTimeToLive = (Integer) DefaultCacheSettings.class.getMethod("timeToLiveSeconds").getDefaultValue();
                if (defaults.timeToLiveSeconds() == defaultTimeToLive) {
                    change = true;
                    builder.timeToLiveSeconds(null);
                }

            } catch (Exception e){
                throw new RuntimeException(e);
            }
            if (change) {
                settings = builder.build();
            }
        }
        return settings;
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
        public CacheSettings.Builder timeToIdle(Duration duration) {
            return timeToIdleSeconds((int) duration.toMillis() / 1000);
        }

        public CacheSettings.Builder timeToLive(Duration duration) {
            return timeToLiveSeconds((int) duration.toMillis() / 1000);
        }

        public CacheSettings.Builder diskExpiryThreadInterval(Duration duration) {
            return diskExpiryThreadIntervalSeconds((int) duration.toMillis() / 1000);
        }
        public CacheSettings.Builder eternal(boolean eternal) {
            if (eternal) {
                timeToIdleSeconds(null);
                timeToLiveSeconds(null);

            }
            this.eternal = eternal;
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
    boolean eternal;
    int maxElementsInMemory;
    String memoryStoreEvictionPolicy;
    boolean overflowToDisk;
    Integer timeToIdleSeconds;
    Integer timeToLiveSeconds;
    int diskExpiryThreadIntervalSeconds;
    int diskSpoolBufferSizeMB;
    int blockingTimeout;
}
