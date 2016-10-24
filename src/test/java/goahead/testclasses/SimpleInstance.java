package goahead.testclasses;

public class SimpleInstance {

    public static void main(String... args) {
        System.out.println(new SimpleInstance().getSomeText());
    }

    public String publicField1 = "public field 1";
    private int privateIntField1;

    public SimpleInstance() {
        privateIntField1 = 42;
    }

    public String getSomeText() {
        privateIntField1++;
        return "Public field: " + publicField1 + ", private field: " + privateIntField1;
    }
}
