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
import java.util.HashMap;



/**
 * Getting here via a character mapping in a parent CharTable, if tokenType is defined,
 * then we have identified a token type. Note however, that this CharTable may map
 * certain characters on to other CharTables and that the longest match is always sought for.
 */
public class CharTable {

    private Integer tokenType;  // getting here via a CharTable mapping identifies a token type
    private HashMap<Character, CharTable> map=new HashMap<Character, CharTable>();
    private CharTable defaultMapping;

    public void setTokenType (Integer tokenType) {
        this.tokenType=tokenType;
    }
    public void setMapping (String chars, CharTable ct) {
        for (int i=0; i<chars.length(); i++) {
            char c=chars.charAt(i);
            map.put(c, ct);
        }
    }

    public void setDefaultMapping (CharTable ct) {
        defaultMapping=ct;
    }

    public void addToken (String token, int tokenType) {
        if (token.length()==0) {
            if (this.tokenType != null) {
                throw new RuntimeException("tokenType already assigned");
            }
            this.tokenType=tokenType;
        } else {
            char c=token.charAt(0);
            String restToken=token.substring(1);

            CharTable sub=map.get(token.charAt(0));
            if (sub==null) {
                sub=new CharTable();
                map.put(c, sub);
            }
            sub.addToken(restToken, tokenType);
        }
    }

//    public Integer parseOld (CharSource source) {
//        if (source.eol()) {
//            return tokenType; // may be null
//        }
//        char nextChar=source.getChar();
//        CharTable next=map.get(nextChar);
//        if (next==null) next=defaultMapping;
//
//        if (next==null) {
//            // no more possible solutions
//            source.ungetChar();
//            return tokenType; // may be null
//        }
//        Integer result=next.parse(source);
//        if (result==null) {
//            source.ungetChar();
//            return tokenType;
//        } else {
//            return result;
//        }
//    }

    public Integer parse (CharSource source) {
        int charCount=0;
        for(;;) {
            if (source.eol()) {
                if (tokenType==null) {
                    source.ungetChar(charCount);
                }
                return tokenType;
            }
            
            char nextChar=source.getChar();
            CharTable next=map.get(nextChar);
            if (next==null) next=defaultMapping;
    
            if (next==null) {
                // no more possible solutions
                source.ungetChar(); // unget nextChar
                if (tokenType == null) {
                    // neither does this map, so undo all iteration characters
                    source.ungetChar(charCount);
                }
                return tokenType; // may be null
            }
            if (next==this) {
                // Avoid deep recursion for typically long strings.
                // Also includes integers and identifiers
                charCount++;
                continue;
            }
            Integer result=next.parse(source);
            if (result==null) {
                source.ungetChar(); // nextChar led nowhere
                if (tokenType==null) {
                    // neither did any repeat matching inside this method
                    source.ungetChar(charCount);
                }
                return tokenType;
            } else {
                return result;
            }

        }
    }

 
}
