//----------------------------------------------------------------------------//
//                                                                            //
//                  K e y S i g n a t u r e V e r i f i e r                   //
//                                                                            //
//----------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">                          //
//  Copyright © Hervé Bitteur 2000-2012. All rights reserved.                 //
//  This software is released under the GNU General Public License.           //
//  Goto http://kenai.com/projects/audiveris to report bugs or suggestions.   //
//----------------------------------------------------------------------------//
// </editor-fold>
package omr.score;

import omr.constant.ConstantSet;

import omr.glyph.Evaluation;
import omr.glyph.GlyphNetwork;
import omr.glyph.Glyphs;
import omr.glyph.Grades;
import omr.glyph.Shape;
import omr.glyph.facets.Glyph;

import omr.grid.StaffInfo;

import omr.log.Logger;

import omr.score.common.PixelRectangle;
import omr.score.entity.Barline;
import omr.score.entity.KeySignature;
import omr.score.entity.Measure;
import omr.score.entity.ScoreSystem;
import omr.score.entity.Staff;
import omr.score.entity.SystemPart;

import omr.sheet.Scale;
import omr.sheet.SystemInfo;

import omr.util.Predicate;
import omr.util.TreeNode;

import java.util.Collection;

/**
 * Class {@code KeySignatureVerifier} verifies, at system level, that all
 * vertical measures exhibit the same key signature, and correct them if
 * necessary.
 *
 * @author Herv� Bitteur
 */
public class KeySignatureVerifier
{
    //~ Static fields/initializers ---------------------------------------------

    /** Specific application parameters */
    private static final Constants constants = new Constants();

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(
            KeySignatureVerifier.class);

    //~ Instance fields --------------------------------------------------------
    // The system concerned
    private final ScoreSystem system;

    // Total number of staves in the system
    private final int staffNb;

    //~ Constructors -----------------------------------------------------------
    //----------------------//
    // KeySignatureVerifier //
    //----------------------//
    /**
     * Creates a new KeySignatureVerifier object.
     *
     * @param system the system at hand
     */
    public KeySignatureVerifier (ScoreSystem system)
    {
        this.system = system;
        staffNb = system.getInfo().getStaves().size();
    }

    //~ Methods ----------------------------------------------------------------
    //------------//
    // verifyKeys //
    //------------//
    /**
     * Perform verifications (and corrections when possible) when all keysigs
     * have been generated for the system: The key signature must be the same
     * (in terms of fifths) for the same measure index in all parts and staves.
     */
    public void verifyKeys ()
    {
        logger.fine("\n------------------------------------------------------");
        logger.fine("verifySystemKeys for {0}", system);

        // Number of measures in the system
        final int measureNb = system.getFirstPart().getMeasures().size();

        // Verify each measure index on turn
        for (int im = 0; im < measureNb; im++) {
            logger.fine("measure index ={0}", im);
            verifyVerticalMeasure(im);
        }

        logger.fine("\n======================================================");
    }

    //-------------//
    // checkKeySig //
    //-------------//
    private Glyph checkKeySig (Collection<Glyph> glyphs,
                               final KeySignature bestKey)
    {
        logger.fine("Merging {0} for shape {1}",
                Glyphs.toString(glyphs), bestKey.getShape());

        SystemInfo systemInfo = system.getInfo();
        Glyphs.purgeManuals(glyphs);

        if (glyphs.isEmpty()) {
            return null;
        }

        Glyph compound = systemInfo.buildTransientCompound(glyphs);

        // Check if a proper key sig appears in the top evaluations
        Evaluation vote = GlyphNetwork.getInstance().rawVote(
                compound,
                Grades.keySigMinGrade,
                new Predicate<Shape>()
                {
                    @Override
                    public boolean check (Shape shape)
                    {
                        return shape == bestKey.getShape();
                    }
                });

        if (vote != null) {
            // We now have a key sig!
            logger.fine("{0} built from {1}",
                    vote.shape, Glyphs.toString(glyphs));
            compound = systemInfo.addGlyph(compound);
            compound.setShape(vote.shape, Evaluation.ALGORITHM);

            return compound;
        } else {
            logger.fine("{0}Could not find {1} in {2}",
                    systemInfo.getLogPrefix(),
                    bestKey.getShape(), Glyphs.toString(glyphs));

            return null;
        }
    }

    //------------------//
    // getContextString //
    //------------------//
    private String getContextString (int measureIndex,
                                     int systemStaffIndex)
    {
        return system.getContextString() + "M" + (measureIndex + 1) + "F"
               + staffOf(systemStaffIndex).getId();
    }

    //--------------//
    // getMeasureOf //
    //--------------//
    private Measure getMeasureOf (int staffIndex,
                                  int measureIndex)
    {
        int staffOffset = 0;

        for (TreeNode node : system.getParts()) {
            SystemPart part = (SystemPart) node;
            staffOffset += part.getStaves().size();

            if (staffIndex < staffOffset) {
                return (Measure) part.getMeasures().get(measureIndex);
            }
        }

        logger.severe("Illegal systemStaffIndex: {0}", staffIndex);

        return null;
    }

    //-------------//
    // harmonizeTo //
    //-------------//
    private void harmonizeTo (KeySignature bestKey,
                              KeySignature[] keyVector,
                              int iMeasure)
    {
        PixelRectangle bestBox = bestKey.getBox();

        for (int iStaff = 0; iStaff < staffNb; iStaff++) {
            KeySignature ks = keyVector[iStaff];

            // Is this staff OK?
            if ((ks != null) && ks.getKey().equals(bestKey.getKey())) {
                continue;
            }

            Staff staff = staffOf(iStaff);
            StaffInfo staffInfo = staff.getInfo();

            logger.fine("{0} Forcing key signature to {1}",
                    getContextString(iMeasure, iStaff), bestKey.getKey());

            try {
                // Define the box to intersect keysig glyph(s)
                int xCenter = bestBox.x + (bestBox.width / 2);
                PixelRectangle inner = new PixelRectangle(
                        xCenter,
                        staffInfo.getFirstLine().yAt(xCenter)
                        + (staffInfo.getHeight() / 2),
                        0,
                        0);
                inner.grow((bestBox.width / 2), (staffInfo.getHeight() / 2));

                // Draw the box, for visual debug
                SystemPart part = system.getPartAt(inner.getCenter());
                Barline barline = part.getStartingBarline();

                if (barline != null) {
                    Glyph line = Glyphs.firstOf(
                            barline.getGlyphs(),
                            Barline.linePredicate);

                    if (line != null) {
                        line.addAttachment("k" + staff.getId(), inner);
                    }
                }

                // We now must find a key sig out of these glyphs
                Collection<Glyph> glyphs = system.getInfo().
                        lookupIntersectedGlyphs(
                        inner);

                Glyph compound = checkKeySig(glyphs, bestKey);

                if (compound != null) {
                    if (ks != null) {
                        ks.getParent().getChildren().remove(ks);
                    }

                    Measure measure = getMeasureOf(iStaff, iMeasure);
                    ks = new KeySignature(measure, staff);
                    ks.addGlyph(compound);
                }
            } catch (Exception ex) {
                logger.warning("Cannot copy key", ex);
                ks.addError("Cannot copy key");
            }

            // TODO deassign glyphs that do not contribute to the key ?
        }
    }

    //---------//
    // staffOf //
    //---------//
    private Staff staffOf (int systemStaffIndex)
    {
        int staffOffset = 0;

        for (TreeNode node : system.getParts()) {
            SystemPart part = (SystemPart) node;
            int partStaffNb = part.getStaves().size();
            staffOffset += partStaffNb;

            if (systemStaffIndex < staffOffset) {
                return (Staff) part.getStaves().get(
                        (partStaffNb + systemStaffIndex) - staffOffset);
            }
        }

        logger.severe("Illegal systemStaffIndex: {0}", systemStaffIndex);

        return null;
    }

    //-----------------------//
    // verifyVerticalMeasure //
    //-----------------------//
    private void verifyVerticalMeasure (int im)
    {
        // Retrieve a key, if any, for this measure in each staff
        KeySignature[] keyVector = new KeySignature[staffNb];
        int staffOffset = 0;
        boolean keyFound = false;

        for (TreeNode node : system.getParts()) {
            SystemPart part = (SystemPart) node;
            Measure measure = (Measure) part.getMeasures().get(im);

            for (TreeNode ksnode : measure.getKeySignatures()) {
                KeySignature ks = (KeySignature) ksnode;
                keyFound = true;

                keyVector[ks.getStaff().getId() - 1 + staffOffset] = ks;
            }

            staffOffset += part.getStaves().size();
        }

        // Some keys found in this vertical measure?
        if (keyFound) {
            logger.fine("{0} key(s) found in M{1}",
                    system.getContextString(), im);

            // Browse all staves for sharp/flat compatibility
            // If compatible, adjust all keysigs to the longest
            boolean compatible = true;
            boolean adjustment = false;
            KeySignature bestKey = null;

            for (int iStaff = 0; iStaff < staffNb; iStaff++) {
                KeySignature ks = keyVector[iStaff];

                if (ks == null) {
                    logger.fine("Key signatures will need to be created");
                    adjustment = true;
                } else if (bestKey == null) {
                    bestKey = ks;
                } else if (!bestKey.getKey().equals(ks.getKey())) {
                    logger.fine("Key signatures will need adjustment");
                    adjustment = true;

                    if ((ks.getKey() * bestKey.getKey()) < 0) {
                        logger.fine("Non compatible key signatures");

                        compatible = false;

                        break;
                    } else if (Math.abs(bestKey.getKey()) < Math.abs(
                            ks.getKey())) {
                        // Keep longest key
                        bestKey = ks;
                    }
                }
            }

            // Force key signatures to this value, if compatible
            if (compatible && adjustment) {
                harmonizeTo(bestKey, keyVector, im);
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------
    //-----------//
    // Constants //
    //-----------//
    private static final class Constants
            extends ConstantSet
    {
        //~ Instance fields ----------------------------------------------------

        Scale.Fraction yOffset = new Scale.Fraction(
                0.5d,
                "Key signature vertical offset since staff line");

    }
}