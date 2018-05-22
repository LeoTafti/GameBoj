/*
 *  @Author : Paul Juillard (288519)
 *  @Author : Leo Tafti (285418)
*/

package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

public final class LcdImage {

    private final List<LcdImageLine> lines;

    /**
     * Constructor for LcdImage
     * 
     * @param lines
     *            the image's lines
     * @throws IllegalArgumentException
     *             if lines consists of 0 lines, or lines are 0 pixels long, or
     *             lines length in pixels isn't a multiple of Integer.SIZE (32)
     * @throws NullPointerException
     *             if given lines is null
     * 
     */
    public LcdImage(List<LcdImageLine> lines) {
        // Note that we deliberately DO NOT check that each line has the same
        // length, since it is too costly (cf. Piazza post @239)
        Objects.requireNonNull(lines);
        Preconditions.checkArgument(lines.get(0).size() > 0 && lines.size() > 0);
        Preconditions.checkArgument(lines.get(0).size() % Integer.SIZE == 0);

        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public static final class Builder {
        private final List<LcdImageLine> lines = new ArrayList<>();

        /**
         * Creates LcdImage builder for an image of given width and given
         * height with default pixel color 0
         * 
         * @param height
         *            number of lines
         * @param width
         *            number of pixels in a line
         * @throws IllegalAgumentException
         *             if width is not a multiple of Integer.SIZE or if
         *             height(width) is negative or null
         */
        public Builder(int width, int height) {
            Preconditions.checkArgument(width % Integer.SIZE == 0);
            Preconditions.checkArgument(width > 0 && height > 0);

            for (int i = 0; i < height; ++i)
                lines.add(new LcdImageLine(new BitVector(width, false),
                        new BitVector(width, false),
                        new BitVector(width, false)));
        }


        /**
         * Sets a line of LcdImage as given line
         * 
         * @param index
         *            index of line to set
         * @param newLine
         *            replaces the previous line at given index
         * @return this
         *            allows method chaining
         * @throws IllegalArgumentException
         *             if line length doesn't match image width (in pixels)
         * @throws IndexOutOfBoundException if given index is out-of-bounds,
         *             ie not in [0, height[
         */
        public Builder setLine(int index, LcdImageLine newLine) {
            Preconditions.checkArgument(newLine.size() == width());
            Objects.checkIndex(index, height());
            lines.set(index, newLine);
            return this;
        }

        /**
         * Builds LcdImage out of the previously set lines
         * 
         * @return new LcdImage of Builder's lines
         */
        public LcdImage build() {
            return new LcdImage(lines);
        }
        
        /**
         * Gets Image Builder's width in pixels
         * 
         * @return Builder's width
         */
        private int width() {
            return lines.get(0).size();
        }
        
        /**
         * Gets Image Builder's height in pixels
         * 
         * @return Builder's height
         */
        private int height() {
            return lines.size();
        }
    }

    /**
     * Gets image width in pixels
     * 
     * @return image width
     */
    public int width() {
        return lines.get(0).size();
    }

    /**
     * Gets image height in pixels
     * 
     * @return image height
     */
    public int height() {
        return lines.size();
    }

    /**
     * The color of pixel at position (x,y)
     * 
     * @param x
     *            horizontal position of pixel starting from the LEFT
     * @param y
     *            vertical position of pixel starting from TOP
     * @return a gray-scale color, encoded on two bits
     * 
     * @throws IndexOutOfBoundsException
     *            if x isn't in [0, width()[
     *            if y isn't in [0, height()[
     */
    public int get(int x, int y) {
        Objects.checkIndex(x, width());
        Objects.checkIndex(y, height());
        
        return lines.get(y).pixelColor(x);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof LcdImage && lines.equals(((LcdImage) o).lines));
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
