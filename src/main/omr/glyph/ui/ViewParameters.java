//----------------------------------------------------------------------------//
//                                                                            //
//                        V i e w P a r a m e t e r s                         //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2007. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Contact author at herve.bitteur@laposte.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.glyph.ui;

import omr.constant.Constant;
import omr.constant.ConstantSet;

import omr.plugin.Plugin;
import omr.plugin.PluginType;

import omr.util.Implement;

import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.Action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;

/**
 * Class <code>ViewParameters</code> handles parameters for GlyphLagView
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
public class ViewParameters
    extends AbstractBean
{
    //~ Static fields/initializers ---------------------------------------------

    /** Specific application parameters */
    private static final Constants constants = new Constants();

    /** Singleton */
    private static ViewParameters INSTANCE;

    //~ Methods ----------------------------------------------------------------

    //-------------//
    // getInstance //
    //-------------//
    public static ViewParameters getInstance ()
    {
        if (INSTANCE == null) {
            synchronized (ViewParameters.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ViewParameters();
                }
            }
        }

        return INSTANCE;
    }

    //-------------------//
    // setCirclePainting //
    //-------------------//
    public void setCirclePainting (boolean value)
    {
        boolean oldValue = constants.circlePainting.getValue();
        constants.circlePainting.setValue(value);
        firePropertyChange(
            "circlePainting",
            oldValue,
            constants.circlePainting.getValue());
    }

    //------------------//
    // isCirclePainting //
    //------------------//
    public boolean isCirclePainting ()
    {
        return constants.circlePainting.getValue();
    }

    //-----------------//
    // setLinePainting //
    //-----------------//
    public void setLinePainting (boolean value)
    {
        boolean oldValue = constants.linePainting.getValue();
        constants.linePainting.setValue(value);
        firePropertyChange(
            "linePainting",
            oldValue,
            constants.linePainting.getValue());
    }

    //----------------//
    // isLinePainting //
    //----------------//
    public boolean isLinePainting ()
    {
        return constants.linePainting.getValue();
    }

    //---------------//
    // toggleCircles //
    //---------------//
    /**
     * Action that toggles the display of approximating circles in selected
     * slur-shaped glyphs
     * @param e the event that triggered this action
     */
    @Action(selectedProperty = "circlePainting")
    public void toggleCircles (ActionEvent e)
    {
    }

    //-------------//
    // toggleLines //
    //-------------//
    /**
     * Action that toggles toggles the display of mean line in selected
     * sticks
     * @param e the event that triggered this action
     */
    @Action(selectedProperty = "linePainting")
    public void toggleLines (ActionEvent e)
    {
    }

    //~ Inner Classes ----------------------------------------------------------

    //------------------//
    // SlurCircleAction //
    //------------------//
    @Deprecated
    @Plugin(type = PluginType.GLYPH_VIEW, item = JCheckBoxMenuItem.class)
    public static class SlurCircleAction
        extends AbstractAction
    {
        //~ Constructors -------------------------------------------------------

        public SlurCircleAction ()
        {
            putValue("SwingSelectedKey", constants.circlePainting.getValue());
        }

        //~ Methods ------------------------------------------------------------

        @Implement(ActionListener.class)
        public void actionPerformed (ActionEvent e)
        {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            constants.circlePainting.setValue(item.isSelected());
        }
    }

    //-----------------//
    // StickLineAction //
    //-----------------//
    @Deprecated
    @Plugin(type = PluginType.GLYPH_VIEW, item = JCheckBoxMenuItem.class)
    public static class StickLineAction
        extends AbstractAction
    {
        //~ Constructors -------------------------------------------------------

        public StickLineAction ()
        {
            putValue("SwingSelectedKey", constants.linePainting.getValue());
        }

        //~ Methods ------------------------------------------------------------

        @Implement(ActionListener.class)
        public void actionPerformed (ActionEvent e)
        {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
            constants.linePainting.setValue(item.isSelected());
        }
    }

    //-----------//
    // Constants //
    //-----------//
    private static final class Constants
        extends ConstantSet
    {
        //~ Instance fields ----------------------------------------------------

        /** Should the lines be painted */
        final Constant.Boolean linePainting = new Constant.Boolean(
            false,
            "Should the stick lines be painted");

        /** Should the circles be painted */
        final Constant.Boolean circlePainting = new Constant.Boolean(
            true,
            "Should the slur circles be painted");
    }
}
