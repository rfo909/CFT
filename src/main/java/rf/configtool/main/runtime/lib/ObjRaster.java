/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2024 Roar Foshaug

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

package rf.configtool.main.runtime.lib;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import rf.configtool.main.Ctx;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjColor;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.RasterImage;

public class ObjRaster extends Obj {
    
    private RasterImage img;
    private Color color;

    public ObjRaster() {
        this.add(new FunctionInit());
        this.add(new FunctionSetColor());
        this.add(new FunctionSetPixel());
        this.add(new FunctionSave());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Raster";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Raster";
    }
    
    private Obj theObj () {
        return this;
    }
    

    class FunctionInit extends Function {
        public String getName() {
            return "init";
        }
        public String getShortDesc() {
            return "init(x,y,color) - create new image with given size and background color, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err = "Expected parameters x,y,color";
            if (params.size() != 3) throw new Exception(err);
            int x=(int) getInt("x", params, 0);
            int y=(int) getInt("y",params,1);
            Obj obj=getObj("color",params,2);
            if (!(obj instanceof ObjColor)) throw new Exception(err);
            ObjColor color=(ObjColor) obj;

            img=new RasterImage(x,y);
            img.setBackground(color.getColor());

            return new ValueObj(theObj());            
        }
    }
    
    
    class FunctionSetColor extends Function {
        public String getName() {
            return "setColor";
        }
        public String getShortDesc() {
            return "setColor(color) - set current color, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err="Expected color parameter";
            if (params.size() != 1) throw new Exception(err);
            Obj obj=getObj("color",params,0);
            if (!(obj instanceof ObjColor)) throw new Exception(err);

            color=((ObjColor) obj).getColor();
            return new ValueObj(theObj());            
        }
    }
    
 
    class FunctionSetPixel extends Function {
        public String getName() {
            return "setPixel";
        }
        public String getShortDesc() {
            return "setPixel(x,y) - set pixel with current color, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err="Expected parameters x,y";
            if (params.size() != 2) throw new Exception(err);
            int x=(int) getInt("x",params,0);
            int y=(int) getInt("y",params,1);

            img.setPixel(x,y,color);

            return new ValueObj(theObj());            
        }
    }
    
 
    class FunctionSave extends Function {
        public String getName() {
            return "save";
        }
        public String getShortDesc() {
            return "savel(file) - save on PNG format, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err="Expected file parameter";
            if (params.size() != 1) throw new Exception(err);
            Obj obj=getObj("file",params,0);
            if (!(obj instanceof ObjFile)) throw new Exception(err);
            ObjFile file=(ObjFile) obj;

            img.savePNG(file.getPath());

            return new ValueObj(theObj());            
        }
    }
    
}
