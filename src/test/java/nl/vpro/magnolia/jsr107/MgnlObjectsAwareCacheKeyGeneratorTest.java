package nl.vpro.magnolia.jsr107;

import info.magnolia.test.mock.jcr.MockNode;
import lombok.extern.log4j.Log4j2;

import java.util.Iterator;

import javax.cache.annotation.CacheResult;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Michiel Meeuwissen
 * @since 1.13
 */
@Log4j2
public class MgnlObjectsAwareCacheKeyGeneratorTest extends AbstractJSR107Test {

    public static class TestClass {

        @CacheResult(cacheKeyGenerator = MgnlObjectsAwareCacheKeyGenerator.class)
        public String getValue(Node node) throws RepositoryException {
            return node.getPath();
        }
    }
    TestClass instance;

    @BeforeEach
    public void setup() {
        instance = injector.getInstance(TestClass.class);
    }

    @Test
    public void test() throws RepositoryException {
        MockNode node = new MockNode("bla");
        assertEquals("/bla", instance.getValue(node));

        Iterator<Object[]> keys = cacheManager.getKeys(TestClass.class, instance, "getValue", Node.class);
        assertTrue(keys.hasNext());
        Object[] key = keys.next();
        assertEquals(1, key.length);
        assertEquals("/bla", key[0]);
        assertFalse(keys.hasNext());

    }

}
