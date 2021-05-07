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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.data.ProgramLine;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.reporttool.Report;
import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.TokenStream;
import rf.configtool.util.Hash;

/**
 * Function body consists of code lines. 
 *
 */
public class CodeLines {
    
    public static final String PIPE_SYMBOL="|"; // separates multiple ProgramLines on same line
    
    private static CodeLinesParseCache clpCache=new CodeLinesParseCache();

    private List<CodeLine> codeLines;
    private String hashString;
    
    
    public CodeLines (String singleLine, SourceLocation loc) {
        //SourceLocation loc=new SourceLocation("<>", 0, 0);
        codeLines=new ArrayList<CodeLine>();
        codeLines.add(new CodeLine(loc,"")); // blank line between previous function and this one
        codeLines.add(new CodeLine(loc,singleLine));
    }

    public CodeLines (List<CodeLine> saveFormat) {
        this.codeLines=saveFormat;
    }
    
    public void update (String singleLine, SourceLocation loc) {
        //SourceLocation loc=new SourceLocation("<>", 0, 0);
        
        // keep initial non-code lines, if present
        List<CodeLine> x=new ArrayList<CodeLine>();
        for (CodeLine s:codeLines) {
            if (s.isWhitespace()) {
                x.add(s);
            } else {
                break;
            }
        }
        if (x.size()==0) x.add(new CodeLine(loc,"")); // at least one empty line before function body
        
        // then add the new single line, without any attempts at breaking it up
        x.add(new CodeLine(loc,singleLine));
        this.codeLines=x;
        this.hashString=null;
    }
        
    public List<String> getSaveFormat() {
        List<String> list=new ArrayList<String>();
        for (CodeLine c:codeLines) {
            if (c.getType()==CodeLine.TYPE_LINE_GENERATED) continue; // write NORMAL and ORIGINAL
            list.add(c.getLine());
        }
        return list;
    }
  
    public String getFirstNonBlankLine () {
        for (CodeLine s:codeLines) {
            if (s.isWhitespace()) continue;
            return s.getLine();
        }
        return ("<no code>");
    }
    
    public boolean hasMultipleCodeLines() {
        int count=0;
          for (CodeLine s:codeLines) {
            if (s.isWhitespace()) continue;
            count++;
            if (count > 1) return true;
        }
        return false;
        
    }
    
     private TokenStream getTokenStream () throws Exception {
        Parser p=new Parser();
        for (CodeLine cl:codeLines) {
            if (cl.getType()==CodeLine.TYPE_LINE_ORIGINAL) continue; // only execute NORMAL and GENERATED
            p.processLine(cl);
        }
        return p.getTokenStream();
     }
    
     
     private synchronized String getHash() throws Exception {
        if (this.hashString==null) {
            Hash hash=new Hash();
            for (CodeLine cl:codeLines) {
                if (cl.getType()==CodeLine.TYPE_LINE_ORIGINAL) continue;
                hash.add(cl.getLine().getBytes("UTF-8"));
            }
            this.hashString=hash.getHashString();
        }
        return this.hashString;
     }
     
     public List<ProgramLine> getProgramLines () throws Exception {
        String key=getHash();
        List<ProgramLine> progLines=clpCache.get(key);
        
        if (progLines==null) {
          TokenStream ts=getTokenStream();
          progLines=new ArrayList<ProgramLine>();
          for(;;) {
              progLines.add(new ProgramLine(ts));
              if (ts.matchStr(PIPE_SYMBOL)) continue;
              break;
          }
            clpCache.put(key, progLines);
        }
         
      return progLines;
     }
     
     

    
    public Value execute (Stdio stdio, ObjGlobal objGlobal, FunctionState functionState) throws Exception {

        if (functionState==null) functionState=new FunctionState();
        
        
        List<ProgramLine> progLines=getProgramLines();

        Value retVal=null;
        
        for (ProgramLine progLine:progLines) {
            Ctx ctx=new Ctx(stdio, objGlobal, functionState);
            if (retVal != null) ctx.push(retVal);
            
            progLine.execute(ctx);
            
            OutText outText=ctx.getOutText();
    
             // Column data is formatted to text and added to outData as String objects
            List<List<Value>> outData=outText.getData();
            Report report=new Report();
            List<String> formattedText=report.formatDataValues(outData);
            for (String s:formattedText) {
                ctx.getOutData().out(new ValueString(s));
            }
            
            retVal=ctx.getResult();
        }
        return retVal;
    }

}

