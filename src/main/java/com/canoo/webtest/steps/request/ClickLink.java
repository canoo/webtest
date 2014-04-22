// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.HtmlConstants;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Clicks a link determined by its id or its label and/or its href.
 *
 * @author Mister Unknown
 * @author Marc Guillemot
 * @author Matt Raible
 * @author Paul King
 * @author Denis N. Antonioli
 * @webtest.step category="Core"
 * name="clickLink"
 * alias="clicklink"
 * description="This step tries to locate a link (like <a href='thisIsAnotherPage.html'>Follow me</a>) and to click on it. The page where this click leads to will be the current one for further actions."
 */
public class ClickLink extends AbstractIdOrLabelTarget {
	private static final Logger LOG = Logger.getLogger(ClickLink.class);
	private String fHref;

	public String getHref() {
		return fHref;
	}

	/**
	 * @webtest.parameter required="yes/no"
	 * description="A substring of the <key>HTML</key> anchor tag's HREF attribute for the link of interest. Useful if (part of the <em>href</em>) uniquely identifies a link (for example, a unique parameter/value pair in the <em>href</em>). Also, useful for distinguisking between links with the same labels (in which case, use <em>label</em> and <em>href</em> combined). Either <em>label</em>, <em>href</em> or <em>htmlId</em> is required. Href has the lowest precedence."
	 */
	public void setHref(final String href) {
		fHref = href;
	}

	protected Page findTarget() throws XPathException, IOException {
		final HtmlPage currentResp = getContext().getCurrentHtmlResponse(this);
		final HtmlAnchor link = (HtmlAnchor) findClickableElement(currentResp);

		if (link == null) 
		{
			final StepFailedException e = new StepFailedException("Link not found in page " + currentResp.getUrl(), this);

			final StringBuffer sb = new StringBuffer();
			for (final Iterator iter = currentResp.getAnchors().iterator(); iter.hasNext();) {
				final HtmlAnchor webLink = (HtmlAnchor) iter.next();

				sb.append("- label \"").append(webLink.asText());
				sb.append("\" with url \"").append(webLink.getHrefAttribute());
				sb.append("\" and id \"").append(webLink.getId());
				sb.append("\"\n");
			}
			e.addDetail("available links", sb.toString());
			throw e;
		}
		LOG.debug("Clicking on link with href: " + link.getHrefAttribute());

		return link.click();
	}

	protected String getLogMessageForTarget() {
		return "by clickLink";
	}

	protected void verifyParameters() {
		super.verifyParameters();
		nullResponseCheck();

		int count = 0;
		if (getXpath() != null) {
			count++;
		}
		if (getHtmlId() != null) {
			count++;
		}
		if (getHref() != null || getLabel() != null) {
			count++;
		}
		paramCheck(count == 0, "\"htmlId\" or \"xpath\" or \"label\" or \"href\" must be set!");
		paramCheck(count > 1, "\"htmlId\", \"xpath\" and \"label\" or \"href\" can't be combined!");
	}

	/**
	 * get the link corresponding to the indications defined in the class
	 *
	 * @param page
	 * @return <code>null</code> if no link found
	 * @deprecated Use {@link #findClickableElement(com.gargoylesoftware.htmlunit.html.HtmlPage)} .
	 */
	protected HtmlAnchor locateWebLink(final HtmlPage page) throws XPathException {
		return (HtmlAnchor) findClickableElement(page);
	}

	protected HtmlElement findClickableElementByAttribute(HtmlPage page) {
		HtmlAnchor link = locateTextLink(page);
		if (link != null) {
			return link;
		}
		return getLinkWithImageText(page, getLabel());
	}


	/**
	 * Returns the first link which contains an image with the specified text as its 'alt' attribute.
	 *
	 * @param htmlPage
	 * @param text
	 * @return <code>null</code> if none found
	 */
	protected static HtmlAnchor getLinkWithImageText(final HtmlPage htmlPage, final String text) {
		final List li = htmlPage.getDocumentElement().getElementsByAttribute(HtmlConstants.IMG, HtmlConstants.ALT, text);

		LOG.debug("Found " + li.size() + " images with alt=\"" + text + "\"");

		// looking for the first enclosing link
		for (final Iterator iter = li.iterator(); iter.hasNext();) {
			final HtmlElement elt = (HtmlElement) iter.next();
			final HtmlAnchor link = (HtmlAnchor) findParent("a", elt);
			if (link != null) {
				return link;
			}
		}
		return null;
	}

	/**
	 * Finds the first parent element with the given tag name
	 *
	 * @param tagName
	 * @param elt
	 * @return <code>null</code> if none found
	 */
	private static HtmlElement findParent(final String tagName, final HtmlElement elt) {
		HtmlElement parent = (HtmlElement) elt.getParentNode();

		while (parent != null) {
			if (tagName.equals(parent.getTagName())) {
				return parent;
			}
			final Object o = parent.getParentNode();
			parent = o instanceof HtmlElement ? (HtmlElement) o : null;
		}
		return null;
	}

	/**
	 * @return null if not found
	 */
	protected HtmlAnchor locateTextLink(final HtmlPage currentResponse) {
		for (final Iterator iter = currentResponse.getAnchors().iterator(); iter.hasNext();) {
			final HtmlAnchor curLink = (HtmlAnchor) iter.next();

			if (isMatching(curLink)) {
				return curLink;
			}
		}
		return null;
	}

	protected boolean isMatching(final HtmlAnchor link) {
		boolean bRep = true;

		if (getLabel() != null) {
			bRep = link.asText().indexOf(getLabel()) >= 0;
		}

		LOG.debug("labelFound = " + bRep + " in " + link.asText());
		if (getHref() == null) {
			return bRep;
		}

		boolean bHrefFound = link.getHrefAttribute().indexOf(getHref()) >= 0;
		LOG.debug("hrefFound = " + bHrefFound + " in " + link.getHrefAttribute());
		return bRep && bHrefFound;
	}

	HtmlElement checkFoundElement(HtmlElement elt)  throws StepFailedException {
		if (elt instanceof HtmlAnchor) {
			return elt;
		}
		throw new StepFailedException("Selected element is a " + elt.getTagName() + " tag and not a link", this);
	}


    /**
     * Called by Ant to set the text nested between opening and closing tags.
     * @param text the text to set
     * @webtest.nested.parameter
     *    required="no"
     *    description="Alternative way to set the 'label' attribute."
     */
    public void addText(final String text) {
	   setLabel(getProject().replaceProperties(text));
 	}
}
