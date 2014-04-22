package com.canoo.webtest.extension;


import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;


/**
 * @author Marc Guillemot, Paul King
 */
public class StoreLinkParameterTest extends BaseStepTestCase
{
    public void testExtractParameterValue() {
        assertEquals("25", StoreLinkParameter.extractParameterValue("toto.html?titi=25&foo=qwertz", "titi"));
        assertEquals("25",
        		StoreLinkParameter.extractParameterValue("http://mysite/toto.html?titi=25&foo=qwertz", "titi"));
        assertEquals("qwertz", StoreLinkParameter.extractParameterValue("toto.html?titi=25&foo=qwertz", "foo"));
        assertEquals("25", StoreLinkParameter.extractParameterValue("toto.html?foo=qwertz&titi=25", "titi"));
        assertEquals("qwertz", StoreLinkParameter.extractParameterValue("toto.html?foo=qwertz&titi=25", "foo"));
        assertNull(StoreLinkParameter.extractParameterValue("toto.html?foo=qwertz&titi=25", "foo2"));
        assertNull(StoreLinkParameter.extractParameterValue("toto.html?foo=qwertz", "foo2"));
        assertNull(StoreLinkParameter.extractParameterValue("toto.html", "foo2"));
    }

    private StoreLinkParameter fStep;

	protected Step createStep() {
        return new StoreLinkParameter();
    }

	protected void setUp() throws Exception
	{
		super.setUp();
		fStep = (StoreLinkParameter) getStep();
	}

    public void testVerifyParameterUsage() {
        fStep.setParameter("someParam");
        assertStepRejectsNullParam("htmlid", getExecuteStepTestBlock());

        fStep.setParameter(null);
        fStep.setHtmlId("someId");
        assertStepRejectsNullParam("parameter", getExecuteStepTestBlock());
    }

    public void testVerifyParametersWithoutPreviousPage() {
        fStep.setParameter("someParam");
        fStep.setHtmlId("someId");
        assertStepRejectsNullResponse(fStep);
    }

}
