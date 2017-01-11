package goahead.testclasses;

public class StaticFields {
    public static String staticField = "a";
    public static final boolean lateInit;
    public static int noInit;
    public static final int finalStatic = 20;

    static {
        lateInit = true;
    }

    public static void main(String... args) {
        System.out.println(staticField);
        if (lateInit) System.out.println("late");
        else if (staticField.length() != 0) System.out.println("wrong1");
        else System.out.println("wrong2");

        if (noInit != 0) System.out.println("wrong");
        else System.out.println("none");
        System.out.println(finalStatic);
        System.out.println(lateInit);
    }
}
