package goahead.testclasses;

public class ArrayTests {
    public static void main(String[] args) {
        simpleBooleans();
        simpleBytes();
        simpleChars();
        simpleDoubles();
        simpleFloats();
        simpleInts();
        simpleShorts();
        simpleLongs();
    }

    public static void simpleBooleans() {
        boolean[] anArray = new boolean[3];
        anArray[0] = true;
        anArray[1] = false;
        System.out.println(anArray[0]);
        System.out.println(anArray[1]);
        System.out.println(anArray[2]);
    }

    public static void simpleBytes() {
        byte[] anArray = new byte[3];
        anArray[0] = 14;
        anArray[1] = 35;
        System.out.println(anArray[0] == 14);
        System.out.println(anArray[1] == 35);
        System.out.println(anArray[2] == 0);
    }

    public static void simpleChars() {
        char[] anArray = new char[3];
        anArray[0] = 'a';
        anArray[1] = 'b';
        System.out.println(anArray[0] == 'a');
        System.out.println(anArray[1] == 'b');
        System.out.println(anArray[2] == '\0');
    }

    public static void simpleDoubles() {
        double[] anArray = new double[3];
        anArray[0] = 100.5;
        anArray[1] = 200.6;
        System.out.println(anArray[0] == 100.5);
        System.out.println(anArray[1] == 200.6);
        System.out.println(anArray[2] == 0.0);
    }

    public static void simpleFloats() {
        float[] anArray = new float[3];
        anArray[0] = 100.5f;
        anArray[1] = 200.6f;
        System.out.println(anArray[0] == 100.5);
        System.out.println(anArray[1] == 200.6);
        System.out.println(anArray[2] == 0.0);
    }

    public static void simpleInts() {
        int[] anArray = new int[3];
        anArray[0] = 100;
        anArray[1] = 200;
        System.out.println(anArray[0] == 100);
        System.out.println(anArray[1] == 200);
        System.out.println(anArray[2] == 0);
    }

    public static void simpleLongs() {
        long[] anArray = new long[3];
        anArray[0] = 100;
        anArray[1] = 200;
        System.out.println(anArray[0] == 100);
        System.out.println(anArray[1] == 200);
        System.out.println(anArray[2] == 0);
    }

    public static void simpleShorts() {
        short[] anArray = new short[3];
        anArray[0] = 100;
        anArray[1] = 200;
        System.out.println(anArray[0] == 100);
        System.out.println(anArray[1] == 200);
        System.out.println(anArray[2] == 0);
    }
}
