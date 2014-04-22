package com.canoo.webtest.steps.request;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests for {@link FollowFrame}.
 * @author <a href="torben@tretau.net">Torben Tretau</a>
 * @author Marc Guillemot
 */
public class FollowFrameTest extends BaseStepTestCase
{

    protected Step createStep() {
        return new FollowFrame();
    }

    public void testParameters() {
        final FollowFrame step = (FollowFrame) getStep();
        
        final String msg = ThrowAssert.assertThrows(StepExecutionException.class, new TestBlock()
            {
                public void call() throws Exception {
                    step.verifyParameters();
                }
            });

        assertEquals("\"name\" or \"htmlId\" must be set!", msg);
    }

    public void testWithNoWebConversation() throws Exception {
        final FollowFrame step = (FollowFrame) getStep();
        step.setName("name");
        assertStepRejectsNullResponse(step);
    }

    public void testGetFrameByName() throws Exception {
        final String htmlContent =
                "<html><body><frameset rows='*'><frame name='dummy' src='about:blank'></frameset></body></html>";
        final HtmlPage page = getDummyPage(htmlContent);
        assertEquals("about:blank",
                FollowFrame.getFrame(page, "dummy", null).getEnclosedPage().getUrl().toExternalForm());
    }

    public void testGetFrameById() throws Exception {
        final String htmlContent =
                "<html><body><frameset rows='*'><frame id='dummy' src='about:blank'></frameset></body></html>";
        final HtmlPage page = getDummyPage(htmlContent);
        assertEquals("about:blank",
                FollowFrame.getFrame(page, null, "dummy").getEnclosedPage().getUrl().toExternalForm());
    }

    public void testNestedText() throws Exception {
    	testNestedTextEquivalent(getStep(), "name");
    }
}
