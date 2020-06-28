package rf.configtool.parser;

import java.io.*;
import java.util.ArrayList;

import rf.configtool.main.CodeLine;

public class Parser {
    
    private CharTable root;
    private ArrayList<Token> tokens=new ArrayList<Token>();

    public Parser() throws Exception {
        this.root=createGraph();
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
        CharSource source=new CharSource(line);
        
        SourceLocation loc=cl.getLoc();
        while (!source.eol()) {
            int startPos=source.getPos();
            Integer tokenType=root.parse(source);
            if (tokenType==null) {
                throw new Exception(cl.getLoc() + ": Parse failed at position " + startPos + " (char=" + source.getChar());
            }
            if (tokenType < 0) {
                // ignore whitespace and comments
                continue;
            }
            int nextPos=source.getPos();
            String tokenString=line.substring(startPos,nextPos);
            SourceLocation loc2=loc.pos(startPos+1);
            tokens.add(new Token(loc2, tokenType, tokenString));
        }

    }

    private CharTable createGraph () {
        CharTable root=new CharTable();

        // --- whitespace ---
        CharTable space=new CharTable();
        space.setTokenType(-1);
        root.setMapping(" \t\r\n", space);
        space.setMapping(" \t\r\n", space);  // group together lumps of ws

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
        CharTable numFloat=new CharTable();
        numFloat.setTokenType(Token.TOK_FLOAT);
        numInt.setMapping(".", numFloat);
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

        // --- special symbols ---
        // sequence is irrelevant, always trying to match as much as possible
        String[] specials={
                "|", "...", "..", "->", "<-", "&&", "||", ">=", "<=", ">", "<", "==", "!=", "!", "<>",
                "=", "{", "}", "[", "]", "&", "(", ")", ".", ";", ":", ",",
                "?", "+", "-", "*", "/", "%", "$", "^", "<<<", "@"
        };

        for (String s:specials) {
            root.addToken(s, Token.TOK_SPECIAL);
        }

        return root;
    }
    
    
    

}
