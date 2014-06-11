// Copyright � 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.self.LogCatchingTestCase;
import org.apache.tools.ant.Project;

import java.io.File;

/**
 * Tests for {@link com.canoo.webtest.engine.ContextHelper}.
 * @author Marc Guillemot
 * @author Paul King
 */
public class ContextHelperTest extends LogCatchingTestCase
{
    /**
     * Tests that no exception is thrown when an IOException occurs but that it gets logged.
     */
    public void testWriteResponseFileIOExceptionHandling() {
        ContextHelper.writeResponseFile(null, new File(""));
        assertTrue(getSpoofAppender().allMessagesToString().indexOf("Failed writing current response to") > -1);
    }

    public void testDefineAsCurrentResponseWhenNoPreviousResponsePresent() {
        // Setup Context
        Project project = new Project();
        final WebtestTask webTest = new WebtestTask();
        webTest.setName("TestWebTest");
        webTest.setProject(project);
        Context context = new Context(webTest);
        Configuration config = new Configuration(webTest);
        config.setContext(context);
        config.setProject(project);
        config.execute();

        String expectedUrl = "http://foo.bar/";
        String expectedContent = "foo";
        String expectedContentType = "text/plain";

        ContextHelper.defineAsCurrentResponse(context, expectedContent, expectedContentType, expectedUrl);

        assertEquals(expectedUrl, context.getCurrentResponse().getUrl().toString());
        assertEquals(expectedContent, context.getCurrentResponse().getWebResponse().getContentAsString());
        assertEquals(expectedContentType, context.getCurrentResponse().getWebResponse().getContentType());
    }

    protected void setUp() throws Exception {
        super.setUp();
        setUpCatchLoggerMessages();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        tearDownCatchLoggerMessages();
    }
}
