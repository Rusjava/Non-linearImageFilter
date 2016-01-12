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

/**
 *
 * @author Ruslan Feshchenko
 * @version 1.0
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
     * NOISE range in pixels
     */
    public static long NOISE = (int) Math.pow(2, 30);

    /**
     * the default relative size of the square
     */
    public double scale = 0.5;

    /**
     * SIGNAL level in pixels
     */
    public static long SIGNAL = (int) Math.pow(2, 31);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
