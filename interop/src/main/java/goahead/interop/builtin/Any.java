package goahead.interop.builtin;

import goahead.interop.GoFunction;
import goahead.interop.GoType;
import goahead.interop.Internal;

@GoType("")
public final class Any {
    @Internal.ImplicitConversion
    public static native Any from(Object v);
    @Internal.ImplicitConversion
    public static native Any from(byte v);
    @Internal.ImplicitConversion
    public static native Any from(char v);
    @Internal.ImplicitConversion
    public static native Any from(short v);
    @Internal.ImplicitConversion
    public static native Any from(int v);
    @Internal.ImplicitConversion
    public static native Any from(long v);
    @Internal.ImplicitConversion
    public static native Any from(float v);
    @Internal.ImplicitConversion
    public static native Any from(double v);
    @Internal.ImplicitConversion
    public static native Any from(boolean v);

    @GoFunction("()")
    public native <T> T typeAssert(Class<T> typ);
}
