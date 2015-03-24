/*
 * A component for images
 */
package NonLinearImageFilter;

import java.awt.Dimension;
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

    private final BufferedImage image;

    /**
     * Constructor
     * @param image
     * @param width
     * @param height
     */
    public ImageComponent(BufferedImage image, int width, int height) {
        super();
        this.setPreferredSize(new Dimension(width, height));
        this.image = image;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        double xscale=1.0*image.getWidth(null)/getPreferredSize().getWidth(),
                yscale=1.0*image.getHeight(null)/getPreferredSize().getHeight();
        AffineTransform at=AffineTransform.getScaleInstance(xscale, yscale);
        BufferedImageOp imgop=new AffineTransformOp (at, AffineTransformOp.TYPE_BICUBIC);
        ((Graphics2D)g).drawImage(image, imgop, 0, 0);
    }

    /**
     * Returning associated image
     * @return
     */
    public BufferedImage getImage() {
        return image;
    }
}
