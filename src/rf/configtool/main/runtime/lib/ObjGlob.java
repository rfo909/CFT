package rf.configtool.main.runtime.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir.FunctionName;

public class ObjGlob extends Obj {
    
    private String pattern;
    private String regex;

    private ObjGlob() {
        add(new FunctionAsRegex());
        add(new FunctionMatch());
    }

    public ObjGlob (String pattern) {
        this();
        this.pattern=pattern;
        this.regex=Regex.createGlobRegex(pattern);
    }
    
    public boolean matches (String s) {
        return s.matches(regex);
    }

    @Override
    public boolean eq(Obj x) {
        if (x instanceof ObjGlob) {
            ObjGlob g=(ObjGlob) x;
            return g.pattern.equals(pattern);
        }
        return false;
    }

    @Override
    public String synthesize() throws Exception {
        return "Glob(" + (new ValueString(pattern)).synthesize() + ")";
    }


    
    @Override
    public String getTypeName() {
        return "Glob";
    }
    
    @Override
    public ColList getContentDescription() {
        return ColList.list().regular(pattern);
    }
    
    
    class FunctionAsRegex extends Function {
        public String getName() {
            return "regex";
        }
        public String getShortDesc() {
            return "regex() - returns Regex object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            
            return new ValueObj(new ObjRegex(regex));
        }
    }
    

    
    
    class FunctionMatch extends Function {
        public String getName() {
            return "match";
        }
        public String getShortDesc() {
            return "match(file) - returns boolean";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected File parameter");
            Obj obj=getObj("file",params,0);
            if (!(obj instanceof ObjFile)) throw new Exception("Expected File parameter");
            
            ObjFile f=(ObjFile) obj;
            
            return new ValueBoolean(f.getName().matches(regex));
        }
    }

}
