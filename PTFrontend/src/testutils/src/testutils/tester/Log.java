package testutils.tester;
/*
import testutils.tester.Log;
 */

import java.util.LinkedList;

public class Log {

    // TODO: Maybe use different lib

    private static boolean verbose = true;
    private static boolean initiated = false;

    private static LinkedList<String> keepTags = new LinkedList<String>();
    private static LinkedList<String> removeTags = new LinkedList<String>();

    public static void setVerbose(boolean val) {
        Log.verbose = val;
    }

    public static void keepTag(String tag) {
        initiated = true;
        if (removeTags.contains(tag)) removeTags.remove(tag);
        keepTags.add(tag);
    }

    public static void removeTag(String tag) {
        initiated = true;
        if (keepTags.contains(tag)) keepTags.remove(tag);
        removeTags.add(tag);
    }

    public static void d(Object o, String msg) {
        d(o.getClass().getName(), msg);
    }

    public static void init() {
        initiated = true;
        removeTag("JPT:AstTree");
        removeTag("JPT:PrettyPrint");
    }

    private static void printTag(String tag, String msg) {
        System.out.println(tag + ": " + msg);
    }

    public static void d(String tag, String msg) {
        if (initiated==false) {
            init();
        }

        if (removeTags.contains(tag)) {
            return;
        }
        else if (Log.verbose) {
            printTag(tag, msg);
        }
        else if (keepTags.contains(tag)) {
            printTag(tag, msg);
        }
    }

}
