package nl.vpro.magnolia.jsr107;

import java.io.*;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Michiel Meeuwissen
 * @since 1.2
 */
@SuppressWarnings("unchecked")
public class CacheValueTest {

    @Test
    public void serialize() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        CacheValue<String> value = new CacheValue<>("hoi");
        out.writeObject(value);
        out.close();

        assertEquals("hoi", value.orNull());

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        CacheValue<String> deserialized = (CacheValue<String>) in.readObject();
        in.close();

        assertEquals("hoi", deserialized.orNull());

    }


    @Test
    public void serializeNull() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        CacheValue<String> value = new CacheValue<>(null);
        out.writeObject(value);
        out.close();

        assertNull(value.orNull());

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        CacheValue<String> deserialized = (CacheValue<String>) in.readObject();
        in.close();

        assertNull(deserialized.orNull());

    }


    @Test
    public void serializeOptional() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        CacheValue<Optional<String>> value = new CacheValue<>(Optional.of("bla"));
        out.writeObject(value);
        out.close();

        assertEquals("bla", value.orNull().get());

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        CacheValue<Optional<String>> deserialized = (CacheValue<Optional<String>>) in.readObject();
        in.close();

        assertEquals("bla", deserialized.orNull().get());

    }

    @Test
    public void serializeEmptyOptional() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        CacheValue<Optional<String>> value = new CacheValue<>(Optional.empty());
        out.writeObject(value);
        out.close();

        assertNull(value.orNull().orElse(null));

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        CacheValue<Optional<String>> deserialized = (CacheValue<Optional<String>>) in.readObject();
        in.close();

        assertNull(deserialized.orNull().orElse(null));

    }
}
