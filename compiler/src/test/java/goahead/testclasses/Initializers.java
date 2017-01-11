package goahead.testclasses;

public class Initializers {

    static class Foo {
        static {
            String test = "1";
            System.out.println("Static Foo " + test);
        }

        static String staticField = "Static Test";

        static {
            System.out.println("Static Foo " + staticField);
        }
        {
            System.out.println("Instance Foo " + staticField);
        }

        String field = "Test";

        {
            System.out.println("Instance Foo " + field);
        }

        public Foo() {
            System.out.println("Instance Foo 2");
        }
    }

    static class Bar extends Foo {
        static {
            System.out.println("Static Bar 1");
        }
        static {
            System.out.println("Static Bar 1");
        }
        {
            System.out.println("Instance Bar 1");
        }
        {
            System.out.println("Instance Bar 2");
            field = "Test2";
        }
    }

    public static void main(String[] args) {
        System.out.println(new Foo().field);
        System.out.println(new Bar().field);
        System.out.println(new Foo().field);
    }
}
