// Copyright © 2005-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.form;


import org.junit.Test;

import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;


/**
 * Test cases for {@link SetRadioButton}.
 * @author Marc Guillemot
 */
public class SetRadionButtonTest extends AbstractSetFieldStepTest
{
    private SetRadioButton fStep;

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (SetRadioButton) getStep();
    }

    protected Step createStep() {
        return new SetRadioButton();
    }

    @Test
    public void testXPathAndHtmlId() throws Exception {
        final String htmlContent = wrapContent(
                "<form name='testForm'>" 
        		+ "<input type='radio' name='car' value='car' id='cbCar'>I have a Ford<br>"
                + "<input type='hidden' name='car'>I have a Holden" 
        		+ "<input type='radio' name='car' value='bike' id='cbBike'>I have a bike<br>"
        		+ "<input type='radio' name='car' value='mini' id='cbMini'><label for='cbMini'>I have a mini</label><br>"
                + "</form>");
        getContext().setDefaultResponse(htmlContent);
        
        final HtmlPage page = getContext().getCurrentHtmlResponse(fStep);
        final HtmlRadioButtonInput radioCar = (HtmlRadioButtonInput) page.getHtmlElementById("cbCar");
        final HtmlRadioButtonInput radioBike = (HtmlRadioButtonInput) page.getHtmlElementById("cbBike");
        final HtmlRadioButtonInput radioMini = (HtmlRadioButtonInput) page.getHtmlElementById("cbMini");

        assertFalse(radioCar.isChecked());
        assertFalse(radioBike.isChecked());

        fStep.setXpath("//input[@type = 'radio' and @value = 'car']");
        fStep.execute();
        
        assertTrue(radioCar.isChecked());
        assertFalse(radioBike.isChecked());


        fStep.setHtmlId("cbBike");
        fStep.setXpath(null);
        fStep.execute();
        assertFalse(radioCar.isChecked());
        assertTrue(radioBike.isChecked());

        fStep.setHtmlId(null);
        fStep.setForLabel("I have a mini");
        fStep.execute();
        assertTrue(radioMini.isChecked());
        assertFalse(radioBike.isChecked());
    }


    @Test
    public void testNameWithQuotes() throws Exception {
        final String htmlContent = wrapContent(
                "<form name='testForm'>" 
        		+ "<input type='radio' name=\"with['bracketAndQuotes']\" value=\"I'm here\" id='cbLisa'>Lisa bug<br>"
                + "</form>");
        getContext().setDefaultResponse(htmlContent);
        
        final HtmlPage page = getContext().getCurrentHtmlResponse(fStep);
        final HtmlRadioButtonInput radioLisa = (HtmlRadioButtonInput) page.getHtmlElementById("cbLisa");

        assertFalse(radioLisa.isChecked());

        fStep.setName("with['bracketAndQuotes']");
        fStep.setValue("I'm here");
        fStep.execute();
        
        assertTrue(radioLisa.isChecked());

        // with ' and "
        radioLisa.setChecked(false);
        assertFalse(radioLisa.isChecked());

        fStep.setName("with['bracketAndQuotes']");
        radioLisa.setValueAttribute("'\"");
        fStep.setValue("'\"");
        fStep.execute();
        assertTrue(radioLisa.isChecked());
        
    }

    private static String wrapContent(final String content) {
        return "<html><head><title>foo</title></head><body>" + content + "</body></html>";
    }

    @Test
    public void testRedundantAttributes() {
    	fStep.setValue("foo");
    	fStep.setHtmlId("foo");
    	assertErrorOnExecute(fStep, "Can't specify attribute value when htmlid or xpath is specified", "");
    }
}
