// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.security;


import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.interfaces.IConnectionInitializer;
import org.apache.log4j.Logger;

import java.security.Provider;
import java.security.Security;


/**
 * @author Carsten Seibert, Dierk Koenig
 */
public abstract class AbstractConnectionInitializer extends SecurityConstants implements IConnectionInitializer {
	private static final Logger LOG = Logger.getLogger(AbstractConnectionInitializer.class);
	protected static final String SUN_JSSE_PROVIDER_CLASS = "com.sun.net.ssl.internal.ssl.Provider";
	protected static final String SUN_SSL_PROTOCOL_HANDLER_PACKAGE = "com.sun.net.ssl.internal.www.protocol";
	protected static final String PROTOCOL_HANDLER_KEY = "java.protocol.handler.pkgs";
	protected static final String TRUST_STORE_KEY = "javax.net.ssl.trustStore";
	protected static final String TRUST_STORE_PASSWORD_KEY = "javax.net.ssl.trustStorePassword";

	public static String getExternalProperty(final Configuration config, final String propertyName) {
		return config.getExternalProperty(propertyName);
	}

	protected static void logProperty(final Configuration config, final String propertyName) {
		LOG.debug("Ext property: " + propertyName + "=" + getExternalProperty(config, propertyName));
	}

	public static boolean isProtocolHttps(final Configuration config) {
		return Configuration.PROTOCOL_HTTPS.equals(config.getProtocol());
	}

	protected static boolean hasProvider(final Class providerClass) {
		LOG.debug("Looking for provider class " + providerClass);

		final Provider[] list = Security.getProviders();
		for (int i = 0; i < list.length; i++) {
			final Provider provider = list[i];
			LOG.debug("Checking: " + providerInfoString(provider));
			if (provider.getClass().equals(providerClass)) {
				return true;
			}
		}
		return false;
	}

	static void logSecurityProviders() {
		Provider[] providers = Security.getProviders();
		for (int i = 0; i < providers.length; i++) {
			LOG.info("Security Provider " + i + ": " + providerInfoString(providers[i]));
		}
	}

	private static String providerInfoString(final Provider provider) {
		StringBuffer sb = new StringBuffer();
		sb.append(provider.getName());
		sb.append("\n\t").append(provider.getInfo());
		sb.append("\n\t").append(provider.getClass().getName());
		sb.append("\n\tversion: ").append(provider.getVersion());
		return sb.toString();
	}

	protected static void installJsseProviderIfRequired(final String providerClassName) throws ConnectionInitializationException {
		Class providerClass;

		try {
			providerClass = Class.forName(providerClassName);
		} catch (ClassNotFoundException e) {
			throw new ConnectionInitializationException("Class " + providerClassName + "not found! Is JSSE correctly installed?");
		}
		if (!hasProvider(providerClass)) {
			try {
				LOG.info(providerClassName + " not present. Current Providers are:");
				logSecurityProviders();
				Security.addProvider((Provider) providerClass.newInstance());
				LOG.info(providerClassName + " added. Providers are now:");
				logSecurityProviders();
			} catch (Exception e) {
				String message = "Can not instantiate class " + providerClassName + "!";

				LOG.info(message, e);
				throw new ConnectionInitializationException(message);
			}
		}
	}

	protected static void setSystemProperty(final String key, final String value) {
		LOG.debug("Set " + key + " to " + value + ", was " + System.setProperty(key, value));
	}
}
