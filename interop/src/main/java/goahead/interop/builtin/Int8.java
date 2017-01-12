package goahead.interop.builtin;

import goahead.interop.GoType;
import goahead.interop.Internal;

@GoType("int8")
public class Int8 extends Integer<Int8> {
    @Internal.ImplicitConversion
    public static native Int8 fromByte(byte b);

    @Internal.ImplicitConversion
    public native byte toByte();
}
