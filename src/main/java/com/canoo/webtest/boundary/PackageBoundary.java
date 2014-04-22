// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.boundary;

/**
 * Boundary class for interacting with Packages.
 *
 * @author Paul King
 * @author Denis N. Antonioli
 */
public final class PackageBoundary {

	private static final Package PACKAGE = PackageBoundary.class.getPackage();

	private PackageBoundary() {
	}

	public static boolean hasVersionInformation() {
		return PACKAGE != null;
	}

	public static String getImplementationTitle() {
		if (!hasVersionInformation() || PACKAGE.getImplementationTitle() == null) {
			return "Canoo Webtest";
		}
		return PACKAGE.getImplementationTitle();
	}

	public static String getImplementationVersion() {
		if (!hasVersionInformation() || PACKAGE.getImplementationVersion() == null) {
			return "development";
		}
		return PACKAGE.getImplementationVersion();
	}

	public static String versionMessage() {
		if (!hasVersionInformation()) {
			return "Unknown Webtest version.";
		}
		final StringBuffer sb = new StringBuffer();
	   sb.append(getImplementationTitle());
	   sb.append(": ").append(getImplementationVersion());
		sb.append(".");
		return sb.toString();

	}
}
