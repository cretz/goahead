package goahead.testclasses;

public class FrameVars {
    public static void main(String[] args) throws Exception {
        uninitializedLabelFrameVars();
    }

    static void uninitializedLabelFrameVars() {
        // When compiled, this uses uninitialized-label frame stack vars
        boolean test = false;
        try {
            throw new Exception(test ? "Test 1" : "Test 2");
        } catch (Exception e) {
            System.out.println("Done!");
        }
    }

    // These just failed compilation
    static class FrameAppendComplex1 {
        private int foo() throws Exception {
            return 0;
        }
    }

    static class FrameAppendComplex2 {
        Object[] arr;
    }

    static class FrameAppendComplex3 {
        FrameAppendComplex1 field1;
        FrameAppendComplex2 field2;

        private Object readRef() throws Exception {
            int i = field1.foo();
            return i == 0 ? null : field2.arr[i];
        }
    }

    static class UnknownFrame1 {
        private UnknownFrame1(String var0, UnknownFrame2 var1, int var2) {
        }
    }

    static class UnknownFrame2 {
        static UnknownFrame2 foo() {
            return null;
        }
    }

    static class UnknownFrame3 {

        private static int doSomething(int var0) {
            return var0;
        }

        private final UnknownFrame1 temp;

        public UnknownFrame3(UnknownFrame2 var0, int var1) {
            temp = new UnknownFrame1(null, var0 == null ? UnknownFrame2.foo() : var0, doSomething(var1));
        }
    }

    private static void duplicateLabelIssue() {
        for (;;) {
            String temp = "Test";
        }
    }
}
