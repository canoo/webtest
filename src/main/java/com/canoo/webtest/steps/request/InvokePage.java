// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import com.canoo.webtest.util.FileUtil;
import com.canoo.webtest.util.MapUtil;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;

/**
 * Performs an initial request to an url and makes the received response
 * available for subsequent steps.
 * <p>
 * The url may be specified as an absolute url (with protocol) that will be used
 * directly or as the end part of an url. In this case the protocol, host, port and basepath
 * properties of the &lt;config&gt; step will be used to build a complete url.
 * </p>
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step
 *   category="Core"
 *   name="invoke"
 *   description="This step executes a request to a particular URL."
 */
public class InvokePage extends AbstractTargetAction {
    private String fUrl;
    private String fCompleteUrl;
    private String fMethod = "GET";
    private File fContentFile;
    private String fContent;

    private String fSoapAction;

    public String getMethod() {
        return fMethod;
    }

    public String getUrl() {
        return fUrl;
    }

    /**
     * Alternative to set the content of a SOAP message.
     * @param txt the content
     * @webtest.nested.parameter
     *    required="no"
     *    description="An alternative way to set the 'url' or the 'content' attribute.
     * When the 'url' attribute is not specified the nested text is considered as the url value.
     * Otherwise this is used to set the 'content' attribute for e.g. large content (properties get evaluated in this content)."
     */
    public void addText(final String txt) {
    	final String expandedText = getProject().replaceProperties(txt);
    	if (getUrl() == null) {
    		setUrl(expandedText);
    	}
    	else {
    		setContent(expandedText);
    	}
    }
    
    /**
     * Sets the url.
     *
     * @param newUrl the relative or absolute url
     * @webtest.parameter required="yes"
     * description="A complete URL or the 'relative' part of an URL which will be appended to the 'static' parts created from the configuration information defined with the <config> step."
     */
    public void setUrl(final String newUrl) {
        fUrl = newUrl;
    }

    /**
     * Sets the HTTP Method.
     *
     * @param method
     * @webtest.parameter
     *   required="no"
     *   default="GET"
     *   description="Sets the HTTP Method, i.e. whether the invoke is a GET or POST."
     */
    public void setMethod(final String method) {
        fMethod = method;
    }

    public File getContentFile() {
        return fContentFile;
    }

    /**
     * Sets the filename of the request contents.
     *
     * @param contentFile
     * @webtest.parameter
     *   required="no"
     *   description="Filename to extract request contents from. 
     *   Ignored for GET requests. 
     *   Only one of <em>content</em> and <em>contentFile</em> should be set."
     */
    public void setContentFile(final File contentFile) {
        fContentFile = contentFile;
    }

    public String getContent() {
        return fContent;
    }

    /**
     * Sets the request content.
     *
     * @param content
     * @webtest.parameter
     *   required="no"
     *   description="Form data in 'application/x-www-form-urlencoded' format, which will be sent in the body of a POST request. Ignored for GET requests. Only one of <em>content</em> and <em>contentFile</em> should be set."
     */
    public void setContent(final String content) {
        fContent = content;
    }

    public String getSoapAction() {
        return fSoapAction;
    }

    /**
     * Sets the SOAP action.
     *
     * @param soapAction
     * @webtest.parameter
     *   required="no"
     *   description="If the HTTP method is POST and is in fact a SOAP POST request, this allows the SOAP Action header to be set. Ignored for GETs."
     */
    public void setSoapAction(final String soapAction) {
        fSoapAction = soapAction;
    }

    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getUrl(), "url");
        paramCheck(getContent() != null && getContentFile() != null, "Only one of 'content' and 'contentFile' must be set.");
        paramCheck("POST".equals(getMethod()) && getContent() == null && getContentFile() == null,
                "One of 'content' or 'contentFile' must be set for POST.");
    }

    protected Page findTarget() throws IOException, SAXException {
        if ("POST".equals(getMethod())) {
            return findTargetByPost();
        }
        fCompleteUrl = getContext().getConfig().getUrlForPage(getUrl());
        final WebRequest settings = new WebRequest(new URL(fCompleteUrl));
        settings.setHttpMethod(HttpMethod.valueOf(getMethod().toUpperCase()));
        return getResponse(settings);
    }

    private Page findTargetByPost() throws IOException, SAXException {
        String url = getContext().getConfig().getUrlForPage(getUrl());
        final WebRequest settings = new WebRequest(new URL(url), HttpMethod.POST);
        
        // get default encoding
        final String charset = System.getProperty("file.encoding");
        
        final Map headers = new HashMap();
        if (!StringUtils.isEmpty(fSoapAction)) {
            headers.put("Content-type", "text/xml; charset=" + charset);
            headers.put("SOAPAction", fSoapAction);
        } 
        else {
            // TODO: is this the correct Content-type for non-SOAP posts?
            headers.put("Content-type", "application/x-www-form-urlencoded");
        }
        settings.setAdditionalHeaders(headers);
        final String content;
        if (getContent() != null) {
            content = getContent();
        } 
        else {
            content = FileUtil.readFileToString(getContentFile(), this);
        }
        settings.setRequestBody(content);
        settings.setCharset(charset);
        return getResponse(settings);
    }

    protected String getLogMessageForTarget() {
        return "by URL: " + getUrl();
    }

    /**
     * Adds the computed url if only a part was specified (nice to have in reports)
     */
    protected void addComputedParameters(final Map map) {
    	super.addComputedParameters(map);
    	if (!StringUtils.equals(fUrl, fCompleteUrl))
    		MapUtil.putIfNotNull(map, "-> complete url", fCompleteUrl);
    }
}
