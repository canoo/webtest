// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.pdftest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFEncryptionPermission;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;

/**
 * @author Paul King
 * @author Marc Guillemot
 * @webtest.step
 *   category="PDF"
 *   name="pdfVerifyEncryptionPermissions"
 *   alias="verifyPdfEncryptionPermissions"
 *   description="This step verifies encryption permissions of an encrypted <key>pdf</key> document."
 */
public class PdfVerifyEncryptionPermissionsStep extends AbstractVerifyPdfStep {
    private String fAllow;
    private String fDeny;
    private final List fAllowPermissions = new ArrayList();
    private final List fDenyPermissions = new ArrayList();

    /**
     * @param allow
     * @webtest.parameter required="yes/no"
     * description="The comma-separated list of permissions which must be allowed (from
     * 'printing', 'degradedPrinting', 'modifyContents', 'copy', 'modifyAnnotations', 'fillIn', 
     * 'screenReaders', 'assembly').
     * Must be set if <em>deny</em> is not set."
     */
    public void setAllow(final String allow) {
        fAllow = allow;
    }

    public String getAllow() {
        return fAllow;
    }

    /**
     * @param deny
     * @webtest.parameter required="yes/no"
     * description="The comma-separated list of permissions which must be disallowed
     * (from 'printing', 'degradedPrinting', 'modifyContents', 'copy', 'modifyAnnotations', 'fillIn', 
     * 'screenReaders', 'assembly'). 
     * Must be set if <em>allow</em> is not set."
     */
    public void setDeny(final String deny) {
        fDeny = deny;
    }

    public String getDeny() {
        return fDeny;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        paramCheck(getAllow() == null && getDeny() == null, "One of 'allow' or 'deny' must be set.");
        
        parsePermissions(fAllowPermissions, getAllow());
        parsePermissions(fDenyPermissions, getDeny());
    }

    void parsePermissions(final List fillIn, final String permissions) {
        if (StringUtils.isEmpty(permissions)) {
            return;
        }
        final String[] tabPermissions = permissions.split("\\W+");
        for (int i = 0; i < tabPermissions.length; i++) {
			final String name = tabPermissions[i];
			final PDFEncryptionPermission pdfPermission = PDFEncryptionPermission.get(name);
			if (pdfPermission != null)
				fillIn.add(pdfPermission);
			else
				throw new StepExecutionException("Unknown PDF permission: \"" + name + "\"", this);
		}
	}

	protected void verifyPdf(final PDFPage pdfPage) {
        checkPermissions(pdfPage, fAllowPermissions, true);
        checkPermissions(pdfPage, fDenyPermissions, false);
    }

    private void checkPermissions(final PDFPage pdfPage, final List permissions, final boolean expectedValue) {
    	for (final Iterator iter = permissions.iterator(); iter.hasNext();) {
			final PDFEncryptionPermission permission = (PDFEncryptionPermission) iter.next();
			final boolean actualVallue = pdfPage.hasPermission(permission);
			
			if (actualVallue != expectedValue)
			{
				final StringBuffer sb = new StringBuffer("Incorrect encryption permission found: ");
				sb.append(permission.getName());
				sb.append(" is ");
				if (expectedValue)
					sb.append("not ");
				sb.append("set");
				throw new StepFailedException(sb.toString(), this);
			}
		}
    }
}
