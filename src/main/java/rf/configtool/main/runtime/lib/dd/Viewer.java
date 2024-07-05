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

package rf.configtool.main.runtime.lib.dd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import javax.imageio.ImageIO;

import rf.configtool.main.runtime.lib.RasterImage;

public class Viewer implements ViewReceiver {
    
    private Color backgroundColor;
    private List<Line> lines=new ArrayList<Line>();
    private List<Polygon> polys=new ArrayList<Polygon>();
    private int width = 1024;
    private int height = 768;
    
    public Viewer (Color backgroundColor) {
        this.backgroundColor=backgroundColor;
    }
    
    public void setWidth (int width) {
        this.width=width;
    }
    
    public void setHeight (int height) {
        this.height=height;
    }
    
    public void setBackgroundColor (Color color) {
        this.backgroundColor=color;
    }

    public void addLine (Line line) {
        lines.add(line);
    }
    
    public void addPolygon (Polygon poly) {
        if (!poly.getLinesOnly()) {
            polys.add(poly);
        }
        for (Line line:poly.getLines()) {
            addLine(line); // include it in bounding box calculations
        }
    }
    
    
    private int convert (double low, double high, double value, int pixels, boolean invert) {
        double fract=(value-low)/(high-low);
        if (invert) {
            fract=(high-value)/(high-low);
        }
        int pix=(int) Math.round(pixels*fract);
        if (pix < 0) pix=0;
        else if (pix >= pixels) pix=pixels-1;
        return pix;
    }
    
    /**
    * Save as PNG
    */
    public void writePNG (File file) throws Exception {
        double x1,x2,y1,y2;
        x1=x2=lines.get(0).getFrom().getX();
        y1=y2=lines.get(0).getFrom().getY();
        for (Line line:lines) {
            double x=line.getFrom().getX();
            double y=line.getFrom().getY();
            if (x < x1) x1=x;
            if (x > x2) x2=x;
            if (y < y1) y1=y;
            if (y > y2) y2=y;

            x=line.getTo().getX();
            y=line.getTo().getY();
            if (x < x1) x1=x;
            if (x > x2) x2=x;
            if (y < y1) y1=y;
            if (y > y2) y2=y;
        }
        
        // ensure aspect ratio
        double pixelsPerUnitX = height / (y2-y1);
        double pixelsPerUnitY = width / (x2-x1);
        
        //System.out.println("PPUx=" + pixelsPerUnitX);
        //System.out.println("PPUy=" + pixelsPerUnitY);
        
        double targetPPU = Math.min(pixelsPerUnitX,  pixelsPerUnitY)*0.95;  // 0.95 for a bit of space around the edges
        //System.out.println("targetPPU=" + targetPPU);

        // ## Why does this work? 
        if (pixelsPerUnitY > targetPPU) {
            double ratio = pixelsPerUnitY / targetPPU;  // what to multiply with current range, to get correct PPU
            double delta=(x2-x1)*ratio-(x2-x1);
            //System.out.println("xDelta=" + delta);

            x1-=delta/2;
            x2+=delta/2;
        } 
        if (pixelsPerUnitX > targetPPU) {
            double ratio = pixelsPerUnitX / targetPPU;  // what to multiply with current range, to get correct PPU
            double delta=(y2-y1)*ratio-(y2-y1);
            //System.out.println("yDelta=" + delta);
            y1-=delta/2;
            y2+=delta/2;
        }
        
        
        BufferedImage img=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);

        // draw lines
        for (Line line:lines) {
            g.setColor(line.getColor());
            
            int ax=convert(x1,x2,line.getFrom().getX(),width, false);
            int ay=convert(y1,y2,line.getFrom().getY(),height, true);
            int bx=convert(x1,x2,line.getTo().getX(),width, false);
            int by=convert(y1,y2,line.getTo().getY(),height, true);
            
            g.drawLine(ax, ay, bx, by);
        }
        
        // draw polygons
        for (Polygon poly:polys) {
            int xPos[]=new int[5];
            int yPos[]=new int[5];
            final int nPoints=5;
            
            int i=0;
            for (Vector2d v:poly.getPoints()) {
                xPos[i]=convert(x1,x2,v.getX(), width, false);
                yPos[i]=convert(y1,y2,v.getY(), height, true);
                i++;
            }
            g.setColor(poly.getColor());
            g.fillPolygon(xPos,yPos,nPoints);
        }

        ImageIO.write((BufferedImage) img, "png", file);
        img.flush();
    }
}
