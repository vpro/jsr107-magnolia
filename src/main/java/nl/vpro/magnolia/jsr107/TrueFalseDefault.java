package nl.vpro.magnolia.jsr107;

import java.util.Optional;
import java.util.function.Supplier;

public enum TrueFalseDefault implements Supplier<Optional<Boolean>> {
    TRUE(true),
    FALSE(false),
    DEFAULT(null);

    private final Boolean b;

    TrueFalseDefault(Boolean b) {
        this.b = b;
    }

    @Override
    public Optional<Boolean> get() {
        return Optional.ofNullable(b);
    }

    public boolean orElse(boolean result) {
        return get().orElse(result);
    }
}
