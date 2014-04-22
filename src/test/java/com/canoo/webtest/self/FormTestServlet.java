// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.canoo.webtest.util.HtmlConstants;

/**
 *
 * @author unknown
 * @author Marc Guillemot
 */
public class FormTestServlet extends HttpServlet {
    private String fServletPath = "formTest";
    private static final boolean DUMMY = false;		// only used to compress dispatching code

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        dispatchRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        dispatchRequest(request, response);
    }

    private boolean dispatchRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String action = getParameterAsString("mode", request);

        if ("postTest".equals(action)) return renderForm(request, response, "POST", "postTest");
        if ("getTest".equals(action)) return renderForm(request, response, "GET", "getTest");
        if ("invalidHtml".equals(action)) return renderInvalidHtml(request, response, "invalid HTML");
        if ("demo".equals(action)) return renderDemoLoginPage(response, "Login Page");
        if ("demoLogin".equals(action)) return renderDemoSuccessPage(response, "Home Page");
        if ("formNestingWrong".equals(action)) return renderFormsBetweenTDs(response);
        if ("tableTest".equals(action)) return renderTableTest(request, response);
        if ("checkboxTest".equals(action)) return renderCheckboxTest(request, response);
        if ("linkTest".equals(action)) return renderLinkTest(request, response);
        if ("redirectTest".equals(action)) return renderRedirectTest(response);
        if ("redirectTestMeta".equals(action)) return renderRedirectTestMeta(request, response);
        if ("redirectTarget".equals(action)) return renderRedirectTarget(request, response);
        if ("redirectPage".equals(action)) return renderRedirectPage(request, response);
        if ("https".equals(action)) return renderHttpsPage(request, response);
        if ("upload".equals(action)) 
        	return renderUploadInfoPage(request, response);
        if ("timeoutTest".equals(action)) 
        	return renderTimeoutTestPage(request, response);

        return renderDefault(request, response, "Canoo WebTest", "Wusi is the greatest");
    }

    /**
     * Waits the time specified in the query parameter "wait" (in seconds)
     * allowing to test the timeout config parameter before rendering the default page
     * @param request the incoming request
     * @param response the outgoing response
     * @return doesn't matter
     */
    private boolean renderTimeoutTestPage(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
    	final String wait = request.getParameter("wait");
    	int waitTime = 0;
    	if (wait != null)
    	{
	    	try
	    	{
	    		waitTime = Integer.parseInt(wait);
	    	}
	    	catch (final NumberFormatException e)
	    	{
	    		// throw an exception to "see" it from the tests are nobody would look at it if we take a default value
	    		// ant the problem appears only in server log
	    		throw new RuntimeException("Request parameter \"wait\" should be an integer", e);
	    	}
    	}
    	
    	try
		{
    		Thread.sleep(1000 * waitTime);
		}
		catch (final InterruptedException e)
		{
    		throw new RuntimeException("Sleep (" + waitTime + "s) interrupted", e);
		}

        return renderDefault(request, response, "Canoo WebTest", "Wusi is the greatest");
	}

	private boolean renderRedirectPage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = renderBegin(response, "Redirect Test Page");
        out.println("<h3>Redirect Test Page</h3>");

        out.println("<a href=\"formTest?mode=redirectTest\">Redirected link</a>");

        out.println("<form method=\"post\" action=\"" + fServletPath + "\" name=\"RedirectFormPost\">");
        out.println("<Input type=hidden name=\"mode\" value=\"redirectTest\" />");
        out.println("<Input type=submit name=\"submitPost\" value=\"RedirectSubmitPost\" />");
        out.println("</form>");

        out.println("<form method=\"get\" action=\"" + fServletPath + "\" name=\"RedirectFormGet\">");
        out.println("<Input type=hidden name=\"mode\" value=\"redirectTest\" />");
        out.println("<Input type=submit name=\"submitGet\" value=\"RedirectSubmitGet\" />");
        out.println("</form>");

        printSubmittedParametersAndValues(out, request);

        renderEnd(out);

        return DUMMY;
    }
    private boolean renderHttpsPage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = renderBegin(response, "Https Test Page");
        out.println("<pre>");
        if (request.getUserPrincipal() == null){
            out.println("no user principal");
        }else{
            out.println("user principal name = "+request.getUserPrincipal().getName());
        }
        out.println("authentication type = "+request.getAuthType());
        out.println("</pre>");
        printSubmittedParametersAndValues(out, request);
        renderEnd(out);
        return DUMMY;
    }

    private boolean renderRedirectTarget(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PrintWriter out = renderBegin(response, "Redirect AbstractTargetAction");
        out.println("<h3>Redirect AbstractTargetAction Page</h3>");

        printSubmittedParametersAndValues(out, request);

        printHeadersAndValues(out, request);

        renderEnd(out);

        return DUMMY;
    }

    private boolean renderRedirectTest(HttpServletResponse response) throws IOException {

        response.sendRedirect(fServletPath + "?mode=redirectTarget");
        return DUMMY;
    }

    private boolean renderRedirectTestMeta(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head>");

        StringBuffer reqURL = request.getRequestURL();
        out.println("<meta http-equiv=\"refresh\" content=\"0;url=" + reqURL + "?mode=redirectTarget\" />");
        out.println("</head>");
        out.println("<body></body>");
        out.println("</html>");

        return DUMMY;
    }

    private void appendCheckBox(PrintWriter out, String name, String value, boolean checked) {
        out.println("<br>");
        out.println("<br>Check box name=" + name + ":");
        String htmlCodeBegin = "<br><input type=\""+ HtmlConstants.CHECKBOX + "\" " + (checked ? "CHECKED" : "") + " name=" + name;
        if (value == null) {
            out.println(htmlCodeBegin + ">Box [no value]");
            return;
        }
        out.println(htmlCodeBegin + " value=\"" + value + "\">Box " + value);
    }

    private void appendLink(PrintWriter out, String label, String attributes) {
        String parameters = "";
        if (attributes != null && !"".equals(attributes.trim())) {
            parameters = "?" + attributes;
        }
        out.println("<br>");
        out.println("<a href=\"" + fServletPath + parameters + "\">" + label + "</a>");
    }

    private void appendRadioButtons(PrintWriter out, String name) {
        out.println("<br>");
        out.println("<br>Radio buttons name=" + name + ":");
        out.println("<br>");
        String beginTag = "<input type=\"" + HtmlConstants.RADIO + "\" name=" + name;
        out.println(beginTag + " value=r1 checked>Radio r1");
        out.println(beginTag + " value=r2>Radio r2");
        out.println(beginTag + " value=r3>Radio r3");
    }

    private void appendSelectBox(PrintWriter out, String name, boolean isMultiSelection, List selectedValues) {
        out.println("<br>");
        out.println("<br>Select name=" + name + ":");
        out.print("<br><select name=" + name + " size=3 ");
        if (isMultiSelection) {
            out.print("multiple");
        }
        out.println(">");
        for (int i = 1; i < 5; i++) {
            appendSelectOption(out, "v" + i, "Name for v" + i, selectedValues.contains("v" + i));
        }
        out.println("</select>");
    }

    private void appendSelectOption(PrintWriter out, String value, String text, boolean isSelected) {
        out.print("<option value=\"" + value + "\" ");
        if (isSelected) {
            out.print("selected");
        }
        out.println(">" + text + "</option>");
    }

    private String getParameterAsString(String parameterName, HttpServletRequest request) {
        String result = request.getParameter(parameterName);
        return result != null ? result : "";
    }

    private List getValuesForParameter(String parameterName, HttpServletRequest request) {
        String[] result = request.getParameterValues(parameterName);
        return result != null ? Arrays.asList(result) : new ArrayList();
    }

    private void printMultipleFormsIfNeeded(PrintWriter out, HttpServletRequest request, String type) {
        if ("multiple".equals(getParameterAsString("forms", request))) {
            out.println("<hr>");
            out.println("Second form: name=2");

            out.println("<form method=" + type + " action=\"" + fServletPath + "\" name=\"2\">");
            out.println("<Input type=\"" + HtmlConstants.HIDDEN + "\" name=Dummy value=\"A Dummy Value\">");
            out.println("Text: <Input type=\"" + HtmlConstants.TEXT + "\" name=field1 value=\"" + getParameterAsString("field1", request) + "\" >");
            List selectedValues = new ArrayList();
            selectedValues.add("v1");
            appendSelectBox(out, "select1", false, selectedValues);
            appendRadioButtons(out, "radio");
            appendCheckBox(out, "check", "c1", false);
            appendLink(out, "link", "link=l1&amp;radio=r1");
            appendLink(out, "link", "link=l2&amp;radio=r1");
            out.println("<br>");
            out.println("<a href=\"ReadMe.pdf\">pdf document</a>");
            out.println("<br>");
            out.println("<a href=\"WebTest.doc\">word document</a>");
            out.println("<br>");
            out.println("<br><Input type=submit name=first  value=doItAgain>");
            out.println("<br><Input type=submit name=second value=doItAgain></form>");

            out.println("<hr>");
            out.println("Third form: name=3");

            out.println("<form method=" + type + " action=\"" + fServletPath + "\" name=\"3\">");
            out.println("<Input type=hidden name=Dummy value=\"A Dummy Value\">");
            out.println("<Input type=hidden name=mode value=getTest>");
            out.println("Text: <Input type=text name=field2 value=\"" + getParameterAsString("field2", request) + "\" >");
            appendSelectBox(out, "select1", false, new ArrayList());
            out.println("<br><Input type=submit name=third value=doItAgain>");
            out.println("<br><Input type=submit value=doItAgain3></form>");
        }
    }

    private void printSubmittedParametersAndValues(PrintWriter out, HttpServletRequest request) {
        printSubTitle(out, "Submitted parameter names and values:");
        Enumeration en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String name = getAndPrintNextKey(en, out);
            for (int i = 0; i < request.getParameterValues(name).length; i++) {
                out.print(" " + request.getParameterValues(name)[i]);
            }
            out.println("<br>");
        }
        String fillSession = getParameterAsString("fillsession", request);
        if (fillSession != null && fillSession.length() > 0) {
            request.getSession().setAttribute(fillSession, fillSession);
        }
        String withSession = getParameterAsString("session", request);
        if (!"YES".equalsIgnoreCase(withSession)) {
            return;
        }
        printSubTitle(out, "Session values");
        en = request.getSession().getAttributeNames();
        while (en.hasMoreElements()) {
            String name = getAndPrintNextKey(en, out);
            out.print(" " + request.getSession().getAttribute(name));
        }
    }

    private void printHeadersAndValues(PrintWriter out, HttpServletRequest request) {
        printSubTitle(out, "Header names and values");
        Enumeration en = request.getHeaderNames();
        while (en.hasMoreElements()) {
            String name = getAndPrintNextKey(en, out);
            Enumeration values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                out.print(" " + values.nextElement().toString());
            }
            out.println("<br>");
        }
    }

    private String getAndPrintNextKey(Enumeration en, PrintWriter out) {
        String name = (String) en.nextElement();
        out.print(name + " =");
        return name;
    }

    private void printSubTitle(PrintWriter out, String subtitle) {
        out.println("<hr>");
        out.println(subtitle);
        out.println(": ");
        out.println("<br>");
        out.println("<br>");
    }

    private boolean renderCheckboxTest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = renderBegin(response, "CheckboxTest");

        out.println("<form method=POST name=\"checkboxForm\" action=\"" + fServletPath + "?mode=checkboxTest\">");

        out.println("<INPUT value=\"Testfield without Type\" style=\" HEIGHT: 22px\" maxLength=\"30\" size=\"34\" name=\"input\"><BR>");

        appendCheckBox(out, "c1", "check1", getParameterAsString("c1", request).length() > 0);
        appendCheckBox(out, "c2", "check2", getValuesForParameter("c2", request).contains("check2"));
        appendCheckBox(out, "c2", "check3", getValuesForParameter("c2", request).contains("check3"));
        appendCheckBox(out, "c3NoValue", null, getParameterAsString("c3NoValue", request).length() > 0);

        out.println("<BR/><H2>The Multiselection list</H2>");
        String selectName = "MultiSelect";

        List selectedValues = getMultiValueParameterList(request, selectName);
        appendSelectBox(out, selectName, true, selectedValues);

        out.println("<BR/><H2>The Single Selection list</H2>");
        selectName = "CBoxSelect";

        selectedValues = getMultiValueParameterList(request, selectName);
        appendSelectBox(out, selectName, true, selectedValues);


        out.println("<br><Input type=submit value=doIt>");

        out.println("</form>");

        printSubmittedParametersAndValues(out, request);
        renderEnd(out);
        return DUMMY;
    }

    private List getMultiValueParameterList(HttpServletRequest request, String parameterName) {

        if (request.getParameterValues(parameterName) != null) {
            return Arrays.asList(request.getParameterValues(parameterName));
        }

        return new ArrayList();
    }

    private boolean renderLinkTest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = renderBegin(response, "LinkTest");

        String linkStatus = getParameterAsString("linkStatus", request);

        if ("startGood".equals(linkStatus)) {
            appendLink(out, "Page with good Link", "mode=linkTest&linkStatus=stillGood");
        }
        if ("stillGood".equals(linkStatus)) {
            appendLink(out, "Page with bad Link", "mode=linkTest&linkStatus=bad");
        }
        if ("bad".equals(linkStatus)) {
            out.println("<A HREF=\"youwillnotfindthis.htm\">Bad Link</A>");
        }
        if ("external".equals(linkStatus)) {
            out.println("<A HREF=\"http://www.canoo.com\">Canoo.com</A>");
        }
        if ("special".equals(linkStatus)) {
            out.println("<A HREF=\"ftp://youwillnotfindthisserver.com\">Bad FTP Address</A><BR>");
            out.println("<A HREF= \"mailto:feedback-online@canoo.com\">Canoo-Mail</A>");
        }
        printSubmittedParametersAndValues(out, request);
        renderEnd(out);
        return DUMMY;
    }

    private boolean renderTableTest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = renderBegin(response, "TableTest");
        out.println("<table id=\"test\" border=\"1\">");
        for (int i = 0; i < 3; i++) {
            out.println("<tr>");
            for (int j = 0; j < 3; j++) {
                out.println("<td>");
                out.println("" + i + ":" + j);
                out.println("</td>");
            }
            out.println("</tr>");
        }
        out.println("</table>");

        printSubmittedParametersAndValues(out, request);
        renderEnd(out);
        return DUMMY;
    }

    private boolean renderDemoSuccessPage(HttpServletResponse response, String title) throws IOException {
        PrintWriter out = renderBegin(response, title);
        out.println("<h1>Welcome to my home page!</h1>");
        renderEnd(out);
        return DUMMY;
    }

    private boolean renderDemoLoginPage(HttpServletResponse response, String title) throws IOException {
        PrintWriter out = renderBegin(response, title);

        // Render form with image buttons
        out.println("<h1>Please login</h1>");
        out.println("<form method=POST action=\"" + fServletPath + "\">");
        out.println("<Input type=hidden name=\"mode\" value=\"demoLogin\">");
        out.println("<br>Username:<Input type=text value=\"\" name=\"username\" >");
        out.println("<br>Password:<Input type=password value=\"\" name=\"password\" >");
        out.println("<br>&nbsp;<br><Input type=submit value=Login></form>");

        renderEnd(out);
        return DUMMY;
    }

//    private void writeButtonTestImage(PrintWriter out, String value, String buttonNr, String formNr) {
//        out.println("<Input type=image src=\"seibertec.jpg\" value=\"" + value + "\" name=\"imageButton" + buttonNr + "\" " +
//                "alt=\"value=" + value + " name=imageButton" + buttonNr + " form=form" + formNr + "\" />");
//    }

    private boolean renderInvalidHtml(HttpServletRequest request, HttpServletResponse response, String title) throws IOException {
        PrintWriter out = renderBegin(response, title);

        // Invalid HTML with tr/td outside of table
        out.println("<tr><td><INPUT type=text name=field1 value=\"TD in body\">");

        out.println("<form method=POST action=\"" + fServletPath + "\">");
        out.println("<br>Text (valid): <Input type=text name=field2 value=\"" + getParameterAsString("field1", request) + "\" >");
        out.println("<br><Input type=submit value=doIt></form>");

        printSubmittedParametersAndValues(out, request);

        renderEnd(out);
        return DUMMY;
    }

    private boolean renderDefault(HttpServletRequest request, HttpServletResponse response, String title, String text) throws IOException {

        PrintWriter out = renderBegin(response, title);
        out.println("<h1>" + text + "</h1>");

        appendLink(out, "MyLink", "mode=getTest&amp;field1=MyLink");
        appendLink(out, "<img src=\"seibertec.jpg\" alt=\"imagelink\">", "image=clicked");

        out.println("<br>");

        appendLink(out, "Multiple forms", "mode=getTest&forms=multiple");

        out.println("<br>");
        out.println("<br>");
        out.println("<form action=\"\">");

        out.println("<textarea name=\"theArea\">");
        out.println("Wusi was here ...");
        out.println("</textarea>");
        out.println("<br>");
        out.println("<textarea name=\"theOtherArea\">");
        out.println("and here.");
        out.println("</textarea>");
        out.println("<br>");
        out.println("<textarea name=\"theOtherArea\" id=\"textarea2\">");
        out.print("Wusi was here as well...");
        out.println("</textarea>");
        out.println("</form>");

        printSubmittedParametersAndValues(out, request);

        out.print("<br><a href=\"");
        String sessionUrlEncodedLink = request.getContextPath() + request.getServletPath() + "?" + request.getQueryString();
        out.print(response.encodeRedirectURL(sessionUrlEncodedLink));
        out.println("\">Url encoded back link</a>");


        printHeadersAndValues(out, request);

        if ("true".equals(request.getParameter("brokenlink"))) {
            out.println("<br><a href=\"youwillnotfindthis.html\">a broken link</a><br>");
        }
        renderEnd(out);
        return DUMMY;
    }

    private boolean renderForm(HttpServletRequest request, HttpServletResponse response, String type, String nextMode) throws IOException {
        PrintWriter out = renderBegin(response, "SelfTest");
        out.println("<h1>SelfTest</h1>");

        out.println("<form method=" + type + " action=\"" + fServletPath + "\">");
        out.println("<Input type=hidden name=mode value=" + nextMode + ">");
        out.println("<Input type=hidden name=Dummy value=\"A Dummy Value\">");
        out.println("<br>Text: <Input type=text name=field1 value=\"" + getParameterAsString("field1", request) + "\" >");
        out.println("<br><Input type=submit value=doIt></form>");

        printMultipleFormsIfNeeded(out, request, type);

        renderEnd(out);
        return DUMMY;
    }

    private boolean renderFormsBetweenTDs(HttpServletResponse response) throws IOException {
        PrintWriter out = renderBegin(response, "SelfTest");
        out.println("<h1>SelfTest</h1>");

        out.println("<table><tr><td>first form with button named bla : </td>");
        out.println("<form method=GET action=\"" + fServletPath + "\">");
        out.println("<td><Input type=submit name=bla value=doIt></td></form>");

        // JTidy reads one form with both buttons until here (but there should only be one)

        out.println("<br> second form with button named clickme: <form method=GET action=\"" + fServletPath + "\">");
        out.println("<br><Input type=submit name=clickme value=doIt></form>");

        renderEnd(out);
        return DUMMY;
    }

    private void renderEnd(PrintWriter out) {
        out.println("</body>");
        out.println("</html>");
    }

    private PrintWriter renderBegin(HttpServletResponse response, String title) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1 id=\"headingId\">title</h1>");
        return out;
    }

    /**
     * Renders information on the uploaded file
     */
	private boolean renderUploadInfoPage(final HttpServletRequest request, 
			final HttpServletResponse response) throws IOException, ServletException {

        final PrintWriter out = renderBegin(response, "Upload Test Page");
        out.println("<h3>Upload Test Page</h3>");

        final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart)
        {
        	out.println("Request is not multipart");
            renderEnd(out);
            return DUMMY;
        }

        // Create a factory for disk-based file items
        final FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        final ServletFileUpload upload = new ServletFileUpload(factory);
        
        // Parse the request
        final List items;
        try
        {
        	items = upload.parseRequest(request);
        }
        catch (final FileUploadException e)
        {
        	throw new ServletException(e);
        }
        out.println(items.size() + " uploaded file(s):");
        out.println("<ul>");
        for (final ListIterator iter = items.listIterator(); iter.hasNext();)
		{
			final FileItem item = (FileItem) iter.next();
			if (item.isFormField())
				continue; // normal form field, not an uploaed file
			final String idSuffix = item.getFieldName();
			out.println("<li id='file_" + idSuffix + "'>");
			renderLineWithSpan(out, "Content type", "cntType_" + idSuffix, item.getContentType());
			renderLineWithSpan(out, "Field name", "fieldName_" + idSuffix, item.getFieldName());
			renderLineWithSpan(out, "File name", "fileName_" + idSuffix, item.getName());
			renderLineWithSpan(out, "File size", "fileSize_" + idSuffix, String.valueOf(item.getSize()));
			renderLineWithSpan(out, "MD5", "md5_" + idSuffix, computeMD5(item.get()));
			out.println("</li>");
		}
        out.println("</ul>");

        renderEnd(out);

        return DUMMY;
    }

	private void renderLineWithSpan(final PrintWriter _out, final String _label, final String _id, final String _value)
	{
		_out.println(_label + ": <span id='" + _id + "'>" + _value + "</span>");
	}

	private String computeMD5(final byte[] _bs)
	{
        MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("MD5");
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
        digest.update(_bs);
        final byte[] md5 = digest.digest();
        
		return new String(Hex.encodeHex(md5));
	}
}
