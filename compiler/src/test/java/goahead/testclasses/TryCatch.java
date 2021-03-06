package goahead.testclasses;

public class TryCatch {
    public static void main(String[] args) {
        simpleTryCatch();
        insideSynchronized();
    }

    static void simpleTryCatch() {
        String test1 = null;
        try {
            String test2 ="try-inside-begin";
            System.out.println("try-outside-begin");
            try {
                System.out.println(test2);
                System.out.println(test1.length());
                System.out.println("try-inside-end");
            } catch (NullPointerException e) {
                test2 = "catch-inside: NPE";
                throw new Exception(test2, e);
            }
            System.out.println("try-outside-end");
        } catch (Exception e) {
            System.out.println("catch-outside: " + e.getMessage());
        } finally {
            System.out.println("finally");
        }
    }

    static void insideSynchronized() {
        try {
            throwingInSynchronized();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    static void throwingInSynchronized() throws Exception {
        String lock = "lock";
        synchronized (lock) {
            throw new Exception("Test");
        }
    }
}
