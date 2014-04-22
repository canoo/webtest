// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.emailtest;

import javax.mail.Folder;
import javax.mail.MessagingException;

import com.canoo.webtest.engine.StepFailedException;

/**
 * Abstract class used by email steps which store a result.
 *
 * @author Paul King
 */
public abstract class AbstractStoreStep extends AbstractBaseStep {
    private String fPropertyName;
    private String fPropertyType;

    public String getProperty() {
        return fPropertyName;
    }

    /**
     * Sets the target property name.
     *
     * @param name The Property Name
     * @webtest.parameter
     *   required="yes"
     *   description="The target property name."
     */
    public void setProperty(final String name) {
        fPropertyName = name;
    }

    /**
     * Sets the target property type.
     *
     * @param type The Property type
     * @webtest.parameter
     *   required="no"
     *   description="The target property type. Either \"ant\" or \"dynamic\"."
     *   default="the \"defaultPropertyType\" as specified in the \"config\" element is used."
     */
    public void setPropertyType(final String type) {
        fPropertyType = type;
    }

    public String getPropertyType() {
        return fPropertyType;
    }

    public void doExecute() {
        final EmailConfigInfo configInfo = (EmailConfigInfo) getContext().get("EmailConfigInfo");
        Folder folder = null;
        try {
            folder = getHelper().getInboxFolder(configInfo);
            setWebtestProperty(getProperty(), processContent(folder), getPropertyType());
        }
        catch (final MessagingException e) {
            throw new StepFailedException("Error processing content: " + e.getMessage(), this);
        }
        finally {
            getHelper().logout(folder, false);
        }
    }

    protected abstract String processContent(Folder info) throws MessagingException;

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getProperty(), "property");
    }
}
