package nl.vpro.magnolia.jsr107;

import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.List;

import javax.cache.annotation.CacheResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Michiel Meeuwissen
 * @since 1.4
 */
public class CreateConfigurationTasksTest {

    public static class AbstractBean {
        @CacheResult(cacheName = "methodZeroCache")
        public static String methodZero() {
            return "";
        }
    }

    public static class TestBean extends AbstractBean {
        @CacheResult(cacheName = "methodOneCache")
        public String methodOne() {
            return "";
        }

        @CacheResult(cacheName = "methodTwoCache")
        @DefaultCacheSettings(overflowToDisk = false)
        protected String methodTwo() {
            return "";
        }

        @CacheResult(cacheName = "staticMethodCache")
        @DefaultCacheSettings(overflowToDisk = false)
        protected static String staticMethod() {
            return "";
        }


        public String methodThree() {
            return "";
        }
    }

    @Test
    public void createConfigurationTasks() throws TaskExecutionException {
        List<Task> tasks = CreateConfigurationTasks.createConfigurationTasks(TestBean.class);
        assertEquals(4, tasks.size());
        // TODO, can I get a mock install context?
    }

}
