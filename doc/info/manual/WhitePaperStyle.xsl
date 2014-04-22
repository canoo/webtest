<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE xsl:stylesheet [
		<!ENTITY space "&#32;">
		<!ENTITY nbsp "&#160;">
		<!ENTITY jira "http://webtest-community.canoo.com/jira/">
		]>

<xsl:stylesheet
		version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:common="http://exslt.org/common"
		extension-element-prefixes="common"
		>
	<xsl:output
			method="html"
			encoding="UTF-8"
			indent="yes"
			doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
			doctype-system="http://www.w3.org/TR/html4/loose.dtd"
			/>


	<xsl:param name="buildnumber"/>
	<xsl:param name="today"/>
	<xsl:param name="include.dir" select="'.'"/>

	<xsl:variable name="references" select="document('references.xml')"/>

	<xsl:template match="buildNumber">
		<xsl:value-of select="$buildnumber"/>
	</xsl:template>

	<xsl:template match="today">
		<xsl:value-of select="$today"/>
	</xsl:template>

	<xsl:template name="ols-styles">
		<link rel="stylesheet" type="text/css" href="website_base.css"/>
		<link rel="stylesheet" type="text/css" href="website_part.css"/>
	</xsl:template>

	<xsl:template name="styles"> <!-- do not inline, needed as template method -->
		<xsl:call-template name="ols-styles"/>
	</xsl:template>

	<xsl:template match="/">
		<html lang="en">
			<head>
				<link rel="shortcut icon" type="image/x-icon" href="favicon.ico"/>
				<link rel="icon" type="image/x-icon" href="favicon.ico"/>
				<xsl:apply-templates select="/manpage/site/level1" mode="link"/>

				<xsl:call-template name="styles"/>

				<meta name="description">
					<xsl:attribute name="content">
						<xsl:value-of select="normalize-space(manpage/head)"/>
					</xsl:attribute>
				</meta>
				<meta name="keywords">
					<xsl:attribute name="content">Canoo,
						WebTest, web application testing, automatic, XP, ANT, httpunit, htmlunit, groovy,
						acceptance testing, extreme programming, agile development,
						<xsl:apply-templates select="manpage/section" mode="keywords"/>
					</xsl:attribute>
				</meta>
				<title>
					<xsl:value-of select="translate(manpage/head/@title,'|',':')"/>
				</title>
			</head>
			<body>
				<div id="page">
					<img class="mosaic" src="images/header.gif" usemap="#headerLinks" width="949" height="89"
						 alt="Canoo Webtest"/>
					<map name="headerLinks">
						<area shape="rect" coords="35px,35px,200px,70px" href="http://webtest.canoo.com/"
							  alt="WebTest"/>
						<area shape="rect" coords="770px,35px,856px,70px" href="http://www.canoo.com/" alt="Canoo"/>
					</map>

					<div id="navigation-top">
						<ul>
							<xsl:apply-templates select="/manpage/site/level1"/>
						</ul>
					</div>

					<table id="main" frame="void" rules="none" border="0" cellspacing="0" cellpadding="0"
						   style="border-collapse: collapse;">
						<col width="190"/>
						<col width="569"/>
						<col width="189"/>
						<xsl:choose>
							<xsl:when test="/manpage/@siteid='Home'">
								<tr valign="top">
									<!-- height is the total height of the two img -->
									<td valign="top" colspan="2" height="161">
										<img class="mosaic"
											 src="images/teaser.jpg" width="668" height="116"
											 alt="The Most Effective Way to Test Your WebApp!"
												/>
										<img class="mosaic"
											 src="images/motto.gif" width="421" height="45"
											 alt="Free testing of web apps"
												/>
									</td>
									<xsl:call-template name="main-right">
										<xsl:with-param name="span" select="3"/>
									</xsl:call-template>
								</tr>
								<tr valign="top">
									<xsl:call-template name="main-left"/>
									<xsl:call-template name="main-content"/>
								</tr>
							</xsl:when>
							<xsl:otherwise>
								<tr valign="top">
									<xsl:call-template name="main-left"/>
									<xsl:call-template name="main-content"/>
									<xsl:call-template name="main-right">
										<xsl:with-param name="span" select="2"/>
									</xsl:call-template>
								</tr>
							</xsl:otherwise>
						</xsl:choose>
						<tr valign="top">
							<td valign="bottom" class="main-left">
								<xsl:call-template name="canooFooter"/>
							</td>
						</tr>
					</table>

				</div>
				<!-- setting a bottom margin on div#page would be more elegant, but ie flushes then div#page left -->
				<div class="filler"/>
			</body>
		</html>
	</xsl:template>

	<xsl:template name="main-left">
		<td valign="top" class="main-left">
			<div id="search">
				<form action="http://www.google.de/search" id="searchForm">
					<input type="hidden" name="sitesearch" value="webtest.canoo.com"/>
					<input type="submit" id="searchButton" name="go" value="Search" title="Search" alt="Search"/>
					<input type="text" id="searchText" name="q" onfocus="this.select();" title="Search"/>
				</form>
			</div>
			<div id="navigation-left">
				<ul>
					<xsl:apply-templates select="/manpage/site/level1[.//@name=/manpage/@siteid]/level2"/>
				</ul>
<div style="margin-top: 20px">
<a style="border-bottom: none; padding-left: 40px" href="http://webtestrecorder.canoo.com/">
<img alt="WebTestRecorder" src="images/WebTestRecorderFirefox.gif" style="position:absolute; left: 3px"/>
WebTestRecorder<br/> the Firefox Extension for WebTest</a>
</div>
			</div>
			<!-- make sure that the copyright is never too close to the navigation. -->
			<div class="filler"/>
		</td>
	</xsl:template>

	<xsl:template name="main-content">
		<td valign="top" rowspan="2">
			<div id="main-content">
				<xsl:apply-templates select="manpage/head"/>
				<xsl:apply-templates select="manpage/section"/>
			</div>
		</td>
	</xsl:template>

	<xsl:template name="main-right">
		<xsl:param name="span"/>
		<td valign="top" id="main-right">
			<xsl:if test="$span>1">
				<xsl:attribute name="rowspan">
					<xsl:value-of select="$span"/>
				</xsl:attribute>
			</xsl:if>

			<h1>
				<img src="images/news.gif" alt="news" height="18" width="97"/>
			</h1>
			<!-- To be included on every page-->
			<xsl:apply-templates select="document('_news.xml')/news"/>
			<!-- To be included on a single page ?
							<xsl:apply-templates select="document(concat(/manpage/@siteid, '_news.xml'))/news" />
						-->
			<div id="blogs">
				<h3>Committers' blogs</h3>
				<ul>
					<li>
						<a href="http://www.jroller.com/page/dna">Denis N. Antonioli's blog</a>
					</li>
					<li>
						<a href="http://www.amazon.com/gp/blog/A368TUB0Q1IE3F/103-4309468-8306223">Dierk König's blog
						</a>
					</li>
					<li>
						<a href="http://mguillem.wordpress.com/">Marc Guillemot's blog</a>
					</li>
				</ul>
			</div>
		</td>
	</xsl:template>

	<xsl:template match="level1" mode="link">
		<link rel="chapter" href="{@url}" title="{@name}"/>
		<xsl:apply-templates select="level2[current()//@name=/manpage/@siteid]" mode="link"/>
	</xsl:template>

	<xsl:template match="level2" mode="link">
		<link rel="section" href="{@url}" title="{@name}"/>

		<xsl:if test=".//@name=/manpage/@siteid">
			<xsl:choose>
				<xsl:when test="level3">
					<xsl:for-each select="level3">
						<link rel="subsection" href="{@url}" title="{@name}"/>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:for-each select="/manpage/section">
						<link rel="subsection" href="{current()/@url}#{@title}" title="{@title}"/>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>

	<xsl:template match="level1">
		<li>
			<a href="{@url}">
				<xsl:if test=".//@name=/manpage/@siteid">
					<xsl:attribute name="class">selected</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="@name"/>
			</a>
		</li>
	</xsl:template>

	<xsl:template match="level2">
		<li>
			<a href="{@url}">
				<xsl:if test=".//@name=/manpage/@siteid">
					<xsl:attribute name="class">selected</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="@name"/>
			</a>
			<xsl:if test=".//@name=/manpage/@siteid">
				<xsl:variable name="thirdLevel">
					<xsl:choose>
						<xsl:when test="level3">
							<xsl:apply-templates select="level3"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="/manpage/section" mode="toc"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:if test="count(common:nodeSet($thirdLevel)/*) > 0">
					<ul>
						<xsl:copy-of select="$thirdLevel"/>
					</ul>
				</xsl:if>
			</xsl:if>
		</li>
	</xsl:template>

	<xsl:template match="level3">
		<li>
			<a href="{@url}">
				<xsl:if test=".//@name=/manpage/@siteid">
					<xsl:attribute name="class">selected</xsl:attribute>
				</xsl:if>
				<xsl:text>&gt;&space;</xsl:text>
				<xsl:value-of select="@name"/>
			</a>
		</li>
	</xsl:template>

	<!-- skip 3rd level if there is a single entry -->
	<xsl:template match="/manpage/section[count(preceding-sibling::section)+count(following-sibling::section)=0]"
				  mode="toc"/>

	<xsl:template name="canooFooter">
		<div id="copyright">
			<xsl:text>Copyright &#169; 2002-2008</xsl:text><br/>
			<xsl:text>Canoo Engineering AG</xsl:text><br/>
			<xsl:text>Kirschgartenstrasse 7</xsl:text><br/>
			<xsl:text>CH - 4051 Basel</xsl:text><br/>
			<xsl:text>Phone: +41 61 228 94 44</xsl:text><br/>
			<xsl:text>All Rights Reserved</xsl:text>
		</div>
	</xsl:template>

	<xsl:template match="head">
		<h1>
			<xsl:choose>
				<xsl:when test="contains(@title,'|')">
					<span class="category">
						<xsl:value-of select="substring-before(@title,'|')"/>
					</span>
					<xsl:value-of select="substring-after(@title,'|')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="@title"/>
				</xsl:otherwise>
			</xsl:choose>
		</h1>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="b">
		<p>
			<xsl:apply-templates/>
		</p>
	</xsl:template>

	<xsl:template match="img">
		<xsl:element name="img">
			<xsl:attribute name="src">
				<xsl:value-of select="@src"/>
			</xsl:attribute>
			<xsl:if test="text()">
				<xsl:attribute name="alt">
					<xsl:value-of select="text()"/>
				</xsl:attribute>
			</xsl:if>
		</xsl:element>
	</xsl:template>

	<xsl:template match="code">
		<div class="code">
			<xsl:if test="string-length(@caption)!=0">
				<div class="caption">
					<xsl:value-of select="@caption"/>
				</div>
			</xsl:if>
			<div class="content">
				<xsl:apply-templates/>
			</div>
		</div>
	</xsl:template>

	<xsl:template match="include">
		<xsl:apply-templates select="document(concat($include.dir, '/', @src))/source"/>
	</xsl:template>

	<xsl:template match="t">
		<xsl:text>&nbsp;&nbsp;</xsl:text>
	</xsl:template>

	<xsl:template match="tag">
		<span class="tag">
			<xsl:apply-templates/>
		</span>
	</xsl:template>

	<xsl:template match="att">
		<span class="att">
			<xsl:apply-templates/>
		</span>
	</xsl:template>

	<xsl:template match="val">
		<xsl:text>="</xsl:text>
		<span class="val">
			<xsl:apply-templates/>
		</span>
		<xsl:text>"</xsl:text>
	</xsl:template>

	<xsl:template match="s">
		<xsl:text>"</xsl:text>
		<span class="val">
			<xsl:apply-templates/>
		</span>
		<xsl:text>"</xsl:text>
	</xsl:template>

	<xsl:template match="n">
		<br/>
	</xsl:template>

	<xsl:template match="dl">
		<dl>
			<xsl:apply-templates/>
		</dl>
	</xsl:template>

	<xsl:template match="li/dt">
		<dt>
			<xsl:if test="@style">
				<xsl:attribute name="class">
					<xsl:value-of select="@style"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates/>
		</dt>
	</xsl:template>

	<xsl:template match="li/dd">
		<dd>
			<xsl:apply-templates/>
		</dd>
	</xsl:template>

	<xsl:template match="file">
		<a href="/webtest/{string(.)}">
			<xsl:value-of select="string(.)"/>
		</a>
	</xsl:template>

	<xsl:template match="em">
		<i>
			<xsl:apply-templates/>
		</i>
	</xsl:template>

	<xsl:template match="ext">
		<a target="_blank" title="external link: opens in new window" class="ext">
			<xsl:attribute name="href">
				<xsl:choose>
					<xsl:when test="@base='jira'">&jira;</xsl:when>
				</xsl:choose>
				<xsl:value-of select="@href"/>
			</xsl:attribute>
			<xsl:apply-templates/>
		</a>
	</xsl:template>

	<xsl:template match="example">
		<span class="example">
			<xsl:apply-templates/>
		</span>
	</xsl:template>

	<xsl:template match="posted">
		<br/>
		<span class="posted">
			<xsl:text>Posted:&space;</xsl:text>
			<xsl:apply-templates/>
		</span>
	</xsl:template>

	<xsl:key name="citekey" match="/references/ref/cite" use="@id"/>

	<xsl:template match="citeref">
		<xsl:variable name="refFor" select="string(.)"/>
		<!--
				for-each is used here to change to the context of $references
				because key() matches in the current context only.
				-->
		<xsl:for-each select="$references">
			<xsl:variable name="id" select="key('citekey', $refFor)"/>
			<xsl:choose>
				<xsl:when test="$id">
					<xsl:apply-templates select="$id"/>
				</xsl:when>
				<xsl:otherwise>
					<br/>
					<span class="conversionError">
						<xsl:text>NO CITEREF FOUND FOR "</xsl:text>
						<xsl:apply-templates/>
						<xsl:text>"</xsl:text>
					</span>
					<br/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="cite">
		<blockquote>
			<p>
				<xsl:apply-templates/>
			</p>
			<p class="author">
				<xsl:value-of select="parent::ref/author[1]/@name"/>
				<br/>
				<xsl:value-of select="parent::ref/author[1]/occupation[1]"/>
			</p>
		</blockquote>
	</xsl:template>

	<xsl:key name="refkey" match="/references/ref" use="@id"/>

	<xsl:template match="ref">
		<a href="annotatedRefs.html#{@id}" title="{note[1]}">
			<xsl:value-of select="@title"/>
		</a>
	</xsl:template>

	<xsl:template match="key">
		<xsl:variable name="refFor" select="string(.)"/>
		<xsl:for-each select="$references">
			<xsl:variable name="id" select="key('refkey', $refFor)"/>
			<xsl:choose>
				<xsl:when test="$id">
					<xsl:apply-templates select="$id"/>
				</xsl:when>
				<xsl:otherwise>
					<br/>
					<span class="conversionError">
						<xsl:text>NO KEYREF FOUND FOR "</xsl:text>
						<xsl:apply-templates/>
						<xsl:text>"</xsl:text>
					</span>
					<br/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="section" mode="toc">
		<li>
			<a href="#{@title}">
				<xsl:text>&gt;&space;</xsl:text>
				<xsl:value-of select="@title"/>
			</a>
		</li>
	</xsl:template>

	<xsl:template match="section" mode="keywords">
		<xsl:value-of select="@title"/>
		<xsl:text>,</xsl:text>
	</xsl:template>

	<xsl:template match="section">
		<h2>
			<a name="{@title}">
				<xsl:value-of select="@title"/>
			</a>
		</h2>
		<xsl:apply-templates/>
	</xsl:template>

	<!-- special matching to generate all annotated references in one page with minimal changes -->
	<xsl:template match="section[@title = 'Annotated References']">
		<dl class="ref">
			<xsl:apply-templates select="$references/references/ref[count(cite) = 0]" mode="list">
				<xsl:sort select="@title"/>
			</xsl:apply-templates>
		</dl>
	</xsl:template>

	<xsl:template match="ref" mode="list">
		<dt>
			<a name="{@id}">
				<xsl:value-of select="@title"/>
			</a>
		</dt>
		<xsl:apply-templates select="link" mode="list"/>
		<xsl:apply-templates select="note" mode="list"/>
	</xsl:template>

	<xsl:template match="link" mode="list">
		<dd class="link">
			<a href="{@href}">
				<xsl:value-of select="@href"/>
			</a>
		</dd>
	</xsl:template>

	<xsl:template match="note" mode="list">
		<dd>
			<xsl:value-of select="."/>
		</dd>
	</xsl:template>

	<xsl:template match="sidebar">
		<a href="{@name}.html" rel="sidebar">
			<xsl:value-of select="@name"/>
		</a>
	</xsl:template>

	<xsl:template match="stepref">
		<a class="stepref" href="{@name}.html">
			<xsl:value-of select="@name"/>
		</a>
	</xsl:template>

	<xsl:template match="sectionref">
		<xsl:choose>
			<xsl:when test="string-length(@title) &gt; 0">
				<a href="#{@title}">
					<xsl:text>"</xsl:text>
					<xsl:value-of select="@title"/>
					<xsl:text>"</xsl:text>
				</a>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="url" select="//section[position()=current()/@number]/@title"/>
				<a href="#{$url}">
					<xsl:text>"</xsl:text>
					<xsl:value-of select="$url"/>
					<xsl:text>"</xsl:text>
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="pageref">
		<xsl:choose>
			<xsl:when test="//level2[@name=current()/@name]">
				<a href="{//level2[@name=current()/@name]/@url}">
					<xsl:value-of select="@name"/>
				</a>
			</xsl:when>
			<xsl:otherwise>
				<a href="{//level3[@name=current()/@name]/@url}">
					<xsl:value-of select="@name"/>
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="attributes">
		<dl class="attributes">
			<xsl:apply-templates select="attribute[required/text() = 'yes']">
				<xsl:sort select="name"/>
			</xsl:apply-templates>
			<xsl:apply-templates select="attribute[required/text() != 'yes']">
				<xsl:sort select="name"/>
			</xsl:apply-templates>
		</dl>
	</xsl:template>

	<xsl:template match="attribute[../../@title='Inline Text']" name="attribute">
		<dd class="required">
			<xsl:text>Required?&space;</xsl:text>
			<xsl:value-of select="required"/>
		</dd>
		<dd>
			<xsl:apply-templates select="description"/>
		</dd>
	</xsl:template>

	<xsl:template match="attribute[../../@title='Nested Parameters']">
		<dt>
			<a href="{name}.html">
				<xsl:value-of select="name"/>
			</a>
		</dt>
		<xsl:call-template name="attribute"/>
	</xsl:template>

	<xsl:template match="attribute">
		<dt>
			<xsl:value-of select="name"/>
		</dt>
		<xsl:call-template name="attribute"/>
	</xsl:template>

	<xsl:template match="jira">
		<xsl:variable name="jiraFeed" select="document(concat('&jira;', @href))/rss/channel/item"/>

		<h3>Bug</h3>
		<ul>
			<xsl:apply-templates select="$jiraFeed[type/@id='1']"/>
		</ul>
		<h3>New Feature</h3>
		<ul>
			<xsl:apply-templates select="$jiraFeed[type/@id='2']"/>
		</ul>
		<h3>Task</h3>
		<ul>
			<xsl:apply-templates select="$jiraFeed[type/@id='3']"/>
		</ul>
	</xsl:template>

	<xsl:template match="item">
		<li>
			<xsl:text>[</xsl:text>
			<a href="&jira;/browse/{key}">
				<xsl:value-of select="key"/>
			</a>
			<xsl:text>]&#x20;</xsl:text>
			<xsl:value-of select="summary"/>
		</li>
	</xsl:template>
</xsl:stylesheet>
