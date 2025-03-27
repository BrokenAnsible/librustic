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
            assertEquals(testValue, ((Result.Ok<String>) result).value());
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
            assertNull(((Result.Ok<String>) result).value());
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
            assertTrue(result instanceof Result.EmptyOk);
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
            assertSame(exception, ((Result.Error<IOException, String>) result).exception());
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
            assertNull(((Result.Error<Exception, String>) result).exception());
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
                case Result.Ok<String> ok -> ok.value();
                case Result.Error<?, String> error -> "Error: " + error.exception().getMessage();
                case Result.EmptyOk<String> empty -> "Empty";
            };

            assertEquals("test", value);
        }

        @Test
        @DisplayName("Pattern matching should work with Error")
        void patternMatchingShouldWorkWithError() {
            Result<String> result = Result.error(new IllegalArgumentException("Invalid argument"));

            String value = switch (result) {
                case Result.Ok<String> ok -> ok.value();
                case Result.Error<?, String> error -> "Error: " + error.exception().getMessage();
                case Result.EmptyOk<String> empty -> "Empty";
            };

            assertEquals("Error: Invalid argument", value);
        }

        @Test
        @DisplayName("Pattern matching should work with EmptyOk")
        void patternMatchingShouldWorkWithEmptyOk() {
            Result<Void> result = Result.ok();

            String value = switch (result) {
                case Result.Ok<Void> ok -> "Ok with value";
                case Result.Error<?, Void> error -> "Error: " + error.exception().getMessage();
                case Result.EmptyOk<Void> empty -> "Empty";
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
            Integer intValue = ((Result.Ok<Integer>) intResult).value();
            String stringValue = ((Result.Ok<String>) stringResult).value();

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
            if (ioErrorResult instanceof Result.Error<?, ?> error) {
                assertEquals("IO Error", error.exception().getMessage());
            } else {
                fail("Result should be an Error");
            }
        }
    }

    @Nested
    @DisplayName("Map and FlatMap Tests")
    class MapAndFlatMapTests {
        @Test
        @DisplayName("map should transform Ok value")
        void mapShouldTransformOkValue() {
            Result<String> result = Result.ok("42");
            Result<Integer> mapped = result.map(Integer::parseInt);

            assertTrue(mapped.isOk());
            assertEquals(Optional.of(42), mapped.getValue());
        }

        @Test
        @DisplayName("map should handle null transformation result")
        void mapShouldHandleNullTransformationResult() {
            Result<String> result = Result.ok("test");
            Result<String> mapped = result.map(str -> null);

            assertTrue(mapped.isOk());
            assertTrue(mapped.getValue().isEmpty());
            assertFalse(mapped.isVoidOk());
        }

        @Test
        @DisplayName("map should preserve Error state")
        void mapShouldPreserveErrorState() {
            Exception exception = new IllegalArgumentException("test error");
            Result<String> result = Result.error(exception);
            Result<Integer> mapped = result.map(Integer::parseInt);

            assertTrue(mapped.isError());
            assertEquals(Optional.of(exception), mapped.getException());
        }

        @Test
        @DisplayName("map should handle EmptyOk")
        void mapShouldHandleEmptyOk() {
            Result<String> result = Result.ok();
            Result<Integer> mapped = result.map(Integer::parseInt);

            assertTrue(mapped.isVoidOk());
            assertTrue(mapped.getValue().isEmpty());
        }

        @Test
        @DisplayName("flatMap should chain Ok operations")
        void flatMapShouldChainOkOperations() {
            Result<String> result = Result.ok("42");
            Result<Integer> flatMapped = result.flatMap(str -> {
                try {
                    return Result.ok(Integer.parseInt(str));
                } catch (NumberFormatException e) {
                    return Result.error(e);
                }
            });

            assertTrue(flatMapped.isOk());
            assertEquals(Optional.of(42), flatMapped.getValue());
        }

        @Test
        @DisplayName("flatMap should handle mapper returning Error")
        void flatMapShouldHandleMapperReturningError() {
            Result<String> result = Result.ok("not a number");
            Result<Integer> flatMapped = result.flatMap(str -> {
                try {
                    return Result.ok(Integer.parseInt(str));
                } catch (NumberFormatException e) {
                    return Result.error(e);
                }
            });

            assertTrue(flatMapped.isError());
            assertTrue(flatMapped.getException().get() instanceof NumberFormatException);
        }

        @Test
        @DisplayName("flatMap should preserve original Error state")
        void flatMapShouldPreserveOriginalErrorState() {
            Exception exception = new IllegalArgumentException("original error");
            Result<String> result = Result.error(exception);
            Result<Integer> flatMapped = result.flatMap(str -> Result.ok(Integer.parseInt(str)));

            assertTrue(flatMapped.isError());
            assertEquals(Optional.of(exception), flatMapped.getException());
        }

        @Test
        @DisplayName("flatMap should handle EmptyOk")
        void flatMapShouldHandleEmptyOk() {
            Result<String> result = Result.ok();
            Result<Integer> flatMapped = result.flatMap(str -> Result.ok(Integer.parseInt(str)));

            assertTrue(flatMapped.isVoidOk());
            assertTrue(flatMapped.getValue().isEmpty());
        }

        @Test
        @DisplayName("flatMap should handle mapper returning EmptyOk")
        void flatMapShouldHandleMapperReturningEmptyOk() {
            Result<String> result = Result.ok("test");
            Result<Integer> flatMapped = result.flatMap(str -> Result.ok());

            assertTrue(flatMapped.isVoidOk());
            assertTrue(flatMapped.getValue().isEmpty());
        }

        @Test
        @DisplayName("map and flatMap should be chainable")
        void mapAndFlatMapShouldBeChainable() {
            Result<String> result = Result.ok("42");
            Result<String> chained = result
                    .map(Integer::parseInt)
                    .flatMap(num -> Result.ok(String.valueOf(num * 2)))
                    .map(String::toUpperCase);

            assertTrue(chained.isOk());
            assertEquals(Optional.of("84"), chained.getValue());
        }
    }

    @Nested
    @DisplayName("Elegant Pattern Matching Usage Tests")
    class ElegantPatternMatchingTests {

        private Result<Integer> divideSafely(int a, int b) {
            try {
                if (b == 0) {
                    return Result.error(new ArithmeticException("Division by zero"));
                }
                return Result.ok(a / b);
            } catch (Exception e) {
                return Result.error(e);
            }
        }

        @Test
        @DisplayName("Pattern matching should provide elegant success handling")
        void patternMatchingShouldProvideElegantSuccessHandling() {
            Result<Integer> result = divideSafely(10, 2);

            String message = switch (result) {
                case Result.Ok<Integer> ok -> "Success: " + ok.value();
                case Result.Error<?, Integer> error -> "Failed: " + error.exception().getMessage();
                case Result.EmptyOk<Integer> empty -> "No result needed";
            };

            assertEquals("Success: 5", message);
        }

        @Test
        @DisplayName("Pattern matching should provide elegant error handling")
        void patternMatchingShouldProvideElegantErrorHandling() {
            Result<Integer> result = divideSafely(10, 0);

            String message = switch (result) {
                case Result.Ok<Integer> ok -> "Success: " + ok.value();
                case Result.Error<?, Integer> error -> "Failed: " + error.exception().getMessage();
                case Result.EmptyOk<Integer> empty -> "No result needed";
            };

            assertEquals("Failed: Division by zero", message);
        }

        @Test
        @DisplayName("Pattern matching should support complex transformations")
        void patternMatchingShouldSupportComplexTransformations() {
            Result<Integer> result = divideSafely(10, 2)
                    .map(value -> value * 2);

            Integer finalValue = switch (result) {
                case Result.Ok<Integer> ok -> ok.value() + 1;
                case Result.Error<?, Integer> error -> -1;
                case Result.EmptyOk<Integer> empty -> 0;
            };

            assertEquals(11, finalValue); // (10/2 * 2) + 1 = 11
        }

        @Test
        @DisplayName("Pattern matching should handle chained operations")
        void patternMatchingShouldHandleChainedOperations() {
            Result<Integer> result = divideSafely(10, 2)
                    .flatMap(value -> divideSafely(value, 2));

            String message = switch (result) {
                case Result.Ok<Integer> ok -> String.format("Chain succeeded with: %d", ok.value());
                case Result.Error<?, Integer> error -> String.format("Chain failed with: %s", error.exception().getMessage());
                case Result.EmptyOk<Integer> empty -> "Chain produced no result";
            };

            assertEquals("Chain succeeded with: 2", message); // (10/2)/2 = 2
        }

        @Test
        @DisplayName("Pattern matching should support type conversions")
        void patternMatchingShouldSupportTypeConversions() {
            Result<Integer> result = divideSafely(10, 2);

            Result<String> stringResult = result.map(String::valueOf);

            String message = switch (stringResult) {
                case Result.Ok<String> ok -> "Converted to string: " + ok.value();
                case Result.Error<?, String> error -> "Conversion failed: " + error.exception().getMessage();
                case Result.EmptyOk<String> empty -> "Nothing to convert";
            };

            assertEquals("Converted to string: 5", message);
        }

        @Test
        @DisplayName("Pattern matching should support default values")
        void patternMatchingShouldSupportDefaultValues() {
            Result<Integer> result = divideSafely(10, 0);

            int finalValue = switch (result) {
                case Result.Ok<Integer> ok -> ok.value();
                case Result.Error<?, Integer> error -> 42; // Default value on error
                case Result.EmptyOk<Integer> empty -> 0;   // Default value for empty
            };

            assertEquals(42, finalValue);
        }
    }
}
