/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import com.canoo.webtest.steps.Step;
import org.apache.oro.text.perl.Perl5Util;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * @author Denis N. Antonioli
 * @webtest.step
 *   category="Extension"
 *   name="reportLinks"
 *   description="Spider-like visit of all the pages."
 */
public class ReportLinksStep extends Step {
	public static final String[] HEADERS = new String[]{
		SimpleLinksValidator.KEY_DEPTH,
		SimpleLinksValidator.KEY_LABEL,
		SimpleLinksValidator.KEY_CLASS,
		SimpleLinksValidator.KEY_TITLE,
		SimpleLinksValidator.KEY_HREF
	};

	static final Perl5Util PERL = new Perl5Util();
	public static final String PATTERN_MENU_ID = "m/.*/";

	private String fFileName;
	private int fDepth;
	private String fVisitPattern = PATTERN_MENU_ID;


	public void setVisitPattern(String visitPattern) {
		fVisitPattern = visitPattern;
	}

	public void setFile(final String filename) {
		fFileName = filename;
	}

	public void setDepth(final int depth) {
		fDepth = depth;
	}

	public void doExecute() throws SAXException, IOException {
		Spider spider = new Spider();
		spider.setDepth(fDepth);
		spider.setFileName(fFileName);
		spider.setReporter(new SeparatedValueReporter(HEADERS));
		spider.setVisitorStrategy(new PatternVisitorStrategy(fVisitPattern));
		spider.setValidator(new SimpleLinksValidator());
		spider.execute(getContext());
	}
}
