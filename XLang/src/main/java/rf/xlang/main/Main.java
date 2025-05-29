package rf.xlang.main;

import java.io.*;
import rf.xlang.lexer.*;
import rf.xlang.parsetree.*;
import rf.xlang.main.runtime.*;
import java.util.*;
import java.io.*;

public class Main {
    
    public static void interactive (Lexer lexer) throws Exception {
        System.out.println("Enter lines of code inside main() - blank line to execute");
        try (
            BufferedReader stdin=new BufferedReader(new InputStreamReader(System.in));
        ) {

            List<String> inputLines=new ArrayList<>();
            inputLines.add("def main () {");

            for(;;) {
                String line=stdin.readLine();
                if (line.trim().length()==0) break;
                inputLines.add(line);
            }
            inputLines.add("}");

            int lineNo=0;
            for (String s : inputLines) { 
                processInputLine(lexer, s, lineNo);
                lineNo++;
            }
        }
    }
    

    public static void main (String[] argsArray) throws Exception {
        Lexer lexer = new Lexer();
        boolean isInteractive = false;
        for (String file:argsArray) {
            if (file.equals("-i")) {
                isInteractive=true;
            } else {
                processFile(lexer, file);
            }
        }
        if (isInteractive) {
            interactive(lexer);
        } 
        executeCode(lexer);
    }
    
    private static void executeCode (Lexer lexer) throws Exception {
        TokenStream ts = lexer.getTokenStream();
        Code code=new Code(ts);

        // create ObjGlobal
        ObjGlobal objGlobal=new ObjGlobal();
        for (TupleType type : code.getTupleTypes()) {
            objGlobal.addTupleType(type);
        }
        for (CodeFunction func : code.getCodeFunctions()) {
            objGlobal.addCodeFunction(func);
        }

        // invoke "main" function
        Ctx ctx=new Ctx(objGlobal);
        CodeFunction mainFunc = objGlobal.getCodeFunction("main");
        if (mainFunc==null) throw new Exception("No script function 'main' found");

        List<Value> mainArgs=new ArrayList<Value>();
        Value returnValue = mainFunc.execute(ctx, mainArgs);

        System.out.println("Returned from script function 'main': " + returnValue.getValAsString());

        List<String> lines = objGlobal.getSystemMessages();
        for (String line : lines) System.out.println("# " + line);

    }

    private static void processFile (Lexer lexer, String filename) throws Exception {
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
    }
 
    
    private static void processInputLine (Lexer lexer, String line, int lineNo) throws Exception {
        line=line.trim();
        SourceLocation loc = new SourceLocation("<input>", lineNo, 0);
        lexer.processLine(new ScriptSourceLine(loc, line));
    }
 
    
}
