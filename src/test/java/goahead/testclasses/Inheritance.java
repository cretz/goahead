package goahead.testclasses;

public class Inheritance {

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

    public static void print(Parent parent) {
        System.out.println(parent.id);
        System.out.println(parent.getMyName());
    }

    public static void main(String[] args) {
        print(new Parent());
        print(new Child());
    }
}
