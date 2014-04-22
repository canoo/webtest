// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import com.gargoylesoftware.htmlunit.StringWebResponse;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author unknown
 * @author Marc Guillemot
 */
public class WebResponseStub extends StringWebResponse {
    private String fContent = "";
    private int fHttpReturnCode = HttpURLConnection.HTTP_OK;
    private Map fHeaders = new HashMap();
    static final String DEFAULT_URL_STRING = "http://myserver";

    public WebResponseStub(final String content, final URL url, final int returnCode) {
        super(content, url);
         
        fContent = content;
        fHttpReturnCode = returnCode;
        fHeaders.put("Content-type", "text/html; charset=us-ascii");
    }

    public static WebResponseStub getDefault() {
        URL defaultUrl = null;
        try {
            defaultUrl = new URL(DEFAULT_URL_STRING);
        } catch (MalformedURLException e) {
        	// does nothing
        }
        
        return new WebResponseStub("", defaultUrl, HttpURLConnection.HTTP_OK);
    }

    public Map getHeaders() {
        return fHeaders;
    }

    public String[] getHeaderFields(String fieldName) {
        String field = getHeaderField(fieldName);
        return new String[]{field};
    }

    public String getHeaderField(String fieldName) {
        return (String) fHeaders.get(fieldName);
    }

    public String getText() {
        return fContent;
    }

    public String toString() {
        return fContent;
    }

    public String[] getHeaderFieldNames() {
        return (String[]) fHeaders.keySet().toArray(new String[fHeaders.keySet().size()]);
    }

    public int getResponseCode() {
        return fHttpReturnCode;
    }

    public String getResponseMessage() {
        return "HTTP_" + fHttpReturnCode;
    }
}
