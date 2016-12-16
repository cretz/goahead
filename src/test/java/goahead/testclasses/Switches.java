package goahead.testclasses;

public class Switches {
    public static void main(String[] args) {
        lookupSwitch(1);
        lookupSwitch(35);
        lookupSwitch(300);
        lookupSwitch(305);
        lookupSwitchFallthrough(1);
        lookupSwitchFallthrough(35);
        lookupSwitchFallthrough(300);
        lookupSwitchFallthrough(305);
        tableSwitch(31);
        tableSwitch(32);
        tableSwitch(33);
        tableSwitch(34);
        tableSwitchFallthrough(41);
        tableSwitchFallthrough(42);
        tableSwitchFallthrough(43);
        tableSwitchFallthrough(44);
    }

    public static void lookupSwitch(int val) {
        switch (val) {
            case 1:
                System.out.println("one");
                break;
            case 35:
                System.out.println("thirty five");
                break;
            case -300:
                System.out.println("negative 300");
                break;
            default:
                System.out.println("other");
        }
    }

    public static void lookupSwitchFallthrough(int val) {
        switch (val) {
            case 1:
                System.out.println("one");
            case 35:
                System.out.println("thirty five");
            case -300:
                System.out.println("negative 300");
            default:
                System.out.println("other");
        }
    }

    public static void tableSwitch(int val) {
        switch (val) {
            case 31:
                System.out.println("thirty one");
                break;
            case 32:
                System.out.println("thirty two");
                break;
            case 33:
                System.out.println("thirty three");
                break;
            default:
                System.out.println("other");
        }
    }

    public static void tableSwitchFallthrough(int val) {
        switch (val) {
            case 41:
                System.out.println("forty one");
            case 42:
                System.out.println("<= forty two");
            case 43:
                System.out.println("<= forty three");
            default:
                System.out.println("other");
        }
    }
}
