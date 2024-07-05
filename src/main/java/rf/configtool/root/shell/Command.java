/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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

import java.util.ArrayList;
import java.util.List;

import rf.configtool.lexer.Lexer;
import rf.configtool.lexer.SourceLocation;
import rf.configtool.lexer.TokenStream;
import rf.configtool.main.ScriptSourceLine;
import rf.configtool.parsetree.Expr;

public class Command {
    
    private final String name;
    private final List<Arg> args;
       
    public Command (List<String> parts) throws Exception {
        this.name=parts.get(0);
        this.args=new ArrayList<Arg>();
        
        for (int i=1; i<parts.size(); i++) {
            String part=parts.get(i);
            String isExpr=null;
            
            if (part.startsWith("(") && part.endsWith(")")) {
                isExpr=part.substring(1,part.length()-1); // (xxx) -> xxx
            } else if (part.startsWith("%")) {
                // symbol lookup 
                isExpr=part;
            } else if (part.startsWith("::") || part.startsWith(":")) {
                // Sys.lastResult or Sys.lastResult(N)
                isExpr=part;
            }
            if (isExpr != null) {
                // create Expr
                Lexer lex=new Lexer();
                SourceLocation loc=new SourceLocation("<ShellCommand>",1);
                lex.processLine(new ScriptSourceLine(loc, isExpr));
                TokenStream ts=lex.getTokenStream();
                Expr expr=new Expr(ts);
                if (!ts.atEOF()) throw new Exception("Invalid expression: " + part);
                
                args.add(new Arg(expr));
                continue;
            } else {
                args.add(new Arg(part));
            }
            
        }
    }

   
    public String getCommandName () {
        return name;
    }
    
    public List<Arg> getArgs() {
        return args;
    }
    
    public boolean noArgs() {
        return args.isEmpty();
    }
    
    
    

}
