package goahead.testclasses;

public class Inheritance {

    static class Parent {
        public String getMyName() {
            return "Parent";
        }
    }

    static class Child extends Parent {
        @Override
        public String getMyName() {
            return "Child";
        }
    }

    public static void main(String[] args) {
        Parent parent = new Parent();
        System.out.println("Parent name: " + parent.getMyName());
        parent = new Child();
        System.out.println("Child name: " + parent.getMyName());
    }
}
