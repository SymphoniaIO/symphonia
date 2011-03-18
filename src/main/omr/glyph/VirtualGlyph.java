//----------------------------------------------------------------------------//
//                                                                            //
//                          V i r t u a l G l y p h                           //
//                                                                            //
//----------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">                          //
//  Copyright (C) Herve Bitteur 2000-2010. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.   //
//----------------------------------------------------------------------------//
// </editor-fold>
package omr.glyph;

import omr.lag.Lag;

import omr.score.common.PixelPoint;

import java.awt.Color;
import java.util.Collection;

/**
 * Class {@code VirtualGlyph} is an artificial glyph specifically build from
 * a MusicFont-based symbol, to carry a shape and features just like a standard
 * glyph would.
 *
 * @author Hervé Bitteur
 */
public class VirtualGlyph
    extends SymbolGlyph
{
    //~ Constructors -----------------------------------------------------------

    //--------------//
    // VirtualGlyph //
    //--------------//
    /**
     * Create a new VirtualGlyph object
     *
     * @param shape the assigned shape
     * @param interline the related interline scaling value
     * @param center where the glyph area center will be located
     */
    public VirtualGlyph (Shape      shape,
                         int        interline,
                         PixelPoint center)
    {
        // Build a glyph of proper size
        super(shape, interline);

        // Translation from generic center to actual center
        translate(getAreaCenter().to(center));
    }

    //~ Methods ----------------------------------------------------------------

    //----------//
    // isActive //
    //----------//
    /**
     * By definition a virtual glyph is always active
     * @return true
     */
    @Override
    public boolean isActive ()
    {
        return true;
    }

    //-----------//
    // isVirtual //
    //-----------//
    @Override
    public boolean isVirtual ()
    {
        return true;
    }

    //----------//
    // colorize //
    //----------//
    @Override
    public void colorize (int   viewIndex,
                          Color color)
    {
        // Nothing to colorize
    }

    //----------//
    // colorize //
    //----------//
    @Override
    public void colorize (Lag   lag,
                          int   viewIndex,
                          Color color)
    {
    }

    //----------//
    // colorize //
    //----------//
    @Override
    public void colorize (int                      viewIndex,
                          Collection<GlyphSection> sections,
                          Color                    color)
    {
    }
}
