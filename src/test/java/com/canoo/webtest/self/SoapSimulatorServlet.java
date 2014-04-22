// Copyright © 2004-2007 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.self;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Simulate output from a SOAP Web Service so we don't have to install an
 * entire SOAP stack just for a single test.
 *
 * @author Paul King
 */
public class SoapSimulatorServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String action = request.getHeader("SOAPAction");
        if (action != null && action.length() > 0) {
            response.setContentType("text/xml");
            PrintWriter out = response.getWriter();
            out.println("<soap:Envelope>\n" +
                    "<soap:Body>\n" +
                    "<GetAustralianPostCodeByLocationResponse>\n" +
                    "<GetAustralianPostCodeByLocationResult>\n" +
                    "<NewDataSet>\n" +
                    "<Table>\n" +
                    "<Location> Karalee </Location>\n" +
                    "<PostCode> QLD 4306 </PostCode>\n" +
                    "</Table>\n" +
                    "</NewDataSet>\n" +
                    "</GetAustralianPostCodeByLocationResult>\n" +
                    "</GetAustralianPostCodeByLocationResponse>\n" +
                    "</soap:Body>\n" +
                    "</soap:Envelope>");
        }
    }
}
