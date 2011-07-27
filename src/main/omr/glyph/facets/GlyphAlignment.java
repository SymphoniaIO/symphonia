//----------------------------------------------------------------------------//
//                                                                            //
//                        G l y p h A l i g n m e n t                         //
//                                                                            //
//----------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">                          //
//  Copyright (C) Herve Bitteur 2000-2010. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.   //
//----------------------------------------------------------------------------//
// </editor-fold>
package omr.glyph.facets;

import omr.math.Line;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

/**
 * Interface {@code GlyphAlignment} describes glyph alignment, either
 * horizontal or vertical. The key feature is the approximating Line which is
 * the least-square fitted line on all points contained in the stick.
 *
 * <ul> <li> Staff lines, ledgers, alternate ends are examples of horizontal
 * sticks </li>
 *
 * <li> Bar lines, stems are examples of vertical sticks </li> </ul>
 *
 * @author Hervé Bitteur
 */
public interface GlyphAlignment
    extends GlyphFacet
{
    //~ Methods ----------------------------------------------------------------

    //------------------//
    // getIntPositionAt //
    //------------------//
    /**
     * Report the precise stick position for the provided coordinate .
     * @param coord the coord value (x for horizontal fil, y for vertical fil)
     * @return the integer pos value (y for horizontal fil, x for vertical fil)
     */
    public int getIntPositionAt (double coord);

    //---------------//
    // getPositionAt //
    //---------------//
    /**
     * Report the precise stick position for the provided coordinate .
     * @param coord the coord value (x for horizontal fil, y for vertical fil)
     * @return the pos value (y for horizontal fil, x for vertical fil)
     */
    public double getPositionAt (double coord);

    //----------------//
    // getThicknessAt //
    //----------------//
    /**
     * Report the stick mean thickness at the provided coordinate
     * @param coord the desired abscissa
     * @return the mean thickness measured, expressed in number of pixels.
     * Beware, this number will be zero if the probe falls entirely in a hole
     * between two sections.
     */
    public double getThicknessAt (double coord);

    //-----------------//
    // getAbsoluteLine //
    //-----------------//
    /**
     * Return the approximating line computed on the stick, as an
     * <b>absolute</b> line, with x for horizontal axis and y for vertical axis
     *
     * @return The absolute line
     * @see #getOrientedLine()
     */
    Line getAbsoluteLine ();

    //------------------//
    // getAlienPixelsIn //
    //------------------//
    /**
     * Report the number of pixels found in the specified rectangle that do not
     * belong to the stick, and are not artificial patch sections.
     *
     * @param area the rectangular area to investigate, in (coord, pos) form
     *
     * @return the number of alien pixels found
     */
    int getAlienPixelsIn (Rectangle area);

    //------------------//
    // getAliensAtStart //
    //------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     * +-------+
     * |       |
     * +=======+==================================+
     * |       |
     * +-------+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    int getAliensAtStart (int dCoord,
                          int dPos);

    //-----------------------//
    // getAliensAtStartFirst //
    //-----------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     * +-------+
     * |       |
     * +=======+==================================+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    int getAliensAtStartFirst (int dCoord,
                               int dPos);

    //----------------------//
    // getAliensAtStartLast //
    //----------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     * +=======+==================================+
     * |       |
     * +-------+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    int getAliensAtStartLast (int dCoord,
                              int dPos);

    //-----------------//
    // getAliensAtStop //
    //-----------------//

    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     *                                    +-------+
     *                                    |       |
     * +==================================+=======+
     *                                    |       |
     *                                    +-------+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    int getAliensAtStop (int dCoord,
                         int dPos);

    //----------------------//
    // getAliensAtStopFirst //
    //----------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     *                                    +-------+
     *                                    |       |
     * +==================================+=======+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    int getAliensAtStopFirst (int dCoord,
                              int dPos);

    //---------------------//
    // getAliensAtStopLast //
    //---------------------//
    /**
     * Count alien pixels in the following rectangle...
     * <pre>
     * +==================================+=======+
     *                                    |       |
     *                                    +-------+
     * </pre>
     *
     * @param dCoord rectangle size along stick length
     * @param dPos   retangle size along stick thickness
     *
     * @return the number of alien pixels found
     */
    int getAliensAtStopLast (int dCoord,
                             int dPos);

    //-----------//
    // getAspect //
    //-----------//
    /**
     * Report the ratio of length over thickness
     *
     * @return the "slimness" of the stick
     */
    double getAspect ();

    //-----------------//
    // setEndingPoints //
    //-----------------//
    /**
     * Force the locations of start point and stop points
     * @param pStart new start point
     * @param pStop new stop point
     */
    void setEndingPoints (Point2D pStart,
                          Point2D pStop);

    //---------------//
    // isExtensionOf //
    //---------------//
    /**
     * Checks whether a provided stick can be considered as an extension of this
     * one.  Due to some missing points, a long stick can be broken into several
     * smaller ones, that we must check for this.  This is checked before
     * actually merging them.
     *
     * @param other           the other stick
     * @param maxDeltaCoord Max gap in coordinate (x for horizontal)
     * @param maxDeltaPos   Max gap in position (y for horizontal)
     *
     * @return The result of the test
     */
    boolean isExtensionOf (Stick other,
                           int   maxDeltaCoord,
                           int   maxDeltaPos);

    //-------------//
    // getFirstPos //
    //-------------//
    /**
     * Return the first position (ordinate for stick of horizontal sections,
     * abscissa for stick of vertical sections and runs)
     *
     * @return the position at the beginning
     */
    int getFirstPos ();

    //---------------//
    // getFirstStuck //
    //---------------//
    /**
     * Compute the number of pixels stuck on first side of the stick
     *
     * @return the number of pixels
     */
    int getFirstStuck ();

    //------------//
    // getLastPos //
    //------------//
    /**
     * Return the last position (maximum ordinate for a horizontal stick,
     * maximum abscissa for a vertical stick)
     *
     * @return the position at the end
     */
    int getLastPos ();

    //--------------//
    // getLastStuck //
    //--------------//
    /**
     * Compute the nb of pixels stuck on last side of the stick
     *
     * @return the number of pixels
     */
    int getLastStuck ();

    //-----------//
    // getLength //
    //-----------//
    /**
     * Report the length of the stick
     *
     * @return the stick length in pixels
     */
    int getLength ();

    //-----------------//
    // getMeanDistance //
    //-----------------//
    /**
     * Return the mean quadratic distance of the defining population of points
     * to the resulting line. This can be used to measure how well the line fits
     * the points.
     *
     * @return the absolute value of the mean distance
     */
    double getMeanDistance ();

    //-----------//
    // getMidPos //
    //-----------//
    /**
     * Return the position (ordinate for horizontal stick, abscissa for vertical
     * stick) at the middle of the stick
     *
     * @return the position of the middle of the stick
     */
    int getMidPos ();

    //-----------------//
    // getOrientedLine //
    //-----------------//
    /**
     * Return the approximating line computed on the stick, as a line
     * <b>oriented</b> according to the orientation of the containing lag,
     * with x for coordinate (along runs) and y for position (across runs)
     * <ul>
     * <li>For a horizontal glyph, {@link #getOrientedLine} and {@link
     * #getAbsoluteLine} are the same</li>
     * <li>For a vertical glyph, {@link #getOrientedLine} and {@link
     * #getAbsoluteLine} are orthogonal</li>
     * </ul>
     *
     * @return The oriented line
     * @see #getAbsoluteLine()
     */
    Line getOrientedLine ();

    //----------//
    // getStart //
    //----------//
    /**
     * Return the beginning of the stick (xmin for horizontal, ymin for
     * vertical)
     *
     * @return The starting coordinate
     */
    int getStart ();

    //---------------//
    // getStartPoint //
    //---------------//
    /**
     * Report the point at the beginning of the approximating line
     * @return the starting point of the stick line
     */
    Point2D getStartPoint ();

    //----------------//
    // getStartingPos //
    //----------------//
    /**
     * Return the best pos value at starting of the stick
     *
     * @return mean pos value at stick start
     */
    int getStartingPos ();

    //---------//
    // getStop //
    //---------//
    /**
     * Return the end of the stick (xmax for horizontal, ymax for vertical)
     *
     * @return The ending coordinate
     */
    int getStop ();

    //--------------//
    // getStopPoint //
    //--------------//
    /**
     * Report the point at the end of the approximating line
     * @return the ending point of the line
     */
    Point2D getStopPoint ();

    //----------------//
    // getStoppingPos //
    //----------------//
    /**
     * Return the best pos value at the stopping end of the stick
     *
     * @return mean pos value at stick stop
     */
    int getStoppingPos ();

    //--------------//
    // getThickness //
    //--------------//
    /**
     * Report the stick thickness
     *
     * @return the thickness in pixels
     */
    int getThickness ();

    //--------------//
    // overlapsWith //
    //--------------//
    /**
     * Check whether this stick overlaps with the other stick along their
     * orientation (that is abscissae for horizontal ones, and ordinates for
     * vertical ones)
     * @param other the other stick to check with
     * @return true if overlap, false otherwise
     */
    boolean overlapsWith (Stick other);

    //------------//
    // renderLine //
    //------------//
    /**
     * Render the main guiding line of the stick, using the current foreground
     * color.
     *
     * @param g the graphic context
     */
    void renderLine (Graphics2D g);
}
