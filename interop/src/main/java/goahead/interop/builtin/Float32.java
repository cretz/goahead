package goahead.interop.builtin;

import goahead.interop.GoType;
import goahead.interop.Internal;

@GoType("float32")
public class Float32 extends Number<Float32> {
    @Internal.ImplicitConversion
    public static native Float32 fromFloat(float f);

    @Internal.ImplicitConversion
    public native float toFloat();
}
