<html>
<head><title>Always set a cookie</title></head>
<body>
<%
       Cookie mycookie = new Cookie("X-APPLET","YES");
       response.addCookie(mycookie);
 %>
 <a href="applet.html">Applet</a>
</body>
</html>