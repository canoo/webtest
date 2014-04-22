// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.interfaces;

import com.canoo.webtest.engine.Configuration;
import com.canoo.webtest.security.ConnectionInitializationException;

/**
 * @author Carsten Seibert, comments by Dierk Koenig
 */
public interface IConnectionInitializer {
	/**
	 * Doing the initialization for https heavily relies on side effects in shared data, i.e. System properties and
	 * static fields in java.security.* and java.net.* .
	 */
	void initializeConnection(final Configuration config) throws ConnectionInitializationException;
}
