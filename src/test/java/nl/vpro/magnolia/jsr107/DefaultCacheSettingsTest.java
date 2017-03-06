package nl.vpro.magnolia.jsr107;

import java.lang.reflect.Method;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Michiel Meeuwissen
 * @since 1.5
 */

public class DefaultCacheSettingsTest {

    public static class A {
        @DefaultCacheSettings(
            copyOnRead = true
        )
        public String test1() {
            return "a";
        }

        @DefaultCacheSettings(
            copyOnRead = false
        )
        public String test2() {
            return "a";
        }
    }

    @Test
    public void test() {
        for (Method m : DefaultCacheSettings.class.getMethods()) {
            System.out.println(m);
        }

    }
    @Test
    public void testOf() throws NoSuchMethodException {
        assertTrue(CacheSettings.of(A.class.getMethod("test1").getAnnotation(DefaultCacheSettings.class)).isCopyOnRead());
        assertFalse(CacheSettings.of(A.class.getMethod("test2").getAnnotation(DefaultCacheSettings.class)).isCopyOnRead());
    }
}
