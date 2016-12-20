package goahead.testclasses;

public class NonStaticInnerClasses {

    static class Outer {
        private int instantiations = 0;

        class Inner {
            private String test = "Test";

            public Inner() {
                instantiations++;
            }
        }
    }

    public static void main(String[] args) {
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();
        System.out.println(inner.test);
        outer.new Inner();
        outer.new Inner();
        outer.new Inner();
        System.out.println(outer.instantiations);
    }
}
