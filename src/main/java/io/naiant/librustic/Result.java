package io.naiant.librustic;

import java.util.Optional;

/**
 * A type that represents either success ({@link Ok} or {@link EmptyOk}) or failure ({@link Error}).
 * <p>
 * This interface is inspired by Rust's Result type and provides a way to handle operations
 * that might fail without using exceptions for control flow.
 * <p>
 * The {@code Result<T>} type is a sealed interface with three implementations:
 * <ul>
 *   <li>{@link Ok} - Represents a successful operation with a value of type T</li>
 *   <li>{@link EmptyOk} - Represents a successful operation with no value (similar to void)</li>
 *   <li>{@link Error} - Represents a failed operation with an associated exception</li>
 * </ul>
 *
 * @param <T> The type of the value in case of success
 */
public sealed interface Result<T> permits Error, Ok, EmptyOk {

    /**
     * Creates a new successful {@code Result} with the provided value.
     *
     * @param <T> The type of the value
     * @param value The value to be wrapped (can be null)
     * @return A new {@link Ok} instance containing the provided value
     */
    static <T> Result<T> ok(T value) {
        return new Ok<>(value);
    }

    /**
     * Creates a new successful {@code Result} with no value.
     * <p>
     * This is useful for representing successful void operations.
     *
     * @param <T> The type parameter (typically Void)
     * @return A new {@link EmptyOk} instance
     */
    static <T> Result<T> ok() {
        return new EmptyOk<>();
    }

    /**
     * Creates a new error {@code Result} with the provided exception.
     *
     * @param <X> The inferred result type
     * @param e The exception that caused the error (can be null)
     * @return A new {@link Error} instance containing the provided exception
     */
    static <X> Result<X> error(Exception e) {
        return new Error<>(e);
    }

    /**
     * Checks if this {@code Result} represents a success.
     * <p>
     * Returns true for both {@link Ok} and {@link EmptyOk}, false for {@link Error}.
     *
     * @return {@code true} if this is a successful result, {@code false} otherwise
     */
    default boolean isOk() {
        return switch (this) {
            case Error<?,?> ignored -> false;
            case Ok<?> ignored -> true;
            case EmptyOk<?> ignored -> true;
        };
    }

    /**
     * Checks if this {@code Result} is an {@link EmptyOk} (a successful void result).
     * <p>
     * Returns true only for {@link EmptyOk}, false for both {@link Ok} and {@link Error}.
     *
     * @return {@code true} if this is an {@link EmptyOk}, {@code false} otherwise
     */
    default boolean isVoidOk() {
        return switch (this) {
            case Error<?,?> ignored -> false;
            case Ok<?> ignored -> false;
            case EmptyOk<?> ignored -> true;
        };
    }

    /**
     * Checks if this {@code Result} represents an error.
     * <p>
     * Returns true for {@link Error}, false for both {@link Ok} and {@link EmptyOk}.
     *
     * @return {@code true} if this is an error result, {@code false} otherwise
     */
    default boolean isError() {
        return switch (this) {
            case Error<?,?> ignored -> true;
            case Ok<?> ignored -> false;
            case EmptyOk<?> ignored -> false;
        };
    }

    /**
     * Gets the value contained in this {@code Result} if it's an {@link Ok} instance.
     * <p>
     * Returns an empty {@link Optional} if this is an {@link Error} or {@link EmptyOk},
     * or if this is an {@link Ok} with a null value.
     *
     * @return An {@link Optional} containing the value if present
     */
    default Optional<T> getValue() {
        return isOk() && !isVoidOk() ? Optional.ofNullable(((Ok<T>) this).value()) : Optional.empty();
    }

    /**
     * Gets the exception contained in this {@code Result} if it's an {@link Error} instance.
     * <p>
     * Returns an empty {@link Optional} if this is an {@link Ok} or {@link EmptyOk},
     * or if this is an {@link Error} with a null exception.
     *
     * @return An {@link Optional} containing the exception if present
     */
    default Optional<? extends Exception> getException() {
        return isError() ? Optional.ofNullable(((Error<? extends Exception, T>) this).exception()) : Optional.empty();
    }
}