// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.verify;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.canoo.webtest.boundary.HtmlUnitBoundary;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.RegExStringVerifier;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Dierk Koenig, Urs-Peter H&auml;ss
 * @author Marc Guillemot, Paul King, Brian Hubbard
 * @webtest.step category="Core"
 * name="verifyLinks"
 * alias="verifylinks"
 * description="This step checks the validity of all links on the current page. Non-<key>HTML</key> pages (CSS, <key>javascript</key>, <key>XML</key> files) are not checked for internal links. Non-<key>HTTP</key> links (mail addresses, ftp etc.) are not checked or followed."
 */
public class VerifyLinks extends Step
{
    private static final Logger LOG = Logger.getLogger(VerifyLinks.class);
    private String fBaseHost;
    private int fMaxDepth;
    private String fMaxDepthStr;
    private int fCurrentDepth;
    private boolean fOnsiteonly;
    private String fExcludes;
    private String fIncludes;
    private final Set fFailedVisits = new HashSet();
    private final Set fVisitedUrls = new HashSet();
    private int fValidLinks;
    private boolean fIgnoreForeignJSErrors;

    protected Set getFailedVisits() {
        return fFailedVisits;
    }

    public String getDepth() {
        return fMaxDepthStr;
    }

    /**
     * @webtest.parameter required="no"
     * default="0"
     * description="The <em>depth</em> parameter defines the depth of the recursive search for broken links on sub-pages."
     */
    public void setDepth(String depth) {
        fMaxDepthStr = depth;
    }

    /**
     * @webtest.parameter required="no"
     * default="&lt;empty&gt;"
     * description="If <em>excludes</em> is set then each link found is compared to the defined string (via regexp), if it matches then the link is not followed."
     */
    public void setExcludes(String regex) {
        fExcludes = regex;
    }

    public String getExcludes() {
        return fExcludes;
    }

    /**
     * @webtest.parameter required="no"
     * default="&lt;all&gt;"
     * description="If <em>includes</em> is set then each link found is compared to the defined string (via regexp), if it matches then the link is processed, others are ignored."
     */
    public void setIncludes(String regex) {
        fIncludes = regex;
    }

    public String getIncludes() {
        return fIncludes;
    }

    /**
     * @webtest.parameter required="no"
     * default="false"
     * description="If <em>onsiteonly</em> is set to <em>true</em>, the recursive search for invalid links is limited to the local host. 
     * Only the initial link to a foreign host is checked, but no deeper search is performed."
     */
    public void setOnsiteonly(final boolean onsiteonly) {
        fOnsiteonly = onsiteonly;
    }
    
    /**
     * 
     * @webtest.parameter required="no"
     * default="false"
     * description="Indicates if JavaScript errors should be ignored on visited pages from a different host
     * than the current page."
     */
    public void setIgnoreForeignJSErrors(final boolean b)
    {
    	fIgnoreForeignJSErrors = b;
    }

    public void doExecute() throws SAXException, MalformedURLException {
        verifyProperties();
        nullResponseCheck();
        final Context context = getContext();
        final HtmlPage htmlPage = context.getCurrentHtmlResponse(this);
        LOG.info("Examining page with title=" + htmlPage.getTitleText());
        if (!StringUtils.isEmpty(getIncludes())) {
            LOG.info("Only including links which match '" + getIncludes() + "'");
        }
        if (!StringUtils.isEmpty(getExcludes())) {
            LOG.info("Excluding links which match '" + getExcludes() + "'");
        }
        fBaseHost = htmlPage.getUrl().getHost();
        final WebClient client = context.getWebClient();
        checkVisits(client, htmlPage);
        if (!fFailedVisits.isEmpty()) {
            throw new StepFailedException(fFailedVisits.size() + " broken link(s): " + brokenLinksToString(), this);
        }
    }

	protected void addComputedParameters(final Map map)
    {
    	map.put("-> valid links", String.valueOf(fValidLinks));
    }

    protected void checkVisits(final WebClient webClient, final HtmlPage response) {
        final Set urls = getGoodLinks(response);
        final RegExStringVerifier verifier = new RegExStringVerifier();
        for (final Iterator iter = urls.iterator(); iter.hasNext();) {
            final URL url = (URL) iter.next();
            if (fVisitedUrls.contains(url)) {
                LOG.debug("Skipped already visited: " + url);
                fValidLinks++;
                continue;
            }
            if (!StringUtils.isEmpty(getIncludes()) && (!verifier.verifyStrings(getIncludes(), url.toString()))) {
                LOG.info("Skipped link as it doesn't match the includes list: " + url);
                continue;
            }
            if (!StringUtils.isEmpty(getExcludes()) && (verifier.verifyStrings(getExcludes(), url.toString()))) {
                LOG.info("Skipped link as matched the excludes list: " + url);
                continue;
            }
            visit(response, url, webClient);
        }
    }

    protected void visit(final HtmlPage referingPage, final URL url, final WebClient webClient) {
    	final boolean ignoreJSErrorsOriginal = webClient.isThrowExceptionOnScriptError();
    	if (fIgnoreForeignJSErrors && isForeignHost(url))
    	{
    		LOG.info("Ignore JS errors (if any) for " + url); 
    		webClient.setThrowExceptionOnScriptError(false);
    	}
        final Page response = HtmlUnitBoundary.tryGetPageNoFail(url, webClient);
        webClient.setThrowExceptionOnScriptError(ignoreJSErrorsOriginal);

		fVisitedUrls.add(url);
        if (response == null) {
            fFailedVisits.add(new ZFailedLink(url, referingPage.getUrl()));
        }
        else {
            fValidLinks++;

        	if (response instanceof HtmlPage) {
                followRecursively((HtmlPage) response, webClient);
        	}
        }
    }

    protected void followRecursively(final HtmlPage htmlPage, final WebClient webClient) {
        LOG.debug("fMaxDepth = " + fMaxDepth);
        if (fCurrentDepth < fMaxDepth && !stopHunting(htmlPage)) {
            ++fCurrentDepth;
            checkVisits(webClient, htmlPage);
            --fCurrentDepth;
        }
    }

    protected String brokenLinksToString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = fFailedVisits.iterator(); iter.hasNext();) {
            ZFailedLink failedLink = (ZFailedLink) iter.next();
            sb.append(failedLink.getFailedUrl()).append(" on ").append(failedLink.getReferingUrl()).append("; ");
        }
        return sb.toString();
    }

    static int getLinkCount(final HtmlPage response) {
        return getGoodLinks(response).size();
    }

    /**
     * Gets all HTTP links in the response
     *
     * @param response
     * @return a set of {@link URL}
     */
    static Set getGoodLinks(final HtmlPage response) {
        LOG.info("Looking for links in " + response);
        final Set urls = new HashSet();

        for (final Iterator iter = response.getAnchors().iterator(); iter.hasNext();) {
            processLink(response, (HtmlAnchor) iter.next(), urls);
        }

        LOG.info(urls.size() + " different links found in page " + response.getUrl());
        return urls;
    }

    private static void processLink(final HtmlPage response, final HtmlAnchor link, final Set urls) {
        try {
            final URL url = response.getFullyQualifiedUrl(link.getHrefAttribute());
            final String protocol = url.getProtocol();
            if ("http".equals(protocol) || "https".equals(protocol)) {
                LOG.info("Adding url to check: " + url);
                urls.add(url);
            }
            else {
                LOG.info("Skipped link due to protocol: " + url);
            }
        } 
        catch (final MalformedURLException e) {
            LOG.info("Skipped link due to bad url: " + link.getHrefAttribute());
        }
    }

    protected boolean stopHunting(final HtmlPage htmlPage) {
        return fOnsiteonly && isForeignHost(htmlPage.getUrl());
    }

    protected boolean isForeignHost(final URL url) {
        return !fBaseHost.equals(url.getHost());
    }

    protected void verifyProperties() {
        fMaxDepth = ConversionUtil.convertToInt(getDepth(), 0);
        optionalIntegerParamCheck(getDepth(), "depth", true);
    }

    public boolean isPerformingAction() {
    	return false;
    }
}

/**
 * Utility data holder
 */
class ZFailedLink
{
    private URL fFailedUrl;
    private URL fReferingUrl;

    ZFailedLink(final URL failedUrl, final URL referingUrl) {
        fFailedUrl = failedUrl;
        fReferingUrl = referingUrl;
    }

    public URL getFailedUrl() {
        return fFailedUrl;
    }

    public URL getReferingUrl() {
        return fReferingUrl;
    }
}
