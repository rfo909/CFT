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

import java.io.*;
import java.util.ArrayList;

import rf.configtool.main.CodeLine;
import rf.configtool.main.SourceException;

public class Parser {

	private static CharTable cachedRoot;
	private static synchronized CharTable getCachedRoot() {
		if (cachedRoot==null) cachedRoot=createGraph();
		return cachedRoot;
	}
    
    private CharTable root;
    private ArrayList<Token> tokens=new ArrayList<Token>();
    
    

    public Parser() throws Exception {
        this.root=getCachedRoot();
    }

    public TokenStream getTokenStream() {
        tokens.add(new Token(new SourceLocation(), Token.TOK_EOF, "<EOF>"));
        return new TokenStream(tokens);
    }
    
    public void processFile (String file) throws Exception {
        BufferedReader br=new BufferedReader(new FileReader(file));

        for (int lineNo=1; true; lineNo++) {
            String line=br.readLine();
            if (line==null) break;
            processLine(new CodeLine(new SourceLocation(file, lineNo, 0), line));
            lineNo++;
        }
        
        br.close();
    }

    public void processLine (CodeLine cl) throws Exception {
        String line=cl.getLine();
        CharSource source=new CharSource();
        SourceLocation loc=cl.getLoc();

        source.addLine(line, loc);
        
        while (!source.eof()) {
        	CharSourcePos startPos=source.getPos();
            Integer tokenType=root.parse(source);
            if (tokenType==null) {
                throw new SourceException(cl.getLoc(), "Parse failed at position " + startPos);
            }
            if (tokenType < 0) {
                // ignore whitespace and comments
                continue;
            }
            //int nextPos=source.getPos();
            String tokenString=source.getChars(startPos); // line.substring(startPos,nextPos);
            SourceLocation loc2=source.getSourceLocation(startPos);
            tokens.add(new Token(loc2, tokenType, tokenString));
        }

    }

    private static CharTable createGraph () {
        CharTable root=new CharTable();

        // --- whitespace ---
        CharTable space=new CharTable();
        space.setTokenType(-1);
        root.setMapping(" \t\r\n", space);

        // --- comments ---
        CharTable comment=new CharTable();
        root.setMapping("#", comment);
        comment.setDefaultMapping(comment); // eat rest of line
        comment.setTokenType(-1);

        // --- identifiers ---
        CharTable ident=new CharTable();
        root.setMapping("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_", ident);
        ident.setMapping("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_", ident);
        ident.setTokenType(Token.TOK_IDENTIFIER);

        // --- integers (positive) ---
        CharTable numInt=new CharTable();
        numInt.setTokenType(Token.TOK_INT);
        root.setMapping("0123456789", numInt);
        numInt.setMapping("0123456789", numInt);
        
        // --- floats ---
        CharTable dot=new CharTable();
        CharTable numFloat=new CharTable();
        numInt.setMapping(".", dot);
        dot.setMapping("0123456789", numFloat);
        numFloat.setTokenType(Token.TOK_FLOAT);
        numFloat.setMapping("0123456789", numFloat);
        
        // --- strings ---
        String[] startEndStrings={"\"\"", "''"};
        for (String startEnd:startEndStrings) {
            String start=""+startEnd.charAt(0);
            String end=""+startEnd.charAt(1);

            CharTable stringBody=new CharTable();
            CharTable stringComplete=new CharTable();

            root.setMapping(start, stringBody);
            stringBody.setDefaultMapping(stringBody);
            stringBody.setMapping(end, stringComplete);
            stringComplete.setTokenType(Token.TOK_STRING);
        }

        // --- raw strings ---
        CharTable rawString=new CharTable();
        root.setMapping("@", rawString);
        rawString.setDefaultMapping(rawString); // eat rest of line
        rawString.setTokenType(Token.TOK_RAW_STRING);

        // --- special symbols ---
        // sequence is irrelevant, always trying to match as much as possible
        String[] specials={
                "|", "...", "..", "->", "<-", "&&", "||", ">=", "<=", ">", "<", "==", "!=", "!", "<>",
                "=", "{", "}", "[", "]", "&", "(", ")", ".", ";", ":", ",",
                "?", "+", "-", "*", "/", "%", "$", "^", "<<<", "=>",
                "\\"
        };

        for (String s:specials) {
            root.addToken(s, Token.TOK_SPECIAL);
        }

        return root;
    }
    
    
    

}
