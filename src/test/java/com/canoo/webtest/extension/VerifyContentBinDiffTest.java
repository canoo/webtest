package com.canoo.webtest.extension;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;

import com.canoo.webtest.boundary.FileBoundary;

import junit.framework.TestCase;

/**
 * Test for {@link VerifyContentBinDiff}.
 *
 * @author Marc Guillemot
 */
public class VerifyContentBinDiffTest extends TestCase
{
    public void testMessage() throws Exception 
    {
        final File file1 = FileBoundary.getFile("/testDocForms.pdf", getClass());
        final byte[] bytes1 = FileUtils.readFileToByteArray(file1);
        final File file2 = FileBoundary.getFile("/testDocFormsSecure.pdf", getClass());
        final byte[] bytes2 = FileUtils.readFileToByteArray(file2);

        final VerifyContentBinDiff diff = new VerifyContentBinDiff();
        assertEquals("First difference at position 17: 32312030206f626a0d3c <> 33352030206f626a0d3c", 
        		diff.produceBinDiffMessage(bytes1, bytes2));
    }

    public void testMessageReachTheEnd() throws Exception 
    {
    	final byte[] additional = {35, -103, 94, 87, -22, 1, 12, 127, -65, -74};
        final byte[] bytes1 = new byte[56];
        for (int i = 0; i < bytes1.length; i++) 
        {
        	final double rnd = Math.random();
			bytes1[i] = (byte) (Math.floor(256 * rnd) - 128);
		}
        
        final byte[] bytes2 = ArrayUtils.addAll(bytes1, additional);
    	
        final VerifyContentBinDiff diff = new VerifyContentBinDiff();
        assertEquals("Reference binary content starts with actual binary content. Longer content continues with: 23995e57ea010c7fbfb6", 
        		diff.produceBinDiffMessage(bytes1, bytes2));

        assertEquals("Actual binary content starts with reference binary content. Longer content continues with: 23995e57ea010c7fbfb6", 
        		diff.produceBinDiffMessage(bytes2, bytes1));
    }
}
