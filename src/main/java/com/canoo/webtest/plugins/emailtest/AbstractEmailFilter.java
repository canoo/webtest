// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.ContextHelper;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.interfaces.IContentFilter;

/**
 * Helper class for filtering Email content.
 */
public abstract class AbstractEmailFilter extends AbstractMessageOperationStep implements IContentFilter
{
    private static final String DUMMY_FILTER_URLSTR = "http://dummyEmailFilterUrl";

    protected AbstractEmailFilter() {
        super(false);
    }

    protected void performOperation(final Message message) throws MessagingException {
        final Context context = getContext();
        try {
            filterContent(message);
        }
        catch (final MessagingException e) {
            throw new StepFailedException("Error performing operation: " + e.getMessage(), this);
        } 
        finally {
            context.saveResponseAsCurrent(context.getCurrentResponse());
            context.getConfig().setResultpath(getContext().getConfig().getWebTestResultDir());
            context.saveResponseAsCurrent(context.getCurrentResponse());
        }
    }

    protected abstract void filterContent(Message message) throws MessagingException;

    /**
     * @webtest.parameter required="no"
     * skip="yes"
     * description="A shorthand: <em>save='prefixName'</em> is the same as
     * <em>savePrefix='prefixName' saveResponse='true'</em>."
     */
    public void setSave(final String prefix) {
        // nothing as StepExecutionListener reads it from attributes
    	// but needs to be here to allow Ant to "set" it
    }

    /**
     * @webtest.parameter required="no"
     * default="the 'savePrefix' parameter as specified in &lt;config&gt;."
     * description="A name prefix can be specified for making a permanent copy of
     * received responses. A unique number and the file extension (depending on the
     * MIME-Type) will be appended. The <em>resultpath</em> attribute of the
     * <config> element is used for determining the location of the saved result."
     */
    public void setSavePrefix(final String prefix) {
        // nothing as StepExecutionListener reads it from attributes
    	// but needs to be here to allow Ant to "set" it
    }

    /**
     * @webtest.parameter required="no"
     * description="Whether to make a permanent copy of received responses.
     * Overrides the default value set in the <config> element."
     */
    public void setSaveResponse(final String response) {
        // nothing as StepExecutionListener reads it from attributes
    	// but needs to be here to allow Ant to "set" it
    }

    /**
     * Place the content as the current response
     *
     * @param responseBytes
     * @param contentType
     */
    protected void defineAsCurrentResponse(final byte[] responseBytes, final String contentType) {
        ContextHelper.defineAsCurrentResponse(getContext(), responseBytes, contentType, DUMMY_FILTER_URLSTR);
    }

    /**
     * Place the content as the current response
     *
     * @param response
     * @param contentType
     */
    protected void defineAsCurrentResponse(final String response, final String contentType) {
        defineAsCurrentResponse(response.getBytes(), contentType);
    }
}
