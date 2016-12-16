package goahead.testclasses;

public class Conditionals {
    public static void main(String[] args) {
        doubles();
        floats();
        longs();
        ifObjects();
        ifInts();
        ifZeros();
        ifNulls();
    }

    public static void doubles() {
        double dA = 10, dB = 12;
        System.out.println(dA < dB);
        System.out.println(dA > dB);
    }

    public static void floats() {
        float fA = 10, fB = 12;
        System.out.println(fA < fB);
        System.out.println(fA > fB);
    }

    public static void longs() {
        long lA = 10, lB = 12;
        System.out.println(lA < lB);
        System.out.println(lA > lB);
    }

    public static void ifObjects() {
        String a = "foo", b = "bar", c = a, d = null, e = null;
        System.out.println(a == b);
        System.out.println(a == c);
        System.out.println(a == d);
        System.out.println(d == e);
        System.out.println(a != b);
        System.out.println(a != c);
        System.out.println(a != d);
        System.out.println(d != e);
    }

    public static void ifInts() {
        int a = 1, b = 2;
        System.out.println(a == b);
        System.out.println(a != b);
        System.out.println(a > b);
        System.out.println(a < b);
        System.out.println(a >= b);
        System.out.println(a <= b);
    }

    public static void ifZeros() {
        int a = 1;
        System.out.println(a == 0);
        System.out.println(a != 0);
        System.out.println(a > 0);
        System.out.println(a < 0);
        System.out.println(a >= 0);
        System.out.println(a <= 0);
    }

    public static void ifNulls() {
        String a = null, b = "foo";
        System.out.println(a == null);
        System.out.println(a != null);
        System.out.println(b == null);
        System.out.println(b != null);
    }
}
