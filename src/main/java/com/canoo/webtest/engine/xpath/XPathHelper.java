// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine.xpath;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionException;

import org.apache.log4j.Logger;
import org.apache.xml.dtm.DTM;
import org.apache.xpath.ExtensionsProvider;
import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.functions.Function;
import org.apache.xpath.functions.WrongNumberArgsException;
import org.apache.xpath.jaxp.JAXPVariableStack;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XObjectFactory;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.canoo.webtest.engine.StepFailedException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.xpath.XPathUtils;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Helper class for a central XPath creation allowing to share variable,
 * function and namespace contexts.
 * TODO: cleanup to use plainly javax.xml.xpath.*
 * @author Marc Guillemot
 */
public class XPathHelper
{
	private static final Logger LOG = Logger.getLogger(XPathHelper.class);
	private SimpleXPathVariableResolver fVariableContext = new SimpleXPathVariableResolver();
	private SimpleXPathFunctionResolver fFunctionContext = new SimpleXPathFunctionResolver();
	private SimpleNamespaceContext fNamespaceContext = new SimpleNamespaceContext();

	// hack for a problem in HtmlUnit-2.4 (see XPathHelperTest#upperCaseHtmlTags for details)
	final ThreadLocal<Boolean> htmlUnitXPathUtil_XPathProcessingFlag_;

	private static Map<QName, Object> sGlobalVariables = Collections
			.synchronizedMap(new HashMap<QName, Object>());
	private static Map<QName, Class<? extends Function>> sGlobalFunctions = Collections
			.synchronizedMap(new HashMap<QName, Class<? extends Function>>());
	private static Map<String, String> sGlobalNamespaces = Collections
			.synchronizedMap(new HashMap<String, String>());

	static
	{
		registerWebTestGoodies();
	}

	/**
	 * Registers a global variable. Global variables are added to the list of
	 * variable of a webtest at the webtest start.
	 * @param namespaceURI the namespace URI of the function to be registered (<code>null</code>
	 *            if none).
	 * @param localName the non-prefixed local portion of the function name to
	 *            be registered
	 * @param value the variable to be registered
	 */
	public static void registerGlobalVariable(final String namespaceURI,
			final String localName, final Object value)
	{
		sGlobalVariables.put(new QName(namespaceURI, localName), value);
	}

	/**
	 * Registers WebTest XPath extras.
	 */
	private static void registerWebTestGoodies()
	{
		final String namespaceURI = "http://webtest.canoo.com";
		registerGlobalNamespace("wt", namespaceURI);
		registerGlobalFunction(namespaceURI, "matches", MatchesFunction.class);
		registerGlobalFunction(namespaceURI, "cleanText", CleanTextFunction.class);
	}

	/**
	 * Gets the registered global variables.
	 * @return a synchronized map of (MemberKey, variable value)
	 */
	public static Map<QName, Object> getGlobalVariables()
	{
		return sGlobalVariables;
	}

	/**
	 * Gets the registered global functions.
	 * @return a synchronized map of (MemberKey, function)
	 */
	public static Map<QName, Class<? extends Function>> getGlobalFunctions()
	{
		return sGlobalFunctions;
	}

	/**
	 * Gets the registered global namespaces.
	 * @return a synchronized map of (prefix, namespaceURI)
	 */
	public static Map<String, String> getGlobalNamespaces()
	{
		return sGlobalNamespaces;
	}

	/**
	 * Registers a global function. Global functions are added to the list of
	 * functions of a webtest at the webtest start.
	 * @param namespaceURI the namespace URI of the function to be registered (<code>null</code>
	 *            if none).
	 * @param localName the non-prefixed local portion of the function name to
	 *            be registered
	 * @param function the function to be registered
	 */
	public static void registerGlobalFunction(final String namespaceURI,
			final String localName, final Class<? extends Function> function)
	{
		sGlobalFunctions.put(new QName(namespaceURI, localName), function);
	}

	/**
	 * Registers a global namespace. Global namespaces are added to the list of
	 * namespaces of a webtest at the webtest start.
	 * @param prefix the namespace prefix to resolve
	 * @param namespaceURI the namespace URI
	 */
	public static void registerGlobalNamespace(final String prefix,
			final String namespaceURI)
	{
		sGlobalNamespaces.put(prefix, namespaceURI);
	}

	/**
	 * Initializes from the global functions, variables and namespaces.
	 */
	public XPathHelper()
	{
		// copy global namespaces
		for (final Entry<String, String> entry : getGlobalNamespaces().entrySet())
		{
			getNamespaceContext().addNamespace(entry.getKey(), entry.getValue());
		}

		// copy global functions
		for (final Entry<QName, Class<? extends Function>> entry : getGlobalFunctions().entrySet())
		{
			final QName memberKey = entry.getKey();
			getFunctionContext().registerFunction(memberKey, entry.getValue());
		}

		// copy global variables
		for (final Entry<QName, Object> entry : getGlobalVariables().entrySet())
		{
			final QName memberKey = entry.getKey();
			getVariableContext().setVariableValue(memberKey, entry.getValue());
		}

		Field f;
		try {
			f = XPathUtils.class.getDeclaredField("PROCESS_XPATH_");
			f.setAccessible(true);
			htmlUnitXPathUtil_XPathProcessingFlag_ = (ThreadLocal<Boolean>) f.get(XPathUtils.class);
		}
		catch (final Exception e) {
			throw new RuntimeException("Failed to hack HtmlUnit-2.4 XPathUtils.PROCESS_XPATH_", e);
		}
	}

	/**
	 * Gets the function resolver used during XPath evaluation for this webtest.
	 * @return the context
	 */
	public SimpleXPathFunctionResolver getFunctionContext()
	{
		return fFunctionContext;
	}

	/**
	 * Gets the namespace context used for namespace resolution during XPath
	 * evaluation for this webtest.
	 * @return the context
	 */
	public SimpleNamespaceContext getNamespaceContext()
	{
		return fNamespaceContext;
	}

	/**
	 * Gets the variable context used for variable resolution (the $foo in an
	 * xpath expression) during XPath evaluation for this webtest.
	 * @return the context
	 */
	public SimpleXPathVariableResolver getVariableContext()
	{
		return fVariableContext;
	}

	/**
	 * Gets the document object associated to the page that could be provided to
	 * the XPath for computations
	 * @param page the page
	 * @return the "document"
	 */
	protected Object getDocument(final Page page)
	{
		if (page == null)
			return null; // no page, only xpath not refering to the document
							// tree should work
		else if (page instanceof HtmlPage)
			return page;
		else if (page instanceof XmlPage)
		{
			final XmlPage xmlPage = (XmlPage) page;
			if (xmlPage.getXmlDocument() == null) // when content type was xml
													// but document couldn't be
													// parsed
				throw new StepFailedException(
						"The xml document couldn't be parsed as it is not well formed");
			return xmlPage.getXmlDocument();
		}
		else
		{
			throw buildInvalidDocumentException(page);
		}
	}

	/**
	 * Utility to build exception for invalid page
	 * @param page the page
	 * @return the exception to throw
	 */
	StepFailedException buildInvalidDocumentException(final Page page)
	{
		return new StepFailedException(
				"Current response is not an HTML or XML page but of type "
						+ page.getWebResponse().getContentType() + " ("
						+ page.getClass().getName() + ")");
	}

	public String stringValueOf(final Page _page, final String _xpath)
			throws XPathExpressionException
	{
		try
		{
			htmlUnitXPathUtil_XPathProcessingFlag_.set(true);
			final XObject result = eval(_xpath, getDocument(_page));
			return result.str();
		}
		catch (final TransformerException e)
		{
			throw handleException(e);
		}
		finally
		{
			htmlUnitXPathUtil_XPathProcessingFlag_.set(false);
		}
	}

	private XPathExpressionException handleException(TransformerException _e)
	{
		final Throwable nestedException = _e.getException();
		if (nestedException instanceof XPathFunctionException)
		{
			return (XPathFunctionException) nestedException;
		}
		else
		{
//			if (true)
//				throw new RuntimeException(_e.getCause());
			LOG.info("XPath error", _e);
			return new XPathExpressionException(_e.getMessage()); // stupid but XPathExpressionException(e).getMessage() is null! 
		}
	}

	public List<? extends Object> selectNodes(final Page _page, final String _xpath)
			throws XPathExpressionException
	{
		return (List<? extends Object>) getByXPath(_page, _xpath, false);
	}

	public Object selectFirst(final Page _page, final String _xpath)
			throws XPathExpressionException
	{
		return getByXPath(_page, _xpath, true);
	}

	protected Object getByXPath(final Page _currentResp, final String _xpath,
			final boolean _onlyFirstResult) throws XPathExpressionException
	{
		try
		{
			htmlUnitXPathUtil_XPathProcessingFlag_.set(true);
			final XObject result = eval(_xpath, getDocument(_currentResp));
			switch (result.getType())
			{
				case XObject.CLASS_BOOLEAN:
					return result.bool();
				case XObject.CLASS_NUMBER:
					return result.num();
				case XObject.CLASS_STRING:
					return result.str();
				case XObject.CLASS_NODESET:
					if (_onlyFirstResult)
						return result.nodeset().nextNode();
					else
						return toList(result.nodeset());
				default:
					throw new RuntimeException("Unexpected result type for >" + _xpath
							+ "<: " + result.getType());
			}
		}
		catch (final TransformerException e)
		{
			throw handleException(e);
		}
		finally
		{
			htmlUnitXPathUtil_XPathProcessingFlag_.set(false);
		}
	}

	private List<Node> toList(final NodeIterator _nodeset)
	{
		final List<Node> result = new ArrayList<Node>();
		Node node = _nodeset.nextNode();
		while (node != null)
		{
			result.add(node);
			node = _nodeset.nextNode();
		}
		// TODO Auto-generated method stub
		return result;
	}

	private XObject eval(final String expression, final Object contextItem)
			throws javax.xml.transform.TransformerException
	{
		final PrefixResolver prefixResolver = new PrefixResolver(fNamespaceContext, contextItem);
		org.apache.xpath.XPath xpath = new org.apache.xpath.XPath(expression, null,
				prefixResolver, org.apache.xpath.XPath.SELECT, null, fFunctionContext.getFunctionTable());
		// function resolver
		final XPathContext[] contexts = {null};
		final ExtensionsProvider extProvider = new ExtensionsProvider()
		{
			public boolean elementAvailable(String _ns, String _elemName)
					throws TransformerException
			{
				return false;
			}
			public Object extFunction(final FuncExtFunction _extFunction, Vector _argVec)
					throws TransformerException
			{
				final String ns = _extFunction.getNamespace();
				final String name = _extFunction.getFunctionName();
				final Function func = fFunctionContext.resolveFunction(new QName(ns, name), 0);
				if (func == null)
					throw new RuntimeException("Can't find function " + name + " (namespace: " + ns + ")");

				for (int i=0; i<_argVec.size(); ++i)
				{
					try
					{
						func.setArg(XObjectFactory.create(_argVec.get(i)), i);
					}
					catch (final WrongNumberArgsException e)
					{
						throw new RuntimeException(e);
					}
				}
				return func.execute(contexts[0]);
			}
			public Object extFunction(String _ns, String _funcName, Vector _argVec,
					Object _methodKey) throws TransformerException
			{
				return null;
			}
			public boolean functionAvailable(String _ns, String _funcName)
					throws TransformerException
			{
				return fFunctionContext.resolveFunction(new QName(_ns, _funcName), 0) != null;
			}
		};
		XPathContext xpathSupport = new XPathContext(extProvider);
		contexts[0] = xpathSupport;
		xpathSupport.setVarStack(new JAXPVariableStack(fVariableContext));

		// If item is null, then we will create a a Dummy contextNode
		final XObject xobj;
		if (contextItem instanceof Node)
		{
			xobj = xpath.execute(xpathSupport, (Node) contextItem, prefixResolver);
		}
		else
		{
			xobj = xpath.execute(xpathSupport, DTM.NULL, prefixResolver);
		}

		return xobj;
	}
	
	/**
	 * Quotes the provided value, handling quotes and double quotes if needed to
	 * @param value the value to quote
	 * @return the quoted value usable in XPath expression
	 */
	public static String quote(final String value)
	{
		if (!value.contains("'")) {
			return "'" + value + "'";
		}
		else if (!value.contains("\""))	{
			return "\"" + value + "\"";
		}
		else {
			final String[] parts = value.split("'");
			String response = "concat(";
			for (int i=0; i<parts.length-1; ++i)
			{
				response += "'" + parts[i] + "', \"'\", ";
			}
			response += "'" + parts[parts.length-1] + "')";
			return response;
		}
	}
}
