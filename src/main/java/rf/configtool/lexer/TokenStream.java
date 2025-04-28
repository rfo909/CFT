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

import java.io.PrintStream;
import java.util.List;
import java.util.StringTokenizer;

import rf.configtool.main.ParseException;

public class TokenStream {
    
    private List<Token> tokens;

    public TokenStream (List<Token> tokens) {
        this.tokens=tokens;
    }

    // --- stream methods for matching tokens - called by Parser ---

    // note: the TYP_EOF token can only be explicitly matched, and so we
    // trust that we never run out of bounds of the tokens vector.

    private int pos=0;

    private Token curr() throws ParseException {
        if (pos < tokens.size()) return tokens.get(pos);
        throw new ParseException("TokenStream: out of tokens");
    }

    private Token curr(int offset) throws ParseException {
        if (pos+offset < tokens.size()) return tokens.get(pos+offset);
        throw new ParseException("TokenStream: out of tokens");
    }

    public int getCurrPos() {
        return pos;
    }
    
    public void setCurrPos(int currPos) {
        pos=currPos;
    }
    
    public int getTokenCount() {
        return tokens.size();
    }

    public Token getTokenAtPos (int pos) {
        return tokens.get(pos);
    }
    /** 
     * peekStr checks the string representation of the following token, 
     * EXCEPT when the following token is of string type,
     */
    public boolean peekStr(String str) throws ParseException {
        Token t=curr();
        return (t.matchStr(str));
    }
    
    public boolean peekStr(int offset, String str) throws ParseException {
        Token t=curr(offset);
        return (t.matchStr(str));
    }
    

    public boolean peekType (int type) throws ParseException {
        Token t=curr();
        return t.matchType(type);
    }
    
    public boolean peekType (int offset, int type) throws ParseException {
        Token t=curr(offset);
        return t.matchType(type);
    }
    
    public String showNextTokens (int count) {
        StringBuffer sb=new StringBuffer();
        for (int i=0; i<count; i++) {
            int p=pos+i;
            if (p < tokens.size()) {
                if (i > 0) sb.append(' ');
                sb.append("[" + tokens.get(p).getStr() + "]");
            }
        }
        return "NextTokens: " + sb.toString();
    }

    /**
     * Match any REGULAR token
     */
    public Token matchAnyToken(String errMsg) throws ParseException {
        if (atEOF()) {
            throw new ParseException("(at EOF) " + errMsg + " " + showNextTokens(2));
        }
        Token t=curr();
        pos++;
        return t;
    }

    public boolean atEOF () throws ParseException {
        return curr().getType()==Token.TOK_EOF;
    }
    
    
    public SourceLocation getSourceLocation() throws ParseException {
        return curr().getSourceLocation();
    }
    
    public Token matchType(int type, String errMsg) throws ParseException {
        Token t=curr();
        if (t.matchType(type)) {
            pos++;
            return t;
        }
        if (errMsg != null) {
            throw new ParseException(t.getSourceLocation(), errMsg + " " + showNextTokens(2));
        }
        return null;
    }

    public Token matchType(int type) throws ParseException {
        return matchType(type,null);
    }

    public Token matchStr(String str, String errMsg) throws ParseException {
        Token t=curr();

        // Can not match string literal content by calling matchStr() or peekStr - string tokens can
        // only be matched by token type ...
        //
        // This is handled inside Token.matchStr() 

        if (t.matchStr(str)) {
            pos++;
            return t;
        }
        if (errMsg != null) {
            throw new ParseException(t.getSourceLocation(),errMsg + " " + showNextTokens(2));
        }
        return null;
    }

    public boolean matchStr(String str) throws ParseException {
        return (matchStr(str,null) != null);
    }
    
    // Match several tokens by string representation. The tokens are given in the str parameter,
    // while the sep is the separator character(s). If no match, the currPos is unchanged, otherwise
    // it is advanced past all matching tokens.
    public boolean matchStrings(String str, String sep) throws Exception {
        StringTokenizer st=new StringTokenizer(str, sep, false);
        int p=pos;
        while (st.hasMoreTokens()) {
            if (!matchStr(st.nextToken())) {
                // reset position
                pos=p;
                return false;
            }
        }
        return true;
    }

    public String matchIdentifier(String errMsg) throws ParseException {
        Token t=matchType(Token.TOK_IDENTIFIER, errMsg);
        if (t != null) return t.getStr();
        return null;
    }

    public Long matchInt(String errMsg) throws ParseException {
        Token t=matchType(Token.TOK_INT, errMsg);
        if (t != null) return Long.parseLong(t.getStr());
        return null;
    }

    public Double matchFloat(String errMsg) throws Exception {
        Token t=matchType(Token.TOK_FLOAT, errMsg);
        if (t != null) return Double.parseDouble(t.getStr());
        return null;
    }

    public String matchIdentifier() throws Exception {
        Token t=matchType(Token.TOK_IDENTIFIER, null);
        if (t != null) return t.getStr();
        return null;
    }

    public void back(int count) {
        pos-=count;
    }

    public void back() {
        back(1);
    }

    /**
     * Report error on the location of the last token consumed
     */
    public String error(String errMsg) throws ParseException {
        int tPos=pos;
        if (tPos > 0) tPos--; // errors occur AFTER processing something
        Token t=tokens.get(tPos);
        return t.getSourceLocation() + " " + errMsg;
    }

    public SourceLocation getCurrLoc() throws ParseException {
        Token t=curr();
        return t.getSourceLocation();
    }


    
}
