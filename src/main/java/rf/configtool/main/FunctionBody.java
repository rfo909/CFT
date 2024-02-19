/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2023 Roar Foshaug

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
import rf.configtool.parsetree.CodeSpace;
import rf.configtool.util.Hash;
import rf.configtool.util.ReportFormattingTool;

/**
 * This class is a container for a list of ScriptSourceLine objects, which represent
 * lines of text for a function (from file) or a single text line of input from the user, if
 * defined interactively. 
 * 
 * The text lines are tokenized via getTokenStream() method, then split into
 * a sequence CodeSpace objects, each representing what is called "loop space"
 * or "code space" in the docs. Function code is split into many such by the PIPE_SYMBOL.
 * 
 * NOTE that the ScriptSourceLines are a modified version of the lines that are read
 * from file, in that here-documents have been modified into code creating corresponding
 * lists, and so we parse only "NORMAL" and "GENERATED" ScriptSourceLines, not "ORIGINAL",
 * which is used when saving the script, to write it in the same form it was read, before
 * the pre-processing into GENERATED lines where applicable.
 * 
 * The CodeSpace constructor identifies code as a sequence of statements, and
 * builds a complete parse tree from the code.
 * 
 * In the execute() method, the sequence of ProgramLine objects are executed, and a
 * result value is produced.
 *
 */
public class FunctionBody {

    public static final String PIPE_SYMBOL = "|"; // separates sequence of CodeSpace

    private static FunctionBodyParseCache clpCache = new FunctionBodyParseCache();

    private List<ScriptSourceLine> sourceLines;
    private boolean isPrivate;
    private ClassDetails classDetails;
    private String hashString;

    public FunctionBody(String singleLine, SourceLocation loc) {
        this(singleLine, false, loc);
    }

    public FunctionBody(String singleLine, boolean isPrivate, SourceLocation loc) {
        sourceLines = new ArrayList<ScriptSourceLine>();
        sourceLines.add(new ScriptSourceLine(loc, "")); // blank line between previous function and this one
        sourceLines.add(new ScriptSourceLine(loc, singleLine));
        this.classDetails=null; // can not define class functions interactively
        this.isPrivate=isPrivate;
    }
    
    public FunctionBody(List<ScriptSourceLine> saveFormat, boolean isPrivate, ClassDetails classDetails) {
        this.sourceLines = saveFormat;
        this.isPrivate = isPrivate;
        this.classDetails=classDetails; // may be null
    }

    public SourceLocation getSourceLocation() {
        for (ScriptSourceLine cl:sourceLines) {
            if (cl.getLoc() != null) return cl.getLoc();
        }
        return null;
    }
    
    /**
     * Redefine function interactively
     */
    public void redefineFunctionInteractively (String singleLine, SourceLocation loc) {
        // SourceLocation loc=new SourceLocation("<>", 0, 0);
        this.classDetails=null;
        this.isPrivate=false;
        
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
    
    /**
     * If this function is a class function ("constructor") this is the data about the class, which are
     * needed when calling the function, ensuring the creation and return of a self object.
     */
    public ClassDetails getClassDetails() {
        return classDetails;
    }
    
    public boolean isClass() {
        return classDetails != null;
    }
    
    public boolean isPrivate() {
        return this.isPrivate;
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
     * descent parser, building a parse tree for the complete function body.
     */
    public List<CodeSpace> getCodeSpaces() throws Exception {
        String key = getHash();
        List<CodeSpace> progLines = clpCache.get(key);

        if (progLines == null) {
            TokenStream ts = getTokenStream();
            progLines = new ArrayList<CodeSpace>();
            for (;;) {
                progLines.add(new CodeSpace(ts));
                if (ts.matchStr(PIPE_SYMBOL))
                    continue;
                break;
            }
            clpCache.put(key, progLines);
        }

        return progLines;
    }
}
