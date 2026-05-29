package dev.blancocl.api.skin;

import java.util.Objects;

/**
 * Immutable Mojang texture payload (base-64 value + signature).
 * {@link #name()} carries the original lookup token (username / "custom") for diagnostics.
 */
public record Skin(String name, String value, String signature, SkinSource source, long fetchedAtMillis) {

    public Skin {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(signature, "signature");
        Objects.requireNonNull(source, "source");
        if (name == null) name = "anonymous";
    }
}
