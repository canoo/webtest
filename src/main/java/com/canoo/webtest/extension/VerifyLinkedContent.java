package com.canoo.webtest.extension;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Selects attributes with an xpath and downloads them.
 *
 * @author Denis N. Antonioli
 * @webtest.step
 *    category="Extension"
 *    name="verifyLinkedContent"
 *    description="Verify all selected links on a page for existence and, optionally, mime-type."
 */
public class VerifyLinkedContent extends Step {
	private static final Logger LOG = Logger.getLogger(VerifyLinkedContent.class);
	private String fXpath;
	private String fAccept;
	private MimeType fMimeTypes;
	public static final String LINE_SEPARATOR;

	static {
		LINE_SEPARATOR = System.getProperty("line.separator");
	}

	{
		fMimeTypes = MimeType.ALL_MEDIA;
	}

	protected void verifyParameters() {
		nullResponseCheck();
		nullParamCheck(getXpath(), "xpath");
		if (getAccept() != null) {
			fMimeTypes = new MimeType(getAccept());
		}
	}

	/**
	 * Selects the resources to check.
	 * @param xpath The links to check.
	 * @webtest.parameter
	 *    required="yes"
	 *    description="The links to checks"
	 */
	public void setXpath(final String xpath) {
		fXpath = xpath;
	}

	public String getXpath() {
		return fXpath;
	}

	/**
	 * See description of the Accept header in RFC 2616.
	 *
	 * @param accept The accepted mimetype(s).
	 * @webtest.parameter
	 *    required="no"
	 *    default="Accepts anything."
	 *    description="Type and subtype that the answer must have, in the classical mime-type notation."
	 */
	public void setAccept(final String accept) {
		fAccept = accept;
	}

	public String getAccept() {
		return fAccept;
	}

	public void doExecute() throws Exception {
		final Context context = getContext();
		final HtmlPage currentResponse;
		try {
			currentResponse = (HtmlPage) context.getCurrentResponse();
		} catch (ClassCastException cce) {
			throw new StepFailedException("Current response is not an html page but " + context.getCurrentResponse().getClass(), this);
		}

		final StringBuffer sb = verifyLinksOnPage(currentResponse);

		if (sb.length() > 0) {
			sb.insert(0, currentResponse.getUrl().toExternalForm() + ":" + LINE_SEPARATOR);
			throw new StepFailedException(sb.toString(), this);
		}
	}

	WebClient setupWebClient() {
		final WebClient webClient = getContext().getConfig().createWebClient();

		webClient.setThrowExceptionOnFailingStatusCode(true);
		webClient.setWebConnection(getContext().getWebClient().getWebConnection());
		return webClient;
	}

	StringBuffer verifyLinksOnPage(final HtmlPage htmlPage) throws Exception {
		final WebClient webClient = setupWebClient();

		final StringBuffer sb = new StringBuffer();
		for (final Iterator it = iterateAllMatchingElements(htmlPage).iterator(); it.hasNext();) {
			sb.append(verifyOneLink(webClient, htmlPage, (Attr) it.next()));
		}

		return sb;
	}

	String verifyOneLink(final WebClient webClient, final HtmlPage htmlPage, final Attr linkAttribute) throws IOException {
		final String key = (String) linkAttribute.getLocalName();
		final String src = (String) linkAttribute.getValue();

		final URL url = htmlPage.getFullyQualifiedUrl(src);
		LOG.debug(src + " -> " + url.toExternalForm());
		try {
			final Page resp = webClient.getPage(url);
			final String contentType = resp.getWebResponse().getContentType();

			if (!fMimeTypes.match(contentType)) {
				final StringBuffer sb = new StringBuffer();
				sb.append(src).append(" <").append(url.toExternalForm()).append("> ");
				sb.append(contentType).append(" is not expected").append(LINE_SEPARATOR);
				LOG.info("Failed link with " + key + "=\"" + src + "\", mime-type is " + contentType);
				return sb.toString();
			}
		} 
		catch (final FailingHttpStatusCodeException fhsce) {
			StringBuffer sb = new StringBuffer();
			sb.append(src).append(" <").append(url.toExternalForm()).append("> ");
			sb.append(fhsce.getStatusCode()).append(" ").append(fhsce.getMessage()).append(LINE_SEPARATOR);
			LOG.info("Failed link with " + key + "=\"" + src + "\", code is " + fhsce.getStatusCode());
			return sb.toString();
		}
		LOG.debug("Link with " + key + "=\"" + src + "\" is ok");
		return "";
	}

	List iterateAllMatchingElements(final HtmlPage htmlPage) throws Exception {
		try {
			return getContext().getXPathHelper().selectNodes(htmlPage, getXpath());
		} catch (final Exception e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

    public boolean isPerformingAction() {
    	return false;
    }    	
}
