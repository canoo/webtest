// Copyright © 2004-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.plugins.pdftest;

import java.util.HashMap;
import java.util.Map;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.plugins.pdftest.htmlunit.PDFPage;
import com.canoo.webtest.self.ContextStub;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.MapUtil;

/**
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public class AbstractVerifyPdfStepTest extends BaseStepTestCase
{
    private static final AbstractVerifyPdfStep EMPTY_STEP = new EmptyStep();

    protected Step createStep()
    {
    	return new EmptyStep();
    }

    public void testToString()
    {
    	// nothing as tested class as is abstract
    }
    
    public void testCurrentResponseNotAvailable() throws Exception {
    	setFakedContext(new ContextStub());
        ThrowAssert.assertThrows(StepExecutionException.class, getExecuteStepTestBlock(EMPTY_STEP));
    }

    public void testCurrentResponseNotAPdfDocument() throws Exception {
        setFakedContext(new ContextStub("bla", "text/plain"));
        ThrowAssert.assertThrows(StepExecutionException.class, getExecuteStepTestBlock(EMPTY_STEP));
    }

    public void testAddOnlyNonNullValuesToParameterDictionary() {
        Map map = new HashMap();
        MapUtil.putIfNotNull(map, "key", null);
        assertEquals(0, map.size());
        MapUtil.putIfNotNull(map, "key", "value");
        assertEquals(1, map.size());
        assertEquals("value", map.get("key"));
    }

    private static class EmptyStep extends AbstractVerifyPdfStep
    {
        protected void verifyParameters() {
        }
        protected void verifyPdf(final PDFPage pdfPage) {
        }
    }
}
