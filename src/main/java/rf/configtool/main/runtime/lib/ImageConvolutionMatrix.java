/*
CFT - an interactive programmable shell for automation 
Copyright (C) 2020-2025 Roar Foshaug

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

/**
 * Convolution matrix for image operations
 * Inspired by http://tech.abdulfatir.com/2014/05/kernel-image-processing.html
 * 
 */
public class ImageConvolutionMatrix {

    private final int order;
    private final float multFactor;
    private final float bias;

    private final float[][] kernel;
    private final int offset;
    private final int elementCount;

    public ImageConvolutionMatrix (int order, double multFactor, double bias, double[] data) {
        if (order % 2 != 1) throw new RuntimeException("Convolution matrix order must be an odd number");
        this.order=order;
        this.multFactor=(float) multFactor;
        this.bias=(float) bias;

        this.elementCount=order*order;
        if (data.length != elementCount) throw new RuntimeException("Invalid convolution matrix, should be " + order*order + " elements");
        
        this.kernel=new float[order][order];
        for (int y=0; y<order; y++) 
            for (int x=0; x<order; x++) {
                float value=(float) data[y*order+x];
                kernel[x][y]=value;
                //System.out.println("x="+x+" y=" + y + " : " + value);
            }
        offset=(order-1)/2;
    }

    public RasterImage apply (RasterImage img) {
        int w=img.getWidth()-offset*2;
        int h=img.getHeight()-offset*2;
        RasterImage result=new RasterImage(w,h);

        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                
                // calculate offset pos in original img
                int origX=x+offset;
                int origY=y+offset;

                // apply kernel
                float red=0.0f;
                float green=0.0f;
                float blue=0.0f;

                for (int kx=0; kx<order; kx++) {
                    for (int ky=0; ky<order; ky++) {
                        int imgX=origX+(kx-offset);
                        int imgY=origY+(ky-offset);
                        //System.out.println("imgX=" + imgX + " imgY=" + imgY);
                        float weight=kernel[kx][ky];
                        red = red   + img.getRed   (imgX,imgY) * weight;
                        green= green + img.getGreen(imgX,imgY) * weight;
                        blue = blue  + img.getBlue (imgX,imgY) * weight;

                    }
                }


                int outR = Math.min(Math.max((int)(red*multFactor+bias),0),255);
				int outG = Math.min(Math.max((int)(green*multFactor+bias),0),255);
				int outB = Math.min(Math.max((int)(blue*multFactor+bias),0),255);

                result.setPixel(x,y,new Color(outR, outG, outB));
            }
        }

        return result;
        
    }


}
