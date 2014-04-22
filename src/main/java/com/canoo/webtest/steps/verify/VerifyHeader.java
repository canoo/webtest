// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.verify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.canoo.webtest.engine.StepFailedException;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * Verifies the HTTP Response Header.<p>
 * <p/>
 *
 * @author Marc Guillemot
 * @webtest.step category="Extension"
 * name="verifyHeader"
 * description="Verifies that the current response contains the specified <key>HTTP</key> Header."
 */
public class VerifyHeader extends AbstractVerifyTextStep {
    private String fHeaderName;

    /**
     * Sets the Name of the Header of interest.<p>
     *
     * @param name The Header Name
     * @webtest.parameter required="yes"
     * description="The name of the Http Response Header of interest."
     */
    public void setName(final String name) {
        fHeaderName = name;
    }
    public String getName() {
    	return fHeaderName;
    }

    public void doExecute() {
        final WebResponse response = getContext().getCurrentResponse().getWebResponse();
        final List headers = new ArrayList();
        for (final Iterator iter = response.getResponseHeaders().iterator(); iter.hasNext(); )
        {
        	final NameValuePair nv = (NameValuePair) iter.next(); 
        	if (getName().equals(nv.getName()))
        	{
        		if (verifyText(nv.getValue()))
        			return; // ok, found
        		else
        			headers.add(nv);
        	}
        }
        
        // not found
        if (headers.isEmpty())
            throw new StepFailedException("No header found with name \"" + fHeaderName + "\"!");
        else if (headers.size() == 1)
        {
        	final NameValuePair first = (NameValuePair) headers.get(0);
            throw new StepFailedException("Wrong header value found for header \"" + fHeaderName + "\"!", getText(), first.getValue());
        }
        else // the case with many headers with this name
        {
            throw new StepFailedException("None of the headers \"" + fHeaderName + "\" has the right value: " + headers.toString());
        }
    }

    /**
     * Verifies the parameters.<p>
     */
    protected void verifyParameters() {
        nullParamCheck(fHeaderName, "name");
        nullResponseCheck();
	}

	/**
	 * @param text
	 * @webtest.parameter
	 * 	 required="yes"
	 *   description="The expected header value."
	 */
	public void setText(final String text) {
		super.setText(text);
	}

	/**
     * Called by Ant to set the text nested between opening and closing tags.
     * @param text the text to set
     * @webtest.nested.parameter
     *    required="no"
     *    description="Alternative way to set both name and text attributes separated by a semicolon like \"Content-Type: text/html\"."
     */
    public void addText(String text) {
    	text = getProject().replaceProperties(text);
    	final int p = text.indexOf(':');
    	// verification is done in verifyParameters
    	if (p != -1) {
    		final String name = text.substring(0, p).trim();
    		setName(name);
    		final String value = text.substring(p + 1).trim();
    		setText(value);
    	}
	}
}