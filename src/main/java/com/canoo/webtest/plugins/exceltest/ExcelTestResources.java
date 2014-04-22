// Copyright © 2006-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.plugins.exceltest;

import com.canoo.webtest.boundary.FileBoundary;

import java.io.File;

/**
 * @author Rob Nielsen
 */
public abstract class ExcelTestResources
{
    public static final File DEFAULT_FILE = FileBoundary.getFile("/testExcel.xls", ExcelTestResources.class);
    public static final File MINIMAL_FILE = FileBoundary.getFile("/minimal.xls", ExcelTestResources.class);
}
