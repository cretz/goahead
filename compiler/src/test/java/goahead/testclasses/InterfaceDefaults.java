package goahead.testclasses;

public class InterfaceDefaults {

    public static void main(String[] args) {
        checkOverrides();
        checkOverrideDefaultTwoDeep();
    }

    interface HasDefaultFoo {
        default String foo() {
            return "foo1";
        }

        String bar();
    }

    interface AlsoHasDefaultFoo {
        default String foo() {
            return "foo2";
        }
    }

    static class DoesNotOverrideFoo implements HasDefaultFoo {
        @Override
        public String bar() {
            return "bar";
        }
    }

    static class OverridesFoo extends DoesNotOverrideFoo {
        @Override
        public String foo() {
            return super.foo() + bar();
        }
    }

    interface InterfaceOverridesFoo extends HasDefaultFoo, AlsoHasDefaultFoo {
        @Override
        default String foo() {
            return HasDefaultFoo.super.foo() + AlsoHasDefaultFoo.super.foo() + "foo3";
        }
    }

    static class OverridesFooAgain extends OverridesFoo implements InterfaceOverridesFoo {
        @Override
        public String foo() {
            return super.foo() + "foo4";
        }
    }

    static void checkOverrides() {
        System.out.println(new DoesNotOverrideFoo().foo());
        System.out.println(new DoesNotOverrideFoo().bar());
        System.out.println(new OverridesFoo().bar());
        System.out.println(new OverridesFoo().foo());
        System.out.println(new OverridesFooAgain().foo());
    }

    interface GrandParent<T> {
        T foo();
    }

    interface Parent<T> extends GrandParent<T> {
    }

    interface Child extends Parent<String> {
        @Override
        default String foo() {
            return "Test";
        }
    }

    static class Inst implements Child {

    }

    static void checkOverrideDefaultTwoDeep() {
        System.out.println(new Inst().foo());
    }
}
