/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rf.xlang.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.List;
import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.ScriptSourceLine;
import rf.xlang.parsetree.CodeFile;

/**
 *
 * @author roar
 */
public class Parser {
    
    private Lexer lexer;
    
    public Parser () throws Exception {
        lexer = new Lexer();
    }
    
    public void addSourceFile (File file) throws Exception {
        try (BufferedReader br=new BufferedReader(new FileReader(file))) {
            int lineNo=1;
            for(;;) {
                String line=br.readLine();
                if (line==null) break;
                line=line.trim();
                SourceLocation loc = new SourceLocation(file.getAbsolutePath(), lineNo, 0);
                lexer.processLine(new ScriptSourceLine(loc, line));

                lineNo++;
            }
        }
    }
    
    public void addInlineCode (String asFunction, List<String> lines) throws Exception {
        int lineNo=0;
        for (String line:lines) {
            SourceLocation loc = new SourceLocation("def " + asFunction, lineNo, 0);
            if (lineNo==0) {
                lexer.processLine(new ScriptSourceLine(loc, "def " + asFunction + " () {"));
                lineNo++;
            }
            lexer.processLine(new ScriptSourceLine(loc, line));
            lineNo++;
        }
        
        SourceLocation loc = new SourceLocation("def " + asFunction, lineNo, 0);
        lexer.processLine(new ScriptSourceLine(loc,"}"));
    }
    
    public CodeFile parse () throws Exception {
        TokenStream ts = lexer.getTokenStream();
        return new CodeFile(ts);
    }
    
}
