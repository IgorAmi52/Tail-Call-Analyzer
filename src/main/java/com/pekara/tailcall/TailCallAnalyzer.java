package com.pekara.tailcall;

import java.util.List;

public class TailCallAnalyzer {

    public int countTailCalls(Definition definition) {
        List<Expression> body = definition.body();
        // Only the last body expression is in tail position. Non-last expressions
        // can never contain tail calls since tail position never propagates into them.
        Expression lastExpression = body.getLast();
        return countInTailPosition(lastExpression, definition.functionName());
    }

    private int countInTailPosition(Expression expr, String functionName) {
        return switch (expr) {
            case Expression.NumberLiteral n -> 0;
            case Expression.Symbol s -> 0;
            case Expression.IfExpression ifExpr ->
                    countInTailPosition(ifExpr.thenBranch(), functionName)
                    + countInTailPosition(ifExpr.elseBranch(), functionName);
            case Expression.FunctionCall call ->
                    call.functionName().equals(functionName) ? 1 : 0;
        };
    }
}
