package goahead.interop.builtin;

import goahead.interop.Internal;

public class Pointer<T> {

    @Internal.ImplicitConversion
    public native T forInvoke();
}
