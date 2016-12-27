package goahead.testclasses;

import java.io.Serializable;

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

    static void deserializeLambda() {
        System.out.println(new DeserializeLambdaTest().getTest().test());
    }

    static class DeserializeLambdaTest {
        Test getTest() {
            return (Test & Serializable) () -> "Test";
        }
    }

    static void defaultConsumerChainTest() {
        LongConsumer test = v -> System.out.println("Test: " + v);
        test.andThen(v -> System.out.println("Test2: " + v));
    }

    public interface LongConsumer {

        void accept(long value);

        default LongConsumer andThen(LongConsumer after) {
            return (long t) -> { accept(t); after.accept(t); };
        }
    }
}
