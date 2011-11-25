package testutils.tester;

import com.google.common.base.Joiner;

/* Compiler crash during test.
   Handy to be able to continue testall.. */

public class SemanticTotalFailureReport extends SemanticReport {
    public SemanticTotalFailureReport(String name, String[] filenames, Exception totalFailure, boolean containsPTPackage ) {
        super( name, filenames, true, false, "", "", totalFailure.toString(), containsPTPackage  );
    }
}
