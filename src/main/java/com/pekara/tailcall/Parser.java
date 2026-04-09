package com.pekara.tailcall;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public Definition parse() {
        expect(TokenType.LPAREN, "Expected '(' to start define");
        Token keyword = expect(TokenType.SYMBOL, "Expected 'define'");
        if (!keyword.value().equals("define")) {
            throw error("Expected 'define', got '" + keyword.value() + "'", keyword);
        }

        Definition result = parseDefine();

        if (!isAtEnd()) {
            throw error("Unexpected tokens after define expression", peek());
        }

        return result;
    }

    private Definition parseDefine() {
        expect(TokenType.LPAREN, "Expected '(' for function signature");

        Token nameToken = expect(TokenType.SYMBOL, "Expected function name");
        String functionName = nameToken.value();

        List<String> parameters = new ArrayList<>();
        while (peek().type() == TokenType.SYMBOL) {
            parameters.add(advance().value());
        }
        expect(TokenType.RPAREN, "Expected ')' to close parameter list");

        List<Expression> body = new ArrayList<>();
        while (peek().type() != TokenType.RPAREN) {
            body.add(parseExpression());
        }

        if (body.isEmpty()) {
            throw error("Define body must contain at least one expression", peek());
        }

        expect(TokenType.RPAREN, "Expected ')' to close define");

        return new Definition(functionName, List.copyOf(parameters), List.copyOf(body));
    }

    private Expression parseExpression() {
        Token current = peek();

        return switch (current.type()) {
            case NUMBER -> {
                advance();
                yield new Expression.NumberLiteral(Integer.parseInt(current.value()));
            }
            case SYMBOL -> {
                advance();
                yield new Expression.Symbol(current.value());
            }
            case LPAREN -> {
                advance();
                Token head = peek();
                if (head.type() != TokenType.SYMBOL) {
                    throw error("Expected symbol after '('", head);
                }
                String name = advance().value();

                if (name.equals("if")) {
                    yield parseIf();
                } else if (name.equals("define")) {
                    throw error("Nested define is not allowed", head);
                } else {
                    yield parseFunctionCall(name);
                }
            }
            default -> throw error("Unexpected token: " + current.value(), current);
        };
    }

    private Expression.IfExpression parseIf() {
        Expression condition = parseExpression();
        Expression thenBranch = parseExpression();

        if (peek().type() == TokenType.RPAREN) {
            throw error("if expression must have exactly 3 sub-expressions (condition, then, else)", peek());
        }

        Expression elseBranch = parseExpression();

        if (peek().type() != TokenType.RPAREN) {
            throw error("if expression must have exactly 3 sub-expressions, found extra", peek());
        }

        expect(TokenType.RPAREN, "Expected ')' to close if");
        return new Expression.IfExpression(condition, thenBranch, elseBranch);
    }

    private Expression.FunctionCall parseFunctionCall(String name) {
        List<Expression> arguments = new ArrayList<>();
        while (peek().type() != TokenType.RPAREN) {
            arguments.add(parseExpression());
        }
        expect(TokenType.RPAREN, "Expected ')' to close function call");
        return new Expression.FunctionCall(name, List.copyOf(arguments));
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token advance() {
        return tokens.get(pos++);
    }

    private Token expect(TokenType type, String message) {
        Token current = peek();
        if (current.type() != type) {
            throw error(message + ", got " + current.type() + " '" + current.value() + "'", current);
        }
        return advance();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private TailCallAnalyzerException error(String message, Token token) {
        return new TailCallAnalyzerException(message + " at line " + token.line() + ", column " + token.column());
    }
}
