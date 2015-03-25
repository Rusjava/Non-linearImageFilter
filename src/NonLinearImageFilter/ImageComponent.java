/*
 * A component for images
 */
package NonLinearImageFilter;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImageOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.MemoryImageSource;
import javax.swing.JComponent;

/**
 * A component for images
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class ImageComponent extends JComponent {

    private final Image image;
    private int [] pixels;

    /**
     * Constructor
     * @param image
     * @param width
     * @param height
     */
    public ImageComponent(int xsize, int ysize, double squareScale, int noise, int signal) {
        super();
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel grayColorModel = new ComponentColorModel(cs, new int [] {31}, false, true, Transparency.OPAQUE, DataBuffer.TYPE_INT);
        this.pixels=generatePixelData(xsize, ysize,squareScale, noise, signal);
        this.image = this.createImage(new MemoryImageSource(xsize,ysize, grayColorModel, pixels, 0, xsize));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        /*
        * Scaling image before drawing to the size of container
        */
        double xscale=1.0*getWidth()/image.getWidth(null),
               yscale=1.0*getHeight()/image.getHeight(null);
        AffineTransform at=AffineTransform.getScaleInstance(xscale, yscale);
        BufferedImageOp imgop=new AffineTransformOp (at, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage bimg=new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
        bimg.getGraphics().drawImage(image, 0, 0, null);
        
        /*
        * Draw image
        */
        ((Graphics2D)g).drawImage(bimg, imgop, 0, 0);
    }

    /**
     * Returning associated image
     * @return
     */
    public Image getImage() {
        return image;
    }
    
    /*
     * The method generates pixel arrays for test images
     */
    private int[] generatePixelData(int xsize, int ysize, double squareScale, int noise, int signal) {
        int[] pixels = new int[xsize * ysize];
        System.out.println(noise);
        for (int i = 0; i < xsize; i++) {
            for (int k = 0; k < xsize; k++) {
                if ((Math.abs(i - xsize / 2 + 1) < squareScale * ysize / 2) && (Math.abs(k - ysize / 2 + 1) < squareScale * xsize / 2)) {
                    pixels[i * xsize + k] =1;
                } else {
                    pixels[i * xsize + k] = signal;
                }
                pixels[i * xsize + k] += (int) (Math.random() * noise);
            }
        }
        return pixels;
    } 
}
