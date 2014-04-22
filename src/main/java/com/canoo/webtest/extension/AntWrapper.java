// Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.steps.control.GroupStep;

/**
 * Wrapper for ant task within a webtest.
 *
 * @author Lothar Maerkle lofi@mountproc.org
 * @author Paul King
 * @author Michael Quenzer
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 * name="antWrapper"
 * description="DEPRECATED as native ant tasks are handled transparently. 
 * Behaves just like the group step"
 * @deprecated since it is not needed at all anymore (Ant tasks are handled transparently)
 */
public class AntWrapper extends GroupStep
{
}
