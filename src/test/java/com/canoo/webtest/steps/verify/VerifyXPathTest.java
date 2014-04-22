// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.FunctionDef1Arg;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

import com.canoo.webtest.engine.xpath.XPathHelper;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Unit tests for {@link VerifyXPath}.
 * @author <a href="balld@webslingerZ.com">Donald Ball</a>, Carsten Seibert, Dierk König
 * @author Marc Guillemot, Paul King
 */
public class VerifyXPathTest extends BaseStepTestCase {
	private VerifyXPath fStep;

	protected void setUp() throws Exception {
		super.setUp();
		fStep = (VerifyXPath) getStep();
	}

	protected Step createStep() {
		return new VerifyXPath();
	}

	public void testRejectsNullXpath() {
		assertStepRejectsNullParam("xpath", getExecuteStepTestBlock());
	}

	public void testHandleHtmlPage() throws Exception {
		getContext().setDefaultResponse("<html><head></head><body><h1>hello</h1></body></html>");
		fStep.setXpath("/html/body/h1");
		executeStep(fStep);
	}

	public void testHandleXmlPage() throws Exception {
		getContext().setDefaultResponse("<xml><body><h1>hello</h1></body></xml>", "text/xml");
		fStep.setXpath("/xml/body/h1");
		executeStep(fStep);
	}

	/**
	 * Tests an xpath check on an XmlPage containing an badly formed xml document
	 * @throws Exception if the test fails
	 */
	public void testHandleInvalidXmlPage() throws Exception {
		getContext().setDefaultResponse("<xml type='foo & fii'></xml>", "text/xml");
		fStep.setXpath("/xml");

		assertFailOnExecute(fStep, "", "");
	}

	public void testHandleMissingPage() throws Exception {
		fStep.setXpath("'valeur'='value'");
		fStep.setText("false");
		executeStep(fStep);
		fStep.setXpath("/not/here");
		assertFailOnExecute(fStep, "", "");
	}

	public void testHandleUnknownPage() {
		getContext().setDefaultResponse("hello", "text/plain");
		fStep.setXpath("/html/head/title");
		assertFailOnExecute(fStep, "", "");
	}

    public void testLocateI18nText() throws Exception {
        final String htmlContent
                = "<html><body><h1>&#x4f60;</h1></body></html>";
        getContext().setDefaultResponse(htmlContent);

        fStep.setXpath("/html/body/h1");
        fStep.setText("&#x4f60;");
        assertFailOnExecute(fStep, "Numeric entity not found", "");
        fStep.setText("\u4f60");
        executeStep(fStep);
    }

    public void testLocateI18nNamedEntitiesText() throws Exception {
        final String htmlContent
                = "<html><body><h1>&uuml;</h1></body></html>";
        getContext().setDefaultResponse(htmlContent);

        fStep.setXpath("/html/body/h1");
        fStep.setText("&uuml;");
        assertFailOnExecute(fStep, "Named entity not found", "");
        fStep.setText("\u00fc");
        executeStep(fStep);
    }

    public void testExistingXPathExpression() throws Exception {
    	defineSampleDocumentAsCurrent();
		fStep.setXpath("/html/head/title");
		executeStep(fStep);
	}

	public void testNonExistingXPathExpression() throws Exception {
		defineSampleDocumentAsCurrent();
		fStep.setXpath("/not/here");
		assertFailOnExecute(fStep, "", "");
	}

	public void testNodeValueOK() throws Exception {
		defineSampleDocumentAsCurrent();
		fStep.setXpath("/html/head/title");
		fStep.setText("foo");
		executeStep(fStep);
	}

	public void testNodeValueNotOK() throws Exception {
		defineSampleDocumentAsCurrent();
		fStep.setXpath("/html/head/title");
		fStep.setText("bar");
		assertFailOnExecute(fStep, "", "");
	}
	
	public static class ReverseFunction extends FunctionDef1Arg
	{
		public XObject execute(final XPathContext xctxt) throws TransformerException
		{
			final StringBuffer sb = new StringBuffer();
			final String arg = getArg0AsString(xctxt).toString();
			for (int i=arg.length()-1; i>=0; --i)
				sb.append(arg.charAt(i));
			return new XString(sb.toString());
		}
	}

	/**
	 * Test use of variable, function and namespaces contexts
	 * @throws Exception if the test fails
	 */
	public void testContext() throws Exception {
		defineSampleDocumentAsCurrent();
		
		final XPathHelper xpathHelper = fStep.getContext().getXPathHelper();
		xpathHelper.getVariableContext().setVariableValue(new QName("foo"), "123");
		fStep.setXpath("$foo");
		fStep.setText("123");
		executeStep(fStep);

		// function without context
		fStep.setXpath("myReverse('abcdef')");
		fStep.setText("fedcba");
//		assertThrowOnExecute(fStep, "", "", XPathException.class);

		xpathHelper.getFunctionContext().registerFunction(new QName(null, "myReverse"), ReverseFunction.class);
		executeStep(fStep);

		// function with context
		fStep.setXpath("wt:myReverse2('abcdef')");
		fStep.setText("fedcba");
		assertThrowOnExecute(fStep, "", "", XPathExpressionException.class);
		xpathHelper.getFunctionContext().registerFunction(new QName("webtest/xpath", "myReverse2"), ReverseFunction.class);
		assertThrowOnExecute(fStep, "", "", XPathExpressionException.class);
		xpathHelper.getNamespaceContext().addNamespace("wt", "webtest/xpath");
		executeStep(fStep);
	}

	/**
	 * Xpath evaluating to boolean true without text should pass
	 * @throws Exception if the test fails
	 */
	public void testBooleanValueNoTextTrue() throws Exception {
		defineSampleDocumentAsCurrent();
		fStep.setXpath("1 > 0");
		executeStep(fStep);
	}

	/**
	 * Xpath evaluating to boolean true with text "true" should pass
	 * @throws Exception if the test fails
	 */
	public void testBooleanValueTextTrue() throws Exception {
		defineSampleDocumentAsCurrent();
		fStep.setXpath("1 > 0");
		fStep.setText("true");
		executeStep(fStep);
	}

	/**
	 * Xpath evaluating to boolean false without text should fail
	 * @throws Exception if the test fails
	 */
	public void testBooleanValueNoTextFalse() throws Exception {
		defineSampleDocumentAsCurrent();
		fStep.setXpath("1 < 0");
		assertFailOnExecute(fStep, "", "");
	}


	/**
	 * Xpath evaluating to boolean false with text "false" should pass
	 * @throws Exception if the test fails
	 */
	public void testBooleanValueTextFalse() throws Exception {
		defineSampleDocumentAsCurrent();
		fStep.setXpath("1 < 0");
		fStep.setText("false");
		executeStep(fStep);
	}

	public void testMissingSelectValue() throws Exception {
		final VerifyXPath step = new VerifyXPath();
		step.setXpath("/bla");
		assertFalse(step.isComparingPathAndValue());
		step.setText("bla");
		assertTrue(step.isComparingPathAndValue());
	}

	private void defineSampleDocumentAsCurrent() throws Exception {
		final String htmlDocument = "<html><head><title>foo</title></head><body>foobar</body></html>";
		getContext().setDefaultResponse(htmlDocument);
	}

	/**
	 * Test that text is NOT trimmed
	 * @throws Exception if the test fails
	 */
	public void testBlankBeforeElementContent() throws Exception {
		final String document
		   = "<html><body>"
		   + "<table>"
		   + "<tr><td>1.1</td><td>1.2</td><td>1.3</td></tr>"
		   + "<tr><td>  2.1</td><td>2.2</td><td>2.3</td></tr>"
		   + "</table>"
		   + "</body></html>";
		getContext().setDefaultResponse(document);
		fStep.setXpath("//tr[td/text() = '  2.1']/td[3]");
		fStep.setText("2.3");
		executeStep(fStep);
	}
	
	/**
	 * Test for WT-253: automatically extract namespaces from the xml
	 * @throws Exception if the test fails
	 */
	public void testXPathNamespaces() throws Exception
	{
		final String xml = "<?xml version=\"1.0\"?>\n"
		+ "<SOAP-ENV:Envelope\n"
		+ " xmlns:SOAP-ENV=\"http://webtest.canoo.com/soap/envelope/\"\n"
		+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n"
		+ "  <SOAP-ENV:Body>\n"
		+ "    <calculateFibonacci \n"
		+ "      type=\"xsi:positiveInteger\">10</calculateFibonacci>\n"
		+ "  </SOAP-ENV:Body>\n"
		+ "</SOAP-ENV:Envelope>";
		
		getContext().setDefaultResponse(xml, "text/xml");
		fStep.setXpath("//SOAP-ENV:Body/calculateFibonacci/text()");
		fStep.setText("10");
		executeStep(fStep);

		// check that WebTest goodies are still available
		fStep.setXpath("wt:cleanText(//SOAP-ENV:Body/calculateFibonacci)");
		fStep.setText("10");
		executeStep(fStep);
	}
	
//	/**
//	 * @throws Exception if the test fails
//	 */
//	public void testXPathNamespaces2() throws Exception
//	{
//		final String xml = "<?xml version=\"1.0\"?>\n"
//			+ "<identity xmlns='http://10.0.1.1/identity/v1' "
//			+ "xmlns:fsapi-v1='http://10.0.1.1/v1' version='1.0.20071219.0' "
//			+ "statusMessage='OK' statusCode='200'>"
//			+ "<session id='434DF084FA5968055B802AF6F4073725'/></identity>";
//		
//		getContext().setDefaultResponse(xml, "text/xml");
//		fStep.setXpath("//*:identitiy");
////		fStep.setText("10");
//		executeStep(fStep);
//	}
}
