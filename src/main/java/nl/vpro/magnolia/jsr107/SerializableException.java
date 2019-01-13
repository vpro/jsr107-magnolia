package nl.vpro.magnolia.jsr107;

import java.io.Serializable;

/**
 * A {@link RuntimeException} that is also {@link Serializable} and hence can be safely used in caches that wants to be serialized (e.g. to disk).
 * @author Michiel Meeuwissen
 * @since 1.13
 */
public class SerializableException extends RuntimeException implements Serializable {
    private static final Long serialVersionUID = 0L;

    public SerializableException(String message) {
        super(message);
    }
}
