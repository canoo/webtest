package com.canoo.webtest.steps;

import org.apache.commons.lang.StringUtils;


/**
 * Placeholder for information. This Step does nothing but as it is "executed" its parameter
 * are written in the results and can be formatted as needed for display.
 *
 * @author Marc Guillemot
 * @webtest.step category="Core"
 * name="testInfo"
 * description="Provides the ability to display 'static' information in report.
 * This step does nothing. It just gets reported as it (with its attributes) in the report."
 */
public class TestInfoStep extends Step 
{
    private String fType;
    private String fInfo;


    /**
     * Sets the information.
     *
     * @param info the information. May be any value as it has only to make sense for user
     * @webtest.parameter required="no"
     * description="The information that has to be displayed in the report (ex: related bug number, ...)"
     */
    public void setInfo(final String info) 
    {
        fInfo = info;
    }

    public String getInfo() 
    {
        return fInfo;
    }

    /**
     * Sets the type of the information. The type can be any string and has only a signification for the user.
     * @param type the information type
     * @webtest.parameter required="yes"
     * description="The information type.
     * Helps to classify info. Many information of the same type within a single WebTest are allowed.
     * The types <em>issueNumber</em> and <em>summary</em> have a special signification for WebTest's standard report generation."
     */
    public void setType(final String type) 
    {
        fType = type;
    }

    public String getType() {
        return fType;
    }

    public void doExecute() 
    {
    	// absolutely nothing, the single purpose of the step is to transfer typed information to the report 
    }

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() 
    {
        super.verifyParameters();
        emptyParamCheck(getType(), "type");
	}

    /**
	 * Called by ant to set the text contained in the tag.
	 * An alternative to value="blabla" for e.g. Large TextAreas. Usage:<br/>
	 * &lt;setInputField>blabla<br/>
	 * blibli<br/>
	 * &lt;/setInputField>
	 *
	 * @param str the text value to add
     * @webtest.nested.parameter
     *    required="no"
     *    description="An alternative to the attribute info for e.g. large texts."
     */
	public void addText(final String str) 
	{
		final String expanded = StringUtils.defaultString(getInfo()) + getProject().replaceProperties(str);
		setInfo(expanded);
	}
}