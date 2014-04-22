// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import com.canoo.webtest.interfaces.ITableLocator;
import com.canoo.webtest.self.AntTest;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;

/**
 * Tests for {@link VerifyText}.
 * @author Dierk Koenig, Carsten Seibert
 * @author Denis N. Antonioli
 * @author Marc Guillemot
 * @author Paul King
 */

public class VerifyTextTest extends BaseStepTestCase {
    private VerifyText fStep;

    protected void setUp() throws Exception {
        super.setUp();
        fStep = (VerifyText) getStep();
    }

    protected Step createStep() {
        return new VerifyText();
    }


    public void testTableNesting() {
        AntTest.nested(VerifyText.class, "Table");
    }

    public void testLocatorUsage() throws Exception {
        final ITableLocator tableLocator = (ITableLocator) mock(ITableLocator.class, "tableLocator");

        tableLocator.locateText(getContext(), fStep);
        modify().returnValue("");
        startVerification();

        fStep.addTableInternal(tableLocator);
        fStep.setText("");
        executeStep(fStep);
    }

    public void testLocatePlainText() throws Exception {
        final String htmlContent
                = "<html><body>"
                + "<h1>header</h1>"
                + "</body></html>";
        getContext().saveResponseAsCurrent(getDummyPage(htmlContent));

        fStep.setText("header");
        executeStep(fStep);
    }

    public void testLocateI18nText() throws Exception {
        final String htmlContent
                = "<html><head>"
                + "</head><body>"
                + "<h1>&#x4f60;</h1>"
                + "</body></html>";
        getContext().saveResponseAsCurrent(getDummyPage(htmlContent));

        fStep.setText("\u4f60");
        assertFailOnExecute(fStep, "Numeric entity not found", "");
        fStep.setText("&#x4f60;");
        executeStep(fStep);
    }

    public void testLocateI18nNamedEntitiesText() throws Exception {
        final String htmlContent
                = "<html><head>"
                + "</head><body>"
                + "<h1>&uuml;</h1>"
                + "</body></html>";
        getContext().saveResponseAsCurrent(getDummyPage(htmlContent));

        fStep.setText("\u00fc");
        assertFailOnExecute(fStep, "Named entity not found", "");
        fStep.setText("&uuml;");
        executeStep(fStep);
    }

    public void testLocateTextAndElement() throws Exception {
        final String htmlContent
                = "<html><body>"
                + "<h1>header</h1>"
                + "</body></html>";
        getContext().saveResponseAsCurrent(getDummyPage(htmlContent));

        fStep.setText("<h1>header</h1>");
        executeStep(fStep);
    }
    public void testNestedText() throws Exception {
    	testNestedTextEquivalent(fStep, "text");
    }
}
