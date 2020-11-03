package rf.configtool.main.runtime.lib;

import java.util.ArrayList;
import java.util.List;

import rf.configtool.data.Expr;
import rf.configtool.main.Ctx;
import rf.configtool.main.StdioVirtual;
import rf.configtool.main.runtime.*;
import rf.configtool.main.runtime.lib.ObjExtProcess.FunctionDestroy;
import rf.configtool.main.runtime.lib.ObjExtProcess.FunctionExitCode;
import rf.configtool.main.runtime.lib.ObjExtProcess.FunctionIsAlive;

/**
 * Created from StmtSpawn, which runs CFT code in separate thread
 *
 */
public class ObjProcess extends Obj {

	private Expr expr;
	private StdioVirtual stdioVirtual;;

	public ObjProcess(Expr expr) {
		this.expr = expr;
		
		this.add(new FunctionGetOutput());

	}

	public void start(Ctx ctx) throws Exception {
		stdioVirtual = new StdioVirtual(null); // TODO: use PIPE
		stdioVirtual.println("ObjProcess starting runner");

		Ctx processCtx = new Ctx(stdioVirtual, ctx.getObjGlobal(), ctx.getFunctionState());

		Runner runner = new Runner(processCtx, expr);
		(new Thread(runner)).start();

	}

	@Override
	public boolean eq(Obj x) {
		return x == this;
	}

	public String getTypeName() {
		return "Process";
	}

	public ColList getContentDescription() {
		return ColList.list().regular(getDesc());
	}

	private String getDesc() {
		return "Process";
	}

	class Runner implements Runnable {
		private Ctx ctx;
		private Expr expr;

		public Runner(Ctx ctx, Expr expr) {
			this.ctx = ctx;
			this.expr = expr;
		}

		public void run() {
			try {
				expr.resolve(ctx);
			} catch (Exception ex) {
				// ignore
			}
		}
	}

	class FunctionGetOutput extends Function {
		public String getName() {
			return "getOutput";
		}

		public String getShortDesc() {
			return "getOutput() - get buffered output";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			List<String> output = stdioVirtual.getAndClearOutputBuffer();
			List<Value> result = new ArrayList<Value>();
			for (String s : output)
				result.add(new ValueString(s));
			return new ValueList(result);
		}
	}

}
