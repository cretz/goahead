package goahead.interop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PACKAGE)
public @interface GoPackage {
    String name();
    Class<?>[] topLevelFunctionClasses() default {};
}
