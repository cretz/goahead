package goahead.testclasses;

import goahead.ExpectOpcodes;
import goahead.WarnOnFormatError;
import org.objectweb.asm.Opcodes;

@WarnOnFormatError
@ExpectOpcodes({ Opcodes.DUP_X1, Opcodes.DUP_X2, Opcodes.DUP2, Opcodes.DUP2_X1,
        Opcodes.DUP2_X2, Opcodes.POP, Opcodes.POP2 })
public class StackManips {
    public static void main(String[] args) {
        new Tester().dupx1();
        dupx2();
        dup2();
        new Tester().dup2x1Longs();
        dup2x2Longs();
        // TODO: can't find DUP2_X1 and DUP2_X2 not relating to longs/doubles
        // TODO: can't find NOP
        new Tester().pop();
        new Tester().pop2Long();
        // TODO: can't find POP2 not relating to longs/doubles
        // TODO: still looking for suitable JSR/RET example, probably deprecated in 1.8
        // TODO: can't find a good SWAP example
    }

    static class Tester {
        int b;
        long c, d;

        void dupx1() {
            System.out.println(b++);
        }

        void dup2x1Longs() {
            System.out.println(c = d = 0);
        }

        int incrementB() {
            return b++;
        }

        void pop() {
            incrementB();
            System.out.println(b);
        }

        long incrementCBy(long v) {
            return c += v;
        }

        void pop2Long() {
            long a = 5;
            incrementCBy(a);
            System.out.println(c);
        }
    }

    static void dupx2() {
        int a = 5;
        int[] b = { 0, 1, 2, 3, 4, 5, 6 };
        System.out.println(b[--a] = b[a] + 10);
    }

    static void dup2() {
        int[] a = { 0, 1, 2, 3, 4, 5, 6 };
        int b = 5;
        System.out.println(a[b - 1] += a[b - 1]);
    }

    static void dup2x2Longs() {
        long[] a = { 0, 1, 2, 3, 4, 5, 6 };
        long[] b = { 0, 1, 2, 3, 4, 5, 6 };
        System.out.println(a[4] = b[4] = 0);
    }
}
