// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.AbstractBrowserAction;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.BaseFrame;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Selects a frame as the "current active" response.
 *
 * @author <a href="torben@tretau.net">Torben Tretau</a>
 * @author Marc Guillemot
 * @webtest.step category="Core"
 * name="followFrame"
 * description="Locates a frame by its name (e.g. <em><FRAME NAME='myFrame' SRC='myPage.jsp'></em>)
 * and defines it as the \"current\" response for the next steps."
 * alias="followframe"
 */
public class FollowFrame extends AbstractBrowserAction {
	private static final Logger LOG = Logger.getLogger(FollowFrame.class);
	private String fName;
	private String fHtmlId;
	private String fRelative;
	private boolean fIsRelative;

	/**
	 * @param htmlId the new value.
	 * @webtest.parameter required="yes/no"
	 * description="The id of the frame to select.
	 * One of 'name' or 'htmlid' must be set."
	 */
	public void setHtmlId(final String htmlId) {
		fHtmlId = htmlId;
	}

	public String getHtmlId() {
		return fHtmlId;
	}

	public String getName() {
		return fName;
	}

	/**
	 * @param newName
	 * @webtest.parameter
	 *   required="yes/no"
	 *   description="The name of the frame to follow, specified in the frameset. 
	 *   For nested frames use the name of the frame your are after, e.g. it may be the name 
	 *   in the nested frameset. 
	 *   NOTE: if nested frames bear the same name (e.g. foo inside foo), it is not specified 
	 *   which of the frames will be selected.
	 *   It is possible to select the top window using the special name '_top'.
	 *   One of 'name' or 'htmlid' must be set."
	 */
	public void setName(final String newName) {
		fName = newName;
	}

	/**
	 * Indicates if the given frame name is relative to the current response (versus to the main window)
	 *
	 * @webtest.parameter
	 *   required="no"
	 *   default="false"
	 *   description="Indicates that the frame with the given name should be searched 
	 *   in the current response (rather than in the top window). 
	 *   In the case of nested frames this allows you to follow a first frame, 
	 *   perform some actions and later to follow the nested frame."
	 */
	public String getRelative() {
		return fRelative;
	}

	public void setRelative(final String relative) {
		fRelative = relative;
	}

    protected void verifyParameters() {
		super.verifyParameters();
	    fIsRelative = ConversionUtil.convertToBoolean(fRelative, false);
		nullResponseCheck();

		if (getHtmlId() != null && getName() != null)
		{
			throw new StepExecutionException("\"name\" and \"htmlId\" can't be combined!", this);
		}
		else if (getName() == null && getHtmlId() == null)
		{
			throw new StepExecutionException("\"name\" or \"htmlId\" must be set!", this);
		}
    }

	protected Page findTarget(final Context context) throws XPathException, IOException, SAXException {
		final HtmlPage page;
		if (fIsRelative) {
			page = (HtmlPage) context.getCurrentResponse();
		} else { // start from the page in the top window
			page = (HtmlPage) context.getCurrentResponse().getEnclosingWindow().getTopWindow().getEnclosedPage();
		}

		final WebWindow frameWindow = getFrame(page, fName, fHtmlId);

		if (frameWindow == null) {
			throw new StepFailedException(getStepLabel() + " Frame not found with name: " + fName + " available: "
			   + page.getFrames(),
			   this);
		}
		return frameWindow.getEnclosedPage();
	}

	/**
	 * Search for the frame with the given name inside an html page and its nested frames.
	 *
	 * @param htmlPage the page to search in
	 * @param name the name of the searched frame, <code>null</code> if searched by htmlId
	 * @param htmlId the id of the searched frame, <code>null</code> if searched by name
	 * @return <code>null</code> if not found
	 */
	static WebWindow getFrame(final HtmlPage htmlPage, final String name, final String htmlId) {
		LOG.info("Looking for frame (name: \"" + name 
				+ "\", htmlId: \"" + htmlId + "\") in " + htmlPage.getUrl());

		if ("_top".equals(name))
		{
			LOG.debug("Asked for frame with special name \"_top\", returning the top window for the current page");
			return htmlPage.getEnclosingWindow().getTopWindow();
		}
		
		final WebWindow theFrame;
		
		// first look at the frames directly contained in the page
		if (htmlId != null)
		{
			theFrame = getFrameById(htmlPage, htmlId);
		}
		else // by name
		{
			theFrame = getFrameByName(htmlPage, name);
		}
		
		if (theFrame != null)
		{
			return theFrame;
		}

		// if not a top level frame, perhaps is it a nested frame
		for (final Iterator iter = htmlPage.getFrames().iterator(); iter.hasNext();) {
			final WebWindow elt = (WebWindow) iter.next();
			if (elt.getEnclosedPage() instanceof HtmlPage) {
				LOG.debug("looking at subframes of \"" + elt.getName() + "\": " + elt);
				final WebWindow frame = getFrame((HtmlPage) elt.getEnclosedPage(), name, htmlId);
				if (frame != null) {
					return frame;
				}
			}
		}

		LOG.debug("frame not found");
		return null;
	}

	/**
	 * Gets the frame with the given html id directly in the page
	 * @param htmlPage the page
	 * @param htmlId the id
	 * @return <code>null</code> if no frame found with this id
	 */
	private static WebWindow getFrameById(final HtmlPage htmlPage, final String htmlId) {
		LOG.debug("Looking for element with id \"" + htmlId + "\"");
		HtmlElement elt;
		try
		{
			elt = htmlPage.getHtmlElementById(htmlId);
			if (elt instanceof BaseFrame)
			{
				LOG.debug("it's the right one");
				return ((BaseFrame) elt).getEnclosedWindow();
			}
		}
		catch (final ElementNotFoundException e)
		{
			LOG.debug("No element with id \"" + htmlId + "\"");
		}
		return null;
	}

	/**
	 * Gets the frame with the given name directly in the page
	 * @param htmlPage the page
	 * @param name the name
	 * @return <code>null</code> if no frame found with this id
	 */
	private static WebWindow getFrameByName(final HtmlPage htmlPage, final String name) {
		final List subFrames = htmlPage.getFrames();
		for (final Iterator iter = subFrames.iterator(); iter.hasNext();) {
			final FrameWindow elt = (FrameWindow) iter.next();
			LOG.debug("looking at frame \"" + elt.getName() + "\": " + elt);
			if (elt.getName().equals(name)) {
				LOG.debug("it's the right one");
				return elt;
			}
		}
		return null;
	}

	public void doExecute() throws Exception {
        final Context context = getContext();
        context.saveResponseAsCurrent(findTarget(context));
	}

    /**
     * Called by Ant to set the text nested between opening and closing tags.
     * @param text the text to set
     * @webtest.nested.parameter
     *    required="no"
     *    description="Alternative way to set the 'name' attribute."
     */
    public void addText(final String text) {
    	if (getName() == null)
 		   setName(getProject().replaceProperties(text));
 	}

    public boolean isPerformingAction() {
    	return false;
    }
}
