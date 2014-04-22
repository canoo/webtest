// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine.xpath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.Function3Args;
import org.apache.xpath.functions.WrongNumberArgsException;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;


/**
 * This function should work as the matches function defined by
 * <a href="http://www.w3.org/TR/xquery-operators/#func-matches">XPath 2</a>.
 * This implementation is provided as long as this is not natively available in Xalan.<br/>
 * This function is available under the name "wt:matches".
 *
 * @author Marc Guillemot
 */
public class MatchesFunction extends Function3Args {
	private static final Logger LOG = Logger.getLogger(MatchesFunction.class);

	
	public XObject execute(final XPathContext xctxt) throws TransformerException
	{
		final String input = getArg0().execute(xctxt).xstr().toString();
		final String regex = getArg1().execute(xctxt).xstr().toString();
		LOG.debug("input: " + input);

		final String flags;
		if (getArg2() != null)
		{
			flags = getArg2().execute(xctxt).xstr().toString();
		}
		else
			flags = null;
		
		
		return new XBoolean(doExecute(input, regex, flags));
	}
	
	boolean doExecute(final String input, final String regex, final String flagsString) throws TransformerException
	{
		int flags = (flagsString == null) ? 0 : computeFlags(flagsString);
		final Pattern pattern = Pattern.compile(regex, flags);
		final Matcher matcher = pattern.matcher(input);
		return matcher.find();
	}

	static int computeFlags(final String flags) throws TransformerException {
		int flag = 0;
		for (int i = 0; i < flags.length(); i++) {
			final char c = flags.charAt(i);
			if (flags.indexOf(c) != i) {
				break; // repeated flag, ignore 
			}
			switch (c) {
				case's':
					flag += Pattern.DOTALL;
					break;
				case'm':
					flag += Pattern.MULTILINE;
					break;
				case'i':
					flag += Pattern.CASE_INSENSITIVE;
					break;
				case'x':
					flag += Pattern.COMMENTS;
					break;
				default:
					throw new TransformerException("Illegal flag used for call to matches: " + c);
			}
		}

		return flag;
	}
	
	@Override
	public void checkNumberArgs(int _argNum) throws WrongNumberArgsException
	{
		if (_argNum < 2 || _argNum > 3)
			throw new WrongNumberArgsException("function matches accept 2 or 3 arguments, not " + _argNum);
	}
}