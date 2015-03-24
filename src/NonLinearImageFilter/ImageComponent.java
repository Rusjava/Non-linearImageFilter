/*
 * A component for images
 */
package NonLinearImageFilter;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
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
        g.drawImage(image, 0, 0, null);
    }

    /**
     * Returning associated image
     * @return
     */
    public Image getImage() {
        return image;
    }
}
