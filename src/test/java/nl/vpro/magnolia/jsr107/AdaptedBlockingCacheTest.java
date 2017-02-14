package nl.vpro.magnolia.jsr107;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.blocking.BlockingCache;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.magnolia.jsr107.mock.EHCacheWrapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.6
 */
public class AdaptedBlockingCacheTest {

	private AdaptedCache<String, String> cache;

    @Before
	public void init() {
        CacheManager cm = CacheManager.create();
        Ehcache ehcache = new net.sf.ehcache.Cache("test", 10, false, true, 1000L, 1000L);
        ;
        ehcache.setCacheManager(cm);
        ehcache.initialise();


        net.sf.ehcache.constructs.blocking.BlockingCache blockingCache = new BlockingCache(ehcache);
        blockingCache.setCacheManager(cm);

        EHCacheWrapper wrapper = new EHCacheWrapper(blockingCache);
        cache = new AdaptedCache<>(wrapper, null, null);


	}
	@Test
	public void get() throws Exception {
		assertThat(cache.get("bla")).isNull();
        cache.put("bla", "foo");
        assertThat(cache.get("bla")).isEqualTo("foo");

	}

	@Test
	public void getAll() throws Exception {
        cache.put("bla", "foo");
        cache.put("bloe", "bar");
        cache.put("null", null);

        assertThat(cache.getAll(new HashSet<>(Arrays.asList("bla")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"));
        assertThat(cache.getAll(new HashSet<>(Arrays.asList("bla", "bloe", "blie")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"), new AbstractMap.SimpleEntry<>("bloe", "bar"));
        assertThat(cache.getAll(new HashSet<>(Arrays.asList("bla", "bloe", "null")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"), new AbstractMap.SimpleEntry<>("bloe", "bar"), new AbstractMap.SimpleEntry<>("null", null));
	}

	@Test
	public void containsKey() throws Exception {
        cache.put("bla", "foo");
        cache.put("null", null);


        assertThat(cache.containsKey("bla")).isTrue();
        assertThat(cache.containsKey("bloe")).isFalse();
        assertThat(cache.containsKey("null")).isTrue();
	}

	@Test
	public void loadAll() throws Exception {
        // Not supported.
	}

	@Test
	public void put() throws Exception {
        cache.put("bla", "foo");
        // Test e.g. in containsKey
	}

	@Test
	public void getAndPut() throws Exception {
        assertThat(cache.getAndPut("bla", "foo")).isNull();
        assertThat(cache.getAndPut("bla", "bar")).isEqualTo("foo");
	}

	@Test
	public void putAll() throws Exception {
	    Map<String, String> map = new HashMap<>();
        map.put("bla", "foo");
        map.put("blie", "bar");
	    cache.putAll(map);

        assertThat(cache.get("bla")).isEqualTo("foo");
        assertThat(cache.get("blie")).isEqualTo("bar");
        assertThat(cache).hasSize(2);

	}

	@Test
	public void putIfAbsent() throws Exception {
        assertThat(cache.putIfAbsent("bla", "foo")).isTrue();
        assertThat(cache.putIfAbsent("bla", "bar")).isFalse();

        assertThat(cache.get("bla")).isEqualTo("foo");

	}

	@Test
	public void remove() throws Exception {
        cache.put("bla", "foo");

        assertThat(cache.remove("bla")).isTrue();
        assertThat(cache.remove("bla")).isFalse();
        assertThat(cache.remove("blie")).isFalse();

        assertThat(cache.containsKey("bla")).isFalse();
	}

	@Test
	public void removeWithValue() throws Exception {
        cache.put("bla", "foo");

        assertThat(cache.remove("bla", "bar")).isFalse();
        assertThat(cache.containsKey("bla")).isTrue();
        assertThat(cache.remove("bla", "foo")).isTrue();
        assertThat(cache.containsKey("bla")).isFalse();

	}

	@Test
	public void getAndRemove() throws Exception {
        assertThat(cache.getAndRemove("bla")).isNull();
        cache.put("bla", "foo");
        assertThat(cache.getAndRemove("bla")).isEqualTo("foo");
	}

	@Test
	public void replace() throws Exception {
        assertThat(cache.replace("bla", "bar")).isFalse();
        assertThat(cache.containsKey("bla")).isFalse();
        cache.put("bla", "xx");
        assertThat(cache.replace("bla", "foo")).isTrue();
        assertThat(cache.get("bla")).isEqualTo("foo");


	}

	@Test
	public void replaceWithValue() throws Exception {
        assertThat(cache.replace("bla", "bar")).isFalse();
        assertThat(cache.containsKey("bla")).isFalse();
        cache.put("bla", "bar");
        assertThat(cache.containsKey("bla")).isTrue();
        assertThat(cache.replace("bla", "bar", "foo")).isTrue();
        assertThat(cache.get("bla")).isEqualTo("foo");
        assertThat(cache.replace("bla", "bar", "xx")).isFalse();
        assertThat(cache.get("bla")).isEqualTo("foo");
	}

	@Test
	public void getAndReplace() throws Exception {
        assertThat(cache.getAndReplace("bla", "bar")).isNull();
        assertThat(cache).hasSize(0);
        cache.put("bla", "foo");
        assertThat(cache.getAndReplace("bla", "bar")).isEqualTo("foo");
        assertThat(cache).hasSize(1);
        assertThat(cache.get("bla")).isEqualTo("bar");

	}

	@Test
	public void removeAll() throws Exception {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        cache.put("null", null);
        assertThat(cache).hasSize(3);
        cache.removeAll();
        assertThat(cache).isEmpty();

	}

	@Test
	public void removeAllWithKeys() throws Exception {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        cache.put("null", null);

        assertThat(cache).hasSize(3);
        cache.removeAll(new HashSet<>(Arrays.asList("bla")));
        assertThat(cache).hasSize(2);
	}

	@Test
	public void clear() throws Exception {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        cache.put("null", null);
        assertThat(cache).hasSize(3);
        cache.clear();
        assertThat(cache).isEmpty();
	}




    @Test
    public void registerCacheEntryListener() {
        // UnsupportedOperationException(

    }

    @Test
    public void deregisterCacheEntryListener() {
        // unsupported
    }

}
