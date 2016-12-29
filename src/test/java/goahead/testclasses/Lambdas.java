package goahead.testclasses;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;

public class Lambdas {

    interface Test {
        String test();
    }

    interface TestHolder {
        Test test();
    }

    static class HasString {
        String str;
        HasString(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    static class TestImpl implements Test {
        @Override
        public String test() {
            return "TestImpl";
        }
    }

    public static void main(String[] args) {
        // TODO: figure out how to test other things
        invokeStatic();
        invokeVirtual();
        invokeNewSpecial();
        deserializeLambda();
        defaultConsumerChainTest();
        binaryOperator();
        biConsumer();
        defaultMethodOverride();
    }

    static void invokeStatic() {
        Test test = () -> "Static";
        System.out.println(test.test());
    }

    static void invokeVirtual() {
        HasString str = new HasString("Virtual");
        Test test = str::toString;
        System.out.println(test.test());
    }

    static void invokeNewSpecial() {
        TestHolder test = TestImpl::new;
        System.out.println(test.test().test());
    }

    static class DeserializeLambdaTest {
        Test getTest() {
            return (Test & Serializable) () -> "Test";
        }
    }

    static void deserializeLambda() {
        System.out.println(new DeserializeLambdaTest().getTest().test());
    }

    interface LongConsumer {

        void accept(long value);

        default LongConsumer andThen(LongConsumer after) {
            return (long t) -> { accept(t); after.accept(t); };
        }
    }

    static void defaultConsumerChainTest() {
        LongConsumer test = v -> System.out.println("Test: " + v);
        test.andThen(v -> System.out.println("Test2: " + v));
    }

    public static long longSum(long a, long b) {
        return a + b;
    }

    static void binaryOperator() {
        BinaryOperator<Long> op = Lambdas::longSum;
        System.out.println(op.apply(13L, 14L).longValue());
    }

    static void biConsumer() {
        BiConsumer<StringBuilder, String> op = StringBuilder::append;
        StringBuilder test = new StringBuilder();
        op.accept(test, "Test");
        System.out.println(test.toString());
    }

    interface Sink<T> {
        void accept(T typ);

        default void accept(long value) {
            System.out.println("Fail");
        }
    }

    interface LongSink extends Sink<Long>, LongConsumer {
        @Override
        void accept(long value);

        @Override
        default void accept(Long typ) {
            accept(typ.longValue());
        }
    }

    static void defaultMethodOverride() {
        LongSink sink = v -> System.out.println(v);
        sink.accept(Long.valueOf(20));
    }
}
