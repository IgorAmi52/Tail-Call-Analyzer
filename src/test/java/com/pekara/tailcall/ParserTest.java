package com.pekara.tailcall;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

    private Definition parse(String input) {
        List<Token> tokens = new Scanner(input).scan();
        return new Parser(tokens).parse();
    }

    @Test
    void parsesSimpleDefine() {
        Definition def = parse("(define (f x) x)");
        assertEquals("f", def.functionName());
        assertEquals(List.of("x"), def.parameters());
        assertEquals(1, def.body().size());
        assertInstanceOf(Expression.Symbol.class, def.body().getFirst());
    }

    @Test
    void parsesMultipleParameters() {
        Definition def = parse("(define (add a b) (+ a b))");
        assertEquals(List.of("a", "b"), def.parameters());
    }

    @Test
    void parsesNoParameters() {
        Definition def = parse("(define (f) 42)");
        assertEquals(List.of(), def.parameters());
    }

    @Test
    void parsesNumberLiteral() {
        Definition def = parse("(define (f x) 42)");
        Expression.NumberLiteral num = assertInstanceOf(Expression.NumberLiteral.class, def.body().getFirst());
        assertEquals(42, num.value());
    }

    @Test
    void parsesIfExpression() {
        Definition def = parse("(define (f x) (if x 1 2))");
        Expression.IfExpression ifExpr = assertInstanceOf(Expression.IfExpression.class, def.body().getFirst());
        assertInstanceOf(Expression.Symbol.class, ifExpr.condition());
        assertInstanceOf(Expression.NumberLiteral.class, ifExpr.thenBranch());
        assertInstanceOf(Expression.NumberLiteral.class, ifExpr.elseBranch());
    }

    @Test
    void parsesFunctionCall() {
        Definition def = parse("(define (f x) (g 1 2))");
        Expression.FunctionCall call = assertInstanceOf(Expression.FunctionCall.class, def.body().getFirst());
        assertEquals("g", call.functionName());
        assertEquals(2, call.arguments().size());
    }

    @Test
    void parsesNestedIfInFunctionCallArg() {
        Definition def = parse("(define (f x) (f (if x 1 2)))");
        Expression.FunctionCall call = assertInstanceOf(Expression.FunctionCall.class, def.body().getFirst());
        assertInstanceOf(Expression.IfExpression.class, call.arguments().getFirst());
    }

    @Test
    void parsesMultipleBodyExpressions() {
        Definition def = parse("(define (f x) (g x) (f x))");
        assertEquals(2, def.body().size());
        assertInstanceOf(Expression.FunctionCall.class, def.body().get(0));
        assertInstanceOf(Expression.FunctionCall.class, def.body().get(1));
    }

    @Test
    void rejectsNestedDefine() {
        assertThrows(TailCallAnalyzerException.class,
                () -> parse("(define (f x) (define (g y) y))"));
    }

    @Test
    void rejectsIfWithTwoSubExpressions() {
        assertThrows(TailCallAnalyzerException.class,
                () -> parse("(define (f x) (if x 1))"));
    }

    @Test
    void rejectsIfWithFourSubExpressions() {
        assertThrows(TailCallAnalyzerException.class,
                () -> parse("(define (f x) (if x 1 2 3))"));
    }

    @Test
    void rejectsEmptyBody() {
        assertThrows(TailCallAnalyzerException.class,
                () -> parse("(define (f x))"));
    }

    @Test
    void rejectsTrailingTokens() {
        assertThrows(TailCallAnalyzerException.class,
                () -> parse("(define (f x) x) extra"));
    }
}
