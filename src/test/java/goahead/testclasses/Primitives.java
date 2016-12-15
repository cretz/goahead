package goahead.testclasses;

import goahead.WarnOnFormatError;

// TODO: This is due to our spacing issues around binary operators for now
@WarnOnFormatError
public class Primitives {
    public static void main(String[] args) {
        System.out.println((int) doubles());
        System.out.println((int) floats());
        System.out.println(ints());
        System.out.println(longs());
    }

    public static double doubles() {
        // TODO: test the constant cast issue where we get something like
        // http://stackoverflow.com/questions/16151199/go-lang-constant-truncated-to-integer
        double a = 6.9, b = 15.3, c = 1.3, d = 50000.5, e = 395.2, f = 13;
        System.out.println((float) a);
        System.out.println((int) a);
        System.out.println((long) a);
        System.out.println((int) (a + b / c * -d - e));
        System.out.println(0d);
        System.out.println(1d);
        return d % f;
    }

    public static float floats() {
        float a = 6.9f, b = 15.3f, c = 1.3f, d = 50000.5f, e = 395.2f, f = 13f;
        System.out.println(((double) a) == 6.8);
        System.out.println((int) a);
        System.out.println((long) a);
        System.out.println((int) (a + b / c * -d - e));
        System.out.println(0f);
        System.out.println(1f);
        System.out.println(2f);
        return d % f;
    }

    public static int ints() {
        int a = 69, b = 153, c = 13, d = 500005, e = 3952, f = 130;
        System.out.println((byte) a);
        System.out.println((char) a);
        System.out.println(((double) a) == 69d);
        System.out.println((float) a);
        System.out.println((long) a);
        System.out.println((short) a);
        System.out.println(a + b / c * -d - e);
        System.out.println(0);
        System.out.println(1);
        System.out.println(2);
        System.out.println(3);
        System.out.println(4);
        System.out.println(5);
        System.out.println(-1);
        System.out.println(d & 155323);
        System.out.println(c += 35);
        System.out.println(c);
        System.out.println(d | 155323);
        System.out.println(-a << 3);
        System.out.println(-a >> 3);
        System.out.println(-a >>> 3);
        return d % f;
    }

    public static long longs() {
        long a = 69, b = 153, c = 13, d = 500005, e = 3952, f = 130;
        System.out.println((byte) a);
        System.out.println((char) a);
        System.out.println(((double) a) == 69d);
        System.out.println((float) a);
        System.out.println((int) a);
        System.out.println((short) a);
        System.out.println(a + b / c * -d - e);
        System.out.println(0L);
        System.out.println(1L);
        System.out.println(d & 155323);
        System.out.println(c += 35);
        System.out.println(c);
        System.out.println(d | 155323);
        System.out.println(-a << 3);
        System.out.println(-a >> 3);
        System.out.println(-a >>> 3);
        return d % f;
    }
}
