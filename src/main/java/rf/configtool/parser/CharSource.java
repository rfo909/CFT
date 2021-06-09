/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

package rf.configtool.parser;
import java.util.*;

/**
 * Outputs newline chars at end of lines regardless of whether the 
 * lines added have CRLF, LF or none of those at the end.
 */
public class CharSource  {
    private List<String> lines=new ArrayList<String>();
    private List<Integer> lineLengths=new ArrayList<Integer>();
    private List<SourceLocation> sourceLocations=new ArrayList<SourceLocation>();
    
    // Current position = next character
    private int lineNo=0;
    private int pos=0;
    
    
    public CharSource() {
    }
    
    
    public void addLine (String line, SourceLocation loc) {
        //System.out.println("addLine: " + line + " " + loc.toString());
        if (line.endsWith("\n")) line=line.substring(0,line.length()-1);
        if (line.endsWith("\r")) line=line.substring(0,line.length()-1);
        
        line=line+"\n";  
            // ensures all lines at least one character, which in turn means that
            // startpos (0,0) is always valid
        
        this.lines.add(line);
        this.lineLengths.add(line.length());
        this.sourceLocations.add(loc);
    }
    
    public CharSourcePos getPos() {
        return new CharSourcePos(lineNo,pos);
    }
    
    public void setPos (CharSourcePos csp) {
        this.lineNo=csp.getLineNo();
        this.pos=csp.getPos();
    }
    
    public SourceLocation getSourceLocation(CharSourcePos pos) {
        int ln=pos.getLineNo();
        int po=pos.getPos();
        return this.sourceLocations.get(ln).pos(po);
    }
    
    public boolean eof() {
        return (lineNo >= lines.size());
    }
    
    
    
    /**
     * Move to start of character stream
     */
    public void reset() {
        this.lineNo=0;
        this.pos=0;
    }
    
    
    public char getChar() {
        if (eof()) throw new RuntimeException("end of data");
        
        String currLine=lines.get(lineNo);
        char c=currLine.charAt(pos);
        pos=pos+1;
        if (pos>=lineLengths.get(lineNo)) {
            lineNo++;
            pos=0;
        }
        return c;
    }
    
    
    public void ungetChar() {
        pos--;
        if (pos < 0) {
            lineNo=lineNo-1;
            if (lineNo < 0) {
                throw new RuntimeException("ungetChar underflow");
            }
            pos=lineLengths.get(lineNo)-1;
        }
    }
    
    public void ungetChar(int count) {
        for (int i=0; i<count; i++) ungetChar();
    }
    
    
    /**
     * Get sequence of chars between two positions
     */
    public String getChars(CharSourcePos from, CharSourcePos to) {
        int fromLine=from.getLineNo();
        int fromPos=from.getPos();
        
        int toLine=to.getLineNo();
        int toPos=to.getPos();

        if (toLine==fromLine) return lines.get(fromLine).substring(fromPos,toPos);
        
        if (toLine<fromLine || (toLine==fromLine && toPos <= fromPos)) throw new RuntimeException("Invalid interval: " + from + " to " + to);

        StringBuffer sb=new StringBuffer();
        for(;;) {
            if (fromLine==toLine && fromPos==toPos) break;
            
            sb.append(lines.get(fromLine).charAt(fromPos));
            fromPos++;
            if (fromPos >= lineLengths.get(fromLine)) {
                fromLine++;
                fromPos=0;
            }
        }
        return sb.toString();
    }
    
    public String getChars (CharSourcePos from) {
        return getChars(from, getPos());
    }
    

    
}
