package testutils.utils;

/**
 *  Thrown when serious errors in source tree is discovered.  
 *  For example when an instantiated Template is missing.
 */
public class CriticalPTException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4039814106080793645L;
	public CriticalPTException(String msg) {
		super(msg);
	}
	
	public CriticalPTException() {
		super();
	}
}
