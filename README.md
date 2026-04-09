# Tail-Call Analyzer

A static analyzer that counts tail-recursive calls in a restricted Scheme function definition.

## Table of Contents

- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [How Tail Position Works](#how-tail-position-works)
- [Design Decisions](#design-decisions)
- [Examples](#examples)
- [Test Suite](#test-suite)

## Quick Start

**Prerequisites:** Java 21+, Maven 3.8+

Build:

```bash
mvn package -q
```

Run with a file:

```bash
java -cp target/classes com.pekara.tailcall.Main src/main/resources/examples/given-example.scm
```

Run with interactive stdin (type your input, then press Ctrl+D to finish):

```bash
java -cp target/classes com.pekara.tailcall.Main
```

Run with piped input:

```bash
echo '(define (f x) (if x (f 1) (f 2)))' | java -cp target/classes com.pekara.tailcall.Main
```

Run tests:

```bash
mvn test
```

## Architecture

```
Source string
    |
    v
 Scanner ──> List<Token>
    |
    v
  Parser ──> Definition (AST)
    |
    v
TailCallAnalyzer ──> int (tail-call count)
```

| File | Role |
|------|------|
| `Scanner.java` | Tokenizer with line/column tracking |
| `Parser.java` | Recursive descent parser producing the AST |
| `TailCallAnalyzer.java` | Walks only tail-position nodes, counts self-calls |
| `Main.java` | CLI entry point, reads from file or stdin |
| `Expression.java` | Sealed interface with 4 record variants (NumberLiteral, Symbol, IfExpression, FunctionCall) |
| `Definition.java` | Top-level function definition record |
| `Token.java` | Token record with type, value, position |
| `TokenType.java` | Enum: LPAREN, RPAREN, NUMBER, SYMBOL, EOF |
| `TailCallAnalyzerException.java` | Single exception for all pipeline stages |

## How Tail Position Works

A call to `f` inside `f`'s own body is a **tail call** only if it is the last thing evaluated before the function returns. The rules:

1. Only the **last expression** in the body is in tail position
2. Both **then** and **else** branches of `if` inherit tail position from their parent
3. The **condition** of `if` is never in tail position
4. **Arguments** to a function call are never in tail position

Since non-tail positions can never contribute tail calls, the analyzer only recurses into tail-position nodes and ignores everything else.

**Traced example:** `(define (f x) (if x (f (f x)) (f x)))`

```
(if x ...)             -- tail position (last body expr)
  condition: x         -- ignored (never tail)
  then: (f (f x))      -- tail position (if-branch inherits)
    outer (f ...) is f  -- TAIL CALL (1)
    arg (f x)           -- ignored (argument, never tail)
  else: (f x)           -- tail position (if-branch inherits)
    (f x) is f          -- TAIL CALL (2)

Result: 2
```

## Design Decisions

`Definition` is a standalone record, **not** a variant of the `Expression` sealed interface. `FunctionCall` holds arguments as `List<Expression>`, so if `Definition` were inside `Expression`, the type system would allow constructing `(f (define ...))`. By keeping `Definition` separate, nested define is impossible to represent in the AST — the compiler enforces it, not a runtime check.

```java
public record Definition(String functionName, List<String> parameters, List<Expression> body) {}

public sealed interface Expression permits NumberLiteral, Symbol, IfExpression, FunctionCall { ... }
```

## Examples

| Input | Output |
|-------|--------|
| `(define (f x) (if x (f 1) (f 2)))` | `Tail-recursive calls: 2` |
| `(define (f x) (g (f 1) (f 2) (f 3)))` | `Tail-recursive calls: 0` |
| `(define (f x) (if x (if x (if x (f 1) (f 2)) (f 3)) (f 4)))` | `Tail-recursive calls: 4` |

See `src/main/resources/examples/` for 11 example files covering different patterns.

## Test Suite

40 tests across 3 classes:

| Class | Tests | Scope |
|-------|-------|-------|
| `ScannerTest` | 10 | Unit tests for tokenization (numbers, symbols, positions, error cases) |
| `ParserTest` | 13 | Unit tests for AST construction (define, if, function calls, error cases) |
| `IntegrationTest` | 17 | Full pipeline via `Main.run()` — 13 parameterized tail-call counts + 4 error cases |
