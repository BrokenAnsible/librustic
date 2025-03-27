# LibRustic Result Type

A zero-dependency, lightweight Java library that brings Rust-inspired error handling to your Java applications. Say goodbye to exception-driven control flow and hello to more predictable, type-safe error handling.

## Why LibRustic?

- **Type-Safe Error Handling**: Stop throwing exceptions for control flow. Use a proper type system to handle both success and failure cases.
- **Null-Safe**: Built-in Optional support means no more NPEs in your error handling code.
- **Pattern Matching Ready**: Fully supports Java's pattern matching switch expressions for elegant error handling.
- **Production Ready**: Thoroughly tested with comprehensive test coverage and real-world usage examples.
- **Zero Dependencies**: Just pure Java. No external libraries required.

## Quick Start

```java
// Instead of throwing exceptions
public int divideDangerous(int a, int b) throws ArithmeticException {
    return a / b;  // Could throw!
}

// Use Result type for safe operations
public Result<Integer> divideSafely(int a, int b) {
    try {
        if (b == 0) {
            return Result.error(new ArithmeticException("Division by zero"));
        }
        return Result.ok(a / b);
    } catch (Exception e) {
        return Result.error(e);
    }
}

// Use pattern matching for elegant handling
Result<Integer> result = divideSafely(10, 2);
String message = switch (result) {
    case Result.Ok<Integer> ok -> "Result: " + ok.value();
    case Result.Error<?, Integer> error -> "Error: " + error.exception().getMessage();
    case Result.EmptyOk<Integer> empty -> "No result";
};
```

## Features

### Three Result Types

- `Ok<T>`: Represents successful operations with a value
- `EmptyOk<T>`: Represents successful void operations
- `Error<E extends Exception, T>`: Represents failed operations with an exception

### Functional Programming Support

Chain operations with `map` and `flatMap`:

```java
Result<Integer> result = Result.ok("42")
    .map(Integer::parseInt)                    // Transform value
    .flatMap(num -> divideSafely(num, 2))     // Chain operations that might fail
    .map(String::valueOf);                     // Transform again
```

### Modern Java Features

Takes full advantage of Java's modern features:

- Sealed interfaces for type safety
- Pattern matching for elegant error handling
- Optional for null safety
- Functional interfaces for transformation

## Installation

```xml
<dependency>
    <groupId>io.naiant</groupId>
    <artifactId>librustic</artifactId>
    <version>1.0.0</version>
</dependency>
```

## When to Use LibRustic

- **API Development**: Return Result types instead of throwing exceptions for predictable error handling
- **Data Processing**: Chain transformations safely with proper error propagation
- **Service Layer**: Implement robust error handling without try-catch blocks everywhere
- **Input Validation**: Handle invalid inputs elegantly without exception overhead

## Real World Example

```java
public class UserService {
    public Result<User> createUser(String email, String password) {
        return validateEmail(email)
            .flatMap(validEmail -> validatePassword(password)
            .flatMap(validPassword -> hashPassword(password))
            .flatMap(hashedPassword -> saveUser(validEmail, hashedPassword)));
    }
}
```

## Performance

Using Result types instead of exceptions can lead to better performance in error cases, as exceptions are not designed for control flow. The Result type allows for:

- Predictable performance in both success and error cases
- No stack trace generation overhead
- Better memory usage patterns
- More efficient error propagation

## License

MIT License - See [LICENSE](LICENSE) for details.

---

Made with ♥ by Naiant Technologies