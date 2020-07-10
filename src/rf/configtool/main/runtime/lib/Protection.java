package rf.configtool.main.runtime.lib;

/**
 * Protecting files and directories from unintended operations
 */
public class Protection {

	
	public static Protection NoProtection = new Protection();  // private constructor
	
	private String code;
	
	private Protection() {
		code=null;
	}
	
	public Protection(String code) {
		if (code==null) this.code="<no-code>";
		else this.code=code;
	}
	
	public boolean isActive() {
		return (code != null);
	}
	
	public String getCode() {
		return code;
	}

	public void validateDestructiveOperation (String op, String element) throws Exception {
		if (code==null) return;
		throw new Exception("INVALID-OP " + op + " : " + element + " (PROTECTED: " + code + ")");
	}
}
