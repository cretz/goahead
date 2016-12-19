package goahead.testclasses;

public class InterfaceDefaults {

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

    public static void main(String[] args) {
        System.out.println(new DoesNotOverrideFoo().foo());
        System.out.println(new DoesNotOverrideFoo().bar());
        System.out.println(new OverridesFoo().bar());
        System.out.println(new OverridesFoo().foo());
        System.out.println(new OverridesFooAgain().foo());
    }
}
