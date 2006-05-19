//-----------------------------------------------------------------------//
//                                                                       //
//                                C l e f                                //
//                                                                       //
//  Copyright (C) Herve Bitteur 2000-2006. All rights reserved.          //
//  This software is released under the terms of the GNU General Public  //
//  License. Please contact the author at herve.bitteur@laposte.net      //
//  to report bugs & suggestions.                                        //
//-----------------------------------------------------------------------//

package omr.score;

import omr.glyph.Shape;
import omr.ui.icon.SymbolIcon;
import omr.ui.view.Zoom;
import omr.util.Logger;

import java.awt.*;

/**
 * Class <code>Clef</code> encapsulates a clef.
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
public class Clef
    extends StaffNode
{
    //~ Static variables/initializers -------------------------------------

    private static final Logger logger = Logger.getLogger(Clef.class);

    //~ Instance variables ------------------------------------------------

    // Precise clef shape, from Clefs range in Shape class
    private Shape shape;

    // Location of the clef center WRT staff top-left corner
    private StaffPoint center;

    // Step line of the clef : -4 for top line (Baritone), -2 for Bass, 0
    // for Alto, +2 for Treble and Mezzo-Soprano, +4 for bottom line
    // (Soprano).
    private int pitchPosition;

    //~ Constructors ------------------------------------------------------

    //------//
    // Clef //
    //------//
    /**
     * Create a Clef instance
     * 
     * @param container the container (the measure clef list)
     * @param staff containing staff
     * @param shape precise clef shape
     * @param center center wrt staff (in units)
     * @param pitchPosition pitch position
     */
    public Clef (MusicNode  container,
                 Staff      staff,
                 Shape      shape,
                 StaffPoint center,
                 int        pitchPosition)
    {
        super(container, staff);
        this.shape    = shape;
        this.center   = center;
        this.pitchPosition = pitchPosition;
    }

    //~ Methods -----------------------------------------------------------

    //-----------//
    // paintNode //
    //-----------//
    @Override
        protected boolean paintNode (Graphics  g,
                                     Zoom      zoom,
                                     Component comp)
    {
        // Draw the clef symbol
        paintSymbol(g, zoom, comp,
                    (SymbolIcon) shape.getIcon(),
                    center,
                    pitchPosition);

        return true;
    }
}