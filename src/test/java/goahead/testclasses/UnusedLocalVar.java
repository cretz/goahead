package goahead.testclasses;

public class UnusedLocalVar {
    public static void main(String[] args) {
        String foo = "bar", baz = "qux";
        System.out.println(baz);
    }
}
