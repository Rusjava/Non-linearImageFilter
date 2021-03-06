/*
 * Copyright (C) 2015 Ruslan Feshchenko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package NonLinearImageFilter;

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
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.JComponent;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A component object for images
 *
 * @author Ruslan Feshchenko
 * @version 1.2
 */
public class ImageComponent extends JComponent {

    private final BufferedImage image;
    private final int[] pixels;
    private ColorModel grayColorModel;

    /**
     * Gray color space
     */
    public static final ColorSpace CS = ColorSpace.getInstance(ColorSpace.CS_GRAY);

    /**
     * Constructor generating sample image
     *
     * @param imageParam the object with parameters for image generation
     */
    public ImageComponent(ImageParam imageParam) {
        super();

        /*
         * Create a gray ColorModel
         */
        grayColorModel = new Int32ComponentColorModel(CS, new int[]{imageParam.bitNumber}, false, true,
                Transparency.OPAQUE, initializeDataBufferType(imageParam.bitNumber));
        /*
         * Generate image pixelArray for model image
         */
        pixels = generatePixelData(imageParam);
        /*
         * Create buffered image
         */
        this.image = createImage(pixels, imageParam.xsize, imageParam.ysize);
    }

    /**
     * Constructor generating image from real data
     *
     * @param pixelData real pixel data
     * @param cm color model
     */
    public ImageComponent(double[][] pixelData, ColorModel cm) {
        super();
        /*
         * Generate image pixelArray from real matrix
         */
        pixels = generatePixelData(pixelData);

        /*
         * Create buffered image
         */
        if (cm != null) {
            grayColorModel = cm;
        }
        this.image = createImage(pixels, pixelData[0].length, pixelData.length);
    }

    /**
     * Constructor importing existing image
     *
     * @param image - the image
     * @param bitnum - the number of bits in pixels
     */
    public ImageComponent(BufferedImage image, int bitnum) {
        super();
        int xsize = image.getWidth(null);
        int ysize = image.getHeight(null);
        int[] sampleSizes = image.getSampleModel().getSampleSize();
        int numSamples = sampleSizes.length;
        int numColorComp = image.getColorModel().getNumColorComponents();
        int size = xsize * ysize;
        int shift;
        pixels = new int[size];

        // If specified bitness is smaller that the real bitness of the image samples, use the latter
        for (int i = 0; i < numColorComp; i++) {
            bitnum = bitnum < sampleSizes[i] ? sampleSizes[i] : bitnum;
        }

        //The normalization shift
        shift = bitnum - Collections.max(Arrays.stream(sampleSizes).boxed().collect(Collectors.toList()));
        // Initializing the gray color model
        grayColorModel = new Int32ComponentColorModel(CS, new int[]{bitnum}, false, true,
                Transparency.OPAQUE, initializeDataBufferType(bitnum));

        //Getting all image samples
        double[] dpix = new double[size * numSamples];
        image.getData().getPixels(0, 0, xsize, ysize, dpix);

        /*
         If 32-bit image, treat differently
         */
        if (image.getColorModel().getTransferType() == DataBuffer.TYPE_FLOAT
                | image.getColorModel().getTransferType() == DataBuffer.TYPE_INT) {

            //Normalization parameters
            double[] c = new double[numColorComp], min = new double[numColorComp], max = new double[numColorComp];
            int[] ia = new int[1];
            for (ia[0] = 0; ia[0] < numColorComp; ia[0]++) {
                c[ia[0]] = (double) ((1L << sampleSizes[ia[0]]) - 1);
                min[ia[0]] = Collections.min(IntStream.range(0, dpix.length).filter(n -> n % numSamples == 0)
                        .mapToObj(n -> dpix[n + ia[0]]).collect(Collectors.toList()));
                max[ia[0]] = Collections.max(IntStream.range(0, dpix.length).filter(n -> n % numSamples == 0)
                        .mapToObj(n -> dpix[n + ia[0]]).collect(Collectors.toList()));
                c[ia[0]] = c[ia[0]] / (max[ia[0]] - min[ia[0]]);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < numColorComp; j++) {
                    pixels[i] += (int) Math.round(c[j] * (dpix[i * numSamples + j] - min[j]));
                }
                pixels[i] = Integer.divideUnsigned(pixels[i], numColorComp);
            }
        } else {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < numColorComp; j++) {
                    pixels[i] = (int) dpix[i * numSamples + j];
                }
                pixels[i] /= numColorComp;
                pixels[i] <<= shift;
            }
        }
        this.image = createImage(pixels, xsize, ysize);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        /*
         * Scaling image before drawing to the size of container
         */
        double xscale = (double) getWidth() / image.getWidth(null);
        double yscale = (double) getHeight() / image.getHeight(null);
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
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Returning a 2D double array of image pixelArray
     *
     * @return pixelArray 2D array
     */
    public double[][] getPixelData() {
        int xsize = image.getWidth(null);
        int ysize = image.getHeight(null);
        double[][] data = new double[ysize][xsize];
        for (int i = 0; i < ysize; i++) {
            int offset = i * xsize;
            for (int k = 0; k < xsize; k++) {
                data[i][k] = (double) (pixels[offset + k] & 0xffffffffl);
            }
        }
        return data;
    }

    /*
     * A method generates pixel arrays for test images
     */
    private int[] generatePixelData(ImageParam param) {
        int[] pixelsArray = new int[param.xsize * param.ysize];
        for (int i = 0; i < param.ysize; i++) {
            int offset = i * param.xsize;
            boolean testi = (Math.abs(i - param.ysize / 2 + 1) < param.scale * param.ysize / 2);
            for (int k = 0; k < param.xsize; k++) {
                if (testi && (Math.abs(k - param.xsize / 2 + 1) < param.scale * param.xsize / 2)) {
                    pixelsArray[offset + k] = 0;
                } else {
                    pixelsArray[offset + k] = (int) param.signal;
                }
                pixelsArray[offset + k] += (int) Math.round((Math.random() * (int) param.noise));
            }
        }
        return pixelsArray;
    }

    /*
     * A method generates integer pixel array from a real matrix of data
     */
    private int[] generatePixelData(double[][] pixelData) {
        int xsize = pixelData[0].length;
        int ysize = pixelData.length;
        int[] pixelArray = new int[xsize * ysize];
        for (int i = 0; i < ysize; i++) {
            int offset = i * xsize;
            for (int k = 0; k < xsize; k++) {
                pixelArray[offset + k] = (int) Math.round(pixelData[i][k]);
            }
        }
        return pixelArray;
    }

    /**
     * Creating image from an integer 2D array
     */
    private BufferedImage createImage(int[] pixels, int xsize, int ysize) {
        /*
         * Create an Writableraster from the existing color model and fill it with pixels
         */
        WritableRaster raster = grayColorModel.createCompatibleWritableRaster(xsize, ysize);
        raster.setPixels(0, 0, xsize, ysize, pixels);
        /*
         * Create a BufferedImage from the raster and color model and return it
         */
        return new BufferedImage(grayColorModel, raster, true, null);
    }

    /**
     * Class that fixes the bug with 32-bits per sample images The code was
     * taken from
     * http://stackoverflow.com/questions/26875429/how-to-create-bufferedimage-for-32-bits-per-sample-3-samples-image-data
     */
    public static class Int32ComponentColorModel extends ComponentColorModel {

        /**
         * Calling the superclass constructor.
         *
         * @param cs
         * @param bits
         * @param b
         * @param alpha
         * @param transperancy
         * @param transfertype
         */
        public Int32ComponentColorModel(ColorSpace cs, int[] bits, boolean b, boolean alpha, int transperancy, int transfertype) {
            super(cs, bits, b, alpha, transperancy, transfertype);
        }

        @Override
        public float[] getNormalizedComponents(Object pixel, float[] normComponents, int normOffset) {
            int numComponents = getNumComponents();
            if (normComponents == null || normComponents.length < numComponents + normOffset) {
                normComponents = new float[numComponents + normOffset];
            }
            /*
             Overriding only for the TYPE_INT
             */
            switch (transferType) {
                case DataBuffer.TYPE_INT:
                    int[] ipixel = (int[]) pixel;
                    for (int c = 0, nc = normOffset; c < numComponents; c++, nc++) {
                        normComponents[nc] = (float) ((ipixel[c] & 0xffffffffl) / ((double) ((1L << getComponentSize(c)) - 1)));
                    }
                    break;
                default: // Calling superclass method for other transfer types
                    normComponents = super.getNormalizedComponents(pixel, normComponents, normOffset);
            }
            return normComponents;
        }

        @Override
        public int[] getUnnormalizedComponents(float[] normComponents,
                int normOffset,
                int[] components, int offset) {
            // Make sure that someone isn't using a custom color model
            // that called the super(bits) constructor.
            int numComponents = getNumComponents();
            if (components == null) {
                components = new int[offset + numComponents];
            }
            /*
             Overriding only for the TYPE_INT
             */
            switch (transferType) {
                case DataBuffer.TYPE_INT:
                    if (hasAlpha() && isAlphaPremultiplied()) {
                        double normAlpha = normComponents[normOffset + getNumColorComponents()];
                        for (int i = 0; i < getNumColorComponents(); i++) {
                            components[offset + i] = (int) (normComponents[normOffset + i]
                                    * ((1L << getComponentSize(i)) - 1) * normAlpha + 0.5);
                        }
                        components[offset + getNumColorComponents()] = (int) (normAlpha
                                * ((1L << getComponentSize(getNumColorComponents())) - 1) + 0.5);
                    } else {
                        for (int i = 0; i < numComponents; i++) {
                            components[offset + i] = (int) (normComponents[normOffset + i]
                                    * ((1L << getComponentSize(i)) - 1) + 0.5);
                        }
                    }
                    break;
                default: // Calling superclass method for other transfer types
                    components = super.getUnnormalizedComponents(normComponents, normOffset, components, offset);
            }
            return components;
        }

        @Override
        public Object getDataElements(float[] normComponents, int normOffset,
                Object obj) {
            boolean needAlpha = hasAlpha() && isAlphaPremultiplied();
            /*
             Overriding only for the TYPE_INT
             */
            switch (transferType) {
                case DataBuffer.TYPE_INT:
                    int[] ipixel;
                    if (obj == null) {
                        ipixel = new int[getNumComponents()];
                    } else {
                        ipixel = (int[]) obj;
                    }
                    if (needAlpha) {
                        double alpha
                                = normComponents[getNumColorComponents() + normOffset];
                        for (int c = 0, nc = normOffset; c < getNumColorComponents();
                                c++, nc++) {
                            ipixel[c] = (int) ((normComponents[nc] * alpha)
                                    * ((1L << getComponentSize(c)) - 1) + 0.5);
                        }
                        ipixel[getNumColorComponents()] = (int) (alpha
                                * ((1L << getComponentSize(getNumColorComponents())) - 1) + 0.5);
                    } else {
                        for (int c = 0, nc = normOffset; c < getNumComponents();
                                c++, nc++) {
                            ipixel[c] = (int) (normComponents[nc]
                                    * ((1L << getComponentSize(c)) - 1) + 0.5);
                        }
                    }
                    return ipixel;
                default:
                    return super.getDataElements(normComponents, normOffset, obj);
            }
        }

        private int getRGBComponentMod(Object inData, int idx) {
            // Note that getNormalizedComponents returns non-premultiplied values
            float[] norm = getNormalizedComponents(inData, null, 0);
            ColorSpace cs = getColorSpace();
            float[] rgb = cs.toRGB(norm);
            if (cs.getType() == ColorSpace.TYPE_GRAY) {
                return (int) (norm[idx] * 255.0f + 0.5f);
            } else {
                // Not CS_sRGB, CS_LINEAR_RGB, or any TYPE_GRAY ICC_ColorSpace
                return (int) (rgb[idx] * 255.0f + 0.5f);
            }
        }

        @Override
        public int getRed(Object inData) {
            if (transferType == DataBuffer.TYPE_INT) {
                return getRGBComponentMod(inData, 0);
            } else {
                return super.getRed(inData);
            }
        }

        @Override
        public int getGreen(Object inData) {
            if (transferType == DataBuffer.TYPE_INT) {
                return getRGBComponentMod(inData, 1);
            } else {
                return super.getGreen(inData);
            }
        }

        @Override
        public int getBlue(Object inData) {
            if (transferType == DataBuffer.TYPE_INT) {
                return getRGBComponentMod(inData, 2);
            } else {
                return super.getBlue(inData);
            }
        }
    }

    /**
     * Return data buffer type based on image bitness
     */
    private int initializeDataBufferType(int bitNum) {
        int dataBufferType;
        switch (bitNum) {
            case 8:
                dataBufferType = DataBuffer.TYPE_BYTE;
                break;
            case 16:
                dataBufferType = DataBuffer.TYPE_USHORT;
                break;
            case 32:
                dataBufferType = DataBuffer.TYPE_INT;
                break;
            default:
                dataBufferType = DataBuffer.TYPE_USHORT;
        }
        return dataBufferType;
    }
}
