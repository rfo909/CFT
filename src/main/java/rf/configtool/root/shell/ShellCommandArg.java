package rf.configtool.root.shell;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.Value;
import rf.configtool.parsetree.Expr;
import java.io.File;
import java.util.List;

public class ShellCommandArg {
	
	private String str;
	private Expr expr;
	
	public ShellCommandArg (String str) {
		this.str=str;
	}
	
	public ShellCommandArg (Expr expr) {
		this.expr=expr;
	}
	
	public boolean isExpr() {
		return expr != null;
	}
	
	public Value resolveExpr (Ctx ctx) throws Exception {
		return this.expr.resolve(ctx);
	}
	
	public String getString () throws Exception {
		return str;
	}
}
