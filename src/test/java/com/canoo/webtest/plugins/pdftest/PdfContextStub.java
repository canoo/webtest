// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import com.canoo.webtest.self.ContextStub;

import java.io.File;

/**
 * @author Etienne Studer
 */
public class PdfContextStub extends ContextStub
{
     public PdfContextStub(final File file) {
    	super(file, "application/pdf");
    }
}
