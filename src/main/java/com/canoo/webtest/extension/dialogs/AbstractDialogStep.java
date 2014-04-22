package com.canoo.webtest.extension.dialogs;

import com.canoo.webtest.steps.verify.AbstractVerifyTextStep;
import com.canoo.webtest.engine.StepFailedException;

public abstract class AbstractDialogStep extends AbstractVerifyTextStep
{
    private String fResponse;
    private String fSaveProperty;
    private String fSavePropertyType;

    protected AbstractDialogStep() {
        super();
    }

    protected AbstractDialogStep(final String response, final String text, final String regex,
                                 final String saveProperty, final String savePropertyType) {
        setRegex(regex);
        setText(text);
        this.fResponse = response;
        this.fSaveProperty = saveProperty;
        this.fSavePropertyType = savePropertyType;
    }

    /**
     * The name of the property used to save dialog text.
     *
     * @param value The new saveProperty value
     * @webtest.parameter required="no"
     * description="The name of the property in which to store the dialog text for later checking with \"verifyProperty\".  Must not be set if <em>text</em> is also set."
     */
    public void setSaveProperty(final String value) {
        fSaveProperty = value;
    }

    public String getSaveProperty() {
        return fSaveProperty;
    }

    /**
     * The type of the property used to save dialog text ("ant" or "dynamic").
     *
     * @param value The new savePropertyType value
     * @webtest.parameter required="no"
     * description="The type of the property in which to store the dialog text for later checking. Either \"ant\" or \"dynamic\".  Ignored if <em>saveProperty</em> is not set."
     * default="the \"defaultPropertyType\" as specified in the \"config\" element is used."
     */
    public void setSavePropertyType(final String value) {
        fSavePropertyType = value;
    }

    public String getSavePropertyType() {
        return fSavePropertyType;
    }

    /**
     * The response value returned to the JavaScript
     *
     * @param value The new response value
     * @webtest.parameter required="no"
     * description="simulate user response: ignored for alerts; converted to boolean for confirms - \"OK\" (true) or \"Cancel\" (false); contains typed text for prompts."
     * default="true"
     */
    public void setResponse(final String value) {
        fResponse = value;
    }

    public String getResponse() {
        return fResponse;
    }

    public void verify(final String actual) {
        if (getText() != null && !super.verifyText(actual)) {
            throw new StepFailedException("Wrong dialog message found! [Regex=" + isRegex() + "]",
            		getText(), actual, this);
        }
    }
}
