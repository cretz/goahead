package goahead.testclasses;

public class Inheritance {
    public static void main(String[] args) {
        simpleInheritance();
        abstractVisibilityCovariantOverrides();
    }

    static class Parent {
        String id = "parent-id";

        public String getMyName() {
            return "Parent";
        }
    }

    static class Child extends Parent {
        Child() {
            id = "child-id";
        }

        @Override
        public String getMyName() {
            return "Child";
        }
    }

    static class GrandChild extends Child {

        @Override
        public String getMyName() {
            return "GrandChild";
        }

        public String parentName() {
            return super.getMyName();
        }
    }

    public static void print(Parent parent) {
        System.out.println(parent.id);
        System.out.println(parent.getMyName());
    }

    public static void simpleInheritance() {
        print(new Parent());
        print(new Child());
        System.out.println(new GrandChild().parentName());
    }

    abstract static class Foo {
        String field = "foo";
        abstract Foo doSomething();
    }

    static class Bar extends Foo {
        @Override
        protected Bar doSomething() {
            this.field = "bar";
            return this;
        }
    }

    public static void abstractVisibilityCovariantOverrides() {
        Foo foo = new Bar();
        System.out.println(foo.doSomething().field);
    }
}
