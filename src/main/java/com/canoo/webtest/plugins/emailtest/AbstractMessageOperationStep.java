// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;

/**
 * Abstract class used by email steps that perform an operation on a single message.
 *
 * @author Paul King
 */
public abstract class AbstractMessageOperationStep extends AbstractBaseStep implements ISingleMessageStep
{
    private String fMessageId;
    private boolean fDeleteOnExit;

    protected AbstractMessageOperationStep(final boolean deleteOnExit) {
        fDeleteOnExit = deleteOnExit;
    }

    public String getMessageId() {
        return fMessageId;
    }

    /**
     * Sets the message id.
     *
     * @param id The id of the message of interest
     * @webtest.parameter
     *   required="yes"
     *   description="The message id."
     */
    public void setMessageId(final String id) {
        fMessageId = id;
    }

    protected abstract void performOperation(Message message) throws MessagingException;

    public void doExecute() throws Exception {
        final EmailConfigInfo configInfo = (EmailConfigInfo) getContext().get("EmailConfigInfo");
        Folder folder = null;
        try {
            folder = getHelper().getInboxFolder(configInfo);
            final int id = ConversionUtil.convertToInt(getMessageId(), 0);
            final Message message = getHelper().getMessage(id, folder);
            if (message == null) {
                throw new StepFailedException("Could not retrieve message.", this);
            }
            performOperation(message);
        } catch (MessagingException e) {
            throw new StepFailedException("Error performing operation: " + e.getMessage(), this);
        } finally {
            getHelper().logout(folder, fDeleteOnExit);
        }
    }

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() {
        super.verifyParameters();
        integerParamCheck(fMessageId, "messageId", true);
    }

}
