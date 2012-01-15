package javaworld;

public class OperationImpossible extends Exception {
    String reason;

    public OperationImpossible( String reason_ ) {
        reason = reason_;
    }

    public String getReason() {
        return reason;
    }

}
