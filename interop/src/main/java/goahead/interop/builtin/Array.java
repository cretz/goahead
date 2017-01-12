package goahead.interop.builtin;

import goahead.interop.GoFunction;

public class Array<T> {

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
}
