package rf.xlang.parsetree;

import rf.configtool.lexer.TokenStream;
import rf.configtool.main.Ctx;
import java.util.*;

public class StmtBlock extends Stmt {

    private List<Stmt> statements=new ArrayList<>();

    public StmtBlock (TokenStream ts) throws Exception {
        super(ts);

        ts.matchStr("{","expected '{'");

        while (!ts.matchStr("}")) {
            statements.add(Stmt.parse(ts));
        }
    }

    public void execute (Ctx ctx) throws Exception {
    }
}
