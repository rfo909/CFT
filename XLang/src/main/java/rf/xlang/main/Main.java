package rf.xlang.main;

import java.io.*;
import rf.xlang.lexer.*;
import rf.xlang.parsetree.*;
import rf.xlang.main.runtime.*;
import java.util.*;

public class Main {
    
    public static void main (String[] argsArray) throws Exception {
        Lexer lexer = new Lexer();
        for (String file:argsArray) {
            processFile(lexer, file);
        }
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
 
    
}
