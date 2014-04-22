<?xml version="1.0" encoding="us-ascii" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:output
			method="html"
			encoding="us-ascii"
			indent="yes"
			doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
			doctype-system="http://www.w3.org/TR/html4/loose.dtd"
			/>
	<xsl:template match="text()"/>

	<xsl:template match="/">
		<html lang="en">
			<head>
				<link rel="shortcut icon" type="image/x-icon" href="favicon.ico"/>
				<link rel="icon" type="image/x-icon" href="favicon.ico"/>
				<link rel="stylesheet" type="text/css" href="website_base.css"/>
				<title>All WebTest Steps Sidebar</title>
			</head>
			<body>
				<ul style="list-style: none; margin:0 5px 1em 5px; padding:0;">
					<xsl:apply-templates select="//dt/stepref">
						<xsl:sort select="@name"/>
					</xsl:apply-templates>
				</ul>
			</body>
		</html>
	</xsl:template>
	<xsl:template match="stepref">
		<li>
			<a target="_content" href="{@name}.html">
				<xsl:value-of select="@name"/>
			</a>
		</li>
	</xsl:template>
</xsl:stylesheet>