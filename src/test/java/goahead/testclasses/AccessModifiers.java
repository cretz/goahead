package goahead.testclasses;

public class AccessModifiers {

    static class Base {
        private String stringField = "Base";

        private String stringValue() {
            return "Base";
        }

        String getStr() {
            return stringValue();
        }

        void print() {
            System.out.println(getStr());
        }

        void print2() {
            System.out.println(stringField);
        }
    }

    static class Impl extends Base {
        private String stringField = "Impl";

        private String stringValue() {
            return "Impl";
        }
    }

    public static void main(String[] args) {
        new Impl().print();
        new Impl().print2();
        Base base = new Impl();
        System.out.println(base.stringField);
        System.out.println(((Impl) base).stringField);
    }
}