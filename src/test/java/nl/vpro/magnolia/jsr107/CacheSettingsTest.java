package nl.vpro.magnolia.jsr107;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Michiel Meeuwissen
 * @since 1.11
 */
public class CacheSettingsTest {


    @Test
    public void test() {
        assertEquals(500, CacheSettings.builder().build().getMaxElementsInMemory());
    }

}
