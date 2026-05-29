package dev.blancocl.util;

import java.util.Optional;
import java.util.function.Function;

/** Tiny sum type so command/service layers can return ok/err without exceptions. */
public sealed interface Result<T> permits Result.Ok, Result.Err {

    static <T> Result<T> ok(T value) { return new Ok<>(value); }
    static <T> Result<T> err(String message) { return new Err<>(message); }

    boolean isOk();
    Optional<T> value();
    Optional<String> error();

    default <R> Result<R> map(Function<T, R> fn) {
        return switch (this) {
            case Ok<T> ok  -> ok(fn.apply(ok.rawValue));
            case Err<T> e  -> err(e.message);
        };
    }

    record Ok<T>(T rawValue) implements Result<T> {
        @Override public boolean isOk() { return true; }
        @Override public Optional<T> value() { return Optional.of(rawValue); }
        @Override public Optional<String> error() { return Optional.empty(); }
    }

    record Err<T>(String message) implements Result<T> {
        @Override public boolean isOk() { return false; }
        @Override public Optional<T> value() { return Optional.empty(); }
        @Override public Optional<String> error() { return Optional.of(message); }
    }
}
