package goahead.interop.builtin;

import goahead.interop.GoFunction;

public abstract class Integer<T extends Integer<T>> extends Number<T> {

    @GoFunction("%")
    public native T rem(T other);

    @GoFunction("&")
    public native T and(T other);

    @GoFunction("|")
    public native T or(T other);

    @GoFunction("^")
    public native T xor(T other);

    @GoFunction("&^")
    public native T andNot(T other);

    @GoFunction("<<")
    public native T leftShift(UnsignedInteger<?> other);

    @GoFunction(">>")
    public native T rightShift(UnsignedInteger<?> other);
}
