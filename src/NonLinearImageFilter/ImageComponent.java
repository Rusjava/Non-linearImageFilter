/*
 * A component for images
 */
package NonLinearImageFilter;

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
 * A component object for images
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class ImageComponent extends JComponent {

    private final BufferedImage image;
    private final int[] pixels;

    /**
     * Constructor generating sample image
     *
     * @param imageParam the object with parameters for image generation
     */
    public ImageComponent(ImageParam imageParam) {
        super();
        /*
        * Generate image pixelArray for model image
        */
        pixels = generatePixelData(imageParam);
        /*
        * Create buffered image
        */
        this.image=createImage(pixels, imageParam.xsize, imageParam.ysize);
    }

    /**
     * Constructor generating image from real data
     *
     * @param pixelData real pixel data
     */
    public ImageComponent(double[][] pixelData) {
        super();
        /*
        * Generate image pixelArray from real matrix
        */
        pixels = generatePixelData(pixelData);
        
        /*
        * Create buffered image
        */
        this.image=createImage(pixels, pixelData[0].length, pixelData.length);
    }

    /**
     * Constructor importing existing image
     *
     * @param img
     */
    public ImageComponent(BufferedImage image) {
        super();
        int xsize = image.getWidth(null);
        int ysize = image.getHeight(null);
        this.image=image;
        pixels = new int[ysize * xsize];
        for (int i = 0; i < ysize; i++) {
            for (int k = 0; k < xsize; k++) {
                pixels[i * xsize + k] = (image).getData().getDataBuffer().getElem(i * xsize + k);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        /*
         * Scaling image before drawing to the size of container
         */
        double xscale = 1.0 * getWidth() / image.getWidth(null);
        double yscale = 1.0 * getHeight() / image.getHeight(null);
        BufferedImageOp imgop = new AffineTransformOp(AffineTransform.getScaleInstance(xscale, yscale), AffineTransformOp.TYPE_BICUBIC);
        /*
         * Draw image
         */
        ((Graphics2D) g).drawImage(image, imgop, 0, 0);
    }

    /**
     * Returning the associated image
     *
     * @return image
     */
    public Image getImage() {
        return image;
    }

    /**
     * Returning a 2D double array of image pixelArray
     *
     * @return pixelArray 2D array
     */
    public double[][] getPixels() {
        int xsize = image.getWidth(null);
        int ysize = image.getHeight(null);
        double[][] pixels = new double[ysize][xsize];
        for (int i = 0; i < ysize; i++) {
            for (int k = 0; k < xsize; k++) {
                pixels[i][k] = this.pixels[i * xsize + k];
            }
        }

        return pixels;
    }

    /*
     * A method generates pixel arrays for test images
     */
    private int[] generatePixelData(ImageParam param) {
        int[] pixels = new int[param.xsize * param.ysize];
        for (int i = 0; i < param.ysize; i++) {
            for (int k = 0; k < param.xsize; k++) {
                if ((Math.abs(i - param.xsize / 2 + 1) < param.squareScale * 
                        param.ysize / 2) && (Math.abs(k - param.ysize / 2 + 1) < 
                        param.squareScale * param.xsize / 2)) {
                    pixels[i * param.xsize + k] = 1;
                } else {
                    pixels[i * param.xsize + k] = param.signal;
                }
                pixels[i * param.xsize + k] += (int) (Math.random() * param.noise);
            }
        }
        return pixels;
    }

    /*
     * A method generates integer pixel array from a real matrix of data
     */
    private int[] generatePixelData(double[][] pixelData) {
        int xsize = pixelData[0].length;
        int ysize = pixelData.length;
        int[] pixelArray = new int[xsize * ysize];
        for (int i = 0; i < ysize; i++) {
            for (int k = 0; k < xsize; k++) {
                pixelArray[i * xsize + k] += (int) pixelData[i][k];
            }
        }
        return pixelArray;
    }
    
    /*
    * Create image from integer array pixel data
    */
    private BufferedImage createImage(int [] pixels, int xsize, int ysize) {
        /*
        * Create a gray-scale ColorSpace and corresponding ColorModel
        */
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel grayColorModel = new ComponentColorModel(cs, new int[]{31}, 
                false, true, Transparency.OPAQUE, DataBuffer.TYPE_INT);
        /*
        * Create an image from integer pixel array
        */
        Image img = this.createImage(new MemoryImageSource(xsize, ysize, grayColorModel, pixels, 0, xsize));
        /*
        * Convert image to buffered image
        */
        BufferedImage bImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
        bImage.getGraphics().drawImage(img, 0, 0, null);
        return bImage;
    }
}
