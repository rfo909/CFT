package rf.configtool.parser;

public class CharSource  {
    private String s;
    private int pos=0;

    public CharSource (String s) { this.s=s; }
    public int getPos() {
        return pos;
    }
    public char getChar() {
        if (eol()) throw new RuntimeException("end of line");
        return s.charAt(pos++);
    }
    public void ungetChar() {
        pos--;
    }
    public void ungetChar(int count) {
        pos-=count;
    }
    public boolean eol () {
        return (pos >= s.length());
    }
}
