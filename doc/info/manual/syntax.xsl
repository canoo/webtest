<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet [
		<!ENTITY space "&#32;">
		<!ENTITY nbsp "&#160;"> ]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:redirect="http://xml.apache.org/xalan/redirect"
				xmlns:java="http://xml.apache.org/xalan/java"
				extension-element-prefixes="redirect"
				version="1.0">

	<xsl:output
			method="xml"
			encoding="iso-8859-1"
			indent="no"
			/>
	<xsl:param name="category"/>

	<xsl:variable name="stepList" select="/webtest-doc"/>
	<xsl:variable name="site" select="document('WebTestSite.xml')"/>

	<xsl:key name="typeUsage" match="//node()[nestedParameter[name!='addText']]" use="nestedParameter/name"/>

	<xsl:template match="/">
		<manpage siteid="Syntax Reference - {$category} Steps">
			<xsl:apply-templates select="$site" mode="toc"/>
			<xsl:copy-of select="document(concat('syntaxPre', $category, '.xml'))"/>
		</manpage>

		<xsl:apply-templates select="webtest-doc/nested[category=$category]">
			<xsl:sort select="name"/>
		</xsl:apply-templates>

		<xsl:apply-templates select="webtest-doc/step[category=$category]">
			<xsl:sort select="name"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="nested|step">
		<redirect:write file="{name}.xml">
			<manpage siteid="{name}">

				<xsl:apply-templates select="$site" mode="toc"/>

				<xsl:element name="head">
					<xsl:attribute name="title">
						<xsl:value-of select="$category"/>
						<xsl:text>&space;</xsl:text>

						<xsl:choose>
							<xsl:when test="local-name()='nested'">Type</xsl:when>
							<xsl:otherwise>Step</xsl:otherwise>
						</xsl:choose>

						<xsl:text>|&space;</xsl:text>

						<xsl:value-of select="name"/>
					</xsl:attribute>
				</xsl:element>

				<section title="Description">
					<b>
						<xsl:call-template name="xdocskludge">
							<xsl:with-param name="what">
								<xsl:value-of select="description"/>
							</xsl:with-param>
						</xsl:call-template>
					</b>
					<xsl:copy-of select="pre"/>
				</section>

				<xsl:if test="parameter">
					<section title="Parameters">
						<attributes>
							<xsl:apply-templates select="parameter"/>
						</attributes>
					</section>
				</xsl:if>

				<xsl:if test="nestedParameter[name='addText']">
					<section title="Inline Text">
						<b>The
							<em>inline text</em>
							is all the text between the start tag (
							<example>&lt;<xsl:value-of select="name"/>></example>
							) and the end tag (
							<example>&lt;/<xsl:value-of select="name"/>></example>
							), including blanks, tabs or newlines. Using a pair of start/end tags (
							<example>&lt;<xsl:value-of select="name"/>> &lt;/<xsl:value-of select="name"/>></example>
							) has not the same behavior than the seemingly equivalent empty element tag (
							<example>&lt;<xsl:value-of select="name"/>/></example>
							)! See
							<ext base="jira" href="browse/WT-228">this issue</ext>
							for an example.
						</b>
						<attributes>
							<xsl:apply-templates select="nestedParameter[name='addText']"/>
						</attributes>
					</section>
				</xsl:if>

				<xsl:if test="nestedParameter[name!='addText']">
					<section title="Nested Parameters">
						<attributes>
							<xsl:apply-templates select="nestedParameter[name!='addText']"/>
						</attributes>
					</section>
				</xsl:if>

				<xsl:if test="post">
					<section title="Details">
						<xsl:copy-of select="post"/>
					</section>
				</xsl:if>

				<xsl:if test="name()='nested'">
					<section title="Usage">
						<xsl:variable name="allUsages"
									  select="key('typeUsage', concat('add', java:org.apache.commons.lang.StringUtils.capitalize(name))) | key('typeUsage', concat('create', java:org.apache.commons.lang.StringUtils.capitalize(name)))"/>
						<b>
							<xsl:choose>
								<xsl:when test="$allUsages">
									<xsl:for-each select="$allUsages">
										<xsl:sort select="name" order="ascending"/>
										<xsl:if test="position() > 1">,&#x20;</xsl:if>
										<stepref name="{name}"/>
									</xsl:for-each>
									<xsl:text>.</xsl:text>
								</xsl:when>
								<xsl:otherwise>No usage found.</xsl:otherwise>
							</xsl:choose>
						</b>
					</section>
				</xsl:if>
			</manpage>
		</redirect:write>
	</xsl:template>

	<xsl:template match="nestedParameter">
		<attribute>
			<name>
				<xsl:apply-templates select="name"/>
			</name>
			<description>
				<xsl:call-template name="xdocskludge">
					<xsl:with-param name="what">
						<xsl:value-of select="description"/>
					</xsl:with-param>
				</xsl:call-template>
			</description>
			<required>
				<xsl:value-of select="required"/>
				<xsl:if test="string-length(default/text()) != 0">, default is
					<xsl:value-of select="default"/>
				</xsl:if>
			</required>
		</attribute>
	</xsl:template>

	<xsl:template match="nestedParameter/name">
		<xsl:choose>
			<xsl:when test="starts-with(text(),'add')">
				<xsl:value-of
						select="java:org.apache.commons.lang.StringUtils.uncapitalize(substring-after(text(),'add'))"/>
			</xsl:when>
			<xsl:when test="starts-with(text(),'create')">
				<xsl:value-of
						select="java:org.apache.commons.lang.StringUtils.uncapitalize(substring-after(text(),'create'))"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="text()"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="parameter">
		<attribute>
			<name>
				<xsl:value-of select="name"/>
			</name>
			<description>
				<xsl:call-template name="xdocskludge">
					<xsl:with-param name="what">
						<xsl:value-of select="description"/>
					</xsl:with-param>
				</xsl:call-template>
			</description>
			<required>
				<xsl:value-of select="required"/>
				<xsl:if test="string-length(default/text()) != 0">, default is
					<xsl:value-of select="default"/>
				</xsl:if>
			</required>
		</attribute>
	</xsl:template>

	<xsl:template name="xdocskludge">
		<xsl:param name="what"/>
		<xsl:choose>
			<xsl:when test="contains($what, '&lt;')">
				<xsl:variable name="elementFound" select="substring-after(substring-before($what,'&gt;'), '&lt;')"/>
				<xsl:choose>
					<xsl:when test="contains('em key example', $elementFound)">
						<xsl:value-of select="substring-before($what, '&lt;')"/>
						<xsl:element name="{$elementFound}">
							<xsl:value-of
									select="substring-after(substring-before($what, concat('&lt;/',$elementFound,'&gt;')), concat($elementFound,'&gt;'))"/>
						</xsl:element>
						<xsl:call-template name="xdocskludge">
							<xsl:with-param name="what">
								<xsl:value-of select="substring-after($what, concat('&lt;/',$elementFound,'&gt;'))"/>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="starts-with($elementFound, 'stepref')">
						<xsl:value-of select="substring-before($what, '&lt;')"/>
						<xsl:variable name="steprefstring"
									  select="substring-after(substring-before($what, '/&gt;'), '&lt;')"/>
						<xsl:element name="stepref">
							<xsl:attribute name="name">
								<xsl:value-of
										select="substring-before(substring-after($steprefstring, &quot;name=&apos;&quot;), &quot;&apos;&quot;)"/>
							</xsl:attribute>
							<xsl:attribute name="category">
								<xsl:value-of
										select="substring-before(substring-after($steprefstring, &quot;category=&apos;&quot;), &quot;&apos;&quot;)"/>
							</xsl:attribute>
						</xsl:element>
						<xsl:call-template name="xdocskludge">
							<xsl:with-param name="what">
								<xsl:value-of select="substring-after($what, '/&gt;')"/>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat(substring-before($what,'&gt;'), '&gt;')"/>
						<xsl:call-template name="xdocskludge">
							<xsl:with-param name="what">
								<xsl:value-of select="substring-after($what,'&gt;')"/>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$what"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- copy the site.xml and includes the local information -->
	<xsl:template match="site|level1|@*" mode="toc">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="toc"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="level2" mode="toc">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="toc"/>
			<xsl:if test="@name=concat('Syntax Reference - ',$category,' Steps')">
				<xsl:apply-templates select="$stepList/nested[category=$category]|$stepList/step[category=$category]"
									 mode="toc">
					<xsl:sort select="name"/>
				</xsl:apply-templates>
			</xsl:if>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="nested|step" mode="toc">
		<xsl:element name="level3">
			<xsl:attribute name="name">
				<xsl:value-of select="name"/>
			</xsl:attribute>
			<xsl:attribute name="url">
				<xsl:value-of select="name"/>
				<xsl:text>.html</xsl:text>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
