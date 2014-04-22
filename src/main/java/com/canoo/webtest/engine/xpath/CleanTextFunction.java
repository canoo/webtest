// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine.xpath;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.FunctionDef1Arg;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;


/**
 * Utility function performing following operations on the text:
 * <ul>
 * <li>replace all kind of spaces (space, non breaking space, new line, line feed, tab) with a normal space</li>
 * <li>replace multiple instances of space with a single one</li>
 * <li>trims the resulting string</li>
 * </ul>
 * This function is available under the name "wt:cleanText".
 *
 * @author Marc Guillemot
 */
public class CleanTextFunction extends FunctionDef1Arg {
	private static final Logger LOG = Logger.getLogger(CleanTextFunction.class);

	public XObject execute(final XPathContext xctxt) throws TransformerException
	{
		final String string = getArg0AsString(xctxt).toString();
		return new XString(doExecute(string));
	}
	
	String doExecute(final String string)
	{
		LOG.debug("String to clean: " + string);

		return string.replaceAll("[\\s\u00A0]+", " ").trim();
	}
}