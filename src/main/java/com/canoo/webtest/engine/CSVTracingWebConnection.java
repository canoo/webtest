package com.canoo.webtest.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;

/**
 * Wrapper around a "real" WebConnection that will use the wrapped web connection
 * to do the real job and save information about all performed requests and received responses.<br>
 *
 * @version $Revision: 90396 $
 * @author Marc Guillemot
 */
public class CSVTracingWebConnection extends WebConnectionWrapper {
    private int counter = 0; 
    private final WebConnection wrappedWebConnection_;
    private final OutputStreamWriter fileWriter_; 

    /**
     * Wraps a web connection to have a report generated of the received responses.
     * @param webConnection the webConnection that do the real work
     * @param dirName the name of the directory to create in the tmp folder to save received responses.
     * If this folder already exists, it will be deleted first.
     * @throws IOException in case of problems writing the files
     */
    public CSVTracingWebConnection(final WebConnection webConnection,
            final File resultFile) throws IOException {

        super(webConnection);

        wrappedWebConnection_ = webConnection;
        fileWriter_ = new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8");
        writeHeaders();
    }

    protected void writeHeaders() throws IOException {
    	appendToResultFile(toCSV(getHeaders()) + "\n");
	}

	/**
     * Calls the wrapped WebConnection and save information about the request/response.
     * {@inheritDoc}
     */
    @Override
    public WebResponse getResponse(final WebRequest request) throws IOException {
        final WebResponse response = wrappedWebConnection_.getResponse(request);

        traceRequest(request, response);
        
        return response;
    }

    /**
     * 
     * @param request
     * @param response
     * @throws IOException 
     */
    protected synchronized void traceRequest(final WebRequest request, final WebResponse response) throws IOException {
    	Iterable<String> info = getInformation(response, request);
    	appendToResultFile(toCSV(info) + "\n");
	}



    /**
     * Saves the response content in the temp dir and adds it to the summary page.
     * @param response the response to save
     * @param request the request used to get the response
     * @throws IOException if a problem occurs writing the file
     */
    protected Iterable<String> getInformation(final WebResponse response, final WebRequest request) {
    	final List<String> fields = new ArrayList<String>();
    
    	fields.add(String.valueOf(++counter)); // HTTP method
    	
    	// the request
    	fields.add(request.getHttpMethod().name()); // HTTP method
    	fields.add(request.getUrl().toString()); // url

    	// the response
    	fields.add(String.valueOf(response.getStatusCode())); // HTTP status code
    	fields.add(response.getStatusMessage()); // HTTP status message
    	fields.add(response.getContentType()); // content type
    	fields.add(String.valueOf(response.getLoadTime())); // load time

    	return fields;
    }

	protected Iterable<String> getHeaders() {
		final String[] headers = { "#", "Method", "URL", "Response code", "Response Message", 
				"Content Type", "Duration" };
		return Arrays.asList(headers);
	}
	
	protected String toCSV(Iterable<String> values) {
		final StringBuilder sb = new StringBuilder();
		for (final String s : values) {
			sb.append(";");
			sb.append(s); // what if s contains ";"?
		}
		sb.deleteCharAt(0);
		return sb.toString();
	}

    protected void appendToResultFile(final String str) throws IOException {
    	fileWriter_.write(str);
    	fileWriter_.flush(); // to make it directly available
    }
    
    @Override
    protected void finalize() throws Throwable {
    	super.finalize();
    	fileWriter_.close();
    }
}
