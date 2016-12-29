package goahead.testclasses;

import java.io.Reader;

public class LocalVarReuse {
    public static void main(String[] args) {
        {
            String test = "foo";
            System.out.println(test);
        }
        {
            String[] test = { "bar" };
            System.out.println(test[0]);
        }
        {
            String test = "baz";
            System.out.println(test);
        }
    }

    static class Test {

        // Just compiling this was an error
        private void test() {
            int a = 0, b = 0, c = 0;
            char[] d = null;
            Object e = null;
            int f;
            if (1 == 2) {
            } else {
                int h = b - a;
                if (h >= c) {
                } else {
                    if (c <= d.length) {
                    } else {
                        char i[] = new char[c];
                    }
                }
            }
        }

        // Just compiling this was an error
        private void test2() {
            int a = 0, b = 0, c = 0, d = 1;
            char[] e = null;
            Reader f = null;
            int g;
            if (a <= b) {
            } else {
                int h = c - a;
                if (h >= d) {
                } else {
                    char i[] = new char[d];
                    Object j = i;
                    System.out.println(((char[]) j).length);
                }
            }
        }
    }
}
