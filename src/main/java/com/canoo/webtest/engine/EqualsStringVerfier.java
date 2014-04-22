// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;


/**
 * @author Carsten Seibert
 */
public class EqualsStringVerfier implements IStringVerifier
{
    public static final IStringVerifier INSTANCE = new EqualsStringVerfier();

    public boolean verifyStrings(final String expectedValue, final String actualValue) {
		return expectedValue != null && expectedValue.equals(actualValue);
	}
}
