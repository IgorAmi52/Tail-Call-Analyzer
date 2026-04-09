package com.pekara.tailcall;

import java.util.List;

public sealed interface Expression
        permits Expression.NumberLiteral,
                Expression.Symbol,
                Expression.IfExpression,
                Expression.FunctionCall {

    record NumberLiteral(int value) implements Expression {}

    record Symbol(String name) implements Expression {}

    record IfExpression(Expression condition, Expression thenBranch, Expression elseBranch) implements Expression {}

    record FunctionCall(String functionName, List<Expression> arguments) implements Expression {}
}
