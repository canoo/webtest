// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.interfaces;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.steps.locator.TableNotFoundException;
import com.canoo.webtest.steps.Step;
import org.xml.sax.SAXException;

/**
 * @author Dierk König, Carsten Seibert
 */

public interface ITableLocator
{
    String locateText(Context context, Step step) throws TableNotFoundException, IndexOutOfBoundsException, SAXException;

    /**
     * @deprecated use {@link #getHtmlId()} instead
     */
    String getId();

    String getHtmlId();

    int getRow();

    int getColumn();

    String getDescription();
}
