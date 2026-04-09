package com.pekara.tailcall;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScannerTest {

    @Test
    void scansParentheses() {
        List<Token> tokens = new Scanner("()").scan();
        assertEquals(TokenType.LPAREN, tokens.get(0).type());
        assertEquals(TokenType.RPAREN, tokens.get(1).type());
        assertEquals(TokenType.EOF, tokens.get(2).type());
    }

    @Test
    void scansNumber() {
        List<Token> tokens = new Scanner("42").scan();
        assertEquals(TokenType.NUMBER, tokens.get(0).type());
        assertEquals("42", tokens.get(0).value());
    }

    @Test
    void scansNegativeNumber() {
        List<Token> tokens = new Scanner("-7").scan();
        assertEquals(TokenType.NUMBER, tokens.get(0).type());
        assertEquals("-7", tokens.get(0).value());
    }

    @Test
    void scansSymbol() {
        List<Token> tokens = new Scanner("define").scan();
        assertEquals(TokenType.SYMBOL, tokens.get(0).type());
        assertEquals("define", tokens.get(0).value());
    }

    @Test
    void scansSpecialSymbolChars() {
        List<Token> tokens = new Scanner("+ zero? my-func").scan();
        assertEquals("+", tokens.get(0).value());
        assertEquals("zero?", tokens.get(1).value());
        assertEquals("my-func", tokens.get(2).value());
    }

    @Test
    void tracksLineAndColumn() {
        List<Token> tokens = new Scanner("(\n  f)").scan();
        assertEquals(1, tokens.get(0).line());
        assertEquals(1, tokens.get(0).column());
        assertEquals(2, tokens.get(1).line());
        assertEquals(3, tokens.get(1).column());
        assertEquals(2, tokens.get(2).line());
        assertEquals(4, tokens.get(2).column());
    }

    @Test
    void skipsWhitespace() {
        List<Token> tokens = new Scanner("  (  f  )  ").scan();
        assertEquals(4, tokens.size());
        assertEquals(TokenType.LPAREN, tokens.get(0).type());
        assertEquals(TokenType.SYMBOL, tokens.get(1).type());
        assertEquals(TokenType.RPAREN, tokens.get(2).type());
        assertEquals(TokenType.EOF, tokens.get(3).type());
    }

    @Test
    void rejectsNumberFollowedBySymbolChar() {
        assertThrows(TailCallAnalyzerException.class,
                () -> new Scanner("13g").scan());
    }

    @Test
    void rejectsSemicolon() {
        assertThrows(TailCallAnalyzerException.class,
                () -> new Scanner(";").scan());
    }

    @Test
    void scansEmptyInput() {
        List<Token> tokens = new Scanner("").scan();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.EOF, tokens.get(0).type());
    }
}
