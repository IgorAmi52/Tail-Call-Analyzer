package com.pekara.tailcall;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private final String source;
    private int pos;
    private int line;
    private int column;

    public Scanner(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
    }

    public List<Token> scan() {
        List<Token> tokens = new ArrayList<>();

        while (true) {
            skipWhitespace();

            if (isAtEnd()) {
                tokens.add(new Token(TokenType.EOF, "", line, column));
                return tokens;
            }

            char c = peek();

            if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", line, column));
                advance();
            } else if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", line, column));
                advance();
            } else if (isDigit(c) || (c == '-' && hasNextDigit())) {
                tokens.add(scanNumber());
            } else if (isSymbolChar(c)) {
                tokens.add(scanSymbol());
            } else {
                throw new TailCallAnalyzerException(
                        "Unexpected character '" + c + "' at line " + line + ", column " + column);
            }
        }
    }

    private Token scanNumber() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        if (peek() == '-') {
            sb.append(advance());
        }

        while (!isAtEnd() && isDigit(peek())) {
            sb.append(advance());
        }

        if (!isAtEnd() && isSymbolChar(peek())) {
            throw new TailCallAnalyzerException(
                    "Invalid number '" + sb.toString() + peek() + "' at line " + startLine + ", column " + startColumn);
        }

        return new Token(TokenType.NUMBER, sb.toString(), startLine, startColumn);
    }

    private Token scanSymbol() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && isSymbolChar(peek())) {
            sb.append(advance());
        }

        return new Token(TokenType.SYMBOL, sb.toString(), startLine, startColumn);
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) {
            advance();
        }
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(pos);
    }

    private char advance() {
        char c = source.charAt(pos);
        pos++;
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private boolean isAtEnd() {
        return pos >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean hasNextDigit() {
        return pos + 1 < source.length() && isDigit(source.charAt(pos + 1));
    }

    private boolean isSymbolChar(char c) {
        return c != '(' && c != ')' && !Character.isWhitespace(c) && c != ';';
    }
}
