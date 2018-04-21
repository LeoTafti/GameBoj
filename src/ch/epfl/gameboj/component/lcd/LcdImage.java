/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

public final class LcdImage {

    private final List<LcdImageLine> lines;

    /**
     * Constructor for LcdImage
     * 
     * @param width
     *            number of pixels in a line
     * @param height
     *            number of pixels in a column
     * @param lines
     *            the image's lines
     * @throws IllegalArgumentException
     *             if dimensions are invalid (if either is negative or null)
     *             or if lines are not of width-length
     */
    public LcdImage(int width, int height, List<LcdImageLine> lines) {
        // TODO : if we decide to take width and height, then store them as
        // attributes
        // see piazza @239
        // TODO : check height vs lines.size() too, not only width
        // TODO : check width to be a multiple of 32
        Preconditions.checkArgument(width == lines.get(0).size());
        Preconditions.checkArgument(width > 0 && height > 0);

        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public static final class Builder {
        private List<LcdImageLine> lines = new LinkedList<>();
        // TODO : Change to ArrayList ?
        // why a LinkedList here ? We'll principaly do
        // get/set operations, for which an ArrayList is generally faster
        // + add (as used in Builder Constructor) is efficient when at the end
        // of the list on an ArraList, so no pb here

        private int height, width; // TODO : redundant since height is
                                   // lines.length() and width is
                                   // lines.get(0).size ? Maybe better define
                                   // private methods ? + see comment in
                                   // LcdImageConstructor

        /**
         * Creates LcdImage builder for and image of given width and given
         * height with default pixel color 0
         * 
         * @param height
         *            number of lines
         * @param width
         *            number of pixels in a line
         */
        public Builder(int height, int width) {
            //TODO : check width to be a multiple of 32 + add throws in javadoc
            this.height = height;
            this.width = width;
            for (int i = 0; i < height; ++i)
                lines.add(new LcdImageLine(new BitVector(width, false),
                        new BitVector(width, false),
                        new BitVector(width, false)));
        }

        /**
         * Sets a line of LcdImage as given line
         * 
         * @param index
         *            line to set (between 0 and height)
         * @param newLine
         *            will replace the line at index, must be of the same length
         * @throws IndexOutOfBoundsException
         *             if index is negative or bigger than height
         * @throws IllegalArgumentException
         *             if line does not have a correct size
         *         IndexOutOfBoundException if given index is out-of-bounds, ie not in [0, height[
         */
        public void setLine(int index, LcdImageLine newLine) {
            Preconditions.checkArgument(newLine.size() <= width);
            Objects.checkIndex(index, height);
            lines.set(index, newLine);
        }

        /**
         * TODO : complete javadoc
         * 
         * @return constructed LcdImage
         */
        public LcdImage build() {
            return new LcdImage(width, height, lines);
        }
    }

    /**
     * TODO : complete javadoc Returns the height of the image in pixels
     */
    public int height() {
        return lines.size();
    }

    /**
     * TODO : complete javadoc Returns the width of the image in pixels
     */
    public int width() {
        return lines.get(0).size();
    }

    /**
     * The color of pixel at position (x,y)
     * 
     * @param x
     *            horizontal position of pixel starting from the LEFT
     * @param y
     *            vertical position of pixel starting from TOP
     * @return a gray-scale color as either 0, 1, 2 or 3
     */
    public int get(int x, int y) {
        return lines.get(y).pixelColor(x);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        // if (!(o instanceof LcdImage))
        // return false;
        // LcdImage that = (LcdImage) o;
        // return this.lines == that.lines;

        // TODO : shorter, usually teacher does it this way
        return (o instanceof LcdImage
                && lines.equals(((LcdImage) o).lines));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return lines.hashCode();
    }

}
