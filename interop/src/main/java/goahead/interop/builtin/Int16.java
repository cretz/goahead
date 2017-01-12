package goahead.interop.builtin;

import goahead.interop.GoType;
import goahead.interop.Internal;

@GoType("int16")
public class Int16 extends Integer<Int16> {
    @Internal.ImplicitConversion
    public static native Int16 fromShort(short s);

    @Internal.ImplicitConversion
    public native short toShort();
}
