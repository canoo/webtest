/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.Properties;

/**
 * @author Denis N. Antonioli
 */
public class SimpleLinksValidator implements IValidator {
	public static final String KEY_DEPTH = "Depth";
	public static final String KEY_LABEL = "Label";
	public static final String KEY_HREF = "Href";
	public static final String KEY_TITLE = "Title";
	public static final String KEY_CLASS = "Class";

	public Properties validate(final int depth, final HtmlPage webResponse, final HtmlAnchor link) {
		final Properties linkInfo = new Properties();
		linkInfo.put(KEY_DEPTH, Integer.toString(depth));
		linkInfo.put(KEY_LABEL, link.asText());
		linkInfo.put(KEY_HREF, link.getHrefAttribute());
		linkInfo.put(KEY_TITLE, link.getAttribute("title"));
		linkInfo.put(KEY_CLASS, link.getAttribute("class"));
		return linkInfo;
	}
}
