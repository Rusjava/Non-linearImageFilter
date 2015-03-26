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

    private final Image image;

    /**
     * Constructor generating sample image
     *
     * @param xsize
     * @param ysize
     * @param squareScale
     * @param noise
     * @param signal
     */
    public ImageComponent(int xsize, int ysize, double squareScale, int noise, int signal) {
        super();
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorModel grayColorModel = new ComponentColorModel(cs, new int[]{31}, false, true, Transparency.OPAQUE, DataBuffer.TYPE_INT);
        int[] pixels = generatePixelData(xsize, ysize, squareScale, noise, signal);
        Image img = this.createImage(new MemoryImageSource(xsize, ysize, grayColorModel, pixels, 0, xsize));
        this.image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
        this.image.getGraphics().drawImage(img, 0, 0, null);
    }

    /**
     * Constructor importing existing image
     *
     * @param img
     */
    public ImageComponent(Image img) {
        super();
        this.image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_BYTE_GRAY);
        this.image.getGraphics().drawImage(img, 0, 0, null);
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
        ((Graphics2D) g).drawImage((BufferedImage) image, imgop, 0, 0);
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
     * Returning a 2D double array of image pixels
     * @return pixels 2D array
     */
    public double[][] getPixels() {
        int xsize = image.getWidth(null);
        int ysize = image.getHeight(null);
        double[][] pixels = new double[xsize][ysize];
        for (int i = 0; i < xsize; i++) {
            for (int k = 0; k < xsize; k++) {
                pixels[i][k] = ((BufferedImage) image).getData().getDataBuffer().getElemDouble(i * xsize + k);
            }
        }

        return pixels;
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
                    pixels[i * xsize + k] = 1;
                } else {
                    pixels[i * xsize + k] = signal;
                }
                pixels[i * xsize + k] += (int) (Math.random() * noise);
            }
        }
        return pixels;
    }
}
