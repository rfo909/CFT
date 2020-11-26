package rf.configtool.main.runtime.lib.vgy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueList;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import rf.configtool.main.runtime.lib.ObjDict;
import rf.configtool.main.runtime.lib.vgy.ObjINode.FunctionConfigure;
import rf.configtool.main.runtime.lib.vgy.ObjINode.FunctionGetConfiguration;
import rf.configtool.main.runtime.lib.vgy.ObjINode.FunctionStart;
import rf.configtool.util.net.IO;
import rf.configtool.util.net.TCPClient;

public class ObjClient extends Obj {

	private static ObjClient inst;

	public synchronized static ObjClient getInstance() {
		if (inst == null)
			inst = new ObjClient();
		return inst;
	}

	private String host="localhost";
	private int portForData = 31033;
	private int portForAdmin = 31034;

	private ObjClient() {
		this.add(new FunctionTarget());
		this.add(new FunctionSave());
		this.add(new FunctionGetLog());
		this.add(new FunctionQuit());
	}

	private Obj self() {
		return this;
	}

	@Override
	public boolean eq(Obj x) {
		return false;
	}

	@Override
	public String getTypeName() {
		return "Client";
	}

	@Override
	public ColList getContentDescription() {
		return ColList.list().regular("Client");
	}

	class FunctionTarget extends Function {
		public String getName() {
			return "target";
		}

		public String getShortDesc() {
			return "target(host) - set host - returns self";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 1)
				throw new Exception("Expected hostPort parameter");
			host = getString("hostPort", params, 0);
			return new ValueObj(self());
		}
	}

	class FunctionSave extends Function {
		public String getName() {
			return "save";
		}

		public String getShortDesc() {
			return "save(key,value) - returns ack message";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 2)
				throw new Exception("Expected parameters key, value");
			String key = getString("key", params, 0);
			String value = getString("value", params, 1);

			TCPClient client = new TCPClient(host, portForData);
			IO io = client.getIO();
			try {
				io.writeOutputString("SAVE");
				io.writeOutputString(key);
				io.writeOutputString(value);
				String ack = io.readInputString();

				return new ValueString(ack);
			} finally {
				io.close();
			}
		}
	}

	class FunctionGetLog extends Function {
		public String getName() {
			return "getLog";
		}

		public String getShortDesc() {
			return "getLog() - returns list of log lines";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 0)
				throw new Exception("Expected no parameters");

			TCPClient client = new TCPClient(host, portForAdmin);
			IO io = client.getIO();
			io.setId("<Client> ");
			try {
				io.writeOutputString("GETLOG");
				int lines = Integer.parseInt(io.readInputString());
				List<Value> result = new ArrayList<Value>();
				for (int i = 0; i < lines; i++)
					result.add(new ValueString(io.readInputString()));
				return new ValueList(result);
			} finally {
				io.close();
			}
		}
	}

	class FunctionQuit extends Function {
		public String getName() {
			return "quit";
		}

		public String getShortDesc() {
			return "quit() - returns ack message";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 0)
				throw new Exception("Expected no parameters");

			TCPClient client = new TCPClient(host, portForAdmin);
			IO io = client.getIO();
			try {
				io.writeOutputString("QUIT");
				String ack=io.readInputString();
				return new ValueString(ack);
			} finally {
				io.close();
			}
		}
	}
}
