package goahead.testclasses;

public class LocalVarReuse {
    public static void main(String[] args) {
        {
            String test = "foo";
            System.out.println(test);
        }
        {
            String[] test = { "bar" };
            System.out.println(test[0]);
        }
        {
            String test = "baz";
            System.out.println(test);
        }
    }
}
