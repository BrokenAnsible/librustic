package io.naiant.librustic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Result Interface Tests")
class ResultTest {

    @Nested
    @DisplayName("Ok Tests")
    class OkTests {
        @Test
        @DisplayName("Ok with value should store and return the value")
        void okWithValueShouldStoreAndReturnValue() {
            String testValue = "test";
            Result<String> result = Result.ok(testValue);

            assertTrue(result.isOk());
            assertFalse(result.isError());
            assertFalse(result.isVoidOk());
            assertEquals(Optional.of(testValue), result.getValue());
            assertTrue(result.getException().isEmpty());
            assertEquals(testValue, ((Ok<String>) result).value());
        }

        @Test
        @DisplayName("Ok with null value should handle null appropriately")
        void okWithNullValueShouldHandleNullAppropriately() {
            Result<String> result = Result.ok(null);

            assertTrue(result.isOk());
            assertFalse(result.isError());
            assertFalse(result.isVoidOk());
            assertEquals(Optional.empty(), result.getValue());
            assertTrue(result.getException().isEmpty());
            assertNull(((Ok<String>) result).value());
        }
    }

    @Nested
    @DisplayName("EmptyOk Tests")
    class EmptyOkTests {
        @Test
        @DisplayName("EmptyOk should represent a successful void result")
        void emptyOkShouldRepresentSuccessfulVoidResult() {
            Result<Void> result = Result.ok();

            assertTrue(result.isOk());
            assertTrue(result.isVoidOk());
            assertFalse(result.isError());
            assertTrue(result.getValue().isEmpty());
            assertTrue(result.getException().isEmpty());
            assertTrue(result instanceof EmptyOk);
        }
    }

    @Nested
    @DisplayName("Error Tests")
    class ErrorTests {
        @Test
        @DisplayName("Error should store and return the exception")
        void errorShouldStoreAndReturnException() {
            IOException exception = new IOException("Test exception");
            Result<String> result = Result.error(exception);

            assertTrue(result.isError());
            assertFalse(result.isOk());
            assertFalse(result.isVoidOk());
            assertTrue(result.getValue().isEmpty());
            assertEquals(Optional.of(exception), result.getException());
            assertSame(exception, ((Error<IOException, String>) result).exception());
        }

        @Test
        @DisplayName("Error with null exception should handle null appropriately")
        void errorWithNullExceptionShouldHandleNullAppropriately() {
            Result<String> result = Result.error(null);

            assertTrue(result.isError());
            assertFalse(result.isOk());
            assertFalse(result.isVoidOk());
            assertTrue(result.getValue().isEmpty());
            assertEquals(Optional.empty(), result.getException());
            assertNull(((Error<Exception, String>) result).exception());
        }
    }

    @Nested
    @DisplayName("Pattern Matching Tests")
    class PatternMatchingTests {
        @Test
        @DisplayName("Pattern matching should work with Ok")
        void patternMatchingShouldWorkWithOk() {
            Result<String> result = Result.ok("test");

            String value = switch (result) {
                case Ok<String> ok -> ok.value();
                case Error<?, String> error -> "Error: " + error.exception().getMessage();
                case EmptyOk<String> empty -> "Empty";
            };

            assertEquals("test", value);
        }

        @Test
        @DisplayName("Pattern matching should work with Error")
        void patternMatchingShouldWorkWithError() {
            Result<String> result = Result.error(new IllegalArgumentException("Invalid argument"));

            String value = switch (result) {
                case Ok<String> ok -> ok.value();
                case Error<?, String> error -> "Error: " + error.exception().getMessage();
                case EmptyOk<String> empty -> "Empty";
            };

            assertEquals("Error: Invalid argument", value);
        }

        @Test
        @DisplayName("Pattern matching should work with EmptyOk")
        void patternMatchingShouldWorkWithEmptyOk() {
            Result<Void> result = Result.ok();

            String value = switch (result) {
                case Ok<Void> ok -> "Ok with value";
                case Error<?, Void> error -> "Error: " + error.exception().getMessage();
                case EmptyOk<Void> empty -> "Empty";
            };

            assertEquals("Empty", value);
        }
    }

    @Nested
    @DisplayName("Real-world Usage Tests")
    class RealWorldUsageTests {
        private Result<Integer> divide(int a, int b) {
            try {
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return Result.ok(a / b);
            } catch (Exception e) {
                return Result.error(e);
            }
        }

        private Result<Void> doSomethingWithoutReturn() {
            // Simulating void operation that returns no value
            return Result.ok();
        }

        @Test
        @DisplayName("Division with valid input should return Ok")
        void divisionWithValidInputShouldReturnOk() {
            Result<Integer> result = divide(10, 2);

            assertTrue(result.isOk());
            assertEquals(Optional.of(5), result.getValue());
        }

        @Test
        @DisplayName("Division by zero should return Error")
        void divisionByZeroShouldReturnError() {
            Result<Integer> result = divide(10, 0);

            assertTrue(result.isError());
            assertTrue(result.getException().isPresent());
            assertTrue(result.getException().get() instanceof ArithmeticException);
            assertEquals("Division by zero", result.getException().get().getMessage());
        }

        @Test
        @DisplayName("Void operation should return EmptyOk")
        void voidOperationShouldReturnEmptyOk() {
            Result<Void> result = doSomethingWithoutReturn();

            assertTrue(result.isOk());
            assertTrue(result.isVoidOk());
        }
    }

    @Nested
    @DisplayName("Type Safety Tests")
    class TypeSafetyTests {
        @Test
        @DisplayName("Result should maintain type safety")
        void resultShouldMaintainTypeSafety() {
            Result<Integer> intResult = Result.ok(42);
            Result<String> stringResult = Result.ok("42");

            // Type checking at compile time
            Integer intValue = ((Ok<Integer>) intResult).value();
            String stringValue = ((Ok<String>) stringResult).value();

            assertEquals(42, intValue);
            assertEquals("42", stringValue);
        }

        @Test
        @DisplayName("Error should maintain exception type safety")
        void errorShouldMaintainExceptionTypeSafety() {
            IOException ioException = new IOException("IO Error");
            IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Illegal Argument");

            Result<String> ioErrorResult = Result.error(ioException);
            Result<Integer> illegalArgErrorResult = Result.error(illegalArgumentException);

            assertTrue(ioErrorResult.getException().get() instanceof IOException);
            assertTrue(illegalArgErrorResult.getException().get() instanceof IllegalArgumentException);

            // Type checking with pattern matching
            if (ioErrorResult instanceof Error<?, ?> error) {
                assertEquals("IO Error", error.exception().getMessage());
            } else {
                fail("Result should be an Error");
            }
        }
    }
}
