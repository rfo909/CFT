package rf.configtool.main.runtime.lib;

import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;

public class ValueObjFileLine extends ValueString {
    
    private Long lineNo;
    private ObjFile file;
    
    public ValueObjFileLine (String line, Long lineNo, ObjFile file) {
        super(line);
        this.lineNo=lineNo;
        this.file=file;
        
        add(new FunctionLineNumber());
        add(new FunctionFile());
    }

    @Override
    public String getTypeName() {
        return "FileLine";
    }
    
    @Override
    public String synthesize() throws Exception {
    	return "FileLine(" + super.synthesize() + "," + lineNo + "," + file.synthesize() + ")";
    }

    

    class FunctionLineNumber extends Function {
        public String getName() {
            return "lineNumber";
        }
        public String getShortDesc() {
            return "lineNumber() - returns line number";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            if (lineNo==null) return new ValueNull();
            return new ValueInt(lineNo);
        }
    }

    class FunctionFile extends Function {
        public String getName() {
            return "file";
        }
        public String getShortDesc() {
            return "file() - returns File object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) {
                throw new Exception("Expected no parameters");
            }
            return new ValueObj(file);
        }
    }

}
