package rf.configtool.main.runtime.lib.dd;

import java.awt.Color;
import java.io.File;
import java.util.List;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjFile;

public class DDWorld extends Obj {

	private Viewer viewer;

	public DDWorld() {
		this.viewer = new Viewer(Color.WHITE);
		this.add(new FunctionBrush());
		this.add(new FunctionRender());
	}

	@Override
	public boolean eq(Obj x) {
		return x == this;
	}

	@Override
	public String getTypeName() {
		return "DD.World";
	}

	@Override
	public ColList getContentDescription() {
		return ColList.list().regular(getDesc());
	}

	private String getDesc() {
		return "DD.World";
	}

	private DDWorld self() {
		return this;
	}

	class FunctionBrush extends Function {
		public String getName() {
			return "Brush";
		}

		public String getShortDesc() {
			return "Brush() - return Brush object";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 0)
				throw new Exception("Expected no parameters");
			return new ValueObj(new DDBrush(viewer));
		}
	}

	class FunctionRender extends Function {
		public String getName() {
			return "render";
		}

		public String getShortDesc() {
			return "render(File) - return PNG file";
		}

		public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
			if (params.size() != 1)
				throw new Exception("Expected File parameter");
			Obj file1 = getObj("File", params, 0);
			if (file1 instanceof ObjFile) {
				File file = ((ObjFile) file1).getFile();
				viewer.writePNG(file);
				return new ValueObj(self());
			} else {
				throw new Exception("Expected File parameter");
			}
		}
	}
}
