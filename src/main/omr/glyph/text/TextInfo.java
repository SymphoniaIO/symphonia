//----------------------------------------------------------------------------//
//                                                                            //
//                              T e x t I n f o                               //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2009. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Please contact users@audiveris.dev.java.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.glyph.text;

import omr.glyph.Glyph;

import omr.lag.HorizontalOrientation;

import omr.log.Logger;

import omr.score.common.PixelPoint;

/**
 * Class <code>TextInfo</code> handles the textual aspects of a glyph. It
 * handles several text contents, by decreasing priority: <ol><li>manual content
 * (entered manually by the user)</li><li>ocr content (as computed by the OCR
 * engine)</li><li>pseudo content, meant to be used as a placeholder, based on
 * the text type</li></ol>
 *
 * <p>The {@link #getContent} method return the manual content if any, otherwise
 * the ocr content. Access to the pseudo content is done only through the {@link
 * #getPseudoContent} method.</p>
 *
 * @author Herv&eacute Bitteur
 * @version $Id$
 */
public class TextInfo
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(TextInfo.class);

    //~ Instance fields --------------------------------------------------------

    /** The glyph this text info belongs to */
    private final Glyph glyph;

    /** Related text area parameters */
    private TextArea textArea;

    /** Mabual content if any */
    private String manualContent;

    /** OCR-based content if any */
    private String ocrContent;

    /** Dummy text content as placeholder, if any */
    private String pseudoContent;

    /** Containing text sentence if any */
    private Sentence sentence;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TextInfo object.
     * @param glyph the related glyph
     */
    public TextInfo (Glyph glyph)
    {
        this.glyph = glyph;
    }

    //~ Methods ----------------------------------------------------------------

    //------------//
    // getContent //
    //------------//
    /**
     * Report the content (the string value) of this text glyph if any
     * @return the text meaning of this glyph if any, either entered manually
     * of via an OCR function
     */
    public String getContent ()
    {
        if (manualContent != null) {
            return manualContent;
        } else {
            return ocrContent;
        }
    }

    //------------------//
    // setManualContent //
    //------------------//
    /**
     * Manually assign a text meaning to the glyph
     * @param manualContent the string value for this text glyph
     */
    public void setManualContent (String manualContent)
    {
        this.manualContent = manualContent;
    }

    //---------------//
    // setOcrContent //
    //---------------//
    /**
     * Remember the text content as provided by the OCR engine
     * @param ocrContent the OCR string value for this text glyph
     */
    public void setOcrContent (String ocrContent)
    {
        this.ocrContent = ocrContent;
    }

    //---------------//
    // getOcrContent //
    //---------------//
    /**
     * Report what the OCR has provided for this glyph
     * @return the text provided by the OCR engine, if any
     */
    public String getOcrContent ()
    {
        return ocrContent;
    }

    //------------------//
    // getPseudoContent //
    //------------------//
    /**
     * Report a dummy content for this glyph (for lack of known content)
     * @return an artificial text content, based on the enclosing sentence type
     */
    public String getPseudoContent ()
    {
        if (pseudoContent == null) {
            if (sentence != null) {
                final int nbChar = (int) Math.rint(
                    ((double) glyph.getContourBox().width) / sentence.getTextHeight());

                if (getTextType() != null) {
                    pseudoContent = getTextType()
                                        .getStringHolder(nbChar);
                }
            }
        }

        return pseudoContent;
    }

    //-------------//
    // setSentence //
    //-------------//
    /**
     * Define the enclosing sentence for this (text) glyph
     * @param sentence the enclosing sentence
     */
    public void setSentence (Sentence sentence)
    {
        this.sentence = sentence;
    }

    //-------------//
    // getSentence //
    //-------------//
    /**
     * Report the sentence, if any, this (text) glyph is a component of
     * @return the containing sentence, or null
     */
    public Sentence getSentence ()
    {
        return sentence;
    }

    //-------------//
    // getTextArea //
    //-------------//
    /**
     * Report the text area that contains this glyph
     * @return the text area for this glyph
     */
    public TextArea getTextArea ()
    {
        if (textArea == null) {
            try {
                textArea = new TextArea(
                    null, // NO SYSTEM !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    null,
                    glyph.getLag().createAbsoluteRoi(glyph.getContourBox()),
                    new HorizontalOrientation());
            } catch (Exception ex) {
                logger.warning("Cannot create TextArea for glyph " + this);
            }
        }

        return textArea;
    }

    //--------------//
    // getTextStart //
    //--------------//
    /**
     * Report the starting point of this text glyph, which is the left side
     * abscissa and the baseline ordinate
     * @return the starting point of the text glyph, specified in pixels
     */
    public PixelPoint getTextStart ()
    {
        return new PixelPoint(
            glyph.getContourBox().x,
            getTextArea().getBaseline());
    }

    //-------------//
    // getTextType //
    //-------------//
    /**
     * Convenient method that report the text type of the sentence, if any, that
     * contains this text glyph
     * @return the text type of the enclosing sentence, or null
     */
    public TextType getTextType ()
    {
        if (sentence != null) {
            return sentence.getTextType();
        } else {
            return null;
        }
    }

    //--------------------//
    // resetPseudoContent //
    //--------------------//
    /**
     * Invalidate the glyph pseudo content, as a consequence of a sentence type
     * change, to force its re-evaluation later
     */
    public void resetPseudoContent ()
    {
        pseudoContent = null;
    }

    //----------//
    // toString //
    //----------//
    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder("{Text");

        if (manualContent != null) {
            sb.append(" manual:")
              .append(manualContent);
        }

        if (ocrContent != null) {
            sb.append(" ocr:")
              .append(ocrContent);
        }

        if (pseudoContent != null) {
            sb.append(" pseudo:")
              .append(pseudoContent);
        }

        sb.append("}");

        return sb.toString();
    }

    //-------------//
    // setTextArea //
    //-------------//
    /**
     * Define the related text area for this glyph
     * @param textArea the related text area which can provide horizontal and
     * vertical histograms
     */
    void setTextArea (TextArea textArea)
    {
        this.textArea = textArea;
    }
}
