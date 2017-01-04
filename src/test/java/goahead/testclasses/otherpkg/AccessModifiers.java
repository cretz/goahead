package goahead.testclasses.otherpkg;

public class AccessModifiers {
    public static class Foo {
        String getFoo() {
            return "Foo";
        }

        public void printFoo() {
            System.out.println(getFoo());
        }
    }
}
