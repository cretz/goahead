package goahead.interop.builtin;

import goahead.interop.GoType;
import goahead.interop.Internal;

@GoType("float64")
public class Float64 extends Number<Float64> {
    @Internal.ImplicitConversion
    public static native Float64 fromDouble(double d);

    @Internal.ImplicitConversion
    public native double toDouble();
}
