package com.canoo.webtest.engine;

/**
 * @author Denis N. Antonioli
 */
public class HeaderTest extends NameValuePairTest {
    NameValuePair getNameValuePair() {
        return new Header();
    }
}