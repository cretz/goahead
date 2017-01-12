package goahead.interop.builtin;

import goahead.interop.GoType;
import goahead.interop.Internal;

@GoType("bool")
public class Bool {
    @Internal.ImplicitConversion
    public static native Bool fromBoolean(boolean b);

    @Internal.ImplicitConversion
    public native boolean toBoolean();
}
