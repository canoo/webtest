/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;

/**
 * Called by the spider to decide if the link is to be visited.
 *
 * @author Denis N. Antonioli
 */
public interface IVisitorStrategy {
	boolean accept(HtmlAnchor link);
}
