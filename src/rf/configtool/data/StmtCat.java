package rf.configtool.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDir;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.ValueObjFileLine;
import rf.configtool.parser.Token;
import rf.configtool.parser.TokenStream;
import rf.configtool.util.TabUtil;

public class StmtCat extends Stmt {

    private Expr fileExpr;
    private String path;
    
    public StmtCat (TokenStream ts) throws Exception {
        super(ts);
        ts.matchStr("cat","expected 'cat'");
        if (ts.matchStr("(")) {
            fileExpr=new Expr(ts);
            ts.matchStr(")", "expected ')' closing cat(...)");
            return;
        }
        StringBuffer sb=new StringBuffer();
        while (!ts.atEOF()) {
            Token t=ts.matchAnyToken("internal error");
            sb.append(t.getStr());
        }
        path=sb.toString().trim();
    }

    public void execute (Ctx ctx) throws Exception {
        File theFile;
        if (fileExpr != null) {
            Value v=fileExpr.resolve(ctx);
            if (!(v instanceof ValueObj)) throw new Exception("Expected ObjFile");
            Obj obj=((ValueObj) v).getVal();
            if (!(obj instanceof ObjFile)) throw new Exception("Expected ObjFile");
            ObjFile file=(ObjFile) obj;

            theFile=file.getFile();
        } else if (path.trim().length()==0) {
            throw new Exception("No file given");
        } else {
            if (path.startsWith(File.separator) || (path.length()>1 && path.charAt(1)==':') ) {
                // absolute path
                theFile=new File(path);
            } else {
                theFile=new File(ctx.getObjGlobal().getCurrDir() + File.separator + path);
            }
        }
        
        if (!theFile.exists() || !theFile.isFile()) {
            throw new Exception("File not found");
        }
        
        BufferedReader br=null;
        try {
            br=new BufferedReader(new FileReader(theFile));
            for (;;) {
                String line=br.readLine();
                if (line==null) break;
                ctx.outln(line);  // no detabbing, not respect for window size - raw dump only
            }
        } finally {
            if (br != null) try {br.close();} catch (Exception ex) {};
        }
        ctx.outln(); // empty line
        ctx.push(new ValueObj(new ObjFile(theFile.getCanonicalPath())));
    }

}
