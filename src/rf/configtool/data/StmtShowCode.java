package rf.configtool.data;

import java.util.*;

import rf.configtool.main.CodeHistory;
import rf.configtool.main.Ctx;
import rf.configtool.main.ObjCfg;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.parser.TokenStream;

public class StmtShowCode extends Stmt {

    private Expr expr;
    
    public StmtShowCode (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("showCode","expected 'showCode'");
        ts.matchStr("(", "expected '(' following showCode");
        expr=new Expr(ts);
        ts.matchStr(")", "expected ')' closing showCode(...)");
    }
    
    private List<String> split(String s, String sep) {
        List<String> tokens=new ArrayList<String>();
        StringTokenizer st=new StringTokenizer(s,sep,false);
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        return tokens;
    }
    
    private String fmt (String name, char c, int len) {
        while (name.length() < len) name=name + c;
        return name;
    }
    
    private String line (String txt) {
        String s="";
        while (s.length()<txt.length()) s+="-";
        return s;
    }

    // Single string arg: "a,b:Title|c,d:Title" where abcd are names of lines
    // all other names presented at the end
    // 2019-03-13 Also supports names on format "a*:Title", and 
    
    public void execute (Ctx ctx) throws Exception {
        Value v=expr.resolve(ctx);
        if (v==null || !(v instanceof ValueString)) throw new Exception("showCode() - expected string parameter");
        String s=((ValueString) v).getVal();
        
        CodeHistory code = ctx.getObjGlobal().getCodeHistory();
        List<String> names=code.getNames();
        ObjCfg cfg=ctx.getObjGlobal().getObjCfg();
        
        
        names.sort(new Comparator<String>() {
            public int compare (String a, String b) {
                return a.compareTo(b);
            }
        });
        
        int maxNameLength=1;
        for (String name:names) {
            if (name.length()>maxNameLength) maxNameLength=name.length();
        }
        
        int maxValueLength=cfg.getScreenWidth()-(maxNameLength+2+1);
        

        List<String> groups=split(s,"|;");
        for (String group:groups) {
            List<String> x=split(group,":");
            List<String> matches=split(x.get(0),",");
            String title=x.get(1);
            
            ctx.outln();
            ctx.outln(title);
            ctx.outln(fmt("-",'-',maxNameLength+2));

            for (String match:matches) {
                boolean trunc=false;
                if (match.endsWith("*")) {
                    match=match.substring(0,match.length()-1);
                    trunc=true;
                }
                boolean found;
                do {
                    found=false;
                    List<String> toRemove=new ArrayList<String>();
                    for (String name:names) {
                        boolean foundMatch = trunc ? name.startsWith(match) : name.equals(match);
                        if (foundMatch) {
                            String codeLine=code.getNamedLine(name).getFirstNonBlankLine();
                            if (codeLine.length() > maxValueLength) {
                                codeLine=codeLine.substring(0,maxValueLength-2) + "+";
                            }
                            ctx.outln(fmt(name,' ',maxNameLength) + " : " + codeLine);
                            found=true;
                            toRemove.add(name);
                            break;
                        }
                    }
                    for (String r:toRemove) {
                        names.remove(r);
                    }
                } while (found);
            }
        }
        
        // To display remaining, include "*:Other" or something at the end
        
        
        ctx.outln();
        
//      // then the remaining
//      ctx.outln();
//      ctx.outln("--------------------------------------------------------------");
//      for (String name:names) {
//          ctx.outln(fmt(name,' ',maxNameLength) + " : " + code.getNamedLine(name));
//      }
        
    }

}
