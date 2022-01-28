/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2022 Roar Foshaug

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

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
        this.add(new FunctionLineBrush());
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
            return "Brush(offsetA,offsetB) - positive offset to right and negative to left - return Brush object";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 2)
                throw new Exception("Expected numeric parameters offsetA, offsetB (positive to right, negative to left)");
            double a=getFloat("offsetA", params, 0);
            double b=getFloat("offsetB", params, 1);
            return new ValueObj(new DDBrush(viewer, a, b));
        }
    }



    class FunctionLineBrush extends Function {
        public String getName() {
            return "LineBrush";
        }

        public String getShortDesc() {
            return "LineBrush() - return LineBrush object";
        }

        public Value callFunction(Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0)
                throw new Exception("Expected no parameters");
            return new ValueObj(new DDLineBrush(viewer));
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
