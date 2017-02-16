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
    static <V> CacheValue<V> of(RuntimeException e) {
        return new CacheValue<V>(e);
    }

    private V value;
    private RuntimeException e;

    CacheValue(V value) {
        this.value = value;
    }

    CacheValue(RuntimeException e) {
        this.e = e;
    }

    public Optional<V> toOptional(){
        if (e != null) {
            throw e;
        }
        return Optional.ofNullable(value);
    }

    public V orNull() {
        if (e != null) {
            throw e;
        }
        return toOptional().orElse(null);
    }

    public boolean hasException() {
        return e != null;
    }

    @SuppressWarnings("unchecked")
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (value instanceof Optional) {
            out.writeObject(Optional.class);
            out.writeObject(((Optional) value).orElse(null));
        } else {
            out.writeObject(value);
        }
        out.writeUTF(e == null ? "" : e.getMessage());
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
        String em = in.readUTF();
        e = em.isEmpty() ? null : new RuntimeException(em);

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
