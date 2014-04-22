// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.ant;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

/**
 * Unit tests for {@link CountWebtestResults}.
 * @author Marc Guillemot
 */
public class CountWebtestResultsTest extends TestCase 
{
	public void testParseResultFile()
	{
		final URL url = getClass().getResource("CountWebtestResults_results.xml");
		assertNotNull(url);
		final File file = FileUtils.toFile(url);
		
		final CountWebtestResults task = new CountWebtestResults();
		task.setResultFile(file);
		task.readResults();
		assertEquals(1, task.getNbFailed());
		assertEquals(2, task.getNbSuccessful());
	}
}