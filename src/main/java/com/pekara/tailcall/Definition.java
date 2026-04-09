package com.pekara.tailcall;

import java.util.List;

public record Definition(String functionName, List<String> parameters, List<Expression> body) {
}
