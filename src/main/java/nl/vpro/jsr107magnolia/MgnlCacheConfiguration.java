package nl.vpro.jsr107magnolia;

import javax.cache.configuration.Configuration;

/**
 * @author Michiel Meeuwissen
 * @since ...
 */
public class MgnlCacheConfiguration implements Configuration<Object, Object> {

	static MgnlCacheConfiguration instance = new MgnlCacheConfiguration();

	@Override
	public Class<Object> getKeyType() {
		return Object.class;

	}

	@Override
	public Class<Object> getValueType() {
		return Object.class;

	}

	@Override
	public boolean isStoreByValue() {
		return true;
	}
}
