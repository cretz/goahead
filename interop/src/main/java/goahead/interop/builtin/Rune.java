package goahead.interop.builtin;

import goahead.interop.GoType;
import goahead.interop.Internal;

@GoType("rune")
public class Rune extends Integer<Rune> {
    @Internal.ImplicitConversion
    public static native Rune fromChar(char c);

    @Internal.ImplicitConversion
    public native char toChar();
}
