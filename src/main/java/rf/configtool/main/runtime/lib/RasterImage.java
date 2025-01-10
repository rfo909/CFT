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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * This class is a cross between PixelSet and ImageTools for applications that need only
 * a fast and easy way of creating completely synthetic images, and saving.
 * @author roar
 *
 */
public class RasterImage {

    public static final int ALPHA_TRANSPARENT = 0;
    public static final int ALPHA_OPAQUE = 255;
    
    private int[] pixels;
    private int w, h;

    public RasterImage (int width, int height) {
        this.w=width;
        this.h=height;
        this.pixels=new int[width * height];
    }
    
    public RasterImage (Image img) throws Exception {
        init (img);
    }
    
    public RasterImage (String fileName) throws Exception {
        Image img = ImageIO.read(new File(fileName));
        init(img);
        img.flush();
    }


    public int getVisiblePixelCount() {
        int count=0;
        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                int pix=getPixel(x,y);
                if (RasterImage.getAlpha(pix)==RasterImage.ALPHA_TRANSPARENT) continue;
                count++;
            }
        }
        return count;
    }
    
    public void compareTo (RasterImage pset) throws Exception {
        if (w != pset.w) throw new Exception("w:" + w + "/" + pset.w);
        if (h != pset.h) throw new Exception("h:" + h + "/" + pset.h);
        if (pixels.length != pset.pixels.length) throw new Exception("length:" + pixels.length + "/" + pset.pixels.length);
        for (int i=0; i<pixels.length; i++) {
            if (pixels[i] != pset.pixels[i]) throw new Exception("i=" + i + ":" + pixels[i] + "/" + pset.pixels[i]);
        }
    }
    

    private void init (Image img) throws Exception {
        this.w=img.getWidth(null);
        this.h=img.getHeight(null);

        int[] pix = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pix, 0, w);
        pg.grabPixels();
        pixels=pix;

    }

    
    /**
    * Get red component of pixel value. Static utility method.
    */
    public static int getRed (int pixel) {
        int red   = (pixel >> 16) & 0xff;
        return red;
    }

    /**
    * Get green component of pixel value. Static utility method.
    */
    public static int getGreen (int pixel) {
        int green = (pixel >>  8) & 0xff;
        return green;
    }

    /**
    * Get blue component of pixel value. Static utility method.
    */
    public static int getBlue (int pixel) {
        int blue  = (pixel      ) & 0xff;
        return blue;
    }

    /**
    * Get alpha component of pixel value. Static utility method.
    */
    public static int getAlpha (int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        return alpha;
    }

    /**
    * Get width of loaded image in pixels
    */
    public int getWidth() {
        return w;
    }

    /**
    * Get height of loaded image in pixels
    */
    public int getHeight() {
        return h;
    }

    /**net.ptkprod.util.img;
    * Return pixel. The returned value ("pixel") contains the following information:
    * <pre>
    * int alpha = (pixel >> 24) & 0xff;
    * int red   = (pixel >> 16) & 0xff;
    * int green = (pixel >>  8) & 0xff;
    * int blue  = (pixel      ) & 0xff;
    * </pre>
    */
    public int getPixel (int xpos, int ypos) {
        if (xpos < 0 || xpos >= w) {
            throw new ArrayIndexOutOfBoundsException("getPixel: xpos=" + xpos + " should be in [0-" + (w-1) + "]");
        }
        if (ypos < 0 || ypos >= h) {
            throw new ArrayIndexOutOfBoundsException("getPixel: ypos=" + ypos + " should be in [0-" + (h-1) + "]");
        }
        return pixels[ypos * w + xpos];
    }

    public void setPixel (int xpos, int ypos, int pix) {
        if (xpos < 0 || xpos >= w) {
            return; // ignore
        }
        if (ypos < 0 || ypos >= h) {
            return; // ignore
        }
        pixels[ypos*w + xpos] = pix;
    }
    
    public void setBackground (Color color) {
        int pix=RasterImage.makePixel(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        
        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++) {
                setPixel(x,y,pix);
            }
        }
    }
    
    
    public void setPixel (int xpos, int ypos, Color color) {
        int pix=RasterImage.makePixel(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        setPixel(xpos, ypos, pix);
    }

    public static int makePixel (int red, int green, int blue, int alpha) {
        int a=(alpha & 0xff) << 24;
        int r=(red & 0xff) << 16;
        int g=(green & 0xff) << 8;
        int b=(blue & 0xff);
        return a | r | g | b;
    }

    /**
    * Get red component of pixel at given position. Utility method combining getPixel() with static
    * method to get component of pixel.
    */
    public int getRed (int xpos, int ypos) {
        return getRed(getPixel(xpos,ypos));
    }

    /**
    * Get green component of pixel at given position. Utility method combining getPixel() with static
    * method to get component of pixel.
    */
    public int getGreen (int xpos, int ypos) {
        return getGreen(getPixel(xpos,ypos));
    }

    /**
    * Get blue component of pixel at given position. Utility method combining getPixel() with static
    * method to get component of pixel.
    */
    public int getBlue (int xpos, int ypos) {
        return getBlue(getPixel(xpos,ypos));
    }

    /**
    * Get alpha component of pixel at given position. Utility method combining getPixel() with static
    * method to get component of pixel.
    */
    public int getAlpha (int xpos, int ypos) {
        return getAlpha(getPixel(xpos,ypos));
    }

    /**
     * Utility method, returns true of alpha component of pixel is zero.
     */
    public boolean isTransparent (int x, int y) {
        return getAlpha(getPixel(x,y))==ALPHA_TRANSPARENT;
    }

    
    public BufferedImage getImage () throws Exception {
        int w=this.getWidth();
        int h=this.getHeight();
        
        BufferedImage img=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        for (int x=0; x<w; x++) {
            for (int y=0; y<h; y++) {
                int pix=this.getPixel(x,y);
                img.setRGB(x,y,pix);
            }
        }
        return img;
    }
    
    public void savePNG (String fname) throws Exception {
        BufferedImage img=getImage();
        ImageIO.write((BufferedImage) img, "png", new File(fname));
        img.flush();
    }


    public RasterImage scaleTo (int width, int height) throws Exception {
        BufferedImage src=getImage();

        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = (Graphics2D) newImage.createGraphics();
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.drawImage(src, 0, 0, width, height, null);
        return new RasterImage(newImage);
    }



}

