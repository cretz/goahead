package goahead.interop.builtin;

import goahead.interop.GoFunction;

public class Map<K, V> {

    @GoFunction("[]")
    public native V get(K key);

    @GoFunction("[]")
    public native Tuple.T2<V, Bool> getSafe(K key);

    @GoFunction("[]")
    public native void set(K key, V val);
}
