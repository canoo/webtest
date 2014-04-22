/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import org.apache.oro.text.perl.Perl5Util;

/**
 * @author Denis N. Antonioli
 */
public class PatternVisitorStrategy implements IVisitorStrategy {
	private static final Perl5Util PERL;

	private final String fPattern;

	static {
		PERL = new Perl5Util();
	}

	public PatternVisitorStrategy(String pattern) {
		fPattern = pattern;
	}

	public boolean accept(HtmlAnchor link) {
		return PERL.match(fPattern, link.getHrefAttribute());
	}
}
