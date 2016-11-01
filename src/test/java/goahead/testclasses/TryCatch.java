package goahead.testclasses;

public class TryCatch {
    public static void main(String[] args) {
        String test = null;
        try {
            System.out.println("try-outside-begin");
            try {
                System.out.println("try-outside-begin");
                System.out.println(test.length());
                System.out.println("try-outside-end");
            } catch (NullPointerException e) {
                throw new RuntimeException("catch-inside: " + e.getMessage(), e);
            }
            System.out.println("try-end");
        } catch (RuntimeException e) {
            System.out.println("catch-outside: " + e.getMessage());
        } finally {
            System.out.println("finally");
        }
    }
}
