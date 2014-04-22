package com.canoo.webtest.extension;

import java.io.IOException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import com.canoo.webtest.engine.StepExecutionException;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Performs a binary comparison of the content. 
 * @author Marc Guillemot
 */
public class VerifyContentBinDiff implements VerifyContentDiff
{
	/**
	 * {@inheritDoc}
	 */
	public String compare(final WebResponse reference, final WebResponse actual, 
			final String referenceLabel, final String actualLabel) {

        if (sameContent(reference, actual)) 
        {
        	return null;
        }
        else
        {
            final byte[] actualBytes, refBytes;
			try {
				actualBytes = IOUtils.toByteArray(actual.getContentAsStream());
	            refBytes = IOUtils.toByteArray(reference.getContentAsStream());
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
        	return produceBinDiffMessage(actualBytes, refBytes);
        }
	}

	private boolean sameContent(final WebResponse reference, final WebResponse actual) {
		try {
			return IOUtils.contentEquals(reference.getContentAsStream(), actual.getContentAsStream());
		}
		catch (final IOException e) {
			throw new StepExecutionException("Error reading content", e);
		}
	}

	protected String produceBinDiffMessage(final byte[] actualBytes, final byte[] referenceBytes) 
	{
		final int extractLength = 10;
		final int minLength = Math.min(actualBytes.length, referenceBytes.length);
		for (int i = 0; i < minLength; i++) 
		{
			if (actualBytes[i] != referenceBytes[i])
			{
				// extract 10 bytes (if available) to show difference in context
				final byte[] extract1 = ArrayUtils.subarray(actualBytes, i, i+extractLength);
				final byte[] extract2 = ArrayUtils.subarray(referenceBytes, i, i+extractLength);
				return "First difference at position " + (i+1) 
					+ ": " + String.valueOf(Hex.encodeHex(extract1)) + " <> " + String.valueOf(Hex.encodeHex(extract2));
			}
		}

		// one file is contained in the other
		final byte[] longerArray;
		final String msg;
		if (actualBytes.length < referenceBytes.length)
		{
			longerArray = referenceBytes;
			msg = "Reference binary content starts with actual binary content";
		}
		else
		{
			longerArray = actualBytes;
			msg = "Actual binary content starts with reference binary content";
		}
		final byte[] startOfLonger = ArrayUtils.subarray(longerArray, minLength, minLength+extractLength);
		return msg + ". Longer content continues with: " + String.valueOf(Hex.encodeHex(startOfLonger));
	}
}
