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

package rf.configtool.main;

import rf.configtool.parser.SourceLocation;

/**
 * Code lines initially are just the combination of a source location plus a line, but as
 * we rewrite code when loading from file (CodeHistory.load()), we need to store both
 * the original lines and the generated lines. 
 * 
 * When parsing for execution, it is the "normal" lines plus the "generated" lines that are used. See CodeLines.getTokenStream().
 * However, when writing original code to the save file, we use "normal" and "original" lines, and ignore "generated". See CodeLines.getSaveFormat()
 * 
 * A "normal" line is one that is read from file and parsed.
 *
 */
public class CodeLine {
	public static final int TYPE_LINE_NORMAL = 0;
	public static final int TYPE_LINE_ORIGINAL = 1;
	public static final int TYPE_LINE_GENERATED = 2;
	
    private SourceLocation loc;
    private String line;
    private int type;
    
    public boolean isWhitespace()  {
        return line.trim().length()==0;
    }
    
    public CodeLine(SourceLocation loc, String line) {
    	this(loc,line,TYPE_LINE_NORMAL);
    }
    
    public CodeLine(SourceLocation loc, String line, int type) {
        super();
        this.loc = loc;
        this.line = line;
        this.type = type;
    }
    public SourceLocation getLoc() {
        return loc;
    }
    public String getLine() {
        return line;
    }
    public int getType() {
    	return type;
    }


}
