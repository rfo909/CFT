package rf.configtool.parser;

public class SourceLocation {
    
    private String file;
    private int line;
    private int pos;
    private boolean eof;
    
    public SourceLocation() {
        eof=true;
    }
    
    public SourceLocation (String file, int line, int pos) {
        this.file=file;
        this.line=line;
        this.pos=pos;
    }
    
    public SourceLocation pos (int pos) {
    	return new SourceLocation(file, line, pos);
    }

    public String toString() {
        StringBuffer sb=new StringBuffer();
        if (eof) {
            return "[eof]";
        }
        sb.append("[");
        if (file != null) sb.append(file);
        if (line > 0) {
            if (file != null) sb.append(":");
            sb.append(line);
        }
        if (pos > 0) {
            if (line > 0) sb.append(".");
            if (line <= 0 && file != null) sb.append(":");
            sb.append(pos);
        }
        sb.append("]");
        return sb.toString();
    }
    
}
