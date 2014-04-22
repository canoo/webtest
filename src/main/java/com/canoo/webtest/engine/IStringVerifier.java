// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;


/**
 * Provides an abstract interface for comparing strings
 *
 * @author Carsten Seibert
 */
public interface IStringVerifier
{

    /**
     * @param expectedValue The expected value of the string
     * @param actualValue The actual value of the string
     *
     * @return true if the two provided strings match according to
     * the caller's criteria.
     */
    boolean verifyStrings(String expectedValue, String actualValue);
}
