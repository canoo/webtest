// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.steps.request;

import java.net.URL;
import java.util.Iterator;

import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.BaseStepTestCase;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * Test class for {@link SelectWindow}.<p>
 *
 * @author Paul King
 * @author Marc Guillemot
 */
public class SelectWindowTest extends BaseStepTestCase
{
	protected Step createStep() {
        return new SelectWindow();
    }

    public void testVerifyIndexParameterInvalid() {
		SelectWindow step = new SelectWindow();

		step.setIndex("x");
		assertVerificationFails("1) index must be integer", step);
		
		step.setIndex("-1");
		assertVerificationFails("2) index must be positive", step);
   }
    
    public void testVerifyIndexParameterValid() {
		SelectWindow step = new SelectWindow();

		step.setIndex("0");
		assertVerificationOk("1) index is 0", step);
		
		step.setIndex("1");
		assertVerificationOk("2) index is positive integer", step);	
   }
    
    public void testVerifyNameParameterValid() {
		SelectWindow step = new SelectWindow();

		step.setName("");
		assertVerificationOk("1) name is empty", step);
		
		step.setName("a");
		assertVerificationOk("2) name is set", step);					
    	
    }
    
    public void testParameterCombination()  {
		SelectWindow step = new SelectWindow();
		
		step.setIndex("");
		step.setName("a");
		assertVerificationOk("1) index is empty and name is set", step);
		
		step = new SelectWindow();
		step.setName("1");
		step.setIndex("1");
		assertVerificationOk("2) name is empty and index is set", step);
 
		step = new SelectWindow();
		step.setName("");
		step.setIndex("");
		assertVerificationOk("3) both name and index are empty", step);
		
		step = new SelectWindow();
		step.setName(null);
		step.setIndex("");
		assertVerificationFails("4) name is null and index is empty", step);

		step = new SelectWindow();
		step.setName("");
		step.setIndex(null);
		assertVerificationOk("5) name is empty and index is null", step);
		
		step = new SelectWindow();
		step.setName(null);
		step.setIndex(null);
		assertVerificationFails("6) both name and index are null", step);
    }
    
	private void assertVerificationFails(final String message, final SelectWindow step) {
		ThrowAssert.assertThrows(message, StepExecutionException.class, new TestBlock() {
			public void call() throws Exception {
                step.verifyParameters();
			}
		});
	}

	private void assertVerificationOk(final String message, final SelectWindow step) {
        configureStep(step);
		ThrowAssert.assertPasses(message, new TestBlock() {
			public void call() throws Exception {
                step.verifyParameters();
			}
		});
    }

	public void testWindowNotFoundMessage() throws Exception {
		final WebClient webClient = new WebClient();
		final MockWebConnection connection = new MockWebConnection();
		webClient.setWebConnection(connection);

		final String contentMain = "<html><head><title>Main</title></head>"
			+ "<body>"
			+ "<a href='2.html' target='foo'>2</a>"
			+ "<a href='3.txt' target='_blank'>3</a>"
			+ "<iframe src='about:blank'></iframe>"
			+ "</body></html>";
		
		final URL urlBase = new URL("http://foo/");
		connection.setResponse(urlBase, contentMain);
		connection.setResponse(new URL(urlBase, "2.html"), "<html><head><title>page 2</title></head></html>");
		connection.setResponse(new URL(urlBase, "3.txt"), "bla", "text/plain");
	
		final HtmlPage page = (HtmlPage) webClient.getPage(urlBase);
		for (Iterator iter = page.getAnchors().iterator(); iter.hasNext();)
		{
			final HtmlAnchor link = (HtmlAnchor) iter.next();
			link.click();
		}
		
		final String expectedMsg = "index: 0, name: ><, title: >Main<, url: http://foo/\n"
			+ "index: 1, name: >foo<, title: >page 2<, url: http://foo/2.html\n"
			+ "index: 2, name: ><, text/plain, url: http://foo/3.txt";
		assertEquals(expectedMsg, SelectWindow.getAvailableWindowsMessage(webClient));
	}
}
