package goahead.testclasses;

public class Casts {

    static class Temp {
        String text = "text";
    }

    public static void main(String[] args) {
        checkCast();
        checkInstanceOf();
    }

    public static void checkCast() {
        Object foo = new Temp();
        System.out.println(((Temp) foo).text);
        try {
            System.out.println(((String) foo));
        } catch (ClassCastException e) {
            System.out.println("EX!");
        }
        // TODO: Check for nulls and array covariance
    }

    public static void checkInstanceOf() {
        Object foo = new Temp();
        System.out.println(foo instanceof Temp);
        System.out.println(foo instanceof String);
        // TODO: Check for nulls and array covariance
    }
}
