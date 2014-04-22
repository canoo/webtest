/*
 * Copyright (c) 2005 Canoo Engineering. All Rights Reserved.
 */
package com.canoo.webtest.extension.spider;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

/**
 * @author Denis N. Antonioli
 */
public class SeparatedValueReporter implements IReporter {
	public static final String VALUE_SEPARATOR = "\t";
	public static final String LINE_SEPARATOR;

	static {
		LINE_SEPARATOR = System.getProperty("line.separator");
	}

	private final String[] fHeaders;
	private Writer fWriter;

	public SeparatedValueReporter(String[] headers) {
		if (headers == null) {
			throw new IllegalArgumentException("Headers must be defined");
		}
		fHeaders = headers;
	}

	public void setWriter(Writer writer) {
		if (writer == null) {
			throw new IllegalArgumentException("Writer must be defined");
		}
		fWriter = writer;
	}


	public void writeHeader() throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fHeaders.length; i++) {
			sb.append(fHeaders[i]).append(VALUE_SEPARATOR);
		}
		fWriter.write(finishLine(sb));
	}

	public void write(Properties linkInfo) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fHeaders.length; i++) {
			sb.append(linkInfo.getProperty(fHeaders[i], "")).append(VALUE_SEPARATOR);
		}
		fWriter.write(finishLine(sb));
	}

	public void writeFooter() {
	}

	static String finishLine(StringBuffer sb) {
		if (sb.length() > 0) {
			sb.setLength(sb.length() - VALUE_SEPARATOR.length());
		}
		sb.append(LINE_SEPARATOR);
		return sb.toString();
	}

}
