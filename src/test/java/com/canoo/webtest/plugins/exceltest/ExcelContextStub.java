// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import java.io.File;

import com.canoo.webtest.self.ContextStub;

/**
 * Test stub for testing Excel files.
 * @author Rob Nielsen
 */
public class ExcelContextStub extends ContextStub
{
    public ExcelContextStub(final String mimeType) {
        super("", mimeType);
    }

    public ExcelContextStub(final File file) {
        super(file, "application/vnd.ms-excel");
    }
}
