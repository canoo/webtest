// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.canoo.webtest.engine.NameValuePair;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Unit tests for {@link ClickButton}.
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King
 * @author Denis N. Antonioli
 */
public class ClickButtonTest extends AbstractTargetActionTest {

	@Test
	public void testValidEmptyParameters() {
		final ClickButton step = new ClickButton();
		step.setName("");
		assertVerificationOk("empty name", step);
		step.setName(null);

		step.setLabel("");
		assertVerificationOk("empty label", step);
		step.setLabel(null);

		step.setHtmlId("");
		assertVerificationOk("empty htmlid", step);
	}

	@Test
	public void testVerifyParameterValid() {
		ClickButton step = new ClickButton();
		step.setX("1");
		step.setY("2");
		step.setLabel("myLabel");
		assertVerificationOk("1) X and Y and label", step);

		step = new ClickButton();
		step.setX("1");
		step.setY("2");
		step.setName("myName");
		assertVerificationOk("2) X and Y and name", step);

		step = new ClickButton();
		step.setX("-1");
		step.setY("-2");
		step.setName("myName");
		assertVerificationOk("2) negative X and Y and name", step);

		step = new ClickButton();
		step.setX("1");
		step.setY("2");
		step.setLabel("myLabel");
		step.setName("myName");
		assertVerificationOk("3) X and Y and name and label", step);

		step = new ClickButton();
		step.setName("myName");
		step.setLabel("myLabel");
		assertVerificationOk("4) name and label", step);

		step = new ClickButton();
		step.setHtmlId("myName");
		assertVerificationOk("5) htmlId", step);
	}

	@Test
	public void testVerifyParameterInvalid() {
		ClickButton step = new ClickButton();

		step.setX("1");
		step.setY("2");
		assertVerificationOk("", step);

		step = new ClickButton();
		step.setName("myName");
		step.setX("1");
		assertVerificationFails("2) if X is set Y must be set as well", step);

		step = new ClickButton();
		step.setName("myName");
		step.setY("2");
		assertVerificationFails("3) if Y is set X must be set as well", step);

		step = new ClickButton();
		step.setName("myName");
		step.setFieldIndex("-15");
		assertVerificationFails("4) index should be >= 0", step);
	}

	private void assertVerificationFails(final String message, final ClickButton step) {
		ThrowAssert.assertThrows(message, StepExecutionException.class, new TestBlock() {
			public void call() throws Exception {
                step.verifyParameters();
			}
		});
	}

	private void assertVerificationOk(final String message, final ClickButton step) {
        configureStep(step);
		ThrowAssert.assertPasses(message, new TestBlock() {
			public void call() throws Exception {
                step.verifyParameters();
			}
		});
	}

	@Test
    public void testErrorWithXmlPage() throws Exception {
		final ClickButton step = new ClickButton();
        configureStep(step);
        step.setName("foo");
        assertErrorOnExecuteIfCurrentPageIsXml(step);
    }

	@Test
    public void testFindButton() throws Exception {
		final String htmlContent = "<html><head><title>foo</title></head>" + "<body>"
		   + "<form id='formId' name='testForm'>"
		   + "  <input type='submit' name='inputSubmit'>"
		   + "  <div><input type='submit' name='inputSubmit2'></div>"
		   + "  <input type='SUBMIT' name='inputSubmitUpperCase'>"
		   + "  <input type='image' name='inputImage'>"
		   + "  <input type='image' name='inputImage2' src='myImage.png' alt='my small image'>"
		   + "  <input type='button' name='inputButton'>"
		   + "  <button type='submit' name='buttonSubmit'>"
		   + "  <button type='submit' id='buttonBlabla'>bla bla</button>"
		   + "  <button id='buttonBlablaBold'>bla <b>bla Bold</b></button>"
		   + "  <div><button type='submit' name='buttonSubmit2'></div>"
		   + "  <input type='button' name='toto' id='myButtonWithId'>"
		   + "  <input type='button' name='toto2' value='toto2_1'>"
		   + "  <input type='button' name='toto2' value='toto2_2'>"
		   + "  <input type='reset' name='resetButton'>"
		   + "</form>"
		   + "No access</body></html>";

		getContext().setDefaultResponse(htmlContent);

		final HtmlPage page = (HtmlPage) getContext().getWebClient().getPage(new URL("http://myHost"));
		final HtmlForm form = page.getFormByName("testForm");
		final HtmlInput inputButtonToto2 = (HtmlInput) form.getInputsByName("toto2").get(1);
		final HtmlInput inputButtonReset = form.getInputByName("resetButton");
		final HtmlInput inputSubmitUpperCase = form.getInputByName("inputSubmitUpperCase");
		final HtmlInput inputSubmit = form.getInputByName("inputSubmit");

        final ClickButton step = new ClickButton();
		configureStep(step);

		// without any attribute => first button
		assertSame(inputSubmit, step.findButton(form));

		step.setName("notExisting");
		assertNull(step.findButton(form));

		step.setName("inputSubmit");
		assertEquals(HtmlConstants.SUBMIT, step.findButton(form).getAttribute(HtmlConstants.TYPE));

		step.setName("inputSubmit2");
		assertEquals(HtmlConstants.SUBMIT, step.findButton(form).getAttribute(HtmlConstants.TYPE));

		step.setName("inputSubmitUpperCase");
		assertSame(inputSubmitUpperCase, step.findButton(form));

		step.setName("inputImage");
		assertEquals(HtmlConstants.IMAGE, step.findButton(form).getAttribute(HtmlConstants.TYPE));

		step.setName("inputButton");
		assertEquals(HtmlConstants.BUTTON, step.findButton(form).getAttribute(HtmlConstants.TYPE));

		step.setName("buttonSubmit");
		assertEquals(HtmlConstants.SUBMIT, step.findButton(form).getAttribute(HtmlConstants.TYPE));

		step.setName("buttonSubmit2");
		assertEquals(HtmlConstants.SUBMIT, step.findButton(form).getAttribute(HtmlConstants.TYPE));

		step.setName("resetButton");
		assertSame(inputButtonReset, step.findButton(form));
		step.setName(null);

		// label
		step.setLabel("bla bla");
		assertSame(page.getHtmlElementById("buttonBlabla"), step.findButton(form));

		step.setLabel("bla bla Bold");
		assertSame(page.getHtmlElementById("buttonBlablaBold"), step.findButton(form));

		step.setLabel("my small image");
		assertSame(form.getInputByName("inputImage2"), step.findButton(form));

		step.setLabel(null);

		// htmlid
		step.setHtmlId("myButtonWithId");
		assertEquals("toto", step.findClickableElementById(page).getAttribute(HtmlConstants.NAME));

		step.setName("toto2");
		step.setHtmlId(null);
		assertEquals("toto2_1", step.findButton(form).getAttribute(HtmlConstants.VALUE));
		step.setFieldIndex("1");
		assertSame(inputButtonToto2, step.findButton(form));

		step.setHtmlId("nonExistingId");
		assertNull(step.findClickableElementById(page));

		step.setHtmlId("formId");
		String msg = ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
		{
			public void call() throws Exception {
				step.findClickableElementById(page);
			}
		});
		assertTrue(msg.indexOf("not a button") != -1);

		step.setName("wrongname");
		step.setHtmlId("myButtonWithId");
		assertNull(step.findClickableElementById(page));


		step.setName(null);
		step.setHtmlId(null);
		step.setXpath("//input[@name='toto2'][2]");
		assertSame(inputButtonToto2, step.findClickableElement(page));
	}

    // <clickButton xpath="//input[@name='toto2'][2]" />
	@Test
    public void testExecuteXPath() throws Exception {
        final String htmlContent = "<html><head><title>foo</title></head>" 
        	+ "<body>"
		   + "<form id='formId' name='testForm'>"
		   + "<input type='button' name='toto2' value='toto2_1'>"
		   + "<input type='button' name='toto2' value='toto2_2' onclick='alert(\"foo\")'>"
		   + "</form>"
		   + "No access</body></html>";

        final ClickButton step = new ClickButton();
		configureStep(step);
        step.setXpath("//input[@name='toto2'][2]");
        final HtmlPage page = getDummyPage(htmlContent);
        final WebClient client = page.getWebClient();
        final List<String> collectedAlerts = new ArrayList<String>();
        final AlertHandler alertHandler = new CollectingAlertHandler(collectedAlerts);
        client.setAlertHandler(alertHandler);
        
        step.execute();
        
        final List<String> expectedAlerts = Collections.singletonList("foo");
        assertEquals(expectedAlerts, collectedAlerts);
    }

    // <clickButton />
	@Test
    public void testExecute_noAttr() throws Exception {
        final String htmlContent = "<html><head><title>foo</title></head>" 
        	+ "<body>"
		   + "<form id='form1' name='testForm1'>"
		   + "<input type='button' name='toto1' value='toto1_1' onclick='alert(this.value)'>"
		   + "<input type='button' name='toto1' value='toto1_2' onclick='alert(this.value)'>"
		   + "</form>"
		   + "<form id='form2' name='testForm2'>"
		   + "<input type='button' name='toto2' value='toto2_1' onclick='alert(this.value)'>"
		   + "<input type='button' name='toto2' value='toto2_2' onclick='alert(this.value)'>"
		   + "</form>"
		   + "No access</body></html>";

        final ClickButton step = new ClickButton();
		configureStep(step);

        final HtmlPage page = getDummyPage(htmlContent);
        final WebClient client = page.getWebClient();
        final List<String> collectedAlerts = new ArrayList<String>();
        final AlertHandler alertHandler = new CollectingAlertHandler(collectedAlerts);
        client.setAlertHandler(alertHandler);
        
        step.execute();
        
        assertEquals(Collections.singletonList("toto1_1"), collectedAlerts);

        step.getContext().setCurrentForm(page.getFormByName("testForm2"));
        collectedAlerts.clear();
        step.execute();
        assertEquals(Collections.singletonList("toto2_1"), collectedAlerts);
    }

    /**
     * Test that the exception contains information on the available buttons
     */
	@Test
    public void testExceptionDetails() throws Exception {
        final String htmlContent = "<html><head><title>foo</title></head>" 
        	+ "<body>"
		   + "<form id='formId' name='testForm'>"
		   + "<input type='button' name='toto2' value='toto2_1'>"
		   + "<input type='submit' name='toto2' value='Submit me!'>"
		   + "</form>"
		   + "No access</body></html>";

        final ClickButton step = new ClickButton();
		configureStep(step);
        step.setLabel("not existing");
        getDummyPage(htmlContent);
        
        final StepFailedException e = (StepFailedException) 
        	ThrowAssert.assertThrows("", StepFailedException.class, getExecuteStepTestBlock(step));
        final NameValuePair detail = (NameValuePair) e.getDetails().get(0);
        assertEquals("available buttons", detail.getName());
        assertTrue(detail.getValue().indexOf("Submit me!") != -1);
    }

	@Test
    public void testNestedText() throws Exception {
    	final ClickButton step = (ClickButton) configureStep(new ClickButton());
    	testNestedTextEquivalent(step, "label");
    }
}
