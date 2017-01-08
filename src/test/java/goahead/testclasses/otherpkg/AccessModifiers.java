package goahead.testclasses.otherpkg;

public class AccessModifiers {

    public AccessModifiers() {

    }

    AccessModifiers(String someVal) {
        System.out.println(someVal);
    }

    String getFoo() {
        return "Foo";
    }

    public void printFoo() {
        System.out.println(getFoo());
    }

    String getBaz() {
        return "Baz";
    }

    public void printBaz() {
        System.out.println(getBaz());
    }

    public static class Another extends AccessModifiers {

        public Another() {
            super();
        }

        public Another(String someVal) {
            super(someVal);
        }

        @Override
        String getFoo() {
            return "Another foo";
        }
    }
}
