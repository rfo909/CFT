package rf.configtool.parser;

public class CharSourcePos {
	
	private int lineNo, pos;

	public CharSourcePos(int lineNo, int pos) {
		super();
		this.lineNo = lineNo;
		this.pos = pos;
	}

	public int getLineNo() {
		return lineNo;
	}

	public int getPos() {
		return pos;
	}
	
	public String toString() {
		return "(" + lineNo + ", " + pos + ")";
	}

}
