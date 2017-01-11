package goahead.testclasses;

public class InheritanceConstructors {

    static class Parent {
        String item = "unset";

        Parent() {
        }

        Parent(String item) {
            this.item = item;
        }
    }

    static class Child extends Parent {
        Child(String item) {
            super("childItem-" + item);
        }

        Child(String item1, String item2) {
            this(item1 + ":" + item2);
        }

        Child(String item1, String item2, String item3) {
            // Skip our constructor
            super(item1 + ":" + item2 + ":" + item3);
        }

        Child(int foo) {
            super(foo > 5 ? "Over 5" : "Not Over 5");
        }
    }

    public static void main(String[] args) {
        System.out.println(new Parent().item);
        System.out.println(new Parent("item").item);
        System.out.println(new Child("item").item);
        System.out.println(new Child("item1", "item2").item);
        System.out.println(new Child("item1", "item2", "item3").item);
        System.out.println(new Child(12).item);
    }
}
