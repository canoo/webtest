<%@page import="javax.servlet.http.Cookie" %>
<html>
<head>
<title>Visit Counter Information as at <%= new java.util.Date() %></title>
</head>
<body>
<h1>
Simple JSP Counter
</h1>
<%! static int hitCount; %>
<%
if (request.getParameter("reset") != null) {
  hitCount = 0;
} else {
  int visitCount = 1;
  Cookie[] ca = request.getCookies();
  if (ca == null) {
    ca = new Cookie[0];
  }
  for (int x = 0; x < ca.length; x++) {
    if ("visitCount".equals(ca[x].getName())) {
      visitCount = Integer.parseInt(ca[x].getValue()) + 1;
    }
  }
  response.addCookie(new Cookie("visitCount", "" + visitCount));
%>
<p id="userinfo">
You have accessed this page <%= visitCount %> time(s).
</p>
<p id="totalinfo">This page has been accessed a total of <%= ++hitCount %> time(s).</p>
<%
}
%>
</body>
</html>
