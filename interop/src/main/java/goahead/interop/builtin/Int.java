package goahead.interop.builtin;

import goahead.interop.GoType;
import goahead.interop.Internal;

@GoType("int")
public class Int extends Integer<Int> {
    @Internal.ImplicitConversion
    public static native Int fromInt(int i);

    @Internal.ImplicitConversion
    public native int toInt();
}
