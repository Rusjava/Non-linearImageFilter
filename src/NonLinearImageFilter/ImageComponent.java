/*
 * A component for images
 */
package NonLinearImageFilter;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImageOp;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * A component for images
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class ImageComponent extends JComponent {

    private final Image image;

    /**
     * Constructor
     * @param image
     * @param width
     * @param height
     */
    public ImageComponent(Image image, int width, int height) {
        super();
        this.setPreferredSize(new Dimension(width, height));
        this.image = image;
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
}
