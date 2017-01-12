package goahead.testclasses;

public class NonStaticInnerClasses {

    public static void main(String[] args) {
        checkSimple();
        checkMultipleThises();
    }

    static class Outer {
        private int instantiations = 0;

        class Inner {
            private String test = "Test";

            public Inner() {
                instantiations++;
            }
        }
    }

    static void checkSimple() {
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();
        System.out.println(inner.test);
        outer.new Inner();
        outer.new Inner();
        outer.new Inner();
        System.out.println(outer.instantiations);
    }

    static class MultiOuter {
        String test = "MultiOuter";

        class InnerBase {
            void updateTest() {
                test += "InnerBase";
            }
        }

        class Inner extends InnerBase {
            @Override
            void updateTest() {
                test += "Inner";
            }
        }
    }

    static void checkMultipleThises() {
        // This used to fail compile due to the synthetic "this" field in both children
        MultiOuter outer = new MultiOuter();
        outer.new InnerBase().updateTest();
        System.out.println(outer.test);
        outer.new Inner().updateTest();
        System.out.println(outer.test);
    }
}
