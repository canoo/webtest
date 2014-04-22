package com.canoo.webtest.extension;

import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * A diff algorithm able to compare the content 2 {@link WebResponse}.
 * An instance should allow multiple calls to {@link #compare} may occur within test execution. 
 * @author Marc Guillemot
 */
public interface VerifyContentDiff 
{
	/**
	 * Produce the diff of the content of 2 {@link WebResponse}. 
	 * @param reference the reference content
	 * @param actual the actual content
	 * @param referenceLabel the label to display in diff message for the reference content
	 * @param actualLabel the label to display in diff message for the actual content
	 * @return <code>null</code> if content is identical, information allowing to 
	 * understand the differences otherwise
	 */
	String compare(final WebResponse reference, final WebResponse actual, 
			final String referenceLabel, final String actualLabel);
}
