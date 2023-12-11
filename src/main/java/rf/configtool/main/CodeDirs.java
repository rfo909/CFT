package rf.configtool.main;

import rf.configtool.lexer.SourceLocation;
import java.util.List;
import java.util.ArrayList;
import rf.configtool.main.runtime.*;

public class CodeDirs {

    private Runtime runtime;
    private PropsFile propsFile;

    private Stdio stdio;

    public CodeDirs(Stdio stdio, Runtime runtime, PropsFile propsFile) {
        this.stdio=stdio;
        this.runtime = runtime;
        this.propsFile = propsFile;
    }

    public List<String> getCodeDirList() throws Exception {
        propsFile.refreshFromFile();
        ArrayList<String> list = new ArrayList<String>();

        FunctionBody code = new FunctionBody(propsFile.getCodeDirs(), new SourceLocation(PropsFile.PROPS_FILE+" .codeDirs", 0));
        try {
            String loc=PropsFile.PROPS_FILE + " .codeDirs";
            CFTCallStackFrame caller=new CFTCallStackFrame(loc);
            Value value = this.runtime.processFunction(stdio, caller, code, new FunctionState("codeDirs"));
            if (!(value instanceof ValueList)) throw new Exception(loc + " must return list of strings");
            for (Value v : ((ValueList) value).getVal()) {
                list.add(v.getValAsString());
            }
        } catch (Exception ex) {
            stdio.println("onLoad function failed with exception");
        }
        return list;
    }
}
