// Copyright � 2002-2007 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.interfaces.IPropertyHandler;
import com.canoo.webtest.steps.HtmlParserMessage;
import com.canoo.webtest.util.Checker;
import com.canoo.webtest.util.MapUtil;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLDocument;
import com.gargoylesoftware.htmlunit.util.Cookie;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Captures configuration information.<p>
 *
 * @author unknown
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step category="General"
 * name="config"
 * description="This is a nested task of
 * <stepref name='webtest' category='General'/>
 * and is used to configure the target host system to use for a particular
 * <stepref name='webtest' category='General'/>
 * and several other features such as reporting of test results
 * and printing of debug information."
 */
public class Configuration extends Task {
    private static final Logger LOG = Logger.getLogger(Configuration.class);
    public static final int PORT_HTTP = 80;
    public static final int PORT_HTTPS = 443;
    public static final int DEFAULT_PORT = PORT_HTTP;
    public static final String DEFAULT_HOST = "localhost";
    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String PROTOCOL_FILE = "file";
    private static final String URL_SEPARATOR = "/";

    /**
     * The number of seconds before http connections timeout: (default={@value})
     */
    protected static final int DEFAULT_TIMEOUT = 5 * 60; // protected visibility to generate javadoc

    private int fPort = DEFAULT_PORT;
    private int fTimeout = DEFAULT_TIMEOUT;
    private String fProtocol = PROTOCOL_HTTP;
    private boolean fSaveResponse;
    private boolean fEasyAjax = false;
    private int fEasyAjaxDelay = 2000;
    private boolean fUseInsecureSSL_ = false;

    private String sslKeyStore;
    private String sslKeyStoreType;
    private String sslKeyStorePassword;
    private String sslTrustStore;
    private String sslTrustStorePassword;

    private String fSavePrefix = "response";
    private String fAutoRefresh = "false";
    private String fResultFile = "WebTestReport.xml";

    private boolean fShowHtmlParserOutput;
    private boolean fHaltOnError;
    private boolean fHaltOnFailure;
    private String fErrorProperty;
    private String fFailureProperty;
    private String fDefaultPropertyType;
    private File fSummaryFile;

    private String fHost = DEFAULT_HOST;
    private String fBasePath;
    private File fResultPath;
    private File fWebtestResultDir;
    private String fBrowser;
    private boolean fRespectVisibility_;

    private IPropertyHandler fPropertyHandler = new IPropertyHandler() {
        public String getProperty(String propertyName) {
            return getProject().getProperty(propertyName);
        }
    };
    private final List fHeaderList = new LinkedList();
    private final List fOptionList = new LinkedList();
    private Context fContext;
    private int resultFolderIndex = -1;

    /**
     * The task properties that may take default values from the corresponding wt.config.* project properties
     * (when not configured explicitly on the task).
     */
    private final static String[] PROPERTIES = {"autorefresh", "basepath", "defaultpropertytype",
            "errorproperty", "failureproperty", "haltonerror", "haltonfailure",
            "host", "port", "protocol", "resultpath", "saveprefix", "saveresponse", "showhtmlparseroutput",
            "timeout", "easyajax", "easyajaxdelay", "browser",
            "useInsecureSSL",
            "sslKeyStore", "sslKeyStoreType", "sslKeyStorePassword",
            "sslTrustStore", "sslTrustStorePassword"};


    /**
     * Configuration constructor used by instance creation as nested element in ant.<p>
     */
    public Configuration() {
        fHaltOnError = true;
        fHaltOnFailure = true;
    }

    /**
     * Get's called from Ant once the project and target references have been set
     * but before the properties are configured
     */
    public void init() {
        // read values from wt.config.* properties
        configureDefaultFromProjectProperties();
    }

    /**
     * Configuration constructor used to generate a default configuration when the user omit it.<p>
     */
    public Configuration(final WebtestTask testSpec) {
        // default properties
        fSaveResponse = true;
        fErrorProperty = "webtest.error";
        fFailureProperty = "webtest.failure";
        fShowHtmlParserOutput = true;
        setProject(testSpec.getProject());
        setOwningTarget(testSpec.getOwningTarget());
        init(); // as ant does, here to configure from wt.config.* properties
    }

    /* Must be done in execute not init as task attributes not available in init */
    public void execute() throws BuildException {
        configureDefaultFromProjectProperties();
        setSystemProperties();

        if (getResultpath() == null) {
            setResultpath(getProject().resolveFile("webtest-results"));
        }
        prepareResultDir(getResultpath());

        Checker.assertTrue(getResultFile() != null, "Result file cannot be null when writing summary");
        fSummaryFile = new File(getWebTestResultDir(), getResultFile());
        LOG.debug("Result file: " + fSummaryFile.getAbsolutePath());

        try {
            setupWebClient();
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private void setSystemProperties() {
        setSystemPropertyIfNotNull("javax.net.ssl.trustStore", getSslTrustStore());
        setSystemPropertyIfNotNull("javax.net.ssl.trustStorePassword", getSslTrustStorePassword());
    }

    private void setSystemPropertyIfNotNull(String key, String value) {
        if (value != null) {
            System.setProperty(key, value);
        }
    }

    protected void setupWebClient() throws IOException {
        WebClient webClient = createWebClient();
        webClient = fContext.getWebtest().getWebtestCustomizer().customizeWebClient(webClient);
        fContext.setWebClient(webClient);

        // catcher for JS background errors (as long as HtmlUnit doesn't provide a better solution to handle this)
        final Thread mainThread = Thread.currentThread();
        final JavaScriptEngine myEngine = new JavaScriptEngine(webClient) {
            protected void handleJavaScriptException(final ScriptException scriptException, final boolean triggerOnError) {
                if (Thread.currentThread() != mainThread && getWebClient().isThrowExceptionOnScriptError()) {
                    fContext.setBackgroundJSError(scriptException);
                }
                super.handleJavaScriptException(scriptException, false);
            }
        };
        webClient.setJavaScriptEngine(myEngine);

        // CSV report about sent/received requests/responses
        final File csvFile = new File(getWebTestResultDir(), "requests.csv");
        final WebConnection newConnection = new CSVTracingWebConnection(webClient.getWebConnection(), csvFile);
        webClient.setWebConnection(newConnection);
    }

    private void configureDefaultFromProjectProperties() {
        // read the properties that have been configured, they should not be replaced!
        final Set existingProps = new HashSet();
        if (getRuntimeConfigurableWrapper() != null) // null when no config is used
        {
            for (final Iterator iter = getRuntimeConfigurableWrapper().getAttributeMap().keySet().iterator(); iter.hasNext(); ) {
                existingProps.add(((String) iter.next()).toLowerCase());
            }
        }

        final IntrospectionHelper ih = IntrospectionHelper.getHelper(getProject(), getClass());
        for (int i = 0; i < PROPERTIES.length; ++i) {
            final String propName = PROPERTIES[i];
            if (!existingProps.contains(propName)) {
                final String propValue = getProject().getProperty("wt.config." + propName);
                if (propValue != null) {
                    LOG.info("Using " + propName + " from project property wt.config." + propName + ": " + propValue);
                    ih.setAttribute(getProject(), this, propName, propValue);
                }
            }
        }
    }

    // package protection for testing purposes
    void prepareResultDir(final File resultDir) {
        if (resultDir.exists() && !resultDir.isDirectory()) {
            throw new BuildException("Result dir is not a directory: " + resultDir.getAbsolutePath());
        }

        // compute subdir for this test
        fWebtestResultDir = computeSubFolder(resultDir);
        LOG.info("Creating result directory: " + fWebtestResultDir.getAbsolutePath());

        if (!fWebtestResultDir.mkdirs()) {
            throw new BuildException("Failed to create result dir: " + fWebtestResultDir.getAbsolutePath());
        }
    }

    /**
     * Compute the name of the subfolder for this test
     *
     * @param _resultDir the "main" result dir
     * @return the name of the subfolder
     */
    protected File computeSubFolder(final File _resultDir) {
        if (resultFolderIndex == -1)
            resultFolderIndex = getResultFolderIndex(_resultDir);

        final String prefix = StringUtils.leftPad(String.valueOf(resultFolderIndex), 3, '0');
        final String fixedTestName = WordUtils.capitalize(fContext.getWebtest().getName()).replaceAll("\\W", "");

        final int dirNameMaxLength = 40;
        String name = StringUtils.left(prefix + "_" + fixedTestName, dirNameMaxLength);

        final String suffix = getProject().getProperty("~wt.config.resultfolder.suffix");
        if (suffix != null) {
            name += suffix;
        }

        return new File(_resultDir, name);
    }

    /**
     * Sets the index of the result folder. Normally this shouldn't be set from
     * outside but the new experimental feature "WebTest parallel" currently needs it.
     *
     * @param index the index
     */
    public void setResultFolderIndex(final int index) {
        resultFolderIndex = index;
    }

    /**
     * Get the index to use as prefix for the dedicated result folder of this test
     *
     * @param _resultDir the base result directory
     * @return the index
     */
    protected int getResultFolderIndex(final File _resultDir) {
        int lastIndex = 0;
        final File[] children = _resultDir.listFiles();
        if (children != null) // null when _resultDir is not (yet?) a directory
        {
            for (int i = 0; i < children.length; i++) {
                final File f = children[i];
                if (f.getName().matches("\\d{3,}_.*")) {
                    final int index = Integer.parseInt(StringUtils.substringBefore(f.getName(), "_"));
                    lastIndex = Math.max(lastIndex, index);
                }
            }
        }
        return lastIndex + 1;
    }

    /**
     * Gets the file where the summary should be written.<p>
     *
     * @return <code>null</code> if no resultfile was specified
     */
    public File getSummaryFile() {
        // TODO: remove it!
        return fSummaryFile;
    }

    /**
     * @param header
     * @webtest.nested.parameter required="no"
     * description="Specify http headers by name and value"
     */
    public void addHeader(final Header header) {
        fHeaderList.add(header);
    }

    public List getHeaderList() {
        return fHeaderList;
    }

    /**
     * @param option
     * @webtest.nested.parameter required="no"
     * description="Tweak the underlying web client options/settings"
     */
    public void addOption(final Option option) {
        fOptionList.add(option);
    }

    public List getOptionList() {
        return fOptionList;
    }

    public String getBasePath() {
        return fBasePath;
    }

    /**
     * Gets the User-Agent header to be sent.<p>
     *
     * @return <code>null</code> if none has been configured
     */
    public String getUserAgent() {
        LOG.debug("Headers: " + getHeaderList());
        for (final Iterator iter = getHeaderList().iterator(); iter.hasNext(); ) {
            final Header elt = (Header) iter.next();
            if ("User-Agent".equals(elt.getName())) {
                LOG.debug("Found User-Agent header: " + elt.getValue());
                return elt.getValue();
            }
            LOG.debug("Not User-Agent header: " + elt.getName());
        }
        return null;
    }

    /**
     * Indicates if the default port for the protocol is used (that is port is not needed in the url).<p>
     */
    private boolean isDefaultPort() {
        return (PROTOCOL_HTTP.equals(getProtocol()) && PORT_HTTP == getPort())
                || (PROTOCOL_HTTPS.equals(getProtocol()) && PORT_HTTPS == getPort());
    }

    public String getHost() {
        return fHost;
    }

    public int getPort() {
        return fPort;
    }

    public String getProtocol() {
        return fProtocol;
    }

    /**
     * This is the configured general result dir.
     * This value should not be used directly as results will be placed in a sub folder of it in the future
     *
     * @return the configured result path
     */
    public File getResultpath() {
        return fResultPath;
    }

    /**
     * Gets the directory where all artifacts of this specific test should be saved.
     * This is currently the same than {@link #getResultpath()} but this may change in the future.
     *
     * @return the folder
     */
    public File getWebTestResultDir() {
        return fWebtestResultDir;
    }

    public String getSavePrefix() {
        return fSavePrefix;
    }

    /**
     * Completes a URL based on configuration values by expanding
     * where needed from a base URL (protocol and optionally
     * host:port). If the provided page is complete (already with protocol), it is returned as is.<p>
     *
     * @param page the maybe relative target URL
     * @return the assembled URL
     */
    public String getUrlForPage(final String page) {
        // first test if the page starts with a protocol like "http://", "https://", "file:/"
        final int index = StringUtils.indexOf(page, "://");
        if (index > -1 && index < 6 || (page != null && page.toLowerCase().startsWith("file:/"))) {
            return page;
        } else if (PROTOCOL_FILE.equals(getProtocol())) {
            return createFileBasedUrl(page);
        } else {
            return createNetworkBasedUrl(page);
        }
    }

    private String createFileBasedUrl(final String page) {
        return getProtocol() + ":" + combineBasePathAndPage(getBasePath(), page);
    }

    private String createNetworkBasedUrl(final String page) {
        final StringBuffer url = new StringBuffer(getProtocol());
        url.append("://");
        url.append(getHost());
        if (!isDefaultPort()) {
            url.append(":");
            url.append(getPort());
        }
        url.append(combineBasePathAndPage(getBasePath(), page));
        return url.toString();
    }

    private static String combineBasePathAndPage(final String basePath, final String page) {
        String basePathClean = StringUtils.strip(basePath, URL_SEPARATOR);
        basePathClean = StringUtils.isEmpty(basePathClean) ? "" : (URL_SEPARATOR + basePathClean);
        String pageClean = StringUtils.stripStart(page, URL_SEPARATOR);
        pageClean = StringUtils.isEmpty(pageClean) ? "" : (URL_SEPARATOR + pageClean);
        return basePathClean + pageClean;
    }

    public boolean isSaveResponse() {
        return fSaveResponse;
    }

    public void setSavePrefix(String savePrefix) {
        fSavePrefix = savePrefix;
    }

    /**
     * Defines the constant base path used to construct request URLs,
     * e.g. "shop" can be considered a basepath in "http://www.myhost.com/shop/productlist"
     * and "http://www.myhost.com/shop/checkout".<p>
     *
     * @param newBasePath the new value
     */
    public void setBasepath(final String newBasePath) {
        fBasePath = newBasePath;
    }

    public void setHost(final String newHost) {
        fHost = newHost;
    }

    public void setPort(final int newPort) {
        fPort = newPort;
    }

    public void setProtocol(final String newProtocol) {
        fProtocol = newProtocol;
    }

    public void setResultpath(final File newResultPath) {
        fResultPath = newResultPath;
    }

    public void setSaveresponse(final boolean newSaveResponse) {
        fSaveResponse = newSaveResponse;
    }

    public void setSummary(final boolean newSummary) {
        LOG.warn("Config attribute summary is deprecated and doesn't do anything");
    }

    public void setHaltonerror(final boolean haltOnError) {
        fHaltOnError = haltOnError;
    }

    public void setHaltonfailure(final boolean haltOnFailure) {
        fHaltOnFailure = haltOnFailure;
    }

    public void setErrorProperty(final String errorProperty) {
        fErrorProperty = errorProperty;
    }

    public void setFailureProperty(final String failureProperty) {
        fFailureProperty = failureProperty;
    }

    public String getFailureProperty() {
        return fFailureProperty;
    }

    public String getErrorProperty() {
        return fErrorProperty;
    }

    public String getDefaultPropertyType() {
        return fDefaultPropertyType;
    }

    public void setDefaultPropertyType(final String type) {
        fDefaultPropertyType = type;
    }

    public void setShowhtmlparseroutput(final boolean showParserOutput) {
        fShowHtmlParserOutput = showParserOutput;
    }

    public boolean isShowHtmlParserOutput() {
        return fShowHtmlParserOutput;
    }

    public boolean isHaltOnFailure() {
        return fHaltOnFailure;
    }

    public boolean isHaltOnError() {
        return fHaltOnError;
    }

    public Map getParameterDictionary() {
        final Map map = new HashMap();
        map.put("host", getHost());
        map.put("protocol", getProtocol());
        map.put("port", String.valueOf(getPort()));
        map.put("timeout", String.valueOf(getTimeout()));
        map.put("basepath", getBasePath());
        map.put("resultpath", getResultpath());
        map.put("saveresponse", isSaveResponse() ? "yes" : "no");
        map.put("saveprefix", getSavePrefix());
        map.put("haltonerror", isHaltOnError() ? "yes" : "no");
        map.put("haltonfailure", isHaltOnFailure() ? "yes" : "no");
        MapUtil.putIfNotNull(map, "errorproperty", getErrorProperty());
        MapUtil.putIfNotNull(map, "failureproperty", getFailureProperty());
        MapUtil.putIfNotNull(map, "defaultpropertytype", getDefaultPropertyType());
        map.put("autorefresh", getAutoRefresh());
        map.put("showhtmlparseroutput", isShowHtmlParserOutput() ? "yes" : "no");
        map.put("resultfile", getResultFile());
        map.put("browser", getBrowser());
        // todo add new props for reporting

        return map;
    }

    public String getResultFile() {
        return fResultFile;
    }

    public void setResultfile(final String resultFile) {
//        fResultFile = resultFile;
    }

    public void setPropertyHandler(final IPropertyHandler propertyHandler) {
        fPropertyHandler = propertyHandler;
    }

    public String getExternalProperty(final String name) {
        if (fPropertyHandler == null) {
            throw new IllegalStateException("No property handler configured!");
        }
        return fPropertyHandler.getProperty(name);
    }

    /**
     * Returns true if the client should automatically follow page refresh requests.<p>
     */
    public String getAutoRefresh() {
        return fAutoRefresh;
    }

    /**
     * Determines if the client should automatically follow page refresh requests.<p>
     *
     * @param str the new refresh value
     */
    public void setAutoRefresh(final String str) {
        fAutoRefresh = str;
    }

    /**
     * Defines the context. This is called to set the context on the task before {@link #execute()}
     * is called
     *
     * @param context the context
     */
    public void setContext(final Context context) {
        fContext = context;
    }

    /**
     * Gets the context for the current webtest
     */
    public Context getContext() {
        return fContext;
    }

    /**
     * Configures the webclient used for the test
     */
    public WebClient createWebClient() {
        final Configuration cfg = this;
        final String strUserAgent = cfg.getUserAgent();
        final BrowserVersion browserVersion = setupBrowserVersion(fBrowser, strUserAgent);
        fBrowser = browserVersion.getNickname(); // to see it in the reports
        final WebClient webClient = setupWebClient(browserVersion);

        webClient.setTimeout(getTimeout() * 1000);

        setupHtmlParser(webClient, cfg);
        setupRefreshHandler(webClient, cfg); // auto refresh settings
        webClient.setThrowExceptionOnScriptError(true); // option for this and report js errors?
        try {
            setupSSLIfNeeded(webClient);
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        setupHttpHeaders(webClient, cfg);
        setupOptions(webClient, cfg);

        configurePageCreator(webClient);

        return webClient;
    }

    private void setupSSLIfNeeded(WebClient webClient) throws GeneralSecurityException {
        if (shouldUseClientCert()) {
            webClient.setSSLClientCertificate(getCertUrl(), getCertPassword(), getCertType());
        }
        if (getUseInsecureSSL()) {
            webClient.setUseInsecureSSL(getUseInsecureSSL());
        }
    }

    boolean shouldUseClientCert() {
        return getCertUrl() != null;
    }

    URL getCertUrl() {
        String pathOrURL = getSslKeyStore();

        if (pathOrURL == null) {
            return null;
        }

        try {
            File certFile = new File(pathOrURL);
            if (certFile.exists()) {
                return certFile.toURI().toURL();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            return new URL(pathOrURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    String getCertPassword() {
        return getSslKeyStorePassword();
    }

    String getCertType() {
        String type = getSslKeyStoreType();
        return type == null ? "jks" : type;
    }

    /**
     * Configures WebTest's custom page creator able to create PDFPage (as long as this is not integrated
     * directly into HtmlUnit)
     */
    protected void configurePageCreator(final WebClient webClient) {
        webClient.setPageCreator(new PdfAwarePageCreator());
    }

    static BrowserVersion setupBrowserVersion(final String browserName, final String strUserAgent) {
        BrowserVersion browserVersion = null;

        if (browserName != null) {
            final String browserNameLC = browserName.toLowerCase().trim();
            if ("ff3".equals(browserNameLC) || "firefox3".equals(browserNameLC))
                browserVersion = BrowserVersion.FIREFOX_3;
            else if ("ff3.6".equals(browserNameLC) || "firefox3.6".equals(browserNameLC))
                browserVersion = BrowserVersion.FIREFOX_3_6;
            else if ("ff10".equals(browserNameLC) || "firefox10".equals(browserNameLC))
                browserVersion = BrowserVersion.FIREFOX_10;
            else if ("ie6".equals(browserNameLC) || "internetexplorer6".equals(browserNameLC))
                browserVersion = BrowserVersion.INTERNET_EXPLORER_6;
            else if ("ie7".equals(browserNameLC) || "internetexplorer7".equals(browserNameLC))
                browserVersion = BrowserVersion.INTERNET_EXPLORER_7;
            else if ("ie8".equals(browserNameLC) || "internetexplorer8".equals(browserNameLC))
                browserVersion = BrowserVersion.INTERNET_EXPLORER_8;
            else
                throw new IllegalArgumentException("Illegal browser version: >" + browserName + "<");
        }
        if (strUserAgent != null) {
            // as long as all browser properties are not configurable from the task,
            // use a "base" browser
            final BrowserVersion baseBrowser;

            if (browserVersion != null) {
                baseBrowser = browserVersion;
            } else if (strUserAgent.indexOf("Gecko") != -1) {
                baseBrowser = BrowserVersion.FIREFOX_3;
            } else {
                baseBrowser = BrowserVersion.INTERNET_EXPLORER_6;
            }

            browserVersion = new BrowserVersion(baseBrowser.getApplicationName(),
                    baseBrowser.getApplicationVersion(), strUserAgent,
                    baseBrowser.getBrowserVersionNumeric());
            LOG.info("Using browser version (" + browserVersion.getApplicationName() + ", "
                    + browserVersion.getApplicationVersion() + ", " + strUserAgent + ", "
                    + ", " + browserVersion.getBrowserVersionNumeric()
                    + "). If the javascript support is not as expected, then it's time to go into the sources");
        } else if (browserVersion == null) { // nothing specified
            browserVersion = BrowserVersion.INTERNET_EXPLORER_6;
            LOG.info("Surfing with default browser " + browserVersion.getUserAgent());
        } else {
            LOG.info("Surfing with browser " + browserVersion.getNickname());
        }

        return browserVersion;
    }

    // Paul Devine 4/12/2005: adding proxy support (for now only UsernamePasswordCredentials. For a proxy
    // that does not require authentication the credentials will not be consulted anyway, so it's ok to
    // leave user/password null or blank in those situations.)
    // package protected for testing purposes
    static WebClient setupWebClient(final BrowserVersion browserVersion) {
        final WebClient webClient;
        final DefaultCredentialsProvider credentialProvider = new DefaultCredentialsProvider();
        String proxyHost = System.getProperty("http.proxyHost");
        if (proxyHost != null && proxyHost.length() > 0) {
            // the properties are set for instance by org.apache.tools.ant.taskdefs.optional.net.SetProxy

            // the proxy setting
            final int proxyPort = Integer.parseInt(System.getProperty("http.proxyPort", "80"));
            LOG.info("Configuring proxy from http.proxyHost* system properties: "
                    + proxyHost + ":" + proxyPort);
            webClient = new WebClient(browserVersion, proxyHost, proxyPort);

            configureProxy(webClient, credentialProvider);
        } else {
            webClient = new WebClient(browserVersion);
        }

        webClient.setCredentialsProvider(credentialProvider);

        return webClient;
    }

    /**
     * Configures the proxy settings from the system properties
     */
    static void configureProxy(final WebClient webClient, final DefaultCredentialsProvider credentialProvider) {
        // the non proxy hosts if any
        final String nonProxyHostsSetting = System.getProperty("http.nonProxyHosts");
        LOG.info("Configuring proxy from http.nonProxyHosts system property: " + nonProxyHostsSetting);
        if (nonProxyHostsSetting != null) {
            final String[] nonProxyHosts = nonProxyHostsSetting.split("\\|");
            for (int i = 0; i < nonProxyHosts.length; ++i) {
                String nonProxyHost = nonProxyHosts[i];
                nonProxyHost = nonProxyHost.replaceAll("\\.", "\\\\."); // escape "."
                nonProxyHost = nonProxyHost.replaceAll("\\*", ".*"); // give regex meaning to *
                LOG.debug("addHostsToProxyBypass: >" + nonProxyHost + "<");
                webClient.getProxyConfig().addHostsToProxyBypass(nonProxyHost);
            }
        }

        // does the proxy need authentification?
        if (System.getProperty("http.proxyUser") != null) {
            final String proxyUser = System.getProperty("http.proxyUser");
            final String proxyPassword = System.getProperty("http.proxyPassword");
            LOG.info("Configuring proxy credentials from http.proxyHost* system properties: "
                    + proxyUser + ", " + proxyPassword);
            credentialProvider.addCredentials(proxyUser, proxyPassword);
        }
    }

    private static void setupHtmlParser(final WebClient webClient, final Configuration cfg) {// Sets a collector to catch parser warnings in order to report
        // misformed/invalid HTML in responses
        if (cfg.isShowHtmlParserOutput()) {
            webClient.setHTMLParserListener(new HtmlParserMessage.MessageCollector());
            LOG.debug("Configured a parser listener to collect messages generated while parsing html");
        } else {
            LOG.debug("showHtmlParserOutput is off, no listener configured");
        }
    }

    private static void setupRefreshHandler(final WebClient webClient, final Configuration cfg) {
        final boolean bUseDelay;
        final boolean bRefreshAll;
        final int acceptedRefreshDelay;
        if (cfg.getAutoRefresh().matches("\\d+")) {
            bUseDelay = true;
            bRefreshAll = false;
            acceptedRefreshDelay = Integer.parseInt(cfg.getAutoRefresh());
        } else {
            bUseDelay = false;
            bRefreshAll = Project.toBoolean(cfg.getAutoRefresh());
            acceptedRefreshDelay = 0;
        }

        LOG.debug("Configuring RefreshHandler (refreshAll: " + bRefreshAll + ", useDelay: " + bUseDelay + ", refreshDelay: " + acceptedRefreshDelay);
        final RefreshHandler refreshHandler = new RefreshHandler() {
            public void handleRefresh(final Page page, final URL url, final int iTimeBeforeRefresh) throws IOException {
                final boolean bRefresh = bRefreshAll || (bUseDelay && iTimeBeforeRefresh <= acceptedRefreshDelay);
                if (bRefresh) {
                    LOG.info("Performing refresh to " + url + " (delay: " + iTimeBeforeRefresh + ") according to configuration");
                    final WebWindow window = page.getEnclosingWindow();
                    if (window == null) {
                        return;
                    }
                    final WebClient client = window.getWebClient();
                    client.getPage(window, new WebRequest(url));
                } else {
                    LOG.info("no refresh performed to " + url + " (delay: " + iTimeBeforeRefresh + ") according to configuration");
                }
            }

        };

        webClient.setRefreshHandler(refreshHandler);
    }

    /**
     * Sets the http headers configured through the &lt;header/&gt; subelements of
     * &lt;config&gt;
     */
    private static void setupHttpHeaders(final WebClient webClient, final Configuration cfg) {
        if (cfg.getHeaderList().size() > 0) {
            LOG.info("Configuring " + cfg.getHeaderList().size() + " HTTP header field(s)");
        }
        // default value for Accept-Language (gets overwritten below if configured in headers)
        webClient.addRequestHeader("Accept-Language", "en-us,en;q=0.5");

        for (final Iterator iter = cfg.getHeaderList().iterator(); iter.hasNext(); ) {
            final Header header = (Header) iter.next();

            if ("User-Agent".equals(header.getName())) {
                LOG.info("Skipped User-Agent header as it has already been configured in the BrowserVersion");
            } else if ("Cookie".equals(header.getName())) {
                try {
                    webClient.getCookieManager().addCookie(
                            HTMLDocument.buildCookie(header.getValue(),
                                    new URL(cfg.getUrlForPage("/"))));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                webClient.addRequestHeader(header.getName(), header.getValue());
                LOG.info("Configured header \"" + header.getName() + "\": " + header.getValue());
            }
        }
    }


    /**
     * Prepares the underlying browser client using option subelements
     * in the config. Currently calls setXXX methods in the WebClient
     * API. See the HtmlUnit JavaDocs for more details.
     */
    private static void setupOptions(final WebClient webClient, final Configuration cfg) {
        final List options = cfg.getOptionList();
        for (Iterator iter = options.iterator(); iter.hasNext(); ) {
            final Option option = (Option) iter.next();
            boolean found = tryBooleanCallingMethod(option, webClient);
            if (!found) {
                found = tryIntCallingMethod(option, webClient);
            }
            if (!found) {
                LOG.warn("Unknown option <" + option.getName() + ">. Ignored.");
            }
        }
    }

    /**
     * Invokes a setXXX method in response to an option subelement
     * in the config named 'XXX'. Currently only boolean values are supported.
     *
     * @param option
     * @param optionObject
     * @return <code>true</code> if option has been successfully set
     */
    private static boolean tryBooleanCallingMethod(final Option option, final Object optionObject) {
        final Class[] booleanClass = {boolean.class};
        try {
            tryCallMethod(optionObject, option, booleanClass, new Object[]{Boolean.valueOf(option.getValue())});
            return true;
        } catch (Exception e) {
            LOG.info("Exception while trying to set boolean option: " + e.getMessage());
            return false;
        }
    }

    /**
     * Invokes a setXXX method in response to an option subelement
     * in the config named 'XXX'. Currently only boolean values are supported.
     *
     * @param option
     * @param optionObject
     * @return <code>true</code> if option has been successfully set
     */
    private static boolean tryIntCallingMethod(final Option option, final Object optionObject) {
        final Class[] intClass = {int.class};
        try {
            tryCallMethod(optionObject, option, intClass, new Object[]{Integer.valueOf(option.getValue())});
            return true;
        } catch (Exception e) {
            LOG.info("Exception while trying to set integer option: " + e.getMessage());
            return false;
        }
    }

    private static void tryCallMethod(final Object optionObject, final Option option,
                                      final Class[] typeSpec, final Object[] params) throws Exception {
        final Method method = optionObject.getClass().getDeclaredMethod("set" + option.getName(), typeSpec);
        method.invoke(optionObject, params);
        LOG.info("set option <" + option.getName() + "> to value <" + option.getValue() + ">");
    }

    /**
     * Gets the timeout in seconds
     *
     * @return the timeout (default value is {@link #DEFAULT_TIMEOUT}).
     */
    public int getTimeout() {
        return fTimeout;
    }

    /**
     * Sets the timeout
     *
     * @param timeout the new value (in seconds), set <code>0</code> for no timeout
     */
    public void setTimeout(final int timeout) {
        fTimeout = timeout;
    }

    /**
     * Experimental.
     * Indicates if WebTest should wait for completion of background js tasks after each step.
     * This should become true per default once HtmlUnit has the necessary functionality to
     * control this which is not yet the case with HtmlUnit 1.14.
     * This feature is intentionally undocumented.
     *
     * @param b the new value
     */
    public void setEasyAjax(final boolean b) {
        fEasyAjax = b;
    }

    public boolean isEasyAjax() {
        return fEasyAjax;
    }

    /**
     * Experimental.
     * See {@link #setEasyAjax(boolean)}
     *
     * @param delay the new value
     */
    public void setEasyAjaxDelay(int delay) {
        fEasyAjaxDelay = delay;
    }

    /**
     * Experimental. Default value is 2000
     */
    public int getEasyAjaxDelay() {
        return fEasyAjaxDelay;
    }

    /**
     * Defines if insecure SSL should be used, ie that certificates should not be validated
     * and therefore outdated or self-signed certificates accepted.
     *
     * @param insecureSSL the new value
     */
    public void setUseInsecureSSL(final boolean insecureSSL) {
        fUseInsecureSSL_ = insecureSSL;
    }

    /**
     * Indicate if insecure SSL should be used, ie that certificates should not be validated
     * and therefore outdated or self-signed certificates accepted.
     *
     * @return the value (default is <code>false</code>)
     */
    public boolean getUseInsecureSSL() {
        return fUseInsecureSSL_;
    }

    public String getSslKeyStore() {
        return sslKeyStore;
    }

    /**
     * Defines the path of the keystore file that is used to load the certificates for client authentication
     *
     * @param sslKeyStore
     */
    public void setSslKeyStore(String sslKeyStore) {
        this.sslKeyStore = sslKeyStore;
    }

    /**
     * @return the path of the keystore file that is used to load the certificates for client authentication
     */
    public String getSslKeyStoreType() {
        return sslKeyStoreType;
    }

    /**
     * Defines the type of the keystore that is used to load the certificates for client authentication
     *
     * @param sslKeyStoreType
     */
    public void setSslKeyStoreType(String sslKeyStoreType) {
        this.sslKeyStoreType = sslKeyStoreType;
    }

    /**
     * @return the type of the keystore that is used to load the certificates for client authentication
     */
    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public void setSslKeyStorePassword(String sslKeyStorePassword) {
        this.sslKeyStorePassword = sslKeyStorePassword;
    }

    public String getSslTrustStore() {
        return sslTrustStore;
    }

    public void setSslTrustStore(String sslTrustStore) {
        this.sslTrustStore = sslTrustStore;
    }

    public String getSslTrustStorePassword() {
        return sslTrustStorePassword;
    }

    public void setSslTrustStorePassword(String sslTrustStorePassword) {
        this.sslTrustStorePassword = sslTrustStorePassword;
    }

    /**
     * Experimental: indicates if WebTest should try to work only on displayed elements.
     * Currently not all WebTest steps respect this configuration option.
     * No documentation is generated as long as it is experimental.
     *
     * @since Build 1783
     */
    public void setRespectVisibility(final boolean b) {
        fRespectVisibility_ = b;
    }

    /**
     * Experimental: indicates if WebTest tries to work only on displayed elements.
     * Currently not all WebTest steps respect this configuration option.
     *
     * @return default is <code>false</code>
     * @since Build 1783
     */
    public boolean isRespectVisibility() {
        return fRespectVisibility_;
    }

    /**
     * Configures the browser that should be simulated in the test
     *
     * @param browser the browser name like "Firefox3" or "FF3"
     */
    public void setBrowser(final String browser) {
        fBrowser = browser;
    }

    /**
     * Gets the configured browser
     *
     * @return <code>null</code> if nothing has been configured
     */
    public String getBrowser() {
        return fBrowser;
    }
}