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

package rf.configtool.root.shell;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.CFTCallStackFrame;
import rf.configtool.main.Ctx;
import rf.configtool.main.FunctionBody;
import rf.configtool.main.FunctionState;
import rf.configtool.main.PropsFile;
import rf.configtool.main.ScriptSourceLine;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBlock;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.parsetree.Expr;

/**
 * Superclass of all Shell command implementations, such as ls, cd, pwd
 */
public abstract class ShellCommand {

    private final List<String> parts;
    private final String name;
    private final List<ShellCommandArg> args;
    
    public ShellCommand (List<String> parts) throws Exception {
        this.parts=parts;
        this.name=parts.get(0);
        this.args=new ArrayList<ShellCommandArg>();
        
        for (int i=1; i<parts.size(); i++) {
            String part=parts.get(i);
            String isExpr=null;
            
            if (part.startsWith("(") && part.endsWith(")")) {
                isExpr=part.substring(1,part.length()-1); // (xxx) -> xxx
            } else if (part.startsWith("%")) {
                // symbol lookup
                isExpr=part;
            } else if (part.startsWith(":")) {
                // identify optional sequence of numbers
                int pos=1; // following the colon
                
                while (pos<part.length() && "0123456789".indexOf(part.charAt(pos)) >= 0) {
                    pos++;
                }
                //System.out.println("---> pos=" + pos);
                String digits=part.substring(1,pos); // may be empty
                String rest=part.substring(pos); // first non-digit char
                if (pos==1) {
                    isExpr="Sys.lastResult" + rest;
                } else {
                    isExpr="Sys.lastResult(" + digits + ")" + rest;
                }
            }
            if (isExpr != null) {
                // create Expr
                Lexer lex=new Lexer();
                SourceLocation loc=new SourceLocation("<ShellCommand>",1);
                lex.processLine(new ScriptSourceLine(loc, isExpr));
                TokenStream ts=lex.getTokenStream();
                Expr expr=new Expr(ts);
                if (!ts.atEOF()) throw new Exception("Invalid expression: " + part);
                
                args.add(new ShellCommandArg(expr));
                continue;
            } else {
                args.add(new ShellCommandArg(part));
            }
            
        }
    }

    protected String getName() {
        return name;
    }
    
    protected List<ShellCommandArg> getArgs() {
        return args;
    }
    
    
    public abstract Value execute (Ctx ctx) throws Exception ;
    
    
    protected void sort (List<String> data) {
        Comparator<String> c=new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        };
        data.sort(c);

    }
    
    protected Value callConfiguredLambda (String contextName, Ctx ctx, String lambda, Value[] lambdaArgs) throws Exception {
        
        PropsFile propsFile=ctx.getObjGlobal().getRoot().getPropsFile();

        SourceLocation loc=propsFile.getSourceLocation(contextName);
        
        FunctionBody codeLines=new FunctionBody(lambda, loc);
        
        CFTCallStackFrame caller=new CFTCallStackFrame("Lambda for '" + contextName + "'");

        Value ret = ctx.getObjGlobal().getRuntime().processFunction(ctx.getStdio(), caller, codeLines, new FunctionState(null,null));
        if (!(ret instanceof ValueBlock)) throw new Exception("Not a lambda: " + lambda + " ---> " + ret.synthesize());
        
        ValueBlock macroObj=(ValueBlock) ret;
        
        List<Value> params=new ArrayList<Value>();
        for (Value v:lambdaArgs) params.add(v);
        
        return macroObj.callLambda(ctx.sub(), params);      
    }
    
    /**
     * Check if srcDir or targetDir equal to or sub-directory of the other, throwing exception if so
     */
    protected void verifySourceTargetDirsIndependent (String op, File srcDir, File targetDir) throws Exception {
        File t=targetDir.getCanonicalFile();
        for (;;) {
            if (t.equals(srcDir)) throw new Exception(op + ": invalid target");
            t=t.getParentFile();
            if (t==null) break;
        }
        File s=srcDir.getCanonicalFile();
        for (;;) {
            if (s.equals(targetDir)) throw new Exception(op + ": invalid source");
            s=s.getParentFile();
            if (s==null) break;
        }
        
    }


}
