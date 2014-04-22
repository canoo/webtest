// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.extension;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Verifies that all images referenced in the current response are present.<p>
 * <p/>
 * Currently doesn't work with images references via JavaScript/CSS.
 *
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step category="Extension"
 * name="verifyImages"
 * alias="verifyimages"
 * description="Verifies that all images referenced in the current response are available. May not correctly detect images referenced in JavaScript or CSS files or images set by JavaScript."
 */
public class VerifyImages extends Step
{
    private static final Logger LOG = Logger.getLogger(VerifyImages.class);

    /**
     * looks for missing images referenced in the page
     */
    public void doExecute() {
        nullResponseCheck();
        final Context context = getContext();
        final WebClient conversation = context.getWebClient();
        final HtmlPage currentResponse = context.getCurrentHtmlResponse(this);

        context.suspendWindowListener();
        final Collection colFailedImagesSrc = checkImages(conversation, currentResponse);
        context.restoreWindowListener();

        if (!colFailedImagesSrc.isEmpty()) {
        	final StepFailedException sfe = new StepFailedException(colFailedImagesSrc.size() + " missing image(s) in page "
        			+ currentResponse.getUrl());

        	final StringBuffer sb = new StringBuffer();
            for (final Iterator iter = colFailedImagesSrc.iterator(); iter.hasNext();) {
                sb.append(iter.next()).append("\r\n ");
            }
            sfe.addDetail("missing images", sb.toString());
            throw sfe;
        }
    }

    /**
     * Checks the images and return the src values of the missing ones
     */
    private static Collection checkImages(final WebClient webClient, final HtmlPage htmlPage) {
        final Set uris = collectImageUris(htmlPage);

        // store original status code setting
        final boolean bPreviousThrowExceptionOnFailingStatusCode = webClient.isThrowExceptionOnFailingStatusCode();
        // adjust setting to be sure to get an exception for failing http code
        webClient.setThrowExceptionOnFailingStatusCode(true);

        final Iterator iter = uris.iterator();
        while (iter.hasNext()) {
            final String src = (String) iter.next();
            tryGetImage(htmlPage, src, webClient, iter);
        }

        // reset status code setting
        webClient.setThrowExceptionOnFailingStatusCode(bPreviousThrowExceptionOnFailingStatusCode);

        if (!uris.isEmpty()) {
            LOG.info("Number of failing images found: " + uris.size());
        }
        return uris;
    }

    private static void tryGetImage(final HtmlPage htmlPage, final String src,
                                    final WebClient webClient, final Iterator iter) {
        try {
            final URL url = htmlPage.getFullyQualifiedUrl(src);
            final Page resp = webClient.getPage(url);

            if (!(resp instanceof UnexpectedPage)) {
                LOG.info("Failed image with src=\"" + src + "\": content type is "
                        + resp.getWebResponse().getContentType());
            } else {
                // TODO: check that the content type corresponds to an image
                LOG.debug("Image with src=\"" + src + "\" is ok");
                iter.remove(); // src is ok
            }
        } catch (final Exception e) {
            LOG.info("Failed image with src=\"" + src + "\"");
            // nothing to do, src stays in the set
        }
    }

    private static Set collectImageUris(final HtmlPage htmlPage) {
        final Set uris = new TreeSet();

        // look for img tags
        for (final Iterator iter = htmlPage.getDocumentElement().getHtmlElementsByTagName(HtmlConstants.IMG).iterator(); iter.hasNext();) {
            final HtmlImage img = (HtmlImage) iter.next();
            uris.add(img.getSrcAttribute());
        }

        // and input tags with type="image"
        final List inputImages = htmlPage.getDocumentElement().getElementsByAttribute(HtmlConstants.INPUT, HtmlConstants.TYPE, HtmlConstants.IMAGE);
        for (final Iterator iter = inputImages.iterator(); iter.hasNext();) {
            final HtmlInput img = (HtmlInput) iter.next();
            uris.add(img.getSrcAttribute());
        }
        return uris;
    }
    
    public boolean isPerformingAction() {
    	return false;
    }
}