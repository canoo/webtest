// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.form;

import java.io.IOException;

import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Test cases for {@link com.canoo.webtest.steps.form.SetInputField}
 * @author Marc Guillemot
 */
public class SetInputFieldTest extends BaseStepTestCase
{
	private SetInputField fStep;

    protected Step createStep() {
        return new SetInputField();
    }

    protected void setUp() throws Exception {
		super.setUp();
		fStep = (SetInputField) getStep();
	}

	public void testDefineTextField() throws IOException {
        final String htmlContent = "<html><head><title>foo</title></head>" + "<body>"
                + "<form name='testForm'>" + "<input name='myInput'>" + "</form>"
                + "No access</body></html>";
        final HtmlPage page = getDummyPage(htmlContent);
        final HtmlForm form = page.getFormByName("testForm");
        final SetInputField step = new SetInputField();

        // test with value
        step.setName("myInput");
        step.setValue("some value");
        step.setField((HtmlElement) step.findFields(form).get(0));
        final HtmlInput input = form.getInputByName("myInput");
        assertEquals("some value", input.getValueAttribute());
    }


	/**
	 * Test identification of text field with the label of an associated 'label' form field.<p> 
	 */
	public void testForLabel()
	{
		final String html = "<html><body>"
			+ "<form name='testForm'>"
			+ "<label for='theFirstName'>First Name</label>: <input name='name' id='theFirstName' type='text'/>"
			+ "<label for='theAddress'>Address</label>: <input name='address' id='theAddress' type='text'/>"
			+ "</form>"
			+ "</body></html>";
        getContext().setDefaultResponse(html);

        final HtmlPage page = getContext().getCurrentHtmlResponse(fStep);
        final HtmlTextInput inputFirstName = (HtmlTextInput) page.getHtmlElementById("theFirstName");
        final HtmlTextInput inputAddress = (HtmlTextInput) page.getHtmlElementById("theAddress");
       
		fStep.setForLabel("First Name");
		fStep.setValue("bla bla");
		assertEquals("", inputFirstName.getValueAttribute());
        fStep.execute();
		assertEquals("bla bla", inputFirstName.getValueAttribute());
		
		fStep.setForLabel("Address");
		fStep.setValue("11 Downing Street");
		assertEquals("", inputAddress.getValueAttribute());
        fStep.execute();
		assertEquals("11 Downing Street", inputAddress.getValueAttribute());
	}

	/**
	 * Test setting of text area 
	 */
	public void testTextArea()
	{
		final String html = "<html><body>"
			+ "<form name='testForm'>"
			+ "<textarea name='foo' id='foo' onchange='document.getElementById(\"log\").innerHTML += 1'></textarea>"
			+ "</form>"
			+ "<span id='log'></span>"
			+ "</body></html>";
        getContext().setDefaultResponse(html);

        final HtmlPage page = getContext().getCurrentHtmlResponse(fStep);
        final HtmlTextArea textArea = page.getHtmlElementById("foo");
//        final HtmlElement log = page.getHtmlElementById("log");
       
		fStep.setHtmlId("foo");
		fStep.setValue("bla bla");
		assertEquals("", textArea.getText());
        fStep.execute();
		assertEquals("bla bla", textArea.getText());
//		assertEquals("bla bla", log.asText()); // doesn't work yet
	}
}
