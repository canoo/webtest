// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.form;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;


/**
 * Test cases for {@link com.canoo.webtest.steps.form.SetSelectField}.
 * @author Marc Guillemot
 */
public class SetSelectFieldTest extends BaseStepTestCase {
	private static final String HTML_CONTENT = "<html><head><title>foo</title></head>"
		+ "<body>"
	   + "<form name='testForm'>"
	   + "<select name='mySelect' id='mySelectId'>"
	   + "<option value='0'>0</option>"
	   + "<option value='1'>first</option>"
	   + "<option value='2' id='opt2'>second</option>"
	   + "<option value='3'>third</option>"
	   + "<option>4</option>"
	   + "<option selected>5</option>"
	   + "</select>"
	   + "</form>"
	   + "No access</body></html>";
    private SetSelectField fStep;

	protected Step createStep() {
		return new SetSelectField();
	}

	protected void setUp() throws Exception {
		super.setUp();
		fStep = (SetSelectField) getStep();
	}

	public void testSelectOption() {
		final HtmlPage page = getDummyPage(HTML_CONTENT);
		final HtmlForm form = page.getFormByName("testForm");
		final HtmlSelect select = form.getSelectByName("mySelect");

		// test with value
		fStep.setName("mySelect");
		fStep.setValue("2");
		HtmlOption option = fStep.findMatchingOption(select);
		fStep.updateOption(select, option);

		assertEquals("second", option.asText());
		assertTrue(option.isSelected());

		// test with value... for an option that has no value
		fStep.setValue("4");
		option = fStep.findMatchingOption(select);
		assertEquals("4", option.asText());

		// test with index
		fStep.setValue(null);
		fStep.setOptionIndex("1");
		option = fStep.findMatchingOption(select);
		fStep.updateOption(select, option);
		assertEquals("first", option.asText());
		assertTrue(option.isSelected());

		// test with text
		fStep.setOptionIndex(null);
		fStep.setText("third");
		option = fStep.findMatchingOption(select);
		fStep.updateOption(select, option);
		assertEquals("3", option.getValueAttribute());
		assertTrue(option.isSelected());

		// check not finding option fails step
		ThrowAssert.assertThrows(StepFailedException.class, new TestBlock() {
			public void call() throws Throwable {
				fStep.updateOption(select, null);
			}
		});
	}

	public void testFailureConditions() throws Exception {
		fStep.setName("mySelect");

		fStep.setOptionIndex("1");
		fStep.setValue("anything");
		fStep.setText("some text");
		assertErrorOnExecute(fStep, "multiple args not allowed", SetSelectField.AT_MOST_ONE_VALUE_TEXT_OPTIONINDEX);

		fStep.setValue(null);
        assertErrorOnExecute(fStep, "multiple args not allowed", SetSelectField.AT_MOST_ONE_VALUE_TEXT_OPTIONINDEX);

		fStep.setOptionIndex(null);
		fStep.verifyParameters();
		assertFalse(fStep.isRegex());
		assertFalse(fStep.isMultiSelect());
	}

	/**
	 * Test identification of select and option with xpath 
	 */
	public void testXPath()
	{
        getContext().setDefaultResponse(HTML_CONTENT);

        final HtmlPage page = getContext().getCurrentHtmlResponse(fStep);
        final HtmlSelect select = page.getFormByName("testForm").getSelectByName("mySelect");
        final HtmlOption option0 = select.getOption(0);
        final HtmlOption option1 = select.getOption(1);
        final HtmlOption option2 = select.getOption(2);
        final HtmlOption option3 = select.getOption(3);
       
        // test with xpath for the option
		fStep.setXpath("//option[@value='3']");
		assertFalse(option3.isSelected());
        fStep.execute();
		assertTrue(option3.isSelected());
		
        // test with xpath for the select and text for the option
		fStep.setXpath("//select");
		fStep.setText("first");
        fStep.execute();
		assertTrue(option1.isSelected());
	
        // test with xpath for the select and text for the option
		fStep.setXpath("//select");
		fStep.setText(null);
		fStep.setValue("0");
        fStep.execute();
		assertTrue(option0.isSelected());

        // test with xpath for the select and optionIndex for the option
		fStep.setXpath("//select");
		fStep.setText(null);
		fStep.setValue(null);
		fStep.setOptionIndex("2");
        fStep.execute();
		assertTrue(option2.isSelected());
	}

	/**
	 * Test identification of select and option with id 
	 */
	public void testHtmlId()
	{
        getContext().setDefaultResponse(HTML_CONTENT);

        final HtmlPage page = getContext().getCurrentHtmlResponse(fStep);
        final HtmlSelect select = page.getFormByName("testForm").getSelectByName("mySelect");
        final HtmlOption option1 = select.getOption(1);
        final HtmlOption option2 = select.getOption(2);
       
        // test with id for the option
		fStep.setHtmlId("opt2");
		assertFalse(option2.isSelected());
        fStep.execute();
		assertTrue(option2.isSelected());
		
        // test with id for the select and text for the option
		fStep.setHtmlId("mySelectId");
		fStep.setText("first");
        fStep.execute();
		assertTrue(option1.isSelected());

        // test with id for the select and nothing for the option
		fStep.setHtmlId("mySelectId");
		fStep.setText(null);
		assertErrorOnExecute(fStep, "", SetSelectField.MESSAGE_MISSING_OPTION_IDENTIFIER);
	}
	
	/**
	 * Regression test for bug WT-143: onchange was wrongly triggered twice
	 */
	public void testOnchangeTriggeredTwice()
	{
		// add onchange handler to standard code
		final String html = HTML_CONTENT.replaceFirst("id='mySelectId'", "id='mySelectId' onchange='alert(this.selectedIndex)')");
        getContext().setDefaultResponse(html);
		
        final HtmlPage page = getContext().getCurrentHtmlResponse(fStep);
        final WebClient client = page.getWebClient();
        final List collectedAlerts = new ArrayList();
        final AlertHandler alertHandler = new CollectingAlertHandler(collectedAlerts);
        client.setAlertHandler(alertHandler);

        fStep.setName("mySelect");
        fStep.setValue("3");
        fStep.execute();
        
        final List expectedAlerts = Collections.singletonList("3");
        assertEquals(expectedAlerts, collectedAlerts);
	}
}
