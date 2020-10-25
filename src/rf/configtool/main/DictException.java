package rf.configtool.main;

import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.parser.SourceLocation;

/**
 * Exception thrown by global function throw() - caught by global expression tryCatchDict()
 */
public class DictException extends Exception{
	
	private String msg;
	private ObjDict dict;
	
	public DictException (String msg, ObjDict dict) {
		super("[DictException] " + msg);
		this.msg=msg;
		this.dict=dict;
	}
	
	// Leaving getMessage() to return the message composed in constructor

	
	public String getDictExceptionMessage() {
		return msg;
	}
	
	public ObjDict getDict() {
		return dict;
	}

	public String toString() {
		return "[DictException] " + msg;
	}
	
	

}
