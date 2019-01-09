package nl.vpro.magnolia.jsr107;

import info.magnolia.module.delta.Task;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheResult;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.4
 */
@Slf4j
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

        @CacheResult
        protected String methodThree(String argument) {
            return "";
        }

        @CacheResult(cacheName = "staticMethodCache")
        @DefaultCacheSettings(overflowToDisk = false)
        protected static String staticMethod() {
            return "";
        }


        @CacheResult(cacheName = "exceptionMethodCache", exceptionCacheName = "exceptionMethodExceptionCache")
        @Defaults(
            cacheSettings = @DefaultCacheSettings(overflowToDisk = true),
            exceptionCacheSettings = @DefaultCacheSettings(overflowToDisk = false)

        )
        protected static String exceptionMethod() {
            return "";
        }


        public String methodFour() {
            return "";
        }
    }

    public static class TestBean2  {

        @CacheResult(cacheName = "exceptionMethodCache", exceptionCacheName = "exceptionMethodExceptionCache")
        @Defaults(
            cacheSettings = @DefaultCacheSettings(overflowToDisk = false),
            exceptionCacheSettings = @DefaultCacheSettings(overflowToDisk = false)
        )
        protected static String exceptionMethod() {
            return "";
        }


    }
    @CacheDefaults(cacheName = "defaultCacheName")
    public static class TestBeanWithCacheDefaults {

        @CacheResult
        protected String methodOne() {
            return "foobar";
        }



    }

    @Test
    public void createConfigurationTasks() {
        List<CreateCacheConfigurationTask> tasks = CreateConfigurationTasks.createConfigurationTasks(TestBean.class);

        for (Task task : tasks) {
            log.info("{} {}", task.getClass(), task);

        }
        assertThat(tasks).hasSize(7);
        assertThat(tasks.get(5).getNodeName()).isEqualTo("nl.vpro.magnolia.jsr107.CreateConfigurationTasksTest$TestBean.methodThree(java.lang.String)");


        // TODO, can I get a mock install context?
    }


    @Test
    public void createConfigurationTasksWithCacheDefaults() {
        List<CreateCacheConfigurationTask> tasks = CreateConfigurationTasks.createConfigurationTasks(TestBeanWithCacheDefaults.class);

        for (Task task : tasks) {
            log.info("{} {}", task.getClass(), task);

        }
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getNodeName()).isEqualTo("defaultCacheName");


        // TODO, can I get a mock install context?
    }


    @Test
    public void createConfigurationTasks2() {
        List<CreateCacheConfigurationTask> tasks = CreateConfigurationTasks.createConfigurationTasks(TestBean2.class);
        System.out.println(tasks);
        CreateCacheConfigurationTask task = (CreateCacheConfigurationTask) tasks.get(0);
        assertThat(task.getCacheSettings().isOverflowToDisk()).isFalse();
    }
}
