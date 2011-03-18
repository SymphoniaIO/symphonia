//----------------------------------------------------------------------------//
//                                                                            //
//                            S t e m S y m b o l                             //
//                                                                            //
//----------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">                          //
//  Copyright (C) Herve Bitteur 2000-2010. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.   //
//----------------------------------------------------------------------------//
// </editor-fold>
package omr.ui.symbol;

import omr.glyph.Shape;

import omr.score.common.PixelPoint;
import static omr.ui.symbol.Alignment.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

/**
 * Class {@code StemSymbol} implements a decorated stem symbol
 *
 * @author Hervé Bitteur
 */
public class StemSymbol
    extends ShapeSymbol
{
    //~ Static fields/initializers ---------------------------------------------

    // The head+stem part
    private static final BasicSymbol quarter = Symbols.SYMBOL_QUARTER;

    // The stem part
    private static final BasicSymbol stem = Symbols.SYMBOL_STEM;

    //~ Constructors -----------------------------------------------------------

    //------------//
    // StemSymbol //
    //------------//
    /**
     * Create a StemSymbol
     */
    public StemSymbol ()
    {
        this(false);
    }

    //------------//
    // StemSymbol //
    //------------//
    /**
     * Create a StemSymbol
     *
     * @param isIcon true for an icon
     */
    protected StemSymbol (boolean isIcon)
    {
        super(isIcon, Shape.COMBINING_STEM, true); // Decorated
    }

    //~ Methods ----------------------------------------------------------------

    //-----------//
    // getParams //
    //-----------//
    @Override
    protected MyParams getParams (MusicFont font)
    {
        MyParams p = new MyParams();

        // Quarter layout
        p.layout = font.layout(quarter.getString());

        // Stem layout
        p.stemLayout = font.layout(stem.getString());

        Rectangle2D qRect = p.layout.getBounds();
        p.rect = new Rectangle(
            (int) Math.ceil(qRect.getWidth()),
            (int) Math.ceil(qRect.getHeight()));

        return p;
    }

    //------------//
    // createIcon //
    //------------//
    @Override
    protected ShapeSymbol createIcon ()
    {
        return new StemSymbol(true);
    }

    //-------//
    // paint //
    //-------//
    @Override
    protected void paint (Graphics2D g,
                          Params     params,
                          PixelPoint location,
                          Alignment  alignment)
    {
        MyParams   p = (MyParams) params;

        PixelPoint loc = alignment.translatedPoint(TOP_RIGHT, p.rect, location);

        // Decoration
        Color oldColor = g.getColor();
        g.setColor(Color.LIGHT_GRAY);
        MusicFont.paint(g, p.layout, loc, TOP_RIGHT);
        g.setColor(oldColor);

        // Stem
        MusicFont.paint(g, p.stemLayout, loc, TOP_RIGHT);
    }

    //~ Inner Classes ----------------------------------------------------------

    //--------//
    // Params //
    //--------//
    protected class MyParams
        extends Params
    {
        //~ Instance fields ----------------------------------------------------

        // layout for quarter layout
        // rect for global image 

        // layout for stem
        TextLayout stemLayout;
    }
}
