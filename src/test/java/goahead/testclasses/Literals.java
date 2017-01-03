package goahead.testclasses;

public class Literals {

    public static void main(String[] args) {
        strings();
    }

    static void strings() {
        // Standard escapes
        System.out.println("\r\"\n\t");
        // Inline
        System.out.println("test 日本語 test");
        // Unicode escape
        System.out.println("\u65e5\u672c\u8a9e");
        // Octal escape
        System.out.println("\101\102\103");
        // This used to fail due to the embedded surrogate in here
        String foo = "\uF8F8\uD8F0\uF0F4\uE8D8\uF0F8\uF8F8\uF0F0\uF8F8";
        System.out.println(foo);
    }
}
