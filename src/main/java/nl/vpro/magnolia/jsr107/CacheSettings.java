package nl.vpro.magnolia.jsr107;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(builderMethodName = "_builder")
@Slf4j
public class CacheSettings {

    public static CacheSettings of(DefaultCacheSettings defaults) {
        CacheSettingsBuilder builder = CacheSettings._builder();
        invoke(builder, defaults);
        return builder.build();
    }

    public static CacheSettingsBuilder builder() {
        CacheSettingsBuilder builder = _builder();
        invoke(builder, null);
        return builder;
    }

    private static void invoke(CacheSettingsBuilder builder, DefaultCacheSettings defaults) {
        for (Method m : DefaultCacheSettings.class.getDeclaredMethods()) {
            try {
                Method tm = builder.getClass().getMethod(
                    m.getName(), m.getReturnType());
                Object value;
                if (defaults == null) {
                    value = m.getDefaultValue();
                } else {
                    value = m.invoke(defaults);
                }
                tm.invoke(builder, value);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    boolean copyOnRead;
    boolean copyOnWrite;
    boolean eternal;
    int maxElementsInMemory;
    String memoryStoreEvictionPolicy;
    boolean overflowToDisk;
    int timeToIdleSeconds;
    int timeToLiveSeconds;
    int diskExpiryThreadIntervalSeconds;
    int diskSpoolBufferSizeMB;
    int blockingTimeout;
}
