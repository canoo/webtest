// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.security;


import com.canoo.webtest.engine.Configuration;


/**
 * Initializer for using https with full ssl authentication.
 * Just like Basic Authentication but with client-side certification handling.
 *
 * @author Dierk Koenig
 * @deprecated use new config options instead
 */
public class SunJsseClientAuthConnectionInitializer extends SunJsseBaseConnectionInitializer {
    protected static final String SECURITY_PROVIDER = "SunX509";
    protected static final String KEYSTORE_TYPE = "JKS";
    protected static final String SSL_TYPE = "TLS";

    protected void logProtocolConfiguration(final Configuration config) {
        super.logProtocolConfiguration(config);
        logProperty(config, PROPERTY_KEYSTORE_FILE);
        logProperty(config, PROPERTY_KEYSTORE_PASSPHRASE);
        logProperty(config, PROPERTY_KEYSTORE_TYPE);
    }

    protected void installTrustAndKeyManager(final Configuration config) throws ConnectionInitializationException {
        setSystemProperty("javax.net.ssl.keyStore", getExternalProperty(config, PROPERTY_KEYSTORE_TYPE, PROPERTY_KEYSTORE_FILE));
        setSystemProperty("javax.net.ssl.keyStorePassword", getExternalProperty(config, PROPERTY_KEYSTORE_TYPE, PROPERTY_KEYSTORE_PASSPHRASE));
        setSystemProperty("javax.net.ssl.keyStoreType", getExternalProperty(config, PROPERTY_KEYSTORE_TYPE, KEYSTORE_TYPE));
    }

    private String getExternalProperty(Configuration config, String key, String defaultValue) {
        String value = SunJsseBaseConnectionInitializer.getExternalProperty(config, key);
        return value != null ? value : defaultValue;
    }

}
