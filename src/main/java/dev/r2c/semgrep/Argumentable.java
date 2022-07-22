package dev.r2c.semgrep;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface Argumentable {
    String getPropertyName();
    Function<String, List<String>> getTransformer();

    static Function<String, List<String>> arg(String argName) {
        return (value) -> Arrays.asList(argName, value);
    }

    static Function<String, List<String>> booleanArg(String trueValue, String falseValue) {
        return (value) -> Collections.singletonList(Boolean.parseBoolean(value) ? trueValue : falseValue);
    }

    static Function<String, List<String>> switchArg(String name) {
        return (value) -> Boolean.parseBoolean(value) ? Collections.singletonList(name) : Collections.emptyList();
    }

    Argumentable[] NO_ARGS = new Argumentable[]{};
}
