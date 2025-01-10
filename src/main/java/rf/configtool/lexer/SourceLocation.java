/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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

package rf.configtool.lexer;

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
    
    public SourceLocation (String file, int line) {
        this.file=file;
        this.line=line;
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
