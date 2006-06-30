//--------------------------------------------------------------------------//
//                                                                          //
//                         P i x e l S u b j e c t                          //
//                                                                          //
//  Copyright (C) Herve Bitteur 2000-2006. All rights reserved.             //
//  This software is released under the terms of the GNU General Public     //
//  License. Please contact the author at herve.bitteur@laposte.net         //
//  to report bugs & suggestions.                                           //
//--------------------------------------------------------------------------//

package omr.ui.view;

import omr.sheet.PixelPoint;
import omr.util.Subject;

import java.awt.Rectangle;

/**
 * Interface <code>PixelSubject</code> is a specific {@link Subject} meant
 * for {@link PixelObserver} observers
 *
 * @author Herv&eacute; Bitteur
 * @version $Id$
 */
public interface PixelSubject
    extends Subject<PixelSubject, PixelObserver, PixelPoint>
{
}
