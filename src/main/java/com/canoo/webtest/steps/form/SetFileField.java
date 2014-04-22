// Released under the Canoo Webtest license.
package com.canoo.webtest.steps.form;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.FormUtil;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlFileInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;

import java.io.File;
import java.io.IOException;

/**
 * Supports the &lt;input type=\"file\" \.\.\. />form field.<p>
 *
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step
 *   category="Core"
 *   name="setFileField"
 *   alias="setfilefield"
 *   description="Provides the ability to set an <input type=\"file\" ...> form field."
 */
public class SetFileField extends AbstractSetFieldStep {
    private File fFileName;
    private boolean fCheckFileExists = true;

    /**
     * Sets the name of the file to be uploaded.
     *
     * @param filename
     * @webtest.parameter
     * 	required="true"
     *  description="The name of the file to upload."
     */
    public void setFileName(final File filename) {
        fFileName = filename;
    }

    public File getFileName() {
        return fFileName;
    }

    /**
     * Indicates if WebTest should verify that the file to upload exists.
     * @param b the new value
     * @webtest.parameter
     * 	required="false"
     *  default="true"
     *  description="Indicates if the step should verify that the file to upload exists.
     *  It is useful to deactivate this check, when you want to test how the application react
     *  when a file input field is filled with a wrong value."
     */
    public void setCheckFileExists(final boolean b) {
    	fCheckFileExists = b;
    }
    
    public boolean isCheckFileExists() {
    	return fCheckFileExists;
    }

    protected HtmlForm findForm() {
        return FormUtil.findFormForField(getContext(), getFormName(),
                HtmlConstants.INPUT, HtmlConstants.FILE, getName(), this);
    }

	protected boolean keepField(HtmlElement elt) {
		return HtmlConstants.FILE.equals(elt.getAttribute(HtmlConstants.TYPE));
	}

	protected void setField(HtmlElement element) throws IOException {
		// useful only if element was searched by id or xpath
		String attributeValue = element.getAttribute(HtmlConstants.TYPE);
		if (!HtmlConstants.FILE.equals(attributeValue)) {
			throw new StepFailedException("HTML input with id='" + getHtmlId() + "' is of type '"
				+ attributeValue + "' but should be '" + HtmlConstants.FILE + "'", this);
		}
		final HtmlFileInput fileInput = (HtmlFileInput) element;
		fileInput.setValueAttribute(getFileName().getAbsolutePath());
	}

	protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getFileName(), "fileName");
        if (isCheckFileExists() && !getFileName().exists())
        {
        	throw new StepExecutionException("File doesn't exist: " + getFileName().getAbsolutePath(), this);
        }
    }
}