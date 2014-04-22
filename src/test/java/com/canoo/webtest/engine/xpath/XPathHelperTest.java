// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.Function;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNamespaceNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests for {@link XPathHelper}.
 * @author Marc Guillemot
 */
public class XPathHelperTest
{
	public static class MyFunction extends Function
	{
		@Override
		public XObject execute(XPathContext _xctxt) throws TransformerException
		{
			return new XString("foo");
		}
		@Override
		public void fixupVariables(final Vector _vars, final int _globalsSize)
		{
			// nothing
		}
	}

	@Test
	public void testGlobalObjects() throws Exception
	{
		final int nbGlobalFunctions = XPathHelper.getGlobalFunctions().size(); // the WebTest default ones
		XPathHelper.registerGlobalFunction("someUri", "myFunc", MyFunction.class);
		assertEquals(nbGlobalFunctions+1, XPathHelper.getGlobalFunctions().size());
		assertEquals(MyFunction.class, XPathHelper.getGlobalFunctions().get(new QName("someUri", "myFunc")));
		
		final int nbGlobalVariables = XPathHelper.getGlobalVariables().size(); // the WebTest default ones
		assertTrue(XPathHelper.getGlobalVariables().isEmpty());
		final Object myObject = new Object();
		XPathHelper.registerGlobalVariable("someUri2", "myObj", myObject);
		assertEquals(nbGlobalVariables+1, XPathHelper.getGlobalVariables().size());
		assertEquals(myObject, XPathHelper.getGlobalVariables().get(new QName("someUri2", "myObj")));

		final int nbGlobalNamespaces = XPathHelper.getGlobalNamespaces().size(); // the WebTest default ones
		XPathHelper.registerGlobalNamespace("my", "http://my.namespace.uri");
		assertEquals(nbGlobalNamespaces + 1, XPathHelper.getGlobalNamespaces().size());
		assertEquals("http://my.namespace.uri", XPathHelper.getGlobalNamespaces().get("my"));
		
		// Test that objects are transmitted to an XPathHelper instance
		final XPathHelper helper = new XPathHelper();
		final Function function = helper.getFunctionContext().resolveFunction(new QName("someUri", "myFunc"), 1);
		assertEquals(function.getClass().getName(), MyFunction.class, function.getClass());
		assertEquals(myObject, helper.getVariableContext().resolveVariable(new QName("someUri2", "myObj")));
		assertEquals("http://my.namespace.uri", helper.getNamespaceContext().getNamespaceURI("my"));
	}
	
	@Test
	public void testQuote()
	{
		assertEquals("''", XPathHelper.quote(""));
		assertEquals("'foo'", XPathHelper.quote("foo"));
		assertEquals("\"'\"", XPathHelper.quote("'"));
		assertEquals("'\"'", XPathHelper.quote("\""));
		assertEquals("\"I'm here\"", XPathHelper.quote("I'm here"));
		assertEquals("concat('I', \"'\", 'm \"here\"')", XPathHelper.quote("I'm \"here\""));
	}

	/**
	 * HtmlUnit-2.4 introduced a "problem" in {@link DomNamespaceNode#getLocalName()} which cause it
	 * to return the tag name with the original case UNLESS HtmlUnit is doing some XPath processing.
	 * This makes evaluation of XPath without using HtmlUnit XPath functions failing as in the test below
	 * when no hack is used to create the same situation as when HtmlUnit processes an XPath by itself. 
	 * @throws Exception
	 */
	@Test
	public void upperCaseHtmlTags() throws Exception
	{
        final WebClient webClient = new WebClient();
        final MockWebConnection mockConnection = new MockWebConnection();
        webClient.setWebConnection(mockConnection);
        
        mockConnection.setDefaultResponse("<html><body><SPAN class='foo'>bla</SPAN></body></html>");
        final HtmlPage page = webClient.getPage(new URL("http://webtest.canoo.com"));
		
		XPathHelper xh = new XPathHelper();
		
		// XPathHelper.stringValue
		assertEquals("bla", ((HtmlElement)page.getFirstByXPath("//span[@class = 'foo']")).asText());
		assertEquals("bla", xh.stringValueOf(page, "//span[@class = 'foo']"));
		
		final HtmlElement elt = page.getFirstByXPath("//span");
		assertNotNull(elt);

		// XPathHelper.getByXPath
		assertSame(elt, xh.getByXPath(page, "//span", true));

		// XPathHelper.selectFirst
		assertSame(elt, xh.selectFirst(page, "//span"));

		// XPathHelper.selectNodes
		final List nodes = xh.selectNodes(page, "//span");
		assertEquals(1, nodes.size());
		assertSame(elt, nodes.get(0));
	}
}
