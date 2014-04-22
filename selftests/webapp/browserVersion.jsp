<html>
<head>
<title>Browser version</title>
</head>
<body>
User-Agent Header: <span id="userAgentHeader"><%=request.getHeader("User-Agent")%></span><br/>
Browser details:
<ul>
<li>appName: <span id="appName"></span></li>
<li>product: <span id="product"></span></li>
<li>userAgent: <span id="userAgent"></span></li>
</ul>
<script>
var properties = ["appName", "product", "userAgent"];
for (var i=0; i<properties.length; ++i)
{
	var prop = properties[i];
	document.getElementById(prop).innerHTML = navigator[prop];
}
</script>
</body>
</html>
