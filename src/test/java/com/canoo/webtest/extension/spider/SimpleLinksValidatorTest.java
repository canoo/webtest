// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension.spider;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import junit.framework.TestCase;

import java.util.Properties;

/**
 * @author Denis N. Antonioli
 */
public class SimpleLinksValidatorTest extends TestCase {
	private SimpleLinksValidator fSimpleLinksValidator;

	protected void setUp() throws Exception {
		super.setUp();
		fSimpleLinksValidator = new SimpleLinksValidator();
	}

	public void testValidateWithoutClass() {
		final Properties linkInfo = fSimpleLinksValidator.validate(2, null, SpiderTest.newLink("/foo/bar?bla"));
		assertEquals("2", linkInfo.getProperty(SimpleLinksValidator.KEY_DEPTH));
		assertEquals("/foo/bar?bla", linkInfo.getProperty(SimpleLinksValidator.KEY_HREF));
		// empty string is undefined, see com.gargoylesoftware.htmlunit.html.StyledElement#getClassAttribute
		assertEquals("", linkInfo.getProperty(SimpleLinksValidator.KEY_CLASS));
	}

	public void testValidateWithClass() {
		final HtmlAnchor link = SpiderTest.newLink("/foo/bar?bla");
		link.setAttribute("class", "blue");
		final Properties linkInfo = fSimpleLinksValidator.validate(2, null, link);

		assertEquals("2", linkInfo.getProperty(SimpleLinksValidator.KEY_DEPTH));
		assertEquals("/foo/bar?bla", linkInfo.getProperty(SimpleLinksValidator.KEY_HREF));
		assertEquals("blue", linkInfo.getProperty(SimpleLinksValidator.KEY_CLASS));
	}
}
