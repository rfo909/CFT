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

package rf.configtool.lexer;

public class Token {
    
    public static final int TOK_IDENTIFIER = 1;
    public static final int TOK_INT = 2;
    public static final int TOK_FLOAT = 3;
    public static final int TOK_STRING = 4;
    public static final int TOK_RAW_STRING = 5;  // do not strip quotes, then map to TOK_STRING
    public static final int TOK_SPECIAL = 6;
    public static final int TOK_EOF = 99;
    
    private int type; 
    private String str;
    private String originalStringRep;
    private SourceLocation loc;
    
    
    public Token (SourceLocation loc, int type, String str) {
        this.loc=loc;
        this.originalStringRep=str;
        
        if (type==TOK_STRING) {
            str=str.substring(1,str.length()-1);  // strip quotes
        }
        if (type==TOK_RAW_STRING) {
            if (str.startsWith("@ ") || str.startsWith("@@")) str=str.substring(2);
            else str=str.substring(1);
            while(str.endsWith("\r") || str.endsWith("\n")) str=str.substring(0,str.length()-1);
            type=TOK_STRING;
        }
        this.type=type;
        this.str=str;
    }
    
    public boolean matchStr (String s) {
        if (type==TOK_STRING) return false;
        return str.equals(s);
    }
    
    public boolean matchType (int type) {
        return this.type==type;
    }

    public String getStr() {
        return str;
    }
    
    public int getType() {
        return type;
    }
    
    public SourceLocation getSourceLocation() {
        return loc;
    }
    
    public String getOriginalStringRep() {
        return originalStringRep;
    }
}
