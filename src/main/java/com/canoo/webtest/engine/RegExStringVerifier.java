// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.engine;


import java.util.regex.Pattern;


/**
 * Compares string evaluating the expected value as a regular expression
 * @see java.util.regex.Pattern
 * @author Carsten Seibert, Dierk Koenig
 * @author Marc Guillemot
 */
public class RegExStringVerifier implements IStringVerifier
{
    public static final IStringVerifier INSTANCE = new RegExStringVerifier();

    /**
     * @param pattern the pattern to search for
     * @see com.canoo.webtest.engine.IStringVerifier#verifyStrings(java.lang.String, java.lang.String)
     * @return <code>true</code> if the pattern can be found in the actual value
     */
    public boolean verifyStrings(final String pattern, final String actualValue) {
        if (pattern == null) {
            return false;
        }
        // "." should match new lines as well, therefore the dotall flag
        return Pattern.compile(pattern, Pattern.DOTALL).matcher(actualValue).find();
    }
}
