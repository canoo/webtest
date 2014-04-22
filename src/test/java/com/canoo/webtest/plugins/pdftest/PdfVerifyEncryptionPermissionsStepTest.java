// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.canoo.webtest.boundary.FileBoundary;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFEncryptionPermission;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.steps.Step;

/**
 * Unit tests for {@link PdfVerifyEncryptionPermissionsStep}.
 * @author Paul King
 * @author Marc Guillemot
 */
public class PdfVerifyEncryptionPermissionsStepTest extends AbstractBaseVerifyPdfTestCase
{

	protected Step createStep() {
        return new PdfVerifyEncryptionPermissionsStep();
    }

    public void testNoPrint() throws IOException {
        final PdfVerifyEncryptionPermissionsStep step = new PdfVerifyEncryptionPermissionsStep();
        setFakedContext(new PdfContextStub(FileBoundary.getFile("/testDocPermissionsNoPrint.pdf", PdfTestResources.class)));
        step.setDeny("printing,degradedPrinting");
        step.execute();
    }
    
	protected Step getMinimallyConfiguredStep() {
        final PdfVerifyEncryptionPermissionsStep step = (PdfVerifyEncryptionPermissionsStep) getStep();
        step.setAllow("copy");
        return step;
	}
	
    public void testAttributes() throws IOException {
        assertErrorOnExecute(getStep(), "One of 'allow' or 'deny' must be set.", "");
    }

    public void testAnalyzerError() throws Exception {
        final PdfVerifyEncryptionPermissionsStep step = new PdfVerifyEncryptionPermissionsStep()
        {
            protected PDFPage getPdfPage() throws Exception {
                return getPDFPageStub();
            }
        };
        setFakedContext(new PdfContextStub(PdfTestResources.ENCRYPTED_FILE));
        step.setAllow("dummy");
        assertErrorOnExecute(step);
    }

    public void testParsePermissions()
    {
        final PdfVerifyEncryptionPermissionsStep step = (PdfVerifyEncryptionPermissionsStep) getStep();
        final List fillIn = new ArrayList();
        step.parsePermissions(fillIn, "printing, degradedPrinting, modifyContents");
        final PDFEncryptionPermission[] perms = {PDFEncryptionPermission.PRINTING, 
        		PDFEncryptionPermission.DEGRADED_PRINTING,
        		PDFEncryptionPermission.MODIFY_CONTENTS};
        assertEquals(Arrays.asList(perms), fillIn);
    	
    }
}
