package goahead.testclasses;

public class Overflows {
    public static void main(String[] args) {
        bytes();
        shorts();
        ints();
        longs();
        floats();
        doubles();
    }

    public static void bytes() {
        byte a = -128, b = 127;
        System.out.println((int)--a);
        System.out.println((int)++b);
    }

    public static void shorts() {
        short a = -32768, b = 32767;
        System.out.println((int)--a);
        System.out.println((int)++b);
    }

    public static void ints() {
        int a = 0x80000000, b = 0x7fffffff;
        System.out.println(--a);
        System.out.println(++b);
    }

    public static void longs() {
        long a = 0x8000000000000000L, b = 0x7fffffffffffffffL;
        System.out.println(--a);
        System.out.println(++b);
    }

    public static void floats() {
        float a = 0x0.000002P-126f, b = 0x1.fffffeP+127f;
        System.out.println(--a == -1.0);
        System.out.println(++b == 340282346638528859811704183484516925440f);
    }

    public static void doubles() {
        double a = 0x0.0000000000001P-1022, b = 0x1.fffffffffffffP+1023;
        System.out.println(--a == -1.0);
        System.out.println(++b == 1.7976931348623157e308);
        double c = 0.1;
        System.out.println(c + c + c == 0.30000000000000004);
    }
}
