package nl.vpro.magnolia.jsr107;

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class DefaultCacheSettingsTest {

    @Test
    public void test() {
        for (Method m : DefaultCacheSettings.class.getMethods()) {
            System.out.println(m);
        }

    }
}
