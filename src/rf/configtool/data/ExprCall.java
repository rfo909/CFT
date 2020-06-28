package rf.configtool.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;

import rf.configtool.main.ExternalScriptState;
import rf.configtool.main.Ctx;
import rf.configtool.main.FuncOverrides;
import rf.configtool.main.ObjGlobal;
import rf.configtool.main.Stdio;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.parser.SourceLocation;
import rf.configtool.parser.TokenStream;
import java.util.*;

/**
 * Return directory object for current directory
 */
public class ExprCall extends LexicalElement {

    private Expr target;
    private Expr data;
    private List<Expr> params;
    
    // call "savefile:name" with Data (...)
    
    public ExprCall (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("call","expected 'call'");
        target=new Expr(ts); // "savefile:func"
        
        if (ts.matchStr("with")) {
            ts.matchStr("(", "expected '( data )'");
            data=new Expr(ts);
            ts.matchStr(")", "expected '( data )'");
        }
        
        params=new ArrayList<Expr>();
        if (ts.matchStr("(")) {
            boolean comma=false;
            while (!ts.matchStr(")")) {
                if(comma) ts.matchStr(",", "expected comma");
                params.add(new Expr(ts));
                comma=true;
            }
        }

    }
    
    private String asString(Ctx ctx, Expr expr, String name) throws Exception {
        Value v=expr.resolve(ctx);
        if (v==null || !(v instanceof ValueString)) throw new Exception(name + " - expected string parameter");
        return ((ValueString) v).getVal();
    }
    

    
    public Value resolve (Ctx ctx) throws Exception {
        String t=asString(ctx,target,"script:function").trim();
        int pos=t.indexOf(":");
        if (pos < 0) throw new Exception("Expected script:function");
        
        String script=t.substring(0,pos);
        
        String func=t.substring(pos+1);
        

        ObjDict dict;
        if (data != null) {
            Value v=data.resolve(ctx);
            if (v==null || !(v instanceof ValueObj)) throw new Exception("invalid data: expected Dict");
            Obj obj=((ValueObj)v).getVal();
            if (!(obj instanceof ObjDict)) throw new Exception("invalid data: expected Dict");
            dict=(ObjDict) obj;
        } else {
            dict=null;
        }
        
        
        List<Value> args=new ArrayList<Value>();
        for (Expr expr:params) args.add(expr.resolve(ctx));
        
        ObjGlobal objGlobal=ctx.getObjGlobal();
        Stdio stdio=objGlobal.getStdio();
        
        // convert data to string map, then create FuncOverrides object
        FuncOverrides funcOverrides=null;
        if (dict != null) {
            Map<String,String> map=new HashMap<String,String>();
            Iterator<String> keys=dict.getKeys();
            while (keys.hasNext()) {
                String key=keys.next();
                Value v=dict.getValue(key);
                String stringVersion=v.synthesize();
                map.put(key, stringVersion);
            }
            funcOverrides=new FuncOverrides(map);
        }
                    
        ExternalScriptState x=objGlobal.getOrCreateExternalScriptState(script);
        Value retVal=x.invokeFunction (func, funcOverrides, args);
        return retVal;
    }
}
