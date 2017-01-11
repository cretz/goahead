package goahead.testclasses;

import goahead.PolymorphicSignature;

public class SigPolys {
    public static void main(String[] args) {
        sigPolyPrimitiveArgs();
        sigPolyPrimitiveReturn();
    }

    @PolymorphicSignature
    public static Object sigPolyRetString(Object... args) {
        // We don't concern ourselves w/ arg conversion because the JVM uses native versions of these
        return "Test";
    }

    public static void sigPolyPrimitiveArgs() {
        Object foo = sigPolyRetString(1L);
        System.out.println((String) foo);
    }

    @PolymorphicSignature
    public static Object sigPolyRet15(Object... args) {
        return 15L;
    }

    public static void sigPolyPrimitiveReturn() {
        Object foo = sigPolyRet15();
        System.out.println((long) foo);
    }
}