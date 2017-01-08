package goahead.testclasses;

public class AccessModifiers extends goahead.testclasses.otherpkg.AccessModifiers {

    public static void main(String[] args) {
        normalFields();
        recreateField();
        packagePrivateOverride();
        multiDepthPackagePrivateOverride();
        abstractPackagePrivateOverride();
    }

    static class Base {
        private String stringField = "Base";

        private String stringValue() {
            return "Base";
        }

        String getStr() {
            return stringValue();
        }

        void print() {
            System.out.println(getStr());
        }

        void print2() {
            System.out.println(stringField);
        }
    }

    static class Impl extends Base {
        private String stringField = "Impl";

        public Impl() {
        }

        private Impl(String stringField) {
            this.stringField = stringField;
        }

        private String stringValue() {
            return "Impl";
        }

        public String recreateAndObtainField() {
            return new Impl("Recreated").stringField;
        }
    }

    static void normalFields() {
        new Impl().print();
        new Impl().print2();
        Base base = new Impl();
        System.out.println(base.stringField);
        System.out.println(((Impl) base).stringField);
    }

    static void recreateField() {
        Impl impl = new Impl();
        System.out.println(impl.recreateAndObtainField());
    }

    private String getFoo() {
        return "Bar";
    }

    public String getBaz() {
        return "Qux";
    }

    String getQuux() {
        return "Quux";
    }

    public void printQuux() {
        System.out.println(getQuux());
    }

    String getCorge() {
        return "Corge";
    }

    public void printCorge() {
        System.out.println(getCorge());
    }

    String getGarply() {
        return "Garply";
    }

    public void printGarply() {
        System.out.println(getGarply());
    }

    static class PackagePrivateOverride extends AccessModifiers {
        @Override
        String getQuux() {
            return "Quuz";
        }

        @Override
        public String getCorge() {
            return "Grault";
        }

        @Override
        protected String getGarply() {
            return "Waldo";
        }
    }

    static goahead.testclasses.otherpkg.AccessModifiers obj;
    static AccessModifiers obj2;

    static void packagePrivateOverride() {
        obj = new AccessModifiers();
        obj2 = new PackagePrivateOverride();

        // Package private not overridden, but replaced with private
        obj.printFoo();
        // Package private not overridden, but replaced with public
        obj.printBaz();
        // Package private overridden
        obj2.printQuux();
        // Package private elevated to public
        obj2.printCorge();
        // Package private elevated to protected
        obj2.printGarply();
    }

    static class YetAnother extends Another {

    }

    static void multiDepthPackagePrivateOverride() {
        obj = new YetAnother();
        obj.printFoo();
    }

    static abstract class GrandParent {
        abstract void printSomething();

        abstract void printSomethingElse();
    }

    static class Parent extends GrandParent {
        @Override
        protected void printSomething() {
            System.out.println("Test!");
        }

        @Override
        public void printSomethingElse() {
            System.out.println("Test2!");
        }
    }

    static class Child extends Parent {
        @Override
        public void printSomethingElse() {
            System.out.println("Test3!");
        }
    }

    static GrandParent obj3;

    static void abstractPackagePrivateOverride() {
        obj3 = new Child();
        obj3.printSomething();
        obj3.printSomethingElse();
    }
}
