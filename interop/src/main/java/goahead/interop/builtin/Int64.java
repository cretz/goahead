package goahead.interop.builtin;

import goahead.interop.GoType;
import goahead.interop.Internal;

@GoType("int64")
public class Int64 extends Integer<Int64> {
    @Internal.ImplicitConversion
    public static native Int64 fromLong(long l);

    @Internal.ImplicitConversion
    public native long toLong();
}
