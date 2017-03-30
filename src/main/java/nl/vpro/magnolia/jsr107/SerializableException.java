package nl.vpro.magnolia.jsr107;

import java.io.Serializable;

/**
 * @author Michiel Meeuwissen
 * @since 1.13
 */
public class SerializableException extends RuntimeException implements Serializable {
    private static final Long serialVersionUID = 0L;

    public SerializableException(String message) {
        super(message);
    }
}
