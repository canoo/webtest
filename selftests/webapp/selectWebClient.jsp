<html>
<head>
<title>SelectWebClient Test</title>
</head>
<body>

<%
Integer nbVisit = (Integer) session.getAttribute("nbVisit");
if (nbVisit == null)
	nbVisit = new Integer(1);

session.setAttribute("nbVisit", new Integer(nbVisit.intValue() + 1));
%>

This is your visit number <%=nbVisit%> to this page
</body>
</html>