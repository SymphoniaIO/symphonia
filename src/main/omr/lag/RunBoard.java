//-----------------------------------------------------------------------//
//                                                                       //
//                            R u n B o a r d                            //
//                                                                       //
//  Copyright (C) Herve Bitteur 2000-2006. All rights reserved.          //
//  This software is released under the terms of the GNU General Public  //
//  License. Please contact the author at herve.bitteur@laposte.net      //
//  to report bugs & suggestions.                                        //
//-----------------------------------------------------------------------//

package omr.lag;

import omr.selection.Selection;
import omr.ui.Board;
import omr.ui.field.LIntegerField;
import omr.ui.util.Panel;
import omr.util.Logger;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import omr.selection.SelectionHint;

/**
 * Class <code>RunBoard</code> is dedicated to display of Run information.
 *
 * <dl>
 * <dt><b>Selection Inputs:</b></dt><ul>
 * <li>*_RUN
 * </ul>
 * </dl>
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
public class RunBoard
    extends Board
{
    //~ Static variables/initializers -------------------------------------

    private static final Logger logger = Logger.getLogger(RunBoard.class);

    //~ Instance variables ------------------------------------------------

    private final LIntegerField rStart = new LIntegerField
        (false, "Start", "Pixel coordinate at start of run");

    private final LIntegerField rLength = new LIntegerField
        (false, "Length", "Length of run in pixels");

    private final LIntegerField rLevel = new LIntegerField
        (false, "Level", "Average pixel level on this run");

    //~ Constructors ------------------------------------------------------

    //----------//
    // RunBoard //
    //----------//
    /**
     * Create a Run Board
     * @param input the selection where run input is handled
     * @param name a distinguished name for this instance
     */
    public RunBoard (Selection input,
                     String name)
    {
        super(Board.Tag.RUN, name);
        setInputSelection(input);
        defineLayout();
    }

    //~ Methods -----------------------------------------------------------

    //--------------//
    // defineLayout //
    //--------------//
    private void defineLayout()
    {
        FormLayout layout = Panel.makeFormLayout(2, 3);
        PanelBuilder builder = new PanelBuilder(layout, getComponent());
        builder.setDefaultDialogBorder();

        CellConstraints cst = new CellConstraints();

        int r = 1;                      // --------------------------------
        builder.addSeparator("Run",     cst.xyw(1,  r, 11));

        r += 2;                         // --------------------------------
        builder.add(rStart.getLabel(),  cst.xy (1,  r));
        builder.add(rStart.getField(),  cst.xy (3,  r));

        builder.add(rLength.getLabel(), cst.xy (5,  r));
        builder.add(rLength.getField(), cst.xy (7,  r));

        builder.add(rLevel.getLabel(),  cst.xy (9,  r));
        builder.add(rLevel.getField(),  cst.xy (11, r));
    }

    //--------//
    // update //
    //--------//
    /**
     * Call-back triggered when Run Selection has been modified
     *
     * @param selection the notified Selection
     * @param hint potential notification hint
     */
    public void update (Selection selection,
                        SelectionHint hint)
    {
        Object entity = selection.getEntity();
        if (logger.isFineEnabled()){
            logger.fine("RunBoard " + selection.getTag() + ": " + entity);
        }
        switch (selection.getTag()) {
            case SKEW_RUN :                       // Run of initial skewed lag
            case HORIZONTAL_RUN :                 // Run of horizontal lag
            case VERTICAL_RUN :                   // Run of vertical lag
                Run run = (Run) entity;
                if (run != null) {
                    rStart.setValue(run.getStart());
                    rLength.setValue(run.getLength());
                    rLevel.setValue(run.getLevel());
                } else {
                    emptyFields(getComponent());
                }
                break;

            default:
                logger.severe("Unexpected selection event from " + selection);
        }
    }
}