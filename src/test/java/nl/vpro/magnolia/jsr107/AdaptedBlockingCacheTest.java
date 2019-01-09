package nl.vpro.magnolia.jsr107;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import nl.vpro.magnolia.jsr107.mock.MockCacheFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Michiel Meeuwissen
 * @since 1.6
 */
public class AdaptedBlockingCacheTest {

	private AdaptedCache<String, String> cache;

    @Before
	public void init() {
        cache = new AdaptedCache<>(new MockCacheFactory(true).getCache("test"), null, null);


	}
	@Test
	public void get() {
		assertThat(cache.get("bla")).isNull();
        cache.put("bla", "foo");
        assertThat(cache.get("bla")).isEqualTo("foo");

	}

	@Test
	public void getAll() {
        cache.put("bla", "foo");
        cache.put("bloe", "bar");
        cache.put("null", null);

        assertThat(cache.getAll(new HashSet<>(Arrays.asList("bla")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"));
        assertThat(cache.getAll(new HashSet<>(Arrays.asList("bla", "bloe", "blie")))).containsOnly(new AbstractMap.SimpleEntry<>("bla", "foo"), new AbstractMap.SimpleEntry<>("bloe", "bar"));
        assertThat(cache.getAll(new HashSet<>(Arrays.asList("bla", "bloe", "null")))).containsOnly(
            new AbstractMap.SimpleEntry<>("bla", "foo"),
            new AbstractMap.SimpleEntry<>("bloe", "bar"),
            new AbstractMap.SimpleEntry<>("null", null)
        );
	}

	@Test
	public void containsKey() {
        cache.put("bla", "foo");
        cache.put("null", null);


        assertThat(cache.containsKey("bla")).isTrue();
        assertThat(cache.containsKey("bloe")).isFalse();
        assertThat(cache.containsKey("null")).isTrue();
	}

	@Test
	public void loadAll() {
        // Not supported.
	}

	@Test
	public void put() {
        cache.put("bla", "foo");
        // Test e.g. in containsKey
	}

	@Test
	public void getAndPut() {
        assertThat(cache.getAndPut("bla", "foo")).isNull();
        assertThat(cache.getAndPut("bla", "bar")).isEqualTo("foo");
	}

	@Test
	public void putAll() {
	    Map<String, String> map = new HashMap<>();
        map.put("bla", "foo");
        map.put("blie", "bar");
	    cache.putAll(map);

        assertThat(cache.get("bla")).isEqualTo("foo");
        assertThat(cache.get("blie")).isEqualTo("bar");
        assertThat(cache).hasSize(2);

	}

	@Test
	public void putIfAbsent() {
        assertThat(cache.putIfAbsent("bla", "foo")).isTrue();
        assertThat(cache.putIfAbsent("bla", "bar")).isFalse();

        assertThat(cache.get("bla")).isEqualTo("foo");

	}

	@Test
	public void remove() {
        cache.put("bla", "foo");

        assertThat(cache.remove("bla")).isTrue();
        assertThat(cache.remove("bla")).isFalse();
        assertThat(cache.remove("blie")).isFalse();

        assertThat(cache.containsKey("bla")).isFalse();
	}

	@Test
	public void removeWithValue() {
        cache.put("bla", "foo");

        assertThat(cache.remove("bla", "bar")).isFalse();
        assertThat(cache.containsKey("bla")).isTrue();
        assertThat(cache.remove("bla", "foo")).isTrue();
        assertThat(cache.containsKey("bla")).isFalse();

	}

	@Test
	public void getAndRemove() {
        assertThat(cache.getAndRemove("bla")).isNull();
        cache.put("bla", "foo");
        assertThat(cache.getAndRemove("bla")).isEqualTo("foo");
	}

	@Test
	public void replace() {
        assertThat(cache.replace("bla", "bar")).isFalse();
        assertThat(cache.containsKey("bla")).isFalse();
        cache.put("bla", "xx");
        assertThat(cache.replace("bla", "foo")).isTrue();
        assertThat(cache.get("bla")).isEqualTo("foo");


	}

	@Test
	public void replaceWithValue() {
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
	public void getAndReplace() {
        assertThat(cache.getAndReplace("bla", "bar")).isNull();
        assertThat(cache).hasSize(0);
        cache.put("bla", "foo");
        assertThat(cache.getAndReplace("bla", "bar")).isEqualTo("foo");
        assertThat(cache).hasSize(1);
        assertThat(cache.get("bla")).isEqualTo("bar");

	}

	@Test
	public void removeAll() {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        cache.put("null", null);
        assertThat(cache).hasSize(3);
        cache.removeAll();
        assertThat(cache).isEmpty();

	}

	@Test
	public void removeAllWithKeys() {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        cache.put("null", null);

        assertThat(cache).hasSize(3);
        cache.removeAll(new HashSet<>(Arrays.asList("bla")));
        assertThat(cache).hasSize(2);
	}

	@Test
	public void clear() {
        cache.put("bla", "foo");
        cache.put("blie", "bar");
        assertThat(cache).hasSize(2);
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
