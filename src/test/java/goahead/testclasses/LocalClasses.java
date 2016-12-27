package goahead.testclasses;

public class LocalClasses {

    public static void main(String[] args) {
        String test = "Test";
        class TestPrint {
            void print() {
                System.out.println(test);
            }
        }
        new TestPrint().print();
    }
}
