<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="xml" encoding="iso-8859-1" indent="yes"/>

	<xsl:key name="stepIndex" match="webtest-doc/step" use="substring(name/text(), 1, 1)"/>

	<xsl:template match="/">
		<manpage>
			<xsl:attribute name="siteid">Step Index</xsl:attribute>
			<xsl:copy-of select="document('WebTestSite.xml')"/>
			<head title="Step Index"/>
			<xsl:call-template name="alphabeticalList"/>
		</manpage>
	</xsl:template>

	<xsl:template name="alphabeticalList">
		<xsl:param name="alphabet" select="'abcdefghijklmnopqrstuvwxyz'"/>

		<xsl:variable name="letter" select="substring($alphabet, 1, 1)"/>
		<xsl:variable name="stepWithLetter" select="key('stepIndex', $letter)"/>
		<xsl:if test="$stepWithLetter">
			<section>
				<xsl:attribute name="title">
					<xsl:value-of select="translate($letter, 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
				</xsl:attribute>
				<dl>
					<xsl:apply-templates select="$stepWithLetter">
						<xsl:sort select="name"/>
					</xsl:apply-templates>
				</dl>
			</section>
		</xsl:if>

		<xsl:if test="string-length($alphabet) > 1">
			<xsl:call-template name="alphabeticalList">
				<xsl:with-param name="alphabet" select="substring($alphabet, 2)"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template match="step">
		<li>
			<dt>
				<stepref>
					<xsl:attribute name="name">
						<xsl:value-of select="name/text()"/>
					</xsl:attribute>
					<xsl:attribute name="category">
						<xsl:value-of select="category/text()"/>
					</xsl:attribute>
				</stepref>
			</dt>
			<dd>
				<xsl:call-template name="xdocskludge">
					<xsl:with-param name="what">
						<xsl:value-of select="description"/>
					</xsl:with-param>
				</xsl:call-template>
			</dd>
		</li>
	</xsl:template>

	<xsl:template name="xdocskludge">
		<xsl:param name="what"/>

		<xsl:choose>

			<xsl:when test="contains($what, '&lt;')">
				<xsl:variable name="elementFound" select="substring-after(substring-before($what,'&gt;'), '&lt;')"/>
				<xsl:choose>
					<xsl:when test="contains('em key', $elementFound)">
						<xsl:value-of select="substring-before($what, '&lt;')"/>
						<xsl:element name="{$elementFound}">
							<xsl:value-of select="substring-after(substring-before($what, concat('&lt;/',$elementFound,'&gt;')), concat($elementFound,'&gt;'))"/>
						</xsl:element>
						<xsl:call-template name="xdocskludge">
							<xsl:with-param name="what">
								<xsl:value-of select="substring-after($what, concat('&lt;/',$elementFound,'&gt;'))"/>
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>

					<xsl:when test="starts-with($elementFound, 'stepref')">
						<xsl:value-of select="substring-before($what, '&lt;')"/>
						<xsl:variable name="steprefstring" select="substring-after(substring-before($what, '/&gt;'), '&lt;')"/>
						<xsl:element name="stepref">
							<xsl:attribute name="name">
								<xsl:value-of select="substring-before(substring-after($steprefstring, &quot;name=&apos;&quot;), &quot;&apos;&quot;)"/>
							</xsl:attribute>
							<xsl:attribute name="category">
								<xsl:value-of select="substring-before(substring-after($steprefstring, &quot;category=&apos;&quot;), &quot;&apos;&quot;)"/>
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
</xsl:stylesheet>

