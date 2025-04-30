package rf.xlang.main;

import java.io.*;
import rf.configtool.lexer.*;
import rf.configtool.main.ScriptSourceLine;

import rf.xlang.parsetree.*;

public class Main {

    public static void main (String[] argsArray) throws Exception {
        Parser parser=new Parser();
        for (String file:argsArray) {
            parser.addSourceFile(new File(file));
        }
        CodeFile topLevel = parser.parse();
        for (CodeTupleType type : topLevel.getTypes()) {
            System.out.println("type " + type.getName());
        }
        for (CodeFunction function : topLevel.getFunctions()) {
            System.out.println("def " + function.getFunctionName());
        }
    }



}