// Copyright � 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;


import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.self.TestBlock;
import com.canoo.webtest.self.ThrowAssert;
import com.canoo.webtest.steps.Step;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.taskdefs.Echo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Tests for {@link Configuration}.
 *
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King
 */
public class ConfigurationTest extends TestCase {
    static final String[] PROPERTIES_BOOL = {"autorefresh", "haltonerror", "haltonfailure",
            "saveresponse", "showhtmlparseroutput"};
    static final String[] PROPERTIES_STRING = {"basepath",
            "errorproperty", "failureproperty", "host", "resultpath", "saveprefix"};
    static final String[] PROPERTIES_INT = {"timeout", "port"};

    public void testCtorBasePath() {
        String host = "somehost";
        int port = 10;
        String basePath = "somepath";

        Configuration simpleConfig = new Configuration();

        simpleConfig.setHost(host);
        simpleConfig.setPort(port);
        simpleConfig.setBasepath(basePath);

        assertEquals("host", host, simpleConfig.getHost());
        assertEquals("port", port, simpleConfig.getPort());
        assertEquals("protocol", Configuration.PROTOCOL_HTTP, simpleConfig.getProtocol());
        assertEquals("base path", basePath, simpleConfig.getBasePath());
    }

    public void testCtorDefaultPage() {
        // String defaultProtocol = "http";
        String host = "xxx";
        int port = 10;
        String basePath = "frontnet";
        // String baseUrl = defaultProtocol + "://" + host + ":" + port;
        // String defaultPage = "start.html";
        // String defaultPageUrl = baseUrl + "/" + basePath + "/" + defaultPage;

        Configuration conf = new Configuration();

        conf.setHost(host);
        conf.setPort(port);
        conf.setBasepath(basePath);

        assertEquals("base path", basePath, conf.getBasePath());
    }

    public void testStandard() {
        Configuration standard = new Configuration();

        assertEquals("port", Configuration.PORT_HTTP, standard.getPort());
        assertEquals("protocol", Configuration.PROTOCOL_HTTP, standard.getProtocol());
        assertEquals("host", Configuration.DEFAULT_HOST, standard.getHost());
        assertFalse("saveResp", standard.isSaveResponse());

        assertNotNull("urlForPage", standard.getUrlForPage(null));

        assertNull("basePath", standard.getBasePath());
        assertNull("errorProperty", standard.getErrorProperty());
        assertNull("failureProperty", standard.getFailureProperty());
    }

    public void testErrorAndFailureProperties() {
        Configuration propConf = new Configuration();

        propConf.setErrorProperty("errorProp");
        propConf.setFailureProperty("failureProp");
        assertNotNull("errorProperty", propConf.getErrorProperty());
        assertNotNull("failureProperty", propConf.getFailureProperty());
    }

    public void testDefaultPropertyType() {
        Configuration propConf = new Configuration();

        propConf.setDefaultPropertyType(Step.PROPERTY_TYPE_ANT);
        assertEquals(Step.PROPERTY_TYPE_ANT, propConf.getDefaultPropertyType());
    }

    public void testGetUrlForPage() {
        Configuration config = new Configuration();

        config.setHost("myHost");
        config.setBasepath("/myApplication");

        assertEquals("http://myHost/myApplication", config.getUrlForPage(""));

        config.setProtocol("https");
        config.setPort(443);
        assertEquals("https://myHost/myApplication", config.getUrlForPage(""));

        assertEquals("https://myHost/myApplication", config.getUrlForPage("https://myHost/myApplication"));
        assertEquals("http://myHost/myPage", config.getUrlForPage("http://myHost/myPage"));
        assertEquals("file://myFile", config.getUrlForPage("file://myFile"));
    }

    public void testUrlFromHostPort() {
        Configuration hostPortConfig = new Configuration();

        hostPortConfig.setHost("somehost");
        hostPortConfig.setPort(8080);

        String expectedUrl = "http://somehost:8080/somepage.html";

        assertEquals("urlForPage", expectedUrl, hostPortConfig.getUrlForPage("somepage.html"));
        assertEquals("urlForPage", expectedUrl, hostPortConfig.getUrlForPage("/somepage.html"));
    }

    public void testUrlFromHostWitheEfaultPort() {
        Configuration hostPortConfig = new Configuration();

        hostPortConfig.setHost("somehost");
        assertEquals("urlForPage", "http://somehost/somepage.html",
                hostPortConfig.getUrlForPage("somepage.html"));

        hostPortConfig.setProtocol("https");
        hostPortConfig.setPort(443);
        assertEquals("urlForPage", "https://somehost/somepage.html",
                hostPortConfig.getUrlForPage("somepage.html"));
    }

    public void testUrlFromHostPortBasePath() {
        Configuration basePathConfig = new Configuration();

        basePathConfig.setHost("somehost");
        basePathConfig.setPort(8080);
        basePathConfig.setBasepath("somepath");

        String expectedUrl = "http://somehost:8080/somepath/somepage.html";

        assertEquals(expectedUrl, basePathConfig.getUrlForPage("somepage.html"));
        assertEquals(expectedUrl, basePathConfig.getUrlForPage("/somepage.html"));

        basePathConfig.setBasepath("/somepath");
        assertEquals(expectedUrl, basePathConfig.getUrlForPage("somepage.html"));
        assertEquals(expectedUrl, basePathConfig.getUrlForPage("/somepage.html"));

        basePathConfig.setBasepath("/somepath/");
        assertEquals(expectedUrl, basePathConfig.getUrlForPage("somepage.html"));
        assertEquals(expectedUrl, basePathConfig.getUrlForPage("/somepage.html"));
    }

    public void testUrlFromFileDosPageWithBasepath() {
        Configuration fileConfig = new Configuration();

        fileConfig.setProtocol("file");
        fileConfig.setBasepath("C:/temp");
        assertEquals("page", "file:/C:/temp/XX.xx", fileConfig.getUrlForPage("XX.xx"));
    }

    public void testUrlFromFileUnixPageWithBasepath() {
        Configuration fileConfig = new Configuration();

        fileConfig.setProtocol("file");
        fileConfig.setBasepath("/temp");
        assertEquals("", "file:/temp/XX.xx", fileConfig.getUrlForPage("XX.xx"));
    }

    public void testUrlFromFileDosPageAbsolute() {
        Configuration fileConfig = new Configuration();

        fileConfig.setProtocol("file");
        fileConfig.setBasepath("/");
        assertEquals("", "file:/c:/temp/XX.xx", fileConfig.getUrlForPage("c:/temp/XX.xx"));
    }

    public void testUrlFromFileDosPageAbsoluteNoBasePath() {
        Configuration fileConfig = new Configuration();

        fileConfig.setProtocol("file");
        assertEquals("", "file:/c:/temp/XX.xx", fileConfig.getUrlForPage("c:/temp/XX.xx"));
    }

    public void testUrlFromFileDosPageRelative() {
        Configuration fileConfig = new Configuration();

        fileConfig.setProtocol("file");
        fileConfig.setBasepath("c:/temp");
        assertEquals("", "file:/c:/temp/XX.xx", fileConfig.getUrlForPage("XX.xx"));
    }

    public void testUrlFromFileUnixPageRelative() {
        Configuration fileConfig = new Configuration();

        fileConfig.setProtocol("file");
        fileConfig.setBasepath("/");
        assertEquals("", "file:/temp/XX.xx", fileConfig.getUrlForPage("temp/XX.xx"));
    }

    public void testNoPropertyHandler() {
        ThrowAssert.assertThrows(IllegalStateException.class, new TestBlock() {
            public void call() throws Exception {
                Configuration fileConfig = new Configuration();

                fileConfig.setPropertyHandler(null);
                fileConfig.getExternalProperty("some property");
            }
        });
    }

    public void testResultDirNotDirectoryThrowsBuildException() {
        final Configuration config = new Configuration();
        final File nonDirFile = new File("dummy") {
            public boolean exists() {
                return true;
            }

            public boolean isDirectory() {
                return false;
            }
        };
        ThrowAssert.assertThrows(BuildException.class, "Result dir is not a directory", new TestBlock() {
            public void call() throws Throwable {
                config.prepareResultDir(nonDirFile);
            }
        });
    }

    public void testCantCreateResultDirThrowsBuildException() {
        final File cantCreateDir = new File("dummy") {
            public boolean exists() {
                return false;
            }

            public boolean mkdirs() {
                return false;
            }
        };
        final Configuration config = new Configuration() {
            protected File computeSubFolder(File _resultDir) {
                return cantCreateDir;
            }
        };
        ThrowAssert.assertThrows(BuildException.class, "Failed to create result dir", new TestBlock() {
            public void call() throws Throwable {
                config.prepareResultDir(cantCreateDir);
            }
        });
    }

    public void testComputeSubFolder() {
        final List<File> files = new ArrayList<File>();
        final File reportBaseDir = new File("dummy") {
            public File[] listFiles() {
                return (File[]) files.toArray(new File[]{});
            }
        };
        final WebtestTask webTest = new WebtestTask();
        final Project project = new Project();
        final Context context = new Context(webTest);
        Configuration config = new Configuration();
        config.setProject(project);
        config.setContext(context);

        assertEquals(context, config.getContext());

        webTest.setName("foo");
        assertEquals(1, config.getResultFolderIndex(reportBaseDir));
        assertEquals("001_Foo", config.computeSubFolder(reportBaseDir).getName());

        webTest.setName("first test - blabla");
        assertEquals(1, config.getResultFolderIndex(reportBaseDir));
        assertEquals("001_FirstTestBlabla", config.computeSubFolder(reportBaseDir).getName());

        files.add(new File("001_blabla"));
        config.setResultFolderIndex(-1); // force to compute
        assertEquals(2, config.getResultFolderIndex(reportBaseDir));
        assertEquals("002_FirstTestBlabla", config.computeSubFolder(reportBaseDir).getName());

        files.add(new File("002_Xdkf"));
        files.add(new File("003_Zfkss"));
        files.add(new File("029_AnotherTest"));
        config.setResultFolderIndex(-1); // force to compute
        assertEquals(30, config.getResultFolderIndex(reportBaseDir));
        assertEquals("030_FirstTestBlabla", config.computeSubFolder(reportBaseDir).getName());

        // already 999 files with same webtest name (cf WT-413)
        for (int i = 0; i < 1000; ++i) {
            final String prefix = StringUtils.leftPad(String.valueOf(i), 3, '0');
            files.add(new File(prefix + "_justATest"));
        }
        config.setResultFolderIndex(-1); // force to compute
        assertEquals(1000, config.getResultFolderIndex(reportBaseDir));
        assertEquals("1000_FirstTestBlabla", config.computeSubFolder(reportBaseDir).getName());
        files.add(new File("1000_FirstTestBlabla"));
        config.setResultFolderIndex(-1); // force to compute
        assertEquals(1001, config.getResultFolderIndex(reportBaseDir));
        assertEquals("1001_FirstTestBlabla", config.computeSubFolder(reportBaseDir).getName());

        // listFiles returns null "if this abstract pathname does not denote a directory"
        final File reportBaseDir2 = new File("dummy") {
            public File[] listFiles() {
                return null;
            }
        };
        config.setResultFolderIndex(-1); // force to compute
        assertEquals("001_FirstTestBlabla", config.computeSubFolder(reportBaseDir2).getName());
    }

    public void testNoProxyHandling() throws Exception {
        assertNull(getCredentials());
    }

    public void testProxyHandling() throws Exception {
        System.setProperty("http.proxyUser", "user");
        System.setProperty("http.proxyPassword", "password");
        assertNotNull(getCredentials());
    }

    public void testDefaultPropertiesFromProject_NoConfig() {
        final Project project = new Project();

        setProperties(project, "wt.config.", PROPERTIES_BOOL, "true");
        setProperties(project, "wt.config.", PROPERTIES_STRING, "blabla");
        setProperties(project, "wt.config.", PROPERTIES_INT, "1234");
        project.setProperty("wt.config.protocol", "https");
        project.setProperty("wt.config.defaultpropertytype", "ant");


        WebtestTask webTest = new WebtestTask();
        webTest.setProject(project);
        webTest.addTask(new Echo()); // just to trigger creation of default config

        Configuration config = webTest.getConfig();
        verifyProperties(config, true, 1234, "blabla", "https", "ant");

        // change property values to be sure that this info is considered
        setProperties(project, "wt.config.", PROPERTIES_BOOL, "false");
        setProperties(project, "wt.config.", PROPERTIES_STRING, "foo");
        setProperties(project, "wt.config.", PROPERTIES_INT, "4321");
        project.setProperty("wt.config.protocol", "http");
        project.setProperty("wt.config.defaultpropertytype", "dynamic");

        webTest = new WebtestTask();
        webTest.setProject(project);
        webTest.addTask(new Echo()); // just to trigger creation of default config

        config = webTest.getConfig();
        verifyProperties(config, false, 4321, "foo", "http", "dynamic");
    }

    public void testDefaultPropertiesFromProject_ConfigUsed() {
        final Project project = new Project();

        setProperties(project, "wt.config.", PROPERTIES_BOOL, "true");
        setProperties(project, "wt.config.", PROPERTIES_STRING, "blabla");
        setProperties(project, "wt.config.", PROPERTIES_INT, "1234");
        project.setProperty("wt.config.protocol", "https");
        project.setProperty("wt.config.defaultpropertytype", "ant");


        WebtestTask webTest = new WebtestTask();
        webTest.setProject(project);

        final RuntimeConfigurable rc = new RuntimeConfigurable(null, "config");
        rc.setAttribute("host", "myHost");
        rc.setAttribute("port", "8080");
        Configuration config = new Configuration();
        config.setProject(project);
        config.setRuntimeConfigurableWrapper(rc);
        config.setHost("myHost");
        config.setPort(8080);
        config.init();
        webTest.addConfig(config);

        // test some properties that have been set form project's properties
        assertEquals(true, config.isHaltOnError());
        assertEquals(true, config.isShowHtmlParserOutput());
        assertEquals(1234, config.getTimeout());
        assertEquals("blabla", config.getBasePath());
        assertEquals("blabla", config.getFailureProperty());

        // but that the ones explicitely configured haven't been overwritten
        assertEquals("myHost", config.getHost());
        assertEquals(8080, config.getPort());
    }

    public void testInsecureSSL() throws GeneralSecurityException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        Project project = new Project();

        Configuration config = initConfig(project);

        assertFalse("default should be false", config.getUseInsecureSSL());

        project.setProperty("wt.config.useInsecureSSL", "true");

        config = initConfig(project);
        config.execute();
        assertTrue("should be true if set to true", config.getUseInsecureSSL());

        SchemeRegistry schemeRegistry = retrieveSchemeRegistry(config);
        SSLSocketFactory socketFactory = (SSLSocketFactory) schemeRegistry.get("https").getSchemeSocketFactory();
        assertEquals("configured SocketFactory should hold a HostnameVerifier that ALLOWS ALL.",
                "ALLOW_ALL",
                socketFactory.getHostnameVerifier().toString());

        project.setProperty("wt.config.useInsecureSSL", "false");
        config = initConfig(project);
        config.execute();
        assertFalse("should be false if set to false", config.getUseInsecureSSL());
        schemeRegistry = retrieveSchemeRegistry(config);
        socketFactory = (SSLSocketFactory) schemeRegistry.get("https").getSchemeSocketFactory();
        assertFalse("configured SocketFactory should NOT hold a HostnameVerifier that ALLOWS ALL.",
                "ALLOW_ALL".equals(socketFactory.getHostnameVerifier().toString()));

    }

    private SchemeRegistry retrieveSchemeRegistry(Configuration config) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        WebConnection webConnection = findHttpWebConnection(config);

        if (webConnection instanceof HttpWebConnection) {
            HttpWebConnection httpWebConnection = (HttpWebConnection) webConnection;
            Class clazz = httpWebConnection.getClass();
            Method getHttpClient = clazz.getDeclaredMethod("getHttpClient");
            getHttpClient.setAccessible(true);
            AbstractHttpClient httpClient = (AbstractHttpClient) getHttpClient.invoke(httpWebConnection);
            SchemeRegistry schemeRegistry = httpClient.getConnectionManager().getSchemeRegistry();
            return schemeRegistry;
        }
        fail("no HttpWebConnection found");
        return null;
    }

    private WebConnection findHttpWebConnection(Configuration config) {
        WebClient webClient = config.getContext().getWebClient();
        WebConnection webConnection = webClient.getWebConnection();
        while (webConnection instanceof WebConnectionWrapper) {
            webConnection = ((WebConnectionWrapper) webConnection).getWrappedWebConnection();
        }
        return webConnection;
    }

    public void testSSLClientCertificate() throws GeneralSecurityException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        Project project = new Project();

        Configuration config = initConfig(project);
        config.execute();

        assertFalse("default should be false", config.shouldUseClientCert());
        assertNull("default URL should be null", config.getCertUrl());
        assertEquals("default type should be jks", "jks", config.getCertType());
        assertNull("default password should be null", config.getCertPassword());

        // "sslKeyStore", "sslKeyStoreType", "sslKeyStorePassword",
        project.setProperty("wt.config.sslKeyStore", "file://tmp/foo");
        config = initConfig(project);
        config.execute();

        assertNotNull("default URL should be null", config.getCertUrl());
        assertEquals("file://tmp/foo", config.getCertUrl().toExternalForm());
        assertTrue("should be true", config.shouldUseClientCert());

        URL cert = ConfigurationTest.class.getResource("/user.p12");
        assertNotNull(cert);
        File file = new File(URI.create(cert.toExternalForm()));
        project.setProperty("wt.config.sslKeyStore", file.getAbsolutePath());
        project.setProperty("wt.config.sslKeyStoreType", "pkcs12");
        project.setProperty("wt.config.sslKeyStorePassword", "password");
        config = initConfig(project);
        config.execute();

        assertEquals(cert, config.getCertUrl());
        assertEquals(file.getAbsolutePath(), config.getSslKeyStore());
        assertEquals("default type should be pkcs12", "pkcs12", config.getCertType());
        assertEquals("password", config.getCertPassword());

        X509Certificate[] credentialsMap = findAssociatedSSLClientCerts(config);
        assertEquals(1, credentialsMap.length);
        String dn = "EMAILADDRESS=canoo.it@canoo.com, CN=Johnny the User, OU=Canoo WebTest Support Team, O=Canoo Engineering AG, ST=BS, C=CH";
        assertEquals(dn, credentialsMap[0].getSubjectDN().getName());
    }

    public void testInsecureSSLWithClientCert() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Project project = new Project();

        project.setProperty("wt.config.useInsecureSSL", "true");
        URL cert = ConfigurationTest.class.getResource("/user.p12");
        assertNotNull(cert);
        File file = new File(URI.create(cert.toExternalForm()));
        project.setProperty("wt.config.sslKeyStore", file.getAbsolutePath());
        project.setProperty("wt.config.sslKeyStoreType", "pkcs12");
        project.setProperty("wt.config.sslKeyStorePassword", "password");

        Configuration config = initConfig(project);
        config.execute();

        SchemeRegistry schemeRegistry = retrieveSchemeRegistry(config);
        SSLSocketFactory socketFactory = (SSLSocketFactory) schemeRegistry.get("https").getSchemeSocketFactory();
        assertEquals("configured SocketFactory should hold a HostnameVerifier that ALLOWS ALL.",
                "ALLOW_ALL",
                socketFactory.getHostnameVerifier().toString());

        X509Certificate[] credentialsMap = findAssociatedSSLClientCerts(config);
        assertEquals(1, credentialsMap.length);
        String dn = "EMAILADDRESS=canoo.it@canoo.com, CN=Johnny the User, OU=Canoo WebTest Support Team, O=Canoo Engineering AG, ST=BS, C=CH";
        assertEquals(dn, credentialsMap[0].getSubjectDN().getName());

    }

    private X509Certificate[] findAssociatedSSLClientCerts(Configuration config) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        HttpWebConnection webConnection = ((HttpWebConnection) findHttpWebConnection(config));
        webConnection.setVirtualHost("something.to.trigger.connection.creation");
        SchemeRegistry schemeRegistry = retrieveSchemeRegistry(config);
        SSLSocketFactory socketFactory = (SSLSocketFactory) schemeRegistry.get("https").getSchemeSocketFactory();
        Object keyManager = FieldUtils.readField(FieldUtils.readField(FieldUtils.readField(socketFactory,
                "socketfactory", true), "context", true), "keyManager", true);
        if (keyManager.getClass().getName().equals("com.sun.net.ssl.internal.ssl.JsseX509KeyManager")) {
            keyManager = FieldUtils.readField(keyManager, "keyManager", true);
        }
        HashMap map = (HashMap) FieldUtils.readField(keyManager, "credentialsMap", true);
        X509Certificate[] certificates = (X509Certificate[]) FieldUtils.readField(
                map.values().iterator().next(), "certificates", true);
        return certificates;
    }


    private Configuration initConfig(Project project) {
        WebtestTask webTest = new WebtestTask();
        webTest.setProject(project);
        webTest.setName("TestWebTest");
        Configuration config = new Configuration(webTest);

        Context context = new Context(webTest);
        config.setProject(project);
        config.setContext(context);
        return config;
    }

    private void verifyProperties(final Configuration config, final boolean bValue, final int intValue, final String strValue,
                                  final String protocol, final String propertyType) {
        assertEquals("autorefresh", String.valueOf(bValue), config.getAutoRefresh());
        assertEquals("haltonerror", bValue, config.isHaltOnError());
        assertEquals("haltonfailure", bValue, config.isHaltOnFailure());
        assertEquals("saveresponse", bValue, config.isSaveResponse());
        assertEquals("showhtmlparseroutput", bValue, config.isShowHtmlParserOutput());

        assertEquals("port", intValue, config.getPort());
        assertEquals("timeout", intValue, config.getTimeout());

        assertEquals("basepath", strValue, config.getBasePath());
        assertEquals("errorproperty", strValue, config.getErrorProperty());
        assertEquals("failureproperty", strValue, config.getFailureProperty());
        assertEquals("host", strValue, config.getHost());
        assertEquals("resultpath", strValue, config.getResultpath().getName());
        assertEquals("saveprefix", strValue, config.getSavePrefix());

        assertEquals("protocol", protocol, config.getProtocol());
        assertEquals("defaultpropertytype", propertyType, config.getDefaultPropertyType());
    }

    /**
     * Set the values on all properties
     */
    private void setProperties(final Project project, final String prefix, final String[] properties, final String value) {
        for (int i = 0; i < properties.length; i++) {
            project.setProperty(prefix + properties[i], value);
        }

        assertEquals(value, project.getProperty(prefix + properties[0])); // just to test that it has been set
    }

    private static Credentials getCredentials() throws Exception {
        System.setProperty("http.proxyHost", "dummyHost");
        WebClient wc = Configuration.setupWebClient(BrowserVersion.INTERNET_EXPLORER_6);
        return wc.getCredentialsProvider().getCredentials(new AuthScope("dummyHost", 80));
    }

    public void testProxySettings() {
        System.setProperty("http.proxyHost", "dummyHost");
        System.setProperty("http.nonProxyHosts", "myhost.mydomain|*.myotherdomain.com");
        final List<String> proxyBypassPatterns = new ArrayList<String>();

        ProxyConfig proxyConfig = new ProxyConfig() {
            public void addHostsToProxyBypass(final String _pattern) {
                proxyBypassPatterns.add(_pattern);
                super.addHostsToProxyBypass(_pattern);
            }
        };
        final WebClient webClient = new WebClient();
        webClient.setProxyConfig(proxyConfig);
        final DefaultCredentialsProvider credentialProvider = new DefaultCredentialsProvider();
        Configuration.configureProxy(webClient, credentialProvider);
        assertEquals(2, proxyBypassPatterns.size());
        assertEquals("myhost\\.mydomain", proxyBypassPatterns.get(0));
        assertEquals(".*\\.myotherdomain\\.com", proxyBypassPatterns.get(1));
    }

    public void testUseInsecureSSL() {
        // default => secure
        Configuration config = new Configuration();
        config.createWebClient();
//		assertEquals(SSLProtocolSocketFactory.class, Protocol.getProtocol("https").getSocketFactory().getClass());
//
//		// default => secure
//		config = new Configuration();
//		config.setUseInsecureSSL(true);
//		config.createWebClient();
//		assertEquals(InsecureSSLProtocolSocketFactory.class, Protocol.getProtocol("https").getSocketFactory().getClass());
    }

    @Deprecated // to avoid deprecated warnings in body
    public void testSetupBrowserVersion() {
//		assertSame(BrowserVersion.INTERNET_EXPLORER_6, Configuration.setupBrowserVersion(null, null));
//		assertSame(BrowserVersion.INTERNET_EXPLORER_6, Configuration.setupBrowserVersion("IE6", null));
//		assertSame(BrowserVersion.INTERNET_EXPLORER_6, Configuration.setupBrowserVersion("ie6", null));
//		assertSame(BrowserVersion.INTERNET_EXPLORER_6, Configuration.setupBrowserVersion("InternetExplorer6", null));

        assertSame(BrowserVersion.INTERNET_EXPLORER_7, Configuration.setupBrowserVersion("IE7", null));
        assertSame(BrowserVersion.INTERNET_EXPLORER_7, Configuration.setupBrowserVersion("ie7", null));
        assertSame(BrowserVersion.INTERNET_EXPLORER_7, Configuration.setupBrowserVersion("InternetExplorer7", null));

        assertSame(BrowserVersion.INTERNET_EXPLORER_8, Configuration.setupBrowserVersion("IE8", null));
        assertSame(BrowserVersion.INTERNET_EXPLORER_8, Configuration.setupBrowserVersion("ie8", null));
        assertSame(BrowserVersion.INTERNET_EXPLORER_8, Configuration.setupBrowserVersion("InternetExplorer8", null));

        assertSame(BrowserVersion.FIREFOX_3, Configuration.setupBrowserVersion("FF3", null));
        assertSame(BrowserVersion.FIREFOX_3, Configuration.setupBrowserVersion("ff3", null));
        assertSame(BrowserVersion.FIREFOX_3, Configuration.setupBrowserVersion("Firefox3", null));

        assertSame(BrowserVersion.FIREFOX_3_6, Configuration.setupBrowserVersion("FF3.6", null));
        assertSame(BrowserVersion.FIREFOX_3_6, Configuration.setupBrowserVersion("ff3.6", null));
        assertSame(BrowserVersion.FIREFOX_3_6, Configuration.setupBrowserVersion("Firefox3.6", null));

        BrowserVersion browser = Configuration.setupBrowserVersion("FF3", "myBrowser");
        assertEquals(BrowserVersion.FIREFOX_3.getApplicationCodeName(), browser.getApplicationCodeName());
        assertEquals(BrowserVersion.FIREFOX_3.getApplicationMinorVersion(), browser.getApplicationMinorVersion());
        assertEquals(BrowserVersion.FIREFOX_3.getApplicationName(), browser.getApplicationName());
        assertEquals(BrowserVersion.FIREFOX_3.getApplicationVersion(), browser.getApplicationVersion());
        assertEquals(BrowserVersion.FIREFOX_3.getBrowserLanguage(), browser.getBrowserLanguage());
        assertEquals(BrowserVersion.FIREFOX_3.getBrowserVersionNumeric(), browser.getBrowserVersionNumeric());
        assertEquals("myBrowser", browser.getUserAgent());
    }
}
