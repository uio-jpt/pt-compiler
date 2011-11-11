package javaworld;

public class TypeConstraintFailed extends Exception {
    String failingTypeName;
    String constraintName;
    String reason;

    static String constructMessage( String failingTypeName, String constraintName, String reason ) {
        String myName = failingTypeName;
        if( myName == null ) {
            myName = "type";
        }
        if( constraintName == null ) {
            return myName + " does not satisfy constraints: " + reason;
        }
        return myName + " does not satisfy constraint " + constraintName + ": " + reason;
    }

    TypeConstraintFailed( String failingTypeName, String constraintName, String reason ) {
        super( constructMessage( failingTypeName, constraintName, reason ) );
        this.failingTypeName = failingTypeName;
        this.constraintName = constraintName;
        this.reason = reason;
    }

    public boolean hasTypeName() {
        return failingTypeName != null;
    }

    public boolean hasConstraintName() {
        return constraintName != null;
    }
}
