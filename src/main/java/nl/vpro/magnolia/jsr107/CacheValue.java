package nl.vpro.magnolia.jsr107;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Optional;

/**
 * Makes it possible to store null's in magnolia caches. Also makes it possible to store Optional's (while remaining Serializable)
 * @author Michiel Meeuwissen
 * @since 1.2
 */
class CacheValue<V> implements Serializable {

    static <V> CacheValue<V> of(V value) {
        return new CacheValue<>(value);
    }

    private V value;


    CacheValue(V value) {
        this.value = value;
    }

    public Optional<V> toOptional(){
        return Optional.ofNullable(value);
    }

    public V orNull() {
        return toOptional().orElse(null);
    }


    @SuppressWarnings("unchecked")
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (value instanceof Optional) {
            out.writeObject(Optional.class);
            out.writeObject(((Optional) value).orElse(null));
        } else {
            out.writeObject(value);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object i = in.readObject();
        if (Optional.class.equals(i)) {
            Object optionalValue = in.readObject();
            value = (V) Optional.ofNullable(optionalValue);
        } else {
            value = (V) i;
        }

    }
}
