// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.steps.Step;
import com.canoo.webtest.util.ConversionUtil;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Selects a top level window as the "current active" window.<p>
 *
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step category="Extension"
 * name="selectWindow"
 * description="Selects the content of a window as the current response. 
 * Useful when a test sequence causes many windows to open.
 * Only one from 'name', 'title', or 'index' can be specified."
 */
public class SelectWindow extends Step {
    private static final Logger LOG = Logger.getLogger(SelectWindow.class);
    private String fName;
    private String fIndex;
    private String fTitle;

    public String getTitle() {
        return fTitle;
    }

    /**
	 * One of 'index', 'name', or 'title' must be set. Name takes precedence
	 * in case both 'index' and 'name' are specified.
	 * 
     * @webtest.parameter required="yes/no"
     * description="The title of the html document contained in the window to select."
     */
    public void setTitle(final String newTitle) {
        fTitle = newTitle;
    }

    public String getName() {
        return fName;
    }

    /**
	 * One of 'index' or 'name' must be set. Name takes precedence
	 * in case both 'index' and 'name' are specified.
	 * 
     * @webtest.parameter required="yes/no"
     * description="The name of the window to select."
     */
    public void setName(final String newName) {
        fName = newName;
    }

    /**
	 * Selects the top level window with the requested index (starting with 0).
	 * One of 'index' or 'name' must be set. Name takes precedence
	 * in case both 'index' and 'name' are specified.
	 * 
	 * @param newIndex the new value
	 * @webtest.parameter required="yes/no"
	 * description="The index (starting with 0) of the top level window to select. "
     */
    public void setIndex(final String newIndex) {
        fIndex = newIndex;
    }
    
    public String getIndex() {
    	return fIndex;
    }
    

    public void doExecute() throws Exception {
        LOG.debug("Selecting window " + getName());
        final Context context = getContext();
        final WebClient webClient = context.getWebClient();
        final WebWindow window = findTopLevelWindow(webClient);
        if (window == null) {
        	final StepFailedException sfe = new StepFailedException("No window found");
        	sfe.addDetail("available windows", getAvailableWindowsMessage(webClient));
        	throw sfe;
        }

        context.saveResponseAsCurrent(window.getEnclosedPage());
    }

    /**
     * Finds the top level window with the specified name.
     *
     * @param webClient
     * @return <code>null</code> if not found
     */
    WebWindow findTopLevelWindow(final WebClient webClient) {
    	final WebWindow result;
		if (getName() != null) {
			result = findTopLevelWindowByName(webClient);
		}
		else if (getTitle() != null) {
			result = findTopLevelWindowByTitle(webClient);
		}
		else {
			result = findTopLevelWindowByIndex(webClient);
		}
       return result;
    }
    
    WebWindow findTopLevelWindowByName(final WebClient webClient) {
        for (final Iterator iter = webClient.getWebWindows().iterator(); iter.hasNext();) {
            final WebWindow window = (WebWindow) iter.next();
            if (window instanceof TopLevelWindow && window.getName().equals(getName())) {
                return window;
            }
        }
        return null;
    }

    WebWindow findTopLevelWindowByTitle(final WebClient webClient) {
        for (final Iterator iter = webClient.getWebWindows().iterator(); iter.hasNext();) {
            final WebWindow window = (WebWindow) iter.next();
            if (window instanceof TopLevelWindow)
           	{
            	final Page containedPage = window.getEnclosedPage();
            	if (containedPage instanceof HtmlPage 
            			&& getTitle().equals(((HtmlPage) containedPage).getTitleText())) {
            		return window;
            	}
            }
        }
        return null;
    }

    
    WebWindow findTopLevelWindowByIndex(final WebClient webClient) {
    	int count = 0;
    	int index = ConversionUtil.convertToInt(getIndex(), 0);
        for (final Iterator iter = webClient.getWebWindows().iterator(); iter.hasNext();) {
            final WebWindow window = (WebWindow) iter.next();
            if (window instanceof TopLevelWindow && (count++ == index)) {
                return window;
            }
        }
        
    	return null;
    }
    
    static String getAvailableWindowsMessage(final WebClient webClient) {
    	final StringBuffer sb = new StringBuffer();
        int i = 0;
        for (Iterator iter = webClient.getWebWindows().iterator(); iter.hasNext();) {
            final WebWindow curWindow = (WebWindow) iter.next();
            if (curWindow instanceof TopLevelWindow) {
            	final Page page = curWindow.getEnclosedPage();
            	if (i > 0)
            		sb.append("\n");
            	sb.append("index: " + i + ", name: >" + curWindow.getName() + "<");
            	if (page instanceof HtmlPage) {
            		sb.append(", title: >" + ((HtmlPage) page).getTitleText() + "<");
            	}
            	else {
            		sb.append(", " + page.getWebResponse().getContentType());
            	}
            	sb.append(", url: " + page.getUrl());
            	++i;
            }
        }
        return sb.toString();
    }

    protected void verifyParameters() {
        super.verifyParameters();
		nullResponseCheck();
		paramCheck(getName() == null && getTitle() == null && StringUtils.isEmpty(getIndex()),
		   "Required parameter 'name', 'title' or 'index' must be set!");
        optionalIntegerParamCheck(getIndex(), "index", true);
    }

    public boolean isPerformingAction() {
    	return false;
    }
}
