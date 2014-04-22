// Copyright © 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.canoo.webtest.boundary.HtmlUnitBoundary;
import com.canoo.webtest.boundary.UrlBoundary;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;

/**
 * Utility class for {@link Context}.
 */
public final class ContextHelper
{
    private static final Logger LOG = Logger.getLogger(ContextHelper.class);

    /**
     * Writes the response to the file. If an error occurs, just logs it and returns <code>null</code>.
     * @param response the response to write
     * @param file the file to write in
     * @return file if everything works fine, <code>null</code> if an error occured.
     */
    public static File writeResponseFile(final WebResponse response, final File file) {
        OutputStream out = null;
        InputStream in = null;

        try {
            out = new FileOutputStream(file);
            in = new BufferedInputStream(response.getContentAsStream());
            LOG.debug("Writing current response in " + file.getName());
            IOUtils.copy(in, out); // once we move to commons IO 1.1
        }
        catch (final IOException e) {
            LOG.error("Failed writing current response to " + file.getName() + ". Ignoring.", e);
            return null;
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }

        return file;
    }

    /**
     *
     * @param context
     * @param responseBytes
     * @param contentType
     * @param urlStr
     */
    public static void defineAsCurrentResponse(final Context context, final byte[] responseBytes,
                                               final String contentType, final String urlStr) {
        final WebClient webClient = context.getWebClient();
        final WebConnection originalConnection = webClient.getWebConnection();
        try {
            final MockWebConnection mockConnection = new MockWebConnection();
            webClient.setWebConnection(mockConnection);
            mockConnection.setDefaultResponse(responseBytes, 200, "ok", contentType);
            
            // htmlUnit does't currently follow the "current window"
            // check that our faked content will be loaded in what WebTest considers as the "current window"
            // cf WT-293
            final WebWindow currentTopWindow = context.getCurrentResponse().getEnclosingWindow().getTopWindow();
            // with the <previousResponse/> it may happen that the window is not registerd anymore
            if (webClient.getWebWindows().contains(currentTopWindow)) 
            {
            	webClient.setCurrentWindow(currentTopWindow);
            }
            
            final URL url = UrlBoundary.tryCreateUrl(urlStr);
            HtmlUnitBoundary.tryGetPage(url, webClient);
        }
        finally {
            // remove special connection
            webClient.setWebConnection(originalConnection);
        }
    }

    /**
     *
     * @param context
     * @param responseText
     * @param contentType
     * @param urlStr
     */
    public static void defineAsCurrentResponse(final Context context, final String responseText,
                                               final String contentType, final String urlStr) {
        defineAsCurrentResponse(context, responseText.getBytes(), contentType, urlStr);
    }

    /**
    *
    * @param context
    * @param responseText
    * @param contentType
    * @param urlStr
    */
   public static void defineAsCurrentResponse(final Context context, final String responseText,
                                              final String contentType, final Class clazz) {
       defineAsCurrentResponse(context, responseText.getBytes(), contentType, "http://" + clazz.getName());
   }
}
