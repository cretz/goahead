package goahead.testclasses;

public class Interfaces {

    interface IBar {
        int[] someInts = { 5, 10 };

        String barThing();
    }

    interface IFoo extends IBar {
        String fooThing();
    }

    static class Foo implements IFoo {
        @Override
        public String barThing() {
            return "bar impl";
        }

        @Override
        public String fooThing() {
            return "foo impl";
        }
    }

    static void printIBar(IBar ibar) {
        System.out.println("IBar: " + ibar.barThing());
    }

    static void printIFoo(IFoo ifoo) {
        System.out.println("IFoo: " + ifoo.fooThing());
    }

    public static void main(String[] args) {
        printIFoo(new Foo());
        printIBar(new Foo());
        System.out.println(Foo.someInts[1]);
    }
}
