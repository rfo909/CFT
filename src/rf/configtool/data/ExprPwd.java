package rf.configtool.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ObjGlob;
import rf.configtool.parser.TokenStream;

/**
 * Return directory object for current directory
 */
public class ExprPwd extends LexicalElement {

    public ExprPwd (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("pwd","expected 'pwd'");
    }
    
    public Value resolve (Ctx ctx) throws Exception {
        String currDir=ctx.getObjGlobal().getCurrDir();
        ctx.getOutText().addPlainText(currDir);
        return new ValueObj(new ObjDir(currDir));
    }
}
