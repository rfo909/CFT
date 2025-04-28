package rf.xlang.main;

import java.io.*;
import rf.configtool.lexer.*;
import rf.configtool.main.ScriptSourceLine;

import rf.xlang.parsetree.CodeFile;

public class Main {

    public static void main (String[] argsArray) throws Exception {
        for (String file:argsArray) {
            processFile(file);
        }
    }

    private static void processFile (String filename) throws Exception {
        Lexer lexer = new Lexer();
        try (BufferedReader br=new BufferedReader(new FileReader(filename))) {
            int lineNo=1;
            for(;;) {
                String line=br.readLine();
                if (line==null) break;
                line=line.trim();
                SourceLocation loc = new SourceLocation(filename, lineNo, 0);
                lexer.processLine(new ScriptSourceLine(loc, line));

                lineNo++;
            }
        }
        TokenStream ts = lexer.getTokenStream();
        CodeFile file=new CodeFile(ts);
    }


}