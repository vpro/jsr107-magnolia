package nl.vpro.magnolia.jsr107;

import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


/**
 * @author Michiel Meeuwissen
 * @since 1.5
 */
@Log4j2
public class DefaultCacheSettingsTest {

    public static class A {
        @DefaultCacheSettings(
            copyOnRead = true
        )
        public String test1() {
            return "a";
        }

        @DefaultCacheSettings(
            copyOnRead = false,
            eternal = true
        )
        public String test2() {
            return "a";
        }
    }

    @Test
    public void test() {
        for (Method m : DefaultCacheSettings.class.getMethods()) {
            log.info("{}", m);
        }

    }
    @Test
    public void testOf() throws NoSuchMethodException {
        assertThat(
            CacheSettings.of(A.class.getMethod("test1").getAnnotation(DefaultCacheSettings.class)).isCopyOnRead()
        ).isTrue();

        assertThat(
            CacheSettings.of(A.class.getMethod("test1").getAnnotation(DefaultCacheSettings.class)).getMaxElementsInMemory()
        ).isEqualTo(500); // default value

        assertThat(
            CacheSettings.of(A.class.getMethod("test1").getAnnotation(DefaultCacheSettings.class)).getTimeToIdleSeconds()
        ).isEqualTo(300); // default value

        assertThat(
            CacheSettings.of(A.class.getMethod("test2").getAnnotation(DefaultCacheSettings.class)).isCopyOnRead()
        ).isFalse();
    }

    @Test
    public void testEternal() throws NoSuchMethodException {
        assertThat(
            CacheSettings.of(A.class.getMethod("test2").getAnnotation(DefaultCacheSettings.class)).isEternal()
        ).isTrue();
        assertThat(
            CacheSettings.of(A.class.getMethod("test2").getAnnotation(DefaultCacheSettings.class)).getTimeToIdleSeconds()
        ).isNull();

    }
}
