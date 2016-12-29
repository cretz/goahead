package goahead.testclasses;

public class Literals {

    public static void main(String[] args) {
        strings();
    }

    static void strings() {
        // Standard escapes
        System.out.println("\r\"\n\t");
        // Inline TODO: fix
        //System.out.println("test 日本語 test");
        // Unicode escape TODO: fix
        //System.out.println("\u65e5\u672c\u8a9e");
        // Octal escape
        System.out.println("\101\102\103");
    }
}
