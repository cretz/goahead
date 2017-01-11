package goahead.testclasses;

import java.io.IOException;
import java.io.OutputStream;

public class UnusedLocalVar {
    public static void main(String[] args) {
        String foo = "bar", baz = "qux";
        System.out.println(baz);
    }

    static class Temp {
        public void test() throws IOException {
            // This was causing unused vars
            try (OutputStream o = null) {
                System.out.println("Test");
            }
        }
    }
}
