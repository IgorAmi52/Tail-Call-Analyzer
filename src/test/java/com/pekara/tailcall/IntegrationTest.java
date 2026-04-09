package com.pekara.tailcall;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntegrationTest {

    @ParameterizedTest(name = "[{index}] {0} => {1}")
    @MethodSource("tailCallTestCases")
    void testTailCallCount(String description, int expectedCount, String schemeCode) {
        assertEquals(expectedCount, Main.run(schemeCode));
    }

    static Stream<Arguments> tailCallTestCases() {
        return Stream.of(
                Arguments.of("given example", 2,
                        "(define (f x) (if x (f 1 (f 4)) (if x (f 1) 4)))"),
                Arguments.of("simple direct tail recursion", 1,
                        "(define (f x) (f x))"),
                Arguments.of("no recursion, returns param", 0,
                        "(define (f x) x)"),
                Arguments.of("no recursion, returns literal", 0,
                        "(define (f x) 42)"),
                Arguments.of("both if-branches tail-recursive", 2,
                        "(define (f x) (if x (f 1) (f 2)))"),
                Arguments.of("non-tail call as argument", 0,
                        "(define (f x) (g (f x)))"),
                Arguments.of("outer tail, inner arg", 2,
                        "(define (f x) (if x (f (f x)) (f x)))"),
                Arguments.of("deeply nested ifs, all branches tail", 4,
                        "(define (f x) (if x (if x (if x (f 1) (f 2)) (f 3)) (f 4)))"),
                Arguments.of("all calls are args to another function", 0,
                        "(define (f x) (g (f 1) (f 2) (f 3)))"),
                Arguments.of("nested non-tail in args, only outermost is tail", 1,
                        "(define (f x) (f (f (f x))))"),
                Arguments.of("multiple body, only last is tail", 1,
                        "(define (f x) (g x) (f x))"),
                Arguments.of("multiple body, first f not tail", 1,
                        "(define (f x) (f x) (f x))"),
                Arguments.of("no parameters", 1,
                        "(define (f) (f))")
        );
    }

    @Test
    void rejectsIfWithMissingElse() {
        assertThrows(TailCallAnalyzerException.class,
                () -> Main.run("(define (f x) (if x (f 1)))"));
    }

    @Test
    void rejectsNestedDefine() {
        assertThrows(TailCallAnalyzerException.class,
                () -> Main.run("(define (f x) (define (g y) y))"));
    }

    @Test
    void rejectsEmptyDefineBody() {
        assertThrows(TailCallAnalyzerException.class,
                () -> Main.run("(define (f x))"));
    }

    @Test
    void rejectsInvalidNumberToken() {
        assertThrows(TailCallAnalyzerException.class,
                () -> Main.run("(define (f x) (f 13g))"));
    }
}
