package goahead.testclasses;

public class Arrays {
    public static void main(String[] args) {
        booleans();
        bytes();
        chars();
        doubles();
        floats();
        ints();
        shorts();
        longs();
        objects();
        multidimensional();
        multiNewArray();
        arrayLength();
        exceptions();
    }

    public static void booleans() {
        boolean[] anArray = new boolean[3];
        anArray[0] = true;
        anArray[1] = false;
        System.out.println(anArray[0]);
        System.out.println(anArray[1]);
        System.out.println(anArray[2]);
    }

    public static void bytes() {
        byte[] anArray = new byte[3];
        anArray[0] = 14;
        anArray[1] = 35;
        System.out.println(anArray[0] == 14);
        System.out.println(anArray[1] == 35);
        System.out.println(anArray[2] == 0);
    }

    public static void chars() {
        char[] anArray = new char[3];
        anArray[0] = 'a';
        anArray[1] = 'b';
        System.out.println(anArray[0] == 'a');
        System.out.println(anArray[1] == 'b');
        System.out.println(anArray[2] == '\0');
    }

    public static void doubles() {
        double[] anArray = new double[3];
        anArray[0] = 100.5;
        anArray[1] = 200.6;
        System.out.println(anArray[0] == 100.5);
        System.out.println(anArray[1] == 200.6);
        System.out.println(anArray[2] == 0.0);
    }

    public static void floats() {
        float[] anArray = new float[3];
        anArray[0] = 100.5f;
        anArray[1] = 200.6f;
        System.out.println(anArray[0] == 100.5);
        System.out.println(anArray[1] == 200.6);
        System.out.println(anArray[2] == 0.0);
    }

    public static void ints() {
        int[] anArray = new int[3];
        anArray[0] = 100;
        anArray[1] = 200;
        System.out.println(anArray[0] == 100);
        System.out.println(anArray[1] == 200);
        System.out.println(anArray[2] == 0);
    }

    public static void longs() {
        long[] anArray = new long[3];
        anArray[0] = 100;
        anArray[1] = 200;
        System.out.println(anArray[0] == 100);
        System.out.println(anArray[1] == 200);
        System.out.println(anArray[2] == 0);
    }

    public static void shorts() {
        short[] anArray = new short[3];
        anArray[0] = 100;
        anArray[1] = 200;
        System.out.println(anArray[0] == 100);
        System.out.println(anArray[1] == 200);
        System.out.println(anArray[2] == 0);
    }

    public static void objects() {
        String[] anArray = new String[3];
        anArray[0] = "foo";
        anArray[1] = "bar";
        System.out.println(anArray[0]);
        System.out.println(anArray[1]);
        System.out.println(anArray[2]);
    }

    public static void multidimensional() {
        int[][] anArray = new int[2][];
        anArray[0] = new int[]{ 5, 12 };
        anArray[1] = new int[40];
        System.out.println(anArray[0][0] == 5);
        System.out.println(anArray[0][1] == 12);
        System.out.println(anArray[1][39] == 0);
    }

    public static void multiNewArray() {
        int[][][][][] anArray = new int[3][2][43][][];
        anArray[2][1][0] = new int[][] { { 357, 680 }, { 432, 678 } };
        anArray[2][1][14] = new int[5][];
        anArray[2][1][14][3] = new int[7];
        System.out.println(anArray[2][1][0][1][1] == 678);
        System.out.println(anArray[2][1].length);
        System.out.println(anArray[2][1][0][0][1] == 680);
    }

    public static void arrayLength() {
        int[] anArray = new int[47];
        System.out.println(anArray[35]);
        System.out.println(anArray.length);
    }

    public static void exceptions() {
        try {
            int i = -1;
            int[] temp = new int[i];
        } catch (NegativeArraySizeException e) {
            System.out.println("Neg size exception!");
        }

        try {
            int[] temp = null;
            temp[2] = 5;
        } catch (NullPointerException e) {
            System.out.println("NPE!");
        }
    }
}
