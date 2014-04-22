// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.interfaces.IStepWithTableLocator;
import com.canoo.webtest.interfaces.ITableLocator;
import com.canoo.webtest.steps.locator.TableLocator;
import com.canoo.webtest.steps.locator.TableNotFoundException;

/**
 * @author Carsten Seibert
 * @author Marc Guillemot
 * @webtest.step
 *   category="Core"
 *   name="verifyText"
 *   alias="verifytext"
 *   description="This step verifies the existence of the specified string somewhere in the response received from server
 *   (ie the changes that may have occured since the page has been loaded are not seen by this step). 
 *   The text could represent an <key>HTML</key> fragment like \"<BODY>\" or a arbitrary static text in the page (\"An application error has occurred!\")."
 */
public class VerifyText extends AbstractVerifyTextStep implements IStepWithTableLocator {
	private ITableLocator fTableLocator;

    /**
     * Called by Ant to set the text nested between opening and closing tags.
     * @param text the text to set
     * @webtest.nested.parameter
     *    required="no"
     *    description="Alternative way to set the 'text' attribute."
     */
    public void addText(final String text) {
	   if (getText() == null)
		   setText(getProject().replaceProperties(text));
	}

    /**
     * @param tableLocator
     * @webtest.nested.parameter
     *    required="no"
     *    description="To locate a specific cell in a specific table on the page."
     */
    public void addTable(final TableLocator tableLocator) {
		addTableInternal(tableLocator);
	}

	public void addTableInternal(final ITableLocator tableLocator) {
		fTableLocator = tableLocator;
	}

   public void doExecute() throws Exception {
        if (!isExpectedStringPresent()) {
			throw new StepFailedException(getStepLabel() + ": " + getFailedMessage(), this);
		}
	}

	protected String getFailedMessage() {
		return "Text not found in page. Expected <" + getText() + ">";
	}

    /**
     * @deprecated Use {@link #isExpectedStringPresent()}
     */
    protected boolean isExpectedStringPresent(final Context context) throws SAXException, IOException {
        return isExpectedStringPresent();
    }

	protected boolean isExpectedStringPresent() throws SAXException, IOException {
        Context context = getContext();
        try {
			String text;

			if (getTableLocator() == null) {
				text = context.getCurrentResponse().getWebResponse().getContentAsString();
			} else {
				text = getTableLocator().locateText(context, this);
			}
			if (isRegex()) {
				return verifyText(text);
			}
			return text.indexOf(getText()) > -1;
		}
        catch (TableNotFoundException tnf) {
			throw new StepFailedException("Cannot find table: " + tnf.toString(), this);
		}
        catch (IndexOutOfBoundsException ioobe) {
			throw new StepFailedException("Cannot find cell with supplied index in table", this);
		}
	}

    public ITableLocator getTableLocator() {
        return fTableLocator;
    }
}
