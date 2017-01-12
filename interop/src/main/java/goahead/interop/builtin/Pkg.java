package goahead.interop.builtin;

import goahead.interop.ChanDir;
import goahead.interop.GoFunction;

public final class Pkg {
    @GoFunction("append")
    public static native <T> Slice<T> append(Slice<T> slice, T... elems);

    @GoFunction("cap")
    public static native Int cap(Any v);

    @GoFunction("close")
    public static native void close(Channel<@ChanDir(ChanDir.Dir.SEND) ?> c);

    @GoFunction("complex")
    public static native Complex64 complex(Float32 r, Float32 i);

    @GoFunction("complex")
    public static native Complex128 complex(Float64 r, Float64 i);

    @GoFunction("copy")
    public static native Int copy(Slice<?> dst, Slice<?> src);

    @GoFunction("delete")
    public static native <K> void delete(Map<K, ?> m, K key);

    @GoFunction("imag")
    public static native Float32 imag(Complex64 c);

    @GoFunction("imag")
    public static native Float64 imag(Complex128 c);

    @GoFunction("len")
    public static native Int len(Any v);

    @GoFunction("make")
    public static native <T> Slice<T> makeSlice(Class<T> type, Integer size);

    @GoFunction("make")
    public static native <T> Slice<T> makeSlice(Class<T> type, Integer size, Integer cap);

    @GoFunction("make")
    public static native <K, V> Map<K, V> makeMap(Class<K> key, Class<V> val);

    @GoFunction("make")
    public static native <K, V> Map<K, V> makeMap(Class<K> key, Class<V> val, Integer size);

    @GoFunction("make")
    public static native <T> Channel<T> makeChan(Class<T> type);

    @GoFunction("make")
    public static native <T> Channel<T> makeChan(Class<T> type, Integer size);

    @GoFunction("new")
    public static native <T> Pointer<T> newInst(Class<T> type);

    @GoFunction("panic")
    public static native void panic(Any v);

    @GoFunction("print")
    public static native void print(Slice<Any> v);

    @GoFunction("println")
    public static native void println(Slice<Any> v);

    @GoFunction("real")
    public static native Float32 real(Complex64 c);

    @GoFunction("real")
    public static native Float64 real(Complex128 c);

    @GoFunction("recover")
    public static native Any recover();
}
