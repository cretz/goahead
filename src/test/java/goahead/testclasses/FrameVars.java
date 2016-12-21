package goahead.testclasses;

public class FrameVars {
    public static void main(String[] args) throws Exception {
        // When compiled, this uses uninitialized-label frame stack vars
        boolean test = false;
        try {
            throw new Exception(test ? "Test 1" : "Test 2");
        } catch (Exception e) {
            System.out.println("Done!");
        }
    }
}
