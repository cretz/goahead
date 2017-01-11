package goahead.testclasses;

public class Casts {

    static class Temp implements Foo, Bar {
        String text = "text";

        @Override
        public String foo() {
            return "Foo";
        }

        @Override
        public String bar() {
            return "Bar";
        }
    }

    interface Foo {
        String foo();
    }

    interface Bar {
        String bar();
    }

    public static void main(String[] args) {
        checkCast();
        checkInterfaceCast();
        checkInstanceOf();
        nullCast();
        checkThisInstanceOfFoo();
    }

    public static void checkCast() {
        Object foo = new Temp();
        System.out.println(((Temp) foo).text);
        try {
            System.out.println(((String) foo));
        } catch (ClassCastException e) {
            System.out.println("EX!");
        }
        // TODO: Check for nulls and array covariance
    }

    public static void checkInterfaceCast() {
        Foo foo = new Temp();
        System.out.println(foo.foo());
        System.out.println(((Bar) foo).bar());
    }

    public static void checkInstanceOf() {
        Object foo = new Temp();
        System.out.println(foo instanceof Temp);
        System.out.println(foo instanceof String);
        // TODO: Check for nulls and array covariance
    }

    public static void nullCast() {
        Foo foo = (Foo) null;
        System.out.println(foo == null);
    }

    static class ThisInstanceOfFoo {
        boolean isFoo = this instanceof Foo;
    }

    public static void checkThisInstanceOfFoo() {
        ThisInstanceOfFoo v = new ThisInstanceOfFoo();
        System.out.println(v.isFoo);
        class SomethingElse extends ThisInstanceOfFoo implements Foo {
            @Override
            public String foo() {
                return null;
            }
        }
        v = new SomethingElse();
        System.out.println(v.isFoo);
    }
}
