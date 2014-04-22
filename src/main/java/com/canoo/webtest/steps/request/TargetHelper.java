// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.request;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.engine.Context;
import com.canoo.webtest.engine.StepExecutionException;
import com.canoo.webtest.interfaces.IConnectionInitializer;
import com.canoo.webtest.security.ConnectionInitializationException;
import com.canoo.webtest.steps.AbstractBrowserAction;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;

/**
 * This is a helper class for all test specification steps
 * that initiate a request.
 * <p/>
 * It offers common functionality like:
 * preparing the request in case basic authentication is required,
 * handling http error scenarios,
 * reporting and saving away the response if successful.
 *
 * @author Carsten Seibert
 * @author Marc Guillemot
 * @author Paul King
 */
public class TargetHelper implements Serializable {
	private static final Logger LOG = Logger.getLogger(TargetHelper.class);
	public static final String CONNECTION_INITIALIZER_KEY = "webtest.connectioninitializer";
	private String fUserName;
	private String fPassword;
	private final AbstractBrowserAction fStep;

	public TargetHelper(final AbstractBrowserAction step) {
		fStep = step;
	}

	public void setUsername(final String userName) {
		fUserName = userName;
	}

	public void setPassword(final String password) {
		fPassword = password;
	}

	/**
	 * Hook for test stubs in order to avoid invocation of a real request
	 *
	 * @param context The current test context.
	 * @param strUrl  The URL to invoke.
	 */
	public Page getResponse(final Context context, final String strUrl) throws IOException, SAXException {
		LOG.info("getting response for url: " + strUrl);
		prepareConversationIfNeeded(context);
		final URL url = new URL(strUrl);
		return context.getWebClient().getPage(url);
	}

	public Page getResponse(final Context context, final WebRequest settings) throws IOException, SAXException {
		LOG.info("getting response for url: " + settings.getUrl());
		prepareConversationIfNeeded(context);
		return context.getWebClient().getPage(settings);
	}

	/**
	 * Prepare the specified WebConversation for a request by setting
	 * user and password (if specified) and install a custom HostnameVerifier
	 * if required by the currently used protocol.
	 *
	 * @param context The current test context.
	 */
	protected void prepareConversationIfNeeded(final Context context) {
		if (context.isPrepared()) {
			LOG.debug("Conversation already prepared - attempting to skip preparation");
			if (!hasSuppliedCredentials()) {
				return; // already set up, keep https session throughout webtest
			}
			if (hasSuppliedCredentials() && fUserName.equals(context.getSavedUserName())
				&& fPassword.equals(context.getSavedPassword())) {
				return; // same username and password, nothing to do
			}
			LOG.debug("Conversation already prepared - but username and password have changed - continuing preparation");
		}
		if (hasSuppliedCredentials()) {
			LOG.info("Setting password for username: " + fUserName);
			// credentials provider has already been set as DefaultCredentialsProvider
			// in Setup (may already contain credentials for proxy)
			final DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
			context.getWebClient().setCredentialsProvider(credentialsProvider);
			credentialsProvider.addCredentials(fUserName, fPassword);
			context.setSavedUserName(fUserName);
			context.setSavedPassword(fPassword);
		}
		
		final Configuration config = context.getConfig();
		final String customInitializerClassName = config.getExternalProperty(CONNECTION_INITIALIZER_KEY);
		if (customInitializerClassName != null) {
			invokeCustomInitializer(context, customInitializerClassName);
		}
		context.setPrepared(true);
	}

	private boolean hasSuppliedCredentials() {
		return fUserName != null && fPassword != null;
	}

	protected void invokeCustomInitializer(final Context context, final String customInitializerClassName) {
		LOG.info("Using custom initializer: " + customInitializerClassName);
		try {
			final Object object = Class.forName(customInitializerClassName).newInstance();
			final IConnectionInitializer customInitializer = (IConnectionInitializer) object;
			customInitializer.initializeConnection(context.getConfig());
		} catch (final ConnectionInitializationException e) {
			final Throwable throwme = new StepExecutionException("ConnectionInitializer raised exception: " + e.getMessage(), fStep);
			throw (RuntimeException) throwme.fillInStackTrace();
		} catch (final Exception e) {
			LOG.info("Root exception from Connection Initializer", e);
			final Throwable throwme = new StepExecutionException("Exception raised while trying to create custom ConnectionInitializer <"
				+ customInitializerClassName + "> / Exception: " + e, fStep);
			throw (RuntimeException) throwme.fillInStackTrace();
		}
	}
}
