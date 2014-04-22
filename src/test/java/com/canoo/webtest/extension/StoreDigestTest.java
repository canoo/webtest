// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.steps.control.BaseWrappedStepTestCase;
import com.canoo.webtest.steps.locator.TableLocator;
import org.xml.sax.SAXException;

/**
 * Tests for {@link StoreDigest}.<p>
 *
 * @author <a href="mailto:paulk at asert dot com dot au">Paul King</a>
 */
public class StoreDigestTest extends BaseWrappedStepTestCase
{
    private StoreDigest fStep;

    protected Step createStep() {
        return new StoreDigest();
    }

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (StoreDigest) getStep();
    }

    public void testVerifyParameterUsage() {
        assertStepRejectsNullParam("property", new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
    }

    public void testRejectsBadAlgorithm() {
        fStep.setProperty("someProp");
        fStep.setType("unknownAlgorithmType");
        ThrowAssert.assertThrows(StepFailedException.class, new TestBlock()
        {
            public void call() throws Exception {
                executeStep(fStep);
            }
        });
    }

    public void testVerifyParametersWithoutPreviousResponse() {
        fStep.setProperty("someProp");
        assertStepRejectsNullResponse(fStep);
    }

    public void testExecuteSurvivesSaxException() throws Exception {
        fStep.setProperty("someProp");
        final TableLocator locator = new TableLocator()
        {
            public String locateText(final Context context, final Step step) throws IndexOutOfBoundsException, SAXException {
                throw new SAXException("Can't parse table:");
            }
        };
        fStep.addTable(locator);
        assertFailOnExecute(fStep, "Should catch parsing exception", "Can't parse table:");
    }
}
