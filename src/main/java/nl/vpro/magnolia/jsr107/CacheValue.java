package nl.vpro.magnolia.jsr107;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Optional;

/**
 * Makes it possible to store null's in magnolia caches. Also makes it possible to store Optional's (while remaining Serializable)
 * @author Michiel Meeuwissen
 * @since 1.2
 */
@Slf4j
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
        } else if (value instanceof Throwable) {
            try {
                out.writeObject(value);
            } catch(NotSerializableException nse) {
                log.warn(nse.getClass() + " " + nse.getMessage());
                out.writeObject(new SerializableException(nse.getMessage()));
            }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheValue<?> that = (CacheValue<?>) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
