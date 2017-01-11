package goahead.testclasses;

public class Abstracts {

    public static void main(String[] args) {
        checkBaseAndImpl();
        checkAbstractOfIface();
    }

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

    static void checkBaseAndImpl() {
        Base base = new Impl();
        base.print();
        base = new Impl2();
        base.print();
    }

    interface IFoo {
        String getFoo();
    }

    static abstract class AbstractFoo implements IFoo {
        abstract String getBar();
    }

    static class Foo extends AbstractFoo {
        @Override
        public String getFoo() {
            return "Foo";
        }

        @Override
        String getBar() {
            return "Bar";
        }
    }

    static void checkAbstractOfIface() {
        System.out.println(new Foo().getFoo());
    }
}
