<xsl:stylesheet
        version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:java="http://xml.apache.org/xalan/java"
        >

    <xsl:output
            method="xml"
            version="1.0"
            encoding="us-ascii"
            indent="no"
            omit-xml-declaration="no"
            />

    <xsl:param name="buildnumber"/>
    <xsl:param name="today"/>

    <!-- skip -->
    <xsl:template match="nested[name/text()='text']"/>
    <xsl:template match="text()"/>

    <xsl:template match="/">

        <xsl:comment>
            <xsl:text>Generated on&#x20;</xsl:text>
            <xsl:value-of select="$today"/>
            <xsl:text>&#x20;for build&#x20;</xsl:text>
            <xsl:value-of select="$buildnumber"/>
        </xsl:comment>
        <xsl:text>&#x0a;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;!ENTITY</xsl:text>
        <xsl:text>&#x20;&#x25; step "(</xsl:text>
        <xsl:apply-templates select="webtest-doc/step/name" mode="names"/>
        <xsl:text>)"&#x20;</xsl:text>
        <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
        <xsl:text>&#x0a;</xsl:text>
        <xsl:apply-templates select="webtest-doc/step|webtest-doc/nested"/>
    </xsl:template>


    <xsl:template match="step/name" mode="names">
        <xsl:if test="position()>1">&#x20;|&#x20;</xsl:if>
        <xsl:value-of select="text()"/>
    </xsl:template>

    <xsl:template match="step|nested">
        <xsl:text disable-output-escaping="yes">&lt;!ELEMENT</xsl:text>
        <xsl:text>&#x20;</xsl:text>
        <xsl:value-of select="name"/>
        <xsl:text>&#x20;</xsl:text>
        <xsl:choose>
            <xsl:when test="nestedParameter">
                <xsl:text>(&#x20;</xsl:text>
                <xsl:apply-templates select="nestedParameter"/>
                <xsl:text>&#x20;)*</xsl:text>
            </xsl:when>
            <xsl:otherwise>EMPTY</xsl:otherwise>
        </xsl:choose>
        <xsl:text>&#x20;</xsl:text>
        <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
        <xsl:text>&#x0a;</xsl:text>

        <xsl:if test="parameter">
            <xsl:text disable-output-escaping="yes">&lt;!ATTLIST</xsl:text>
            <xsl:text>&#x20;</xsl:text>
            <xsl:value-of select="name"/>
            <xsl:text>&#x0a;</xsl:text>
            <xsl:apply-templates select="parameter">
            	 <xsl:sort select="name/text()"/>
            </xsl:apply-templates>
            <xsl:text disable-output-escaping="yes">&gt;</xsl:text>
            <xsl:text>&#x0a;</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="parameter">
        <xsl:text>&#x09;</xsl:text>
        <xsl:value-of select="name"/>
        <xsl:text>&#x20;CDATA&#x20;</xsl:text>
        <xsl:choose>
            <xsl:when test="required='yes'">
                <xsl:text>#REQUIRED</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>#IMPLIED</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>&#x0a;</xsl:text>
    </xsl:template>

    <xsl:template match="nestedParameter">
        <xsl:if test="position()>1">&#x20;|&#x20;</xsl:if>
        <xsl:choose>
            <xsl:when test="name='addText'">#PCDATA</xsl:when>
            <xsl:when test="name='addStep'">&#x25;step;</xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="name"/>
            </xsl:otherwise>
        </xsl:choose>
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
</xsl:stylesheet>