package goahead.interop.builtin;

import goahead.interop.GoFunction;

public class Slice<T> {

    @GoFunction("...")
    public static native <T> Slice<T> varargs(T v1);

    @GoFunction("...")
    public static native <T> Slice<T> varargs(T v1, T v2);

    // TODO: more of the above

    @GoFunction("[]")
    public native T get(Integer i);

    @GoFunction("[]")
    public native void set(Integer i, T val);

    @GoFunction("[:]")
    public native Slice<T> slice();

    @GoFunction("[:]")
    public native Slice<T> slice(Integer low, Integer high);

    @GoFunction("[:]")
    public native Slice<T> slice(Integer low, Integer high, Integer max);

    @GoFunction("[:]")
    public native Slice<T> sliceFrom(Integer low);

    @GoFunction("[:]")
    public native Slice<T> sliceUntil(Integer high);

    @GoFunction("[:]")
    public native Slice<T> sliceUntil(Integer high, Integer max);

    @GoFunction("...")
    public native Slice<T> varargs();
}
