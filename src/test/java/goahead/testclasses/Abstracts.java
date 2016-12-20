package goahead.testclasses;

public class Abstracts {
    static abstract class Base {
        abstract String getStr();

        void print() {
            System.out.println(getStr());
        }
    }

    static class Impl extends Base {
        @Override
        String getStr() {
            return "Impl";
        }
    }

    static abstract class ReAbstract extends Impl {
        @Override
        abstract String getStr();
    }

    static class Impl2 extends ReAbstract {
        @Override
        String getStr() {
            return "Impl2";
        }
    }

    public static void main(String[] args) {
        Base base = new Impl();
        base.print();
        base = new Impl2();
        base.print();
    }
}
