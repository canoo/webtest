// Copyright © 2005-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.form;


import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * Test cases for {@link SetCheckbox}
 * @author Paul King, ASERT
 * @author Marc Guillemot
 */
public class SetCheckBoxTest extends AbstractSetFieldStepTest
{
    private SetCheckbox fStep;

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (SetCheckbox) getStep();
    }

    protected Step createStep() {
        return new SetCheckbox();
    }

    // <setCheckbox name="nomatch" />
    public void testNoMatch() {
        final String htmlContent = wrapContent(
                "<form name='testForm'>" + "<input type='checkbox' name='bike'>I have a bike<br>"
                + "<input type='checkbox' name='car'>I have a car" + "</form>");
        getContext().setDefaultResponse(htmlContent);

        fStep.setName("nomatch");
        assertFailOnExecute(fStep, "checkbox with this name should not be found", "No suitable form found");
    }

    // <setCheckbox name="car" value="I have a bike" />
    public void testNameValueMismatch() {
        final String htmlContent = wrapContent(
                "<form name='testForm'>" + "<input type='checkbox' name='bike'>I have a bike<br>"
                + "<input type='checkbox' name='car'>I have a car" + "</form>");

        getContext().setDefaultResponse(htmlContent);
        fStep.setName("car");
        fStep.setValue("I have a bike");
        assertFailOnExecute(fStep, "checkbox name should not match value", "No suitable field(s) found");
    }

    // <setCheckbox name="car" value="I have a Holden" />
    public void testNoMatchByValue() {
        final String htmlContent = wrapContent(
                "<form name='testForm'>" + "<input type='checkbox' name='car'>I have a Ford<br>"
                + "<input type='hidden' name='car'>I have a Holden" + "</form>");
        getContext().setDefaultResponse(htmlContent);
        fStep.setName("car");
        fStep.setValue("I have a Holden");
        assertFailOnExecute(fStep, "name should match checkbox not other input field", "No suitable field(s) found");
    }


    // <setCheckbox />
    public void testMandatoryAttributes() {
    	final Throwable thrown = assertErrorOnExecute(fStep, "", "");
        assertTrue(thrown.getMessage().indexOf(AbstractSetFieldStep.MESSAGE_ARGUMENT_MISSING) != -1);
    }

    private static String wrapContent(final String content) {
        return "<html><head><title>foo</title></head><body>" + content + "</body></html>";
    }

    public void testXPath() throws Exception {
        final String htmlContent = wrapContent(
                "<form name='testForm'>" 
        		+ "<input type='checkbox' name='car' value='car' id='it'>I have a Ford<br>"
                + "<input type='hidden' name='car'>I have a Holden" 
        		+ "<input type='checkbox' name='car' value='bike'>I have a bike<br>"
                + "</form>");
        getContext().setDefaultResponse(htmlContent);
        
        final HtmlPage page = getContext().getCurrentHtmlResponse(fStep);
        final HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) page.getHtmlElementById("it");
        assertFalse(checkbox.isChecked());

        fStep.setXpath("//input[@type = 'checkbox' and @value = 'car']");
        fStep.execute();
        
        assertTrue(checkbox.isChecked());

        fStep.setHtmlId("it");
        fStep.setXpath(null);
        fStep.setChecked("false");
        fStep.execute();
        assertFalse(checkbox.isChecked());
    }

}
