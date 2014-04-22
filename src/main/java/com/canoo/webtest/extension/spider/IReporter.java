/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

/**
 * Called by the spider to report each page.
 *
 * @author Denis N. Antonioli
 */
public interface IReporter {
	/**
	 * Called before the first page has been visited.
	 *
	 * @throws IOException
	 */
	void writeHeader() throws IOException;

	/**
	 * Called for each page.
	 *
	 * @param linkInfo
	 * @throws IOException
	 */
	void write(Properties linkInfo) throws IOException;

	/**
	 * Must be called first to set the output chanel.
	 *
	 * @param writer
	 */
	void setWriter(Writer writer);

	/**
	 * Called after the last page has been visited.
	 *
	 * @throws IOException
	 */
	void writeFooter() throws IOException;
}
