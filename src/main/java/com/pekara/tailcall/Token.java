package com.pekara.tailcall;

public record Token(TokenType type, String value, int line, int column) {
}
