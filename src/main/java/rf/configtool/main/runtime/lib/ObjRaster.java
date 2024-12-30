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
import java.io.File;
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
import rf.configtool.main.runtime.ValueInt;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.lib.ObjColor;
import rf.configtool.main.runtime.lib.ObjFile;
import rf.configtool.main.runtime.lib.RasterImage;

public class ObjRaster extends Obj {
    
    private RasterImage img;
    private Color color;

    public ObjRaster() {
        this.add(new FunctionLoad());
        this.add(new FunctionInit());
        this.add(new FunctionWidth());
        this.add(new FunctionHeight());
        this.add(new FunctionSetColor());
        this.add(new FunctionSetPixel());
        this.add(new FunctionSave());
        this.add(new FunctionPixel());
        this.add(new FunctionGray());
        this.add(new FunctionScaleTo());
        this.add(new FunctionApplyConvolution3());
    }

    private ObjRaster (RasterImage img) {
        this();
        this.img=img;
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

    class FunctionLoad extends Function {
        public String getName() {
            return "load";
        }
        public String getShortDesc() {
            return "load(imgFile) - initialize with image file, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err = "Expected parameter imgFile parameter";
            if (params.size() != 1) throw new Exception(err);
            Obj obj=getObj("imgFile",params,0);
            if (!(obj instanceof ObjFile)) {
                throw new Exception(err);
            }
            File f=((ObjFile) obj).getFile();
            img=new RasterImage(f.getAbsolutePath());

            return new ValueObj(theObj());            
        }
    }
    

    class FunctionInit extends Function {
        public String getName() {
            return "init";
        }
        public String getShortDesc() {
            return "init(width,height,color) - create new image with given size and background color, returns self";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err = "Expected parameters x,y,color";
            if (params.size() != 3) throw new Exception(err);
            int x=(int) getInt("width", params, 0);
            int y=(int) getInt("height",params,1);
            Obj obj=getObj("color",params,2);
            if (!(obj instanceof ObjColor)) throw new Exception(err);
            ObjColor color=(ObjColor) obj;

            img=new RasterImage(x,y);
            img.setBackground(color.getColor());

            return new ValueObj(theObj());            
        }
    }

    class FunctionWidth extends Function {
        public String getName() {
            return "width";
        }
        public String getShortDesc() {
            return "width() - returns width (after init or load)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(img.getWidth());
        }
    }
    
    
    class FunctionHeight extends Function {
        public String getName() {
            return "height";
        }
        public String getShortDesc() {
            return "height() - returns height (after init or load)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueInt(img.getHeight());
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

            img.setPixel(x,img.getHeight()-y,color);

            return new ValueObj(theObj());            
        }
    }
    
 
    class FunctionSave extends Function {
        public String getName() {
            return "save";
        }
        public String getShortDesc() {
            return "save(file) - save on PNG format, returns self";
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


    class FunctionPixel extends Function {
        public String getName() {
            return "pixel";
        }
        public String getShortDesc() {
            return "pixel(x,y) - return Color for pixel";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err="Expected parameters x,y";

            if (params.size() != 2) throw new Exception(err);
            int x=(int) getInt("x",params,0);
            int y=(int) getInt("y",params,1);

            int red=img.getRed(x,y);
            int green=img.getGreen(x,y);
            int blue=img.getBlue(x,y);

            return new ValueObj(new ObjColor(red,green,blue));
        }
    }
 

    class FunctionGray extends Function {
        public String getName() {
            return "gray";
        }
        public String getShortDesc() {
            return "gray(x,y) - get gray level for pixel in range 0-1 (float)";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err="Expected parameters x,y";

            if (params.size() != 2) throw new Exception(err);
            int x=(int) getInt("x",params,0);
            int y=(int) getInt("y",params,1);

            int red=img.getRed(x,y);
            int green=img.getGreen(x,y);
            int blue=img.getBlue(x,y);

            return new ValueFloat((red+green+blue)/3.0/256.0);
        }
    }


    class FunctionScaleTo extends Function {
        public String getName() {
            return "scaleTo";
        }
        public String getShortDesc() {
            return "scaleTo(width,height) - return new image";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err="Expected parameters width, height";

            if (params.size() != 2) throw new Exception(err);
            int width=(int) getInt("width",params,0);
            int height=(int) getInt("height",params,1);

            RasterImage newImage=img.scaleTo(width,height);
            return new ValueObj(new ObjRaster(newImage));

        }
    }



    class FunctionApplyConvolution3 extends Function {
        public String getName() {
            return "applyConvolution3";
        }
        public String getShortDesc() {
            return "applyConvolution3(multFactor, bias, k1,k2,k3,...k9) - return new Raster object";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            final String err="Expected parameters multFactor, bias, k1,k2,k3,...k9";

            if (params.size() != 11) throw new Exception(err);
            double multFactor=getFloat("multFactor", params, 0);
            double bias=getFloat("bias",params,1);
            double[] kernel=new double[9];
            for (int i=0; i<9; i++) {
                kernel[i]=getFloat("kernel",params,i+2);
            }

            int order=3;
            ImageConvolutionMatrix m=new ImageConvolutionMatrix(order, multFactor, bias, kernel);
            RasterImage newImg=m.apply(img);
            return new ValueObj(new ObjRaster(newImg));
        }
    }

}
