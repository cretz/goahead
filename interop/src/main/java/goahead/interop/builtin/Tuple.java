package goahead.interop.builtin;

public abstract class Tuple {
    public static class T2<V1, V2> extends Tuple {
        public V1 v1;
        public V2 v2;
    }

    public static class T3<V1, V2, V3> extends Tuple {
        public V1 v1;
        public V2 v2;
        public V3 v3;
    }

    // TODO: more
}
