/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NonLinearImageFilter;

/**
 *
 * @author Ruslan Feshchenko
 * @version 0.1
 */
public class ImageParam implements Cloneable {

    /**
     * width of the image in pixels
     */
    public int xsize = 300;

    /**
     * height of the image in pixels
     */
    public int ysize = 200;

    /**
     * noise range in pixels
     */
    public int noise = (int) Math.pow(2, 14);

    /**
     * the default relative size of the square
     */
    public double scale = 0.5;

    /**
     * signal level in pixels
     */
    public int signal = (int) Math.pow(2, 15);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
