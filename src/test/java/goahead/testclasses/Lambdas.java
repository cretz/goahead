package goahead.testclasses;

public class Lambdas {

    interface Test {
        String test();
    }

    public static void main(String[] args) {
        // TODO:
         Test test = () -> "Yay";
         System.out.println(test.test());
    }
}
