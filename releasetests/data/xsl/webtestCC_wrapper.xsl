<?xml version="1.0"?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY nbsp "&#160;">
	<!ENTITY copy "&#xa9;">
	]>
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
   xmlns:xslt="http://xml.apache.org/xslt"
	>
	<xsl:import href="../../../webtestCC.xsl"/>

	<xsl:output
		method="html"
	   encoding="us-ascii"
	   indent="yes"
	   doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
	   doctype-system="http://www.w3.org/TR/html4/loose.dtd"
		/>

	<xsl:template match="/">
		<!-- following code fragment derived from servlet's template -->
		<html lang="en">
			<head>
				<link rel="alternate" type="application/rss+xml" title="Canoo WebTest Build Status"
				      href="/buildstatus.rss"/>
				<link href="website_base.css" type="text/css" rel="stylesheet"/>
				<link href="website_cc.css" type="text/css" rel="stylesheet"/>

				<meta name="description" content=""/>
				<meta name="keywords" content="WebTest, Cruise Control, Build"/>
				<title>WebTest Build Info</title>
			</head>

			<body>
				<div id="page">
					<img alt="Canoo Webtest" height="90" width="955" usemap="#headerLinks" src="images/header.gif"
					     id="header"/>
					<map name="headerLinks">
						<area alt="WebTest" href="http://webtest.canoo.com/" coords="35px,35px,200px,70px" shape="rect"/>
						<area alt="Canoo" href="http://www.canoo.com/" coords="770px,35px,856px,70px" shape="rect"/>
					</map>

					<div id="navigation-top">
						<ul>
							<li>
								<a href="http://webtest.canoo.com/webtest/manual/WebTestHome.html">Home</a>
							</li>
							<li>
								<a href="http://webtest.canoo.com/webtest/manual/manualOverview.html">Manual</a>
							</li>
							<li>
								<a href="http://webtest.canoo.com/webtest/manual/buildOverview.html">Build Info</a>
							</li>
							<li>
								<a href="http://webtest.canoo.com/buildservlet/BuildServlet" class="selected">Cruise Control</a>
							</li>
						</ul>
					</div>
					<table cellpadding="0" cellspacing="0" border="0" rules="none" frame="void" id="main">
						<col width="190"/>
						<col width="570"/>
						<col width="189"/>
						<tr>
							<td id="main-left" valign="top">
								<div id="search"></div>

								<div id="navigation-left">
									<ul>
										<li>
											<a href="/buildservlet/BuildServlet?log=log20051124130017LR_1035">
												24.11.2005&nbsp;13:00:00&nbsp;(R_1035)</a>
										</li>
									</ul>
									<form method="GET" action="/buildservlet/BuildServlet">
										<select name="log" onchange="form.submit()" >
											<option value="log20051124130017LR_1034">
												24.11.2005&nbsp;13:00:00&nbsp;(R_1034)
											</option>
											<option value="log20051124130017LR_1033">
												24.11.2005&nbsp;13:00:00&nbsp;(R_1033)
											</option>
											<option value="log20051124130017LR_1032" selected="selected">
												24.11.2005&nbsp;13:00:00&nbsp;(R_1032)
											</option>
										</select>
									</form>
								</div>

								<div class="filler"></div>
							</td>
							<td id="main-content" valign="top">
								<xsl:apply-imports/>
							</td>
							<td id="main-right" valign="top">
								<h1>more</h1>
								<p>
									<a href="/buildstatus.rss">
										<img src="/fisheye/static/images/rss20feed.png" width="80" height="15"
										     alt="RSS 2.0"/>
										<xsl:text>&nbsp;feed</xsl:text>
									</a>
								</p>
							</td>
						</tr>
					</table>
					<div id="copyright">Copyright &copy; 2002-2005
						<br/>
						Canoo Engineering AG
						<br/>
						Kirschgartenstrasse 7
						<br/>
						CH - 4051 Basel
						<br/>
						Phone: +41 61 228 94 44
						<br/>
						All Rights Reserved
					</div>
				</div>

				<div class="filler"></div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>