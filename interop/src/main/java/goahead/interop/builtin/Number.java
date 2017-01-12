package goahead.interop.builtin;

import goahead.interop.GoFunction;

public abstract class Number<T extends Number<T>> {
    @GoFunction("+")
    public native T add(T other);

    @GoFunction("-")
    public native T sub(T other);

    @GoFunction("*")
    public native T mul(T other);

    @GoFunction("/")
    public native T quo(T other);

    @GoFunction("+")
    public native T unaryPlus();

    @GoFunction("-")
    public native T neg();
}
