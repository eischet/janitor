package com.eischet.janitor.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

import java.util.function.BiFunction;

public interface BinOp<L, R, T> {
    T apply(JanitorScriptProcess process, L left, R right) throws JanitorRuntimeException;


    static <L, R, T> BinOp<L, R, T> adapt(BiFunction<L, R, T> function) {
        return (process, left, right) -> function.apply(left, right);
    }

}
