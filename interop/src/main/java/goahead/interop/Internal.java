package goahead.interop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public interface Internal {
    @Target(ElementType.METHOD)
    @interface ImplicitConversion {

    }
}
