// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.security;

import com.canoo.webtest.engine.Configuration;
import org.apache.log4j.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Initializer for using https with basic authentication, i\.e\.\ no keystore processing is used. Uses the sun
 * truststore for server certificate checking. Hostname verification accepts all hostnames. (you may want to override
 * this)<p> See also <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/jsse/JSSERefGuide.html">JSSE
 * Reference</a>
 *
 * @author Carsten Seibert, Dierk Koenig
 */
public class SunJsseBaseConnectionInitializer extends AbstractConnectionInitializer {
    private static final Logger LOG = Logger.getLogger(SunJsseBaseConnectionInitializer.class);

    /**
     * Doing the initialization for https heavily relies on side effects in shared data, i.e. System properties and static
     * fields in java.security.* and java.net.* .
     */
    public void initializeConnection(final Configuration config) throws ConnectionInitializationException {
        LOG.debug("Using Custom ConnectionInitializer: " + getClass().getName());
        if (isProtocolHttps(config)) {
            if (LOG.isDebugEnabled()) {
                System.setProperty("javax.net.debug", "all");
            }
            logProtocolConfiguration(config);

            installJsseProviderIfRequired(SUN_JSSE_PROVIDER_CLASS);
            setSystemProperty(PROTOCOL_HANDLER_KEY, SUN_SSL_PROTOCOL_HANDLER_PACKAGE);
            if (!config.getUseInsecureSSL()) {
                attemptSetSystemProperty(config, TRUST_STORE_KEY, PROPERTY_TRUSTSTORE_FILE);
                attemptSetSystemProperty(config, TRUST_STORE_PASSWORD_KEY, PROPERTY_TRUSTSTORE_PASSPHRASE);
            }

            // Ordering is important! The trust store is read upon connectionHandler
            // initialization which occurs implicitly when the HostnameVerifier is
            // installed.
            installTrustAndKeyManager(config);
            installHostnameVerifier(config);
        }
    }

    private static void attemptSetSystemProperty(final Configuration config, final String propertyKey, final String propertyName)
            throws ConnectionInitializationException {
        String property = getExternalProperty(config, propertyName);
        if (property == null) {
            throw new ConnectionInitializationException("Property not set: " + propertyName);
        }
        setSystemProperty(propertyKey, property);
    }

    /**
     * Install a customized HostnameVerifier in order to handle mismatches between common name used in the certificate
     * and the actual hostname specified in the URL. Only required if protocol is HTTPS.
     *
     * @param config The current test context.
     */
    protected void installHostnameVerifier(final Configuration config) {
        LOG.info("Installing HostnameVerifier");
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession sslSession) {
                LOG.info("Granting access for " + hostname);
                return true;
            }
        });
    }

    protected void installTrustAndKeyManager(final Configuration config) throws ConnectionInitializationException {// not needed for the standard case
        LOG.debug("No Trust and no Key manager installed.");
    }

    protected void logProtocolConfiguration(final Configuration config) {
        logProperty(config, PROPERTY_TRUSTSTORE_FILE);
        logProperty(config, PROPERTY_TRUSTSTORE_PASSPHRASE);
    }

}
