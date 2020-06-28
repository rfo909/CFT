package rf.configtool.main.runtime.lib;

import java.io.*;
import java.util.*;

import rf.configtool.main.Ctx;
import rf.configtool.main.OutText;
import rf.configtool.main.runtime.ColList;
import rf.configtool.main.runtime.Function;
import rf.configtool.main.runtime.Obj;
import rf.configtool.main.runtime.Value;
import rf.configtool.main.runtime.ValueBoolean;
import rf.configtool.main.runtime.ValueFloat;
import rf.configtool.main.runtime.ValueObj;
import rf.configtool.main.runtime.ValueString;
import java.awt.Color;

public class ObjPlot extends Obj {
    
    public static final int TYPE_TIMELINE = 0;
    public static final int TYPE_XY = 1; // first series is X-pos
    
    private List<List<Double>> data=new ArrayList<List<Double>>();
    private int width=1000;
    private int height=800;
    
    private int diagramType = TYPE_TIMELINE;

    
    
    public ObjPlot() {
        this.add(new FunctionReadCSVFile());
        this.add(new FunctionPlotTimeline());
        this.add(new FunctionMin());
        this.add(new FunctionMax());
        this.add(new FunctionTypeTimeline());
        this.add(new FunctionTypeXY());
    }
    
    @Override
    public boolean eq(Obj x) {
        return x==this;
    }


    public String getTypeName() {
        return "Plot";
    }
    

    public ColList getContentDescription() {
        return ColList.list().regular(getDesc());
    }

    
    private String getDesc() {
        return "Plot";
    }
    
    private Obj theObj () {
        return this;
    }
    

    class FunctionReadCSVFile extends Function {
        public String getName() {
            return "readCSVFile";
        }
        public String getShortDesc() {
            return "readCSVFile(File) - read data, return this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected file parameter");
            Value value=params.get(0);
            if (!(value instanceof ValueObj)) throw new Exception("Expected file parameter");
            Obj obj=((ValueObj) value).getVal();
            if (!(obj instanceof ObjFile)) throw new Exception("Expected file parameter");

            ObjFile objFile=(ObjFile) obj;
            
            data.clear();
            BufferedReader br=null;
            try {
                br=new BufferedReader(new InputStreamReader(new FileInputStream(objFile.getPath())));
                for(;;) {
                    String line=br.readLine();
                    if (line==null) break;
                    
                    StringTokenizer st=new StringTokenizer(line,",",false);
                    List<Double> dataLine=new ArrayList<Double>();
                    while (st.hasMoreTokens()) {
                        Double d=Double.parseDouble(st.nextToken().trim());
                        dataLine.add(d);
                    }
                    data.add(dataLine);
                }
            } finally {
                if (br != null) try {br.close();} catch (Exception ex) {};
            }
            
            return new ValueObj(theObj());
        }
    }
    
    
    
    class FunctionPlotTimeline extends Function {
        public String getName() {
            return "plot";
        }
        public String getShortDesc() {
            return "plot(File) - plot data as PNG image, save to given file";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 1) throw new Exception("Expected file parameter");
            OutText outText=ctx.getOutText();

            Value value=params.get(0);
            if (!(value instanceof ValueObj)) throw new Exception("Expected file parameter");
            Obj obj=((ValueObj) value).getVal();
            if (!(obj instanceof ObjFile)) throw new Exception("Expected file parameter");

            ObjFile objFile=(ObjFile) obj;
            
            RasterImage img=new RasterImage(width,height);
            img.setBackground(Color.white);

            if (diagramType==TYPE_TIMELINE) {
                plotSimpleTimeline(img);
            } else if (diagramType==TYPE_XY) {
                plotXY(img);
            }
            
            img.savePNG(objFile.getPath());
            outText.addPlainText("File " + objFile.getPath() + " written");
            
            return new ValueObj(theObj());
        }
    }
    
    private final Color[] colors={
            Color.red, Color.blue, Color.green,  
            Color.orange, Color.cyan, Color.darkGray, Color.magenta, 
            Color.pink, Color.yellow,
                                
            Color.red.brighter(), Color.blue.brighter(), Color.green.brighter(),  
            Color.orange.brighter(), Color.cyan.brighter(), Color.darkGray.brighter(), Color.magenta.brighter(), 
            Color.pink.brighter(),
            
            Color.red.darker(), Color.green.darker(),
            Color.pink.darker(), Color.orange.darker(),
                                
            Color.black,
    };
    
    private void plotSimpleTimeline (RasterImage img) throws Exception {
        MinMax vertical=new MinMax();
        MinMax horizontal=new MinMax(0,data.size()-1);
        
        double horDiff=horizontal.mapToPos(2.0, width, false) - horizontal.mapToPos(1.0, width, false);
        int symbolWidth=(int) Math.round(horDiff/1.5); // in pixels
        if (symbolWidth > 4) symbolWidth=4;
        if (symbolWidth < 2) symbolWidth=2;
        
        for (int xVal=0; xVal<data.size(); xVal++) {
            List<Double> row=data.get(xVal);
            for (int series=0; series<row.size(); series++) {
                double yVal=row.get(series);
                int x=horizontal.mapToPos(xVal, width, false);
                int y=vertical.mapToPos(yVal, height, true);
                Color color=colors[series %  colors.length];
                plotSymbol(img,x,y,symbolWidth,color);
            }
        }
    }
    
    private void plotXY (RasterImage img) throws Exception {
        MinMax vertical=new MinMax(false);
        MinMax horizontal=new MinMax(true);
        
//      int symbolWidth=data.size() / width;
//      if (symbolWidth > 4) symbolWidth=4;
//      if (symbolWidth < 2) symbolWidth=2;

        int symbolWidth=2;
        
        for (List<Double> row:data) {
            int x=horizontal.mapToPos(row.get(0), width,  false);
            for (int series=1; series<row.size(); series++) {
                double yVal=row.get(series);
                int y=vertical.mapToPos(yVal, height, true);
                Color color=colors[(series-1) % colors.length];
                plotSymbol(img,x,y,symbolWidth,color);
            }
        }
    }
    
    class FunctionMin extends Function {
        public String getName() {
            return "min";
        }
        public String getShortDesc() {
            return "min() - return smallest value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueFloat(new MinMax().getMin());
        }
    }

    class FunctionMax extends Function {
        public String getName() {
            return "max";
        }
        public String getShortDesc() {
            return "max() - return biggest value";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            return new ValueFloat(new MinMax().getMax());
        }
    }
    
    class FunctionTypeTimeline extends Function {
        public String getName() {
            return "typeTimeline";
        }
        public String getShortDesc() {
            return "typeTimeline() - set diagram type to timeline, returns this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            diagramType = TYPE_TIMELINE;
            return new ValueObj(theObj());
        }
    }
    
    class FunctionTypeXY extends Function {
        public String getName() {
            return "typeXY";
        }
        public String getShortDesc() {
            return "typeXY() - set diagram type to XY, where first series is X, returns this";
        }
        public Value callFunction (Ctx ctx, List<Value> params) throws Exception {
            if (params.size() != 0) throw new Exception("Expected no parameters");
            diagramType = TYPE_XY;
            return new ValueObj(theObj());
        }
    }
    
    
    // --- helpers


    private void plotSymbol(RasterImage img, int x, int y, int symbolWidth, Color color) {
        int h=symbolWidth/2;
        for (int i=x-h; i<=x+h; i++) {
            for (int j=y-h; j<=y+h; j++) {
                img.setPixel(i, j, color);
            }
        }
    }


    class MinMax {
        private double min, max;
        
        public MinMax (double min, double max) {
            this.min=min;
            this.max=max;
        }
        
        private void check (double d) {
            if (d<min) min=d;
            if (d>max) max=d;
        }
        
        /**
         * For when first col is x, and the rest are y
         * @param firstSeries
         * @throws Exception
         */
        public MinMax (boolean firstSeries) throws Exception {
            if (data.size()==0) throw new Exception("No data");
            if (data.get(0).size()<2) throw new Exception("First row contains too little data (require 2 sets)");
            if (firstSeries) {
                min=max=data.get(0).get(0);
            } else {
                min=max=data.get(0).get(1);
            }

            for (List<Double> row:data) {
                if (firstSeries) {
                    check(row.get(0));
                } else {
                    for (int pos=1; pos<row.size(); pos++) check(row.get(pos));
                }
            }
            
        }
        
        /**
         * For when all cols are data, and x-pos is given by line number
         */
        public MinMax() throws Exception {
            if (data.size()==0) throw new Exception("No data");
            if (data.get(0).size()==0) throw new Exception("First row contains no data");
            min=max=data.get(0).get(0);
            
            for (List<Double> row:data) {
                for (Double d:row) check(d);
            }
        }
        
        public double getMin() {
            return min;
        }
        
        public double getMax() {
            return max;
        }
        
        public int mapToPos (double val, int pixels, boolean reverse) {
            final int border=10;
            
            int x=(int) Math.round(  ((val-min)/(max-min))*(pixels-border*2) );
            if (reverse) x=(pixels-border*2)-x;
            return x+border;
        }
        
    }
    
}
