// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.extension;


import java.util.Map;

import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.StepUtil;


/**
 * A "Copy-Me" template for possible extension steps for Canoo WebTest.<p>
 * You can use such a step as a nested element of the &lt;steps&gt; element in your WebTest ANT file.
 * You need to declare it via a proper &lt;taskdef&gt; in that file to allow ANT to lookup this class.
 * <p/>
 * This class contains what we believe to be "the least you need to do" for implementing a good-style
 * extension step.<br>
 * In case you need to change one of existing steps, you can use the very same approach: just
 * subclass the step you want to adapt and let the &lt;taskdef&gt; point to your implementation.
 *
 * @webtest.step category="Extension"
 * name="myCustomStep"
 * description="A \"Copy-Me\" template for possible extension steps for Canoo WebTest."
 */
public class MyCustomStep extends Step {
	private String fMyOptionalAttribute;
	private String fMyMandatoryAttribute;

	/**
	 * Perform the step's actual work. The minimum you need to implement.
	 *
	 * @throws com.canoo.webtest.engine.StepFailedException
	 *          if step was not successful
	 */
	public void doExecute() {
		// execution goes here
	}

	public String getMyOptionalAttribute() {
		return fMyOptionalAttribute;
	}

	/**
	 * Webtest's build uses javadoc tag to discover, document and access attributes.
	 * Ant does not support mixed-case attribute names. All characters are lower
	 * case except the first one.
	 *
	 * @webtest.parameter required="no"
	 * description="This attribute may or may not be there."
	 */
	public void setMyOptionalAttribute(final String myValue) {
		fMyOptionalAttribute = myValue;
	}

	public String getMyMandatoryAttribute() {
		return fMyMandatoryAttribute;
	}

	/**
	 * Webtest's build uses javadoc tag to discover, document and access attributes.
	 *
	 * @webtest.parameter required="yes"
	 * description="This attribute must always be there."
	 */
	public void setMyMandatoryAttribute(final String myValue) {
		fMyMandatoryAttribute = myValue;
	}

	/**
	 * Good style to verify mandatory parameters before execution.
	 * This method is called just before {@link #doExecute()}; parameter expansion has
	 * already occurred.
	 *
	 * @throws com.canoo.webtest.engine.StepExecutionException if a mandatory attribute is not set
	 */
	protected void verifyParameters() {
		super.verifyParameters();
		nullParamCheck(fMyMandatoryAttribute, "myMandatoryAttribute");
	}
}
