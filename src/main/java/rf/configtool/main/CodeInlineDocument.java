/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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

import rf.configtool.lexer.SourceLocation;
import rf.configtool.main.runtime.ValueString;

/**
 * <<< EOF
 * ...
 * >>> EOF
 */
public class CodeInlineDocument {
    private String eofMark;
    private List<String> lines;
    private SourceLocation loc;
    
    public CodeInlineDocument (String eofMark, SourceLocation loc) {
        this.lines=new ArrayList<String>();
        this.eofMark=eofMark;
        this.loc=loc;
    }

    public void addLine (String line) {
        lines.add(line);
    }
    
    public boolean matchesEofMark (String eof) {
        return eof.equals(eofMark);
    }
    
    public String createCodeLine () {
        StringBuffer sb=new StringBuffer();
        sb.append("List(");
        boolean comma=false;
        for (String s:lines) {
            if (comma) sb.append(",");
            try {
                sb.append((new ValueString(s)).synthesize());
            } catch (Exception ex) {
                throw new RuntimeException("Should never happen");
            }
            comma=true;
        }
        sb.append(")");
        return sb.toString();
    }


}
