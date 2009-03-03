//----------------------------------------------------------------------------//
//                                                                            //
//                            S h e e t S t e p s                             //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2009. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Please contact users@audiveris.dev.java.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.step;

import omr.Main;

import omr.constant.Constant;
import omr.constant.ConstantSet;

import omr.glyph.Glyph;
import omr.glyph.GlyphInspector;
import omr.glyph.Shape;

import omr.log.Logger;

import omr.score.ui.ScoreActions;
import omr.score.visitor.ScoreFixer;

import omr.sheet.HorizontalsBuilder;
import omr.sheet.LinesBuilder;
import omr.sheet.Scale;
import omr.sheet.Sheet;
import omr.sheet.SkewBuilder;
import omr.sheet.SystemInfo;
import omr.sheet.picture.ImageFormatException;
import omr.sheet.picture.Picture;
import static omr.step.Step.*;

import java.io.*;
import java.util.*;

import javax.swing.*;

/**
 * Class <code>SheetSteps</code> handles the actual progress of steps for a
 * given sheet instance.
 *
 * @author Herv&eacute; Bitteur
 * @version $Id$
 */
public class SheetSteps
{
    //~ Static fields/initializers ---------------------------------------------

    /** Specific application parameters */
    private static final Constants constants = new Constants();

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(SheetSteps.class);

    //~ Instance fields --------------------------------------------------------

    /** The related sheet instance */
    private final Sheet sheet;

    /** The tasks that relate to each step */
    private final Map<Step, SheetTask> tasks = new LinkedHashMap<Step, SheetTask>();

    //~ Constructors -----------------------------------------------------------

    //------------//
    // SheetSteps //
    //------------//
    /**
     * Create all the task definitions for the given sheet instance
     * @param sheet the given sheet instance
     */
    public SheetSteps (Sheet sheet)
    {
        this.sheet = sheet;

        // Register all tasks
        tasks.put(LOAD, new LoadTask(sheet, LOAD));
        tasks.put(SCALE, new ScaleTask(sheet, SCALE));
        tasks.put(SKEW, new SkewTask(sheet, SKEW));
        tasks.put(LINES, new LinesTask(sheet, LINES));
        tasks.put(HORIZONTALS, new HorizontalsTask(sheet, HORIZONTALS));
        tasks.put(SYSTEMS, new SystemsTask(sheet, SYSTEMS));
        tasks.put(MEASURES, new MeasuresTask(sheet, MEASURES));
        tasks.put(SYMBOLS, new SymbolsTask(sheet, SYMBOLS));
        tasks.put(VERTICALS, new VerticalsTask(sheet, VERTICALS));
        tasks.put(LEAVES, new LeavesTask(sheet, LEAVES));
        tasks.put(CLEANUP, new CleanupTask(sheet, CLEANUP));
        tasks.put(SCORE, new ScoreTask(sheet, SCORE));
    }

    //~ Methods ----------------------------------------------------------------

    //--------//
    // isDone //
    //--------//
    /**
     * Convenient method to check whether a given step has been done
     * @param step the provided step
     * @return true if step has been done
     */
    public boolean isDone (Step step)
    {
        return getTask(step)
                   .isDone();
    }

    //---------------//
    // getLatestStep //
    //---------------//
    /**
     * Report the latest step done so far with the related sheet
     * @return the latest step done, or null
     */
    public Step getLatestStep ()
    {
        Step last = null;

        for (Step step : Step.values()) {
            if (isDone(step)) {
                last = step;
            } else {
                break;
            }
        }

        return last;
    }

    //-----------//
    // isStarted //
    //-----------//
    /**
     * Convenient method to check whether a given step has started
     * @param step the provided step
     * @return true if step has been started
     */
    public boolean isStarted (Step step)
    {
        return getTask(step)
                   .isStarted();
    }

    //-----------//
    // displayUI //
    //-----------//
    /**
     * Launch the UI aspect, if any, of the provided step
     * @param step the provided step
     */
    public void displayUI (Step step)
    {
        getTask(step)
            .displayUI();

        // Selected the view tab most related to the current step
        sheet.getAssembly()
             .selectTab(step);
    }

    //--------//
    // doStep //
    //--------//
    /**
     * Convenient method to run a given step
     * @param step the given step
     * @param systems systems to process (null means all systems)
     * @exception StepException if processing goes wrong
     */
    public void doStep (Step                   step,
                        Collection<SystemInfo> systems)
        throws StepException
    {
        getTask(step)
            .doStep(systems);
    }

    //----------//
    // doSystem //
    //----------//
    /**
     * Convenient method to launch the processing of a given system in a
     * given step
     * @param step the provided step
     * @param system the provided system
     * @exception StepException if processing goes wrong
     */
    public void doSystem (Step       step,
                          SystemInfo system)
        throws StepException
    {
        SheetTask task = getTask(step);

        if (task instanceof SystemTask) {
            SystemTask systemTask = (SystemTask) task;
            systemTask.doSystem(system);
        } else {
            logger.severe("Illegal system processing from step " + step);
        }
    }

    //------//
    // doit //
    //------//
    /**
     * Convenient method to launch the processing of a given step
     * @param step the provided step
     * @exception StepException if processing goes wrong
     */
    public void doit (Step                   step,
                      Collection<SystemInfo> systems)
        throws StepException
    {
        SheetTask task = getTask(step);

        task.started();
        task.doit(systems);
        task.done();
    }

    //--------------//
    // rebuildAfter //
    //--------------//
    /**
     * Update the steps already done, starting right after the provided step.
     * This method will try to minimize the systems to rebuild in each step, by
     * determining the actual impact of the glyphs and shapes.<ul>
     * <li>The glyphs are used to flag the systems containing these glyphs. So
     * the best way to trigger the rebuild of all systems is to pass a null
     * collection of glyphs.</li>
     * <li>The shapes are used to flag or not the systems that follow the ones
     * directly impacted by the modified glyphs. If some of the shapes are
     * "persistent", which means their impact continues past the end of their
     * containing measure, all the following systems must be flagged as well.
     * </li></ul>
     * @param step the step, after which to restart
     * @param glyphs the collection of modified glyphs, or null to flag all
     * systems
     * @param shapes the collection of the modified shapes, only useful if
     * glyphs collection is not null
     * @param imposed flag to indicate that update is imposed
     */
    public void rebuildAfter (Step              step,
                              Collection<Glyph> glyphs,
                              Collection<Shape> shapes,
                              boolean           imposed)
    {
        if (SwingUtilities.isEventDispatchThread()) {
            logger.severe("updateLastSteps should not run on EDT!");
        }

        if (step == null) {
            return;
        }

        // Check whether the update must really be done
        if (!imposed && !ScoreActions.getInstance()
                                     .isRebuildAllowed()) {
            return;
        }

        // Determine impacted systems, from the collection of modified glyphs
        final SortedSet<SystemInfo> impactedSystems = (glyphs != null)
                                                      ? sheet.getImpactedSystems(
            glyphs,
            shapes) : new TreeSet<SystemInfo>(sheet.getSystems());

        if (logger.isFineEnabled()) {
            logger.fine(
                "Rebuild launched after " + step + " on" +
                SystemInfo.toString(impactedSystems));
        }

        // Rebuild after specified step, if needed
        if (sheet.getSheetSteps()
                 .getLatestStep()
                 .compareTo(step) > 0) {
            step.reperformNextSteps(sheet, impactedSystems);
        }
    }

    //---------//
    // getTask //
    //---------//
    /**
     * Give access to the step task
     *
     * @param step the provided step
     * @return the actual task (SheetTask or SystemTask)
     */
    private SheetTask getTask (Step step)
    {
        return tasks.get(step);
    }

    //~ Inner Classes ----------------------------------------------------------

    //-----------//
    // Constants //
    //-----------//
    private static final class Constants
        extends ConstantSet
    {
        //~ Instance fields ----------------------------------------------------

        private final Constant.Integer MaxCleanupIterations = new Constant.Integer(
            "count",
            10,
            "Maximum number of iterations for CLEANUP task");
    }

    //--------------//
    // MeasuresTask //
    //--------------//
    /**
     * Step to retrieve  measures
     */
    private class MeasuresTask
        extends SystemTask
    {
        //~ Constructors -------------------------------------------------------

        MeasuresTask (Sheet sheet,
                      Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void displayUI ()
        {
            Main.getGui().scoreController.setScoreView(sheet.getScore());
        }

        @Override
        public void doSystem (SystemInfo system)
            throws StepException
        {
            system.buildMeasures(); // For Measures
        }

        @Override
        protected void doEpilog (Collection<SystemInfo> systems)
            throws StepException
        {
            if (logger.isFineEnabled()) {
                logger.fine(step + " doEpilog");
            }

            // Update score internal data
            sheet.getScore()
                 .accept(new ScoreFixer(true));
            sheet.getScore()
                 .dumpMeasureCounts(null);
        }
    }

    //-------------//
    // SystemsTask //
    //-------------//
    /**
     * Step to retrieve bar sticks, and thus systems
     */
    private static class SystemsTask
        extends SheetTask
    {
        //~ Constructors -------------------------------------------------------

        SystemsTask (Sheet sheet,
                     Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void displayUI ()
        {
            Main.getGui().scoreController.setScoreView(sheet.getScore());
        }

        @Override
        public void doit (Collection<SystemInfo> systems)
            throws StepException
        {
            sheet.getSystemsBuilder()
                 .buildSystems();
        }
    }

    //-------------//
    // CleanupTask //
    //-------------//
    /**
     * Step to clean up undue constructions, such as wrong stems..
     */
    private class CleanupTask
        extends SystemTask
    {
        //~ Constructors -------------------------------------------------------

        CleanupTask (Sheet sheet,
                     Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void displayUI ()
        {
            getTask(SYMBOLS)
                .displayUI();
            getTask(VERTICALS)
                .displayUI();
        }

        @Override
        public void doSystem (SystemInfo system)
            throws StepException
        {
            final int iterNb = constants.MaxCleanupIterations.getValue();
            boolean   keepGoing = true;

            for (int iter = 0; keepGoing && (iter < iterNb); iter++) {
                int stemModifs = 0;
                int slurModifs = 0;
                int textModifs = 0;

                // Stems
                system.removeInactiveGlyphs();
                stemModifs = system.verifyStems();

                // Slurs
                system.removeInactiveGlyphs();
                system.retrieveGlyphs();
                slurModifs = system.verifySlurs();

                // Texts
                system.removeInactiveGlyphs();
                system.retrieveGlyphs();
                textModifs = system.retrieveTextGlyphs();

                // Progress made?
                keepGoing = (stemModifs + slurModifs + textModifs) > 0;

                if (logger.isFineEnabled()) {
                    logger.fine(
                        "System#" + system.getId() + " CLEANUP#" + iter +
                        " stems:" + stemModifs + " slurs:" + slurModifs +
                        " texts:" + textModifs);
                }
            }
        }
    }

    //-----------------//
    // HorizontalsTask //
    //-----------------//
    /**
     * Step to retrieve all horizontal dashes
     */
    private static class HorizontalsTask
        extends SheetTask
    {
        //~ Constructors -------------------------------------------------------

        HorizontalsTask (Sheet sheet,
                         Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void doit (Collection<SystemInfo> unused)
            throws StepException
        {
            sheet.setHorizontalsBuilder(new HorizontalsBuilder(sheet));
            sheet.setHorizontals(sheet.getHorizontalsBuilder().buildInfo());
        }
    }

    //------------//
    // LeavesTask //
    //------------//
    /**
     * Step to extract newly segmented leaves, since sections belonging to stems
     * are properly assigned.
     */
    private class LeavesTask
        extends SystemTask
    {
        //~ Constructors -------------------------------------------------------

        LeavesTask (Sheet sheet,
                    Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void displayUI ()
        {
            getTask(SYMBOLS)
                .displayUI();
        }

        @Override
        public void doSystem (SystemInfo system)
            throws StepException
        {
            system.inspectGlyphs(GlyphInspector.getLeafMaxDoubt());
        }
    }

    //-----------//
    // LinesTask //
    //-----------//
    /**
     * Step to retrieve all staff lines, and remove them from the picture
     */
    private static class LinesTask
        extends SheetTask
    {
        //~ Constructors -------------------------------------------------------

        LinesTask (Sheet sheet,
                   Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void doit (Collection<SystemInfo> unused)
            throws StepException
        {
            sheet.setLinesBuilder(new LinesBuilder(sheet));
            sheet.setStaves(sheet.getLinesBuilder().getStaves());
        }
    }

    //----------//
    // LoadTask //
    //----------//
    /**
     * Step to (re)load sheet picture. A brand new sheet is created with the
     * provided image file as parameter.
     */
    private static class LoadTask
        extends SheetTask
    {
        //~ Instance fields ----------------------------------------------------

        private Picture picture;

        //~ Constructors -------------------------------------------------------

        LoadTask (Sheet sheet,
                  Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void doit (Collection<SystemInfo> unused)
            throws StepException
        {
            File imageFile = sheet.getImageFile();

            try {
                picture = new Picture(imageFile);
                sheet.setPicture(picture);
            } catch (FileNotFoundException ex) {
                logger.warning("Cannot find file " + imageFile);
                throw new StepException(ex);
            } catch (IOException ex) {
                logger.warning("Input error on file " + imageFile);
                throw new StepException(ex);
            } catch (ImageFormatException ex) {
                logger.warning("Unsupported image format in file " + imageFile);
                logger.warning(ex.getMessage());

                if (Main.getGui() != null) {
                    Main.getGui()
                        .displayWarning(
                        "<B>" + ex.getMessage() + "</B><BR>" +
                        "Please use grey scale with 256 values");
                }

                throw new StepException(ex);
            } catch (Exception ex) {
                logger.warning("Exception", ex);
                throw new StepException(ex);
            }
        }
    }

    //-----------//
    // ScaleTask //
    //-----------//
    /**
     * Step to determine the main scale of the sheet. The scale is the mean
     * distance, in pixels, between two consecutive staff lines. This is based
     * on the population of vertical runs, since most frequent foreground runs
     * come from staff lines, and most frequent background runs come from inter
     * staff lines.
     */
    private static class ScaleTask
        extends SheetTask
    {
        //~ Constructors -------------------------------------------------------

        ScaleTask (Sheet sheet,
                   Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void doit (Collection<SystemInfo> unused)
            throws StepException
        {
            Scale scale = new Scale(sheet);
            sheet.setScale(scale);
        }
    }

    //-----------//
    // ScoreTask //
    //-----------//
    /**
     * Step to translate recognized glyphs into score items
     */
    private class ScoreTask
        extends SystemTask
    {
        //~ Constructors -------------------------------------------------------

        ScoreTask (Sheet sheet,
                   Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void displayUI ()
        {
            // Make sure symbols & verticals are displayed
            getTask(SYMBOLS)
                .displayUI();
            getTask(VERTICALS)
                .displayUI();
        }

        @Override
        public void doEpilog (Collection<SystemInfo> systems)
            throws StepException
        {
            if (logger.isFineEnabled()) {
                logger.fine(step + " doEpilog " + systems);
            }

            // Final cross-system translation tasks
            if (systems == null) {
                systems = sheet.getSystems();
            }

            if (!systems.isEmpty()) {
                systems.iterator()
                       .next()
                       .translateFinal();
            }
        }

        @Override
        public void doSystem (SystemInfo system)
            throws StepException
        {
            system.translateSystem();
        }
    }

    //----------//
    // SkewTask //
    //----------//
    /**
     * Step to determine the general slope of the sheet, still based on
     * pseudo-horizontal (staff) lines. If the absolute value of the computed
     * slope is above a maximum threshold, then the image as a whole is
     * "deskewed", since this significantly eases the subsequent processing.
     * From this step on, we'll play only with the deskewed image.
     */
    private static class SkewTask
        extends SheetTask
    {
        //~ Constructors -------------------------------------------------------

        SkewTask (Sheet sheet,
                  Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void doit (Collection<SystemInfo> unused)
            throws StepException
        {
            sheet.setSkewBuilder(new SkewBuilder(sheet));
            sheet.setSkew(sheet.getSkewBuilder().buildInfo());
        }
    }

    //-------------//
    // SymbolsTask //
    //-------------//
    /**
     * Step to process all glyphs, built with connected sections from the
     * current collection of non-recognized sections.
     */
    private class SymbolsTask
        extends SystemTask
    {
        //~ Constructors -------------------------------------------------------

        SymbolsTask (Sheet sheet,
                     Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void displayUI ()
        {
            sheet.getSymbolsEditor()
                 .refresh();
        }

        @Override
        public void doSystem (SystemInfo system)
            throws StepException
        {
            system.inspectGlyphs(GlyphInspector.getSymbolMaxDoubt());
        }
    }

    //---------------//
    // VerticalsTask //
    //---------------//
    /**
     * Step to extract vertical stick as Stems (or vertical Endings), and
     * recognize newly segmented leaves, since sections belonging to stems are
     * properly assigned.
     */
    private class VerticalsTask
        extends SystemTask
    {
        //~ Constructors -------------------------------------------------------

        VerticalsTask (Sheet sheet,
                       Step  step)
        {
            super(sheet, step);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void displayUI ()
        {
            // Create verticals display
            sheet.getVerticalsController()
                 .refresh();
        }

        @Override
        public void doSystem (SystemInfo system)
            throws StepException
        {
            system.retrieveVerticals();
        }
    }
}
