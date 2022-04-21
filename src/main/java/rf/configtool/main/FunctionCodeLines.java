/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.reporttool.Report;
import rf.configtool.parsetree.ProgramCodeSpace;
import rf.configtool.util.Hash;

/**
 * CodeLines is a container for a list of CodeLine objects, which represent
 * lines of code for a function, or a single line of input from the user, etc.
 * 
 * The text lines are tokenized via getTokenStream() method, then split into
 * a sequence ProgramLine objects, each representing what is called "loop space"
 * or "code space" in the docs. Function code is split into many such by the PIPE_SYMBOL.
 * 
 * The ProgramLine class constructor identifies code as a sequence of statements, and
 * builds a complete parse tree from the code.
 * 
 * In the execute() method, the sequence of ProgramLine objects are executed, and a
 * result value is produced.
 *
 */
public class FunctionCodeLines {

    public static final String PIPE_SYMBOL = "|"; // separates multiple ProgramLines on same line

    private static CodeLinesParseCache clpCache = new CodeLinesParseCache();

    private List<ScriptSourceLine> sourceLines;
    private String hashString;

    public FunctionCodeLines(String singleLine, SourceLocation loc) {
        // SourceLocation loc=new SourceLocation("<>", 0, 0);
        sourceLines = new ArrayList<ScriptSourceLine>();
        sourceLines.add(new ScriptSourceLine(loc, "")); // blank line between previous function and this one
        sourceLines.add(new ScriptSourceLine(loc, singleLine));
    }

    public FunctionCodeLines(List<ScriptSourceLine> saveFormat) {
        this.sourceLines = saveFormat;
    }

    public SourceLocation getSourceLocation() {
    	for (ScriptSourceLine cl:sourceLines) {
    		if (cl.getLoc() != null) return cl.getLoc();
    	}
    	return null;
    }
    
    public void update(String singleLine, SourceLocation loc) {
        // SourceLocation loc=new SourceLocation("<>", 0, 0);

        // keep initial non-code lines, if present
        List<ScriptSourceLine> x = new ArrayList<ScriptSourceLine>();
        for (ScriptSourceLine s : sourceLines) {
            if (s.isWhitespace()) {
                x.add(s);
            } else {
                break;
            }
        }
        if (x.size() == 0)
            x.add(new ScriptSourceLine(loc, "")); // at least one empty line before function body

        // then add the new single line, without any attempts at breaking it up
        x.add(new ScriptSourceLine(loc, singleLine));
        this.sourceLines = x;
        this.hashString = null;
    }

    public List<String> getSaveFormat() {
        List<String> list = new ArrayList<String>();
        for (ScriptSourceLine c : sourceLines) {
            if (c.getType() == ScriptSourceLine.TYPE_LINE_GENERATED)
                continue; // write NORMAL and ORIGINAL
            list.add(c.getLine());
        }
        return list;
    }

    public String getFirstNonBlankLine() {
        for (ScriptSourceLine s : sourceLines) {
            if (s.isWhitespace())
                continue;
            return s.getLine();
        }
        return ("<no code>");
    }

    public boolean hasMultipleCodeLines() {
        int count = 0;
        for (ScriptSourceLine s : sourceLines) {
            if (s.isWhitespace())
                continue;
            count++;
            if (count > 1)
                return true;
        }
        return false;

    }

    /**
     * Parse the sequence of CodeLine objects, and return TokenStream
     */
    private TokenStream getTokenStream() throws Exception {
        Lexer lexer = new Lexer();
        for (ScriptSourceLine cl : sourceLines) {
            if (cl.getType() == ScriptSourceLine.TYPE_LINE_ORIGINAL)
                continue; // only execute NORMAL and GENERATED
            lexer.processLine(cl);
        }
        return lexer.getTokenStream();
    }

    private synchronized String getHash() throws Exception {
        if (this.hashString == null) {
            Hash hash = new Hash();
            for (ScriptSourceLine cl : sourceLines) {
                if (cl.getType() == ScriptSourceLine.TYPE_LINE_ORIGINAL)
                    continue;
                hash.add(cl.getLine().getBytes("UTF-8"));
            }
            this.hashString = hash.getHashString();
        }
        return this.hashString;
    }

    /**
     * Return code as sequence of ProgramCodeSpace objects, created by
     * splitting code body by PIPE. This involves invoking the recursive
     * descent parser, building a parse tree 
     */
    public List<ProgramCodeSpace> getCodeSpaces() throws Exception {
        String key = getHash();
        List<ProgramCodeSpace> progLines = clpCache.get(key);

        if (progLines == null) {
            TokenStream ts = getTokenStream();
            progLines = new ArrayList<ProgramCodeSpace>();
            for (;;) {
                progLines.add(new ProgramCodeSpace(ts));
                if (ts.matchStr(PIPE_SYMBOL))
                    continue;
                break;
            }
            clpCache.put(key, progLines);
        }

        return progLines;
    }
}
