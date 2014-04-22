// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.self;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.security.Principal;

/**
 * @author Dierk König, Carsten Seibert *
 */

public class CertificateSnoopServlet extends HttpServlet {
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        Principal principal = httpServletRequest.getUserPrincipal();
        Writer out = httpServletResponse.getWriter();
        httpServletResponse.setContentType("text/plain");

        if (principal == null) {
            out.write("no prinicipal");
        } else {
            out.write("principal name = " + principal.getName());
        }
        out.close();
    }
}