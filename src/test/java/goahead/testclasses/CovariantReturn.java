package goahead.testclasses;

public class CovariantReturn {

    static class Base {
        Base getBase() {
            return this;
        }

        String getStr() {
            return "Base";
        }

        void print() {
            System.out.println(getBase().getStr());
        }
    }

    static class Impl1 extends Base {
        @Override
        Impl2 getBase() {
            return new Impl2();
        }
    }

    static class Impl2 extends Base {
        @Override
        String getStr() {
            return "Impl2";
        }
    }

    public static void main(String[] args) {
        Base b = new Base();
        b.print();
        b = new Impl1();
        b.print();
    }
}
