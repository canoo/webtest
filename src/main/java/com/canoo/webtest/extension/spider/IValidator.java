/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.Properties;

/**
 * Called by the spider to validate a page.
 *
 * @author Denis N. Antonioli
 */
public interface IValidator {
	/**
	 * @param depth    The depth of the page, counted from the root.
	 * @param htmlPage The page to validate.
	 * @param link     The link used to call the page.
	 * @return Store for any information to be reported.
	 */
	Properties validate(int depth, HtmlPage htmlPage, HtmlAnchor link);
}
