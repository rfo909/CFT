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

import rf.configtool.parser.Parser;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.TokenStream;

/**
 * Function body consists of code lines. Can also represent
 * function override values
 *
 */
public class CodeLines {
    private List<CodeLine> saveFormat;
    
    public CodeLines (String singleLine, SourceLocation loc) {
        //SourceLocation loc=new SourceLocation("<>", 0, 0);
        saveFormat=new ArrayList<CodeLine>();
        saveFormat.add(new CodeLine(loc,"")); // blank line between previous function and this one
        saveFormat.add(new CodeLine(loc,singleLine));
    }

    public CodeLines (List<CodeLine> saveFormat) {
        this.saveFormat=saveFormat;
    }
    
    public void update (String singleLine, SourceLocation loc) {
        //SourceLocation loc=new SourceLocation("<>", 0, 0);
        
        // keep initial non-code lines, if present
        List<CodeLine> x=new ArrayList<CodeLine>();
        for (CodeLine s:saveFormat) {
            if (s.isWhitespace()) {
                x.add(s);
            } else {
                break;
            }
        }
        if (x.size()==0) x.add(new CodeLine(loc,"")); // at least one empty line before function body
        
        // then add the new single line, without any attempts at breaking it up
        x.add(new CodeLine(loc,singleLine));
        this.saveFormat=x;
    }
        
    public List<String> getSaveFormat() {
        List<String> list=new ArrayList<String>();
        for (CodeLine c:saveFormat) {
        	if (c.getType()==CodeLine.TYPE_LINE_GENERATED) continue; // write NORMAL and ORIGINAL
        	list.add(c.getLine());
        }
        return list;
    }
  
    public String getFirstNonBlankLine () {
        for (CodeLine s:saveFormat) {
            if (s.isWhitespace()) continue;
            return s.getLine();
        }
        return ("<no code>");
    }
    
    public boolean hasMultipleCodeLines() {
        int count=0;
          for (CodeLine s:saveFormat) {
            if (s.isWhitespace()) continue;
            count++;
            if (count > 1) return true;
        }
        return false;
        
    }
    public TokenStream getTokenStream () throws Exception {
        Parser p=new Parser();
        for (CodeLine cl:saveFormat) {
        	if (cl.getType()==CodeLine.TYPE_LINE_ORIGINAL) continue; // only execute NORMAL and GENERATED
        	p.processLine(cl);
        }
        return p.getTokenStream();
    }

}

