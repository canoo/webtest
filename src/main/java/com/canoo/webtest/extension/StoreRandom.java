// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.extension;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.canoo.webtest.steps.store.BaseStoreStep;
import com.canoo.webtest.util.ConversionUtil;

/**
 * Stores a random value (number, string or token) into a property.<p>
 * <p/>
 * Either ant or dynamic properties are supported. The random values can
 * be used when invoking subsequent steps and checking responses.
 *
 * @author Paul King, ASERT
 * @webtest.step category="Extension"
 * name="storeRandom"
 * description="Provides the ability to store a random number, string or token value for later processing. Useful to avoid setting (and maintaining) large numbers of properties containing test data when specific test values aren't important."
 */
public final class StoreRandom extends BaseStoreStep {
    private static final Logger LOG = Logger.getLogger(StoreRandom.class);
    private static final int MODE_NUMBER = 1;
    private static final int MODE_STRING = 2;
    private static final int MODE_CHOICE = 3;
    private static final String DEFAULT_CHARS = " abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ 0123456789 ";

    private String fFrom;
    private String fTo;
    private String fLength;
    private String fChars;
    private String fChoice;
    private int fMode;

    public String getFrom() {
        return fFrom;
    }

    /**
     * @webtest.parameter required="yes/no"
     * description="Used when storing a random number. The random number stored will be greater than or equal to this number. Required if <em>to</em> is set. Parameters <em>length</em> and <em>choice</em> must be empty."
     */
    public void setFrom(String from) {
        fFrom = from;
    }

    public String getTo() {
        return fTo;
    }

    /**
     * @webtest.parameter required="yes/no"
     * description="Used when storing a random number. The random number stored will be less than or equal to this number. Required if <em>from</em> is set. Parameters <em>length</em> and <em>choice</em> must be empty."
     */
    public void setTo(final String to) {
        fTo = to;
    }

    public String getLength() {
        return fLength;
    }

    /**
     * @webtest.parameter required="yes/no"
     * description="Used when storing a random string. The length of the random string to store (i.e. the number of characters to randomly select). Parameters <em>from</em>, <em>to</em> and <em>choice</em> must be empty."
     */
    public void setLength(final String length) {
        fLength = length;
    }

    public String getChars() {
        return fChars;
    }

    /**
     * @webtest.parameter required="no"
     * description="Used when storing a random string. The set of characters to choose from when creating the random string. Ignored unless <em>length</em> is set."
     * default="the upper and lowercase alphabets plus numbers plus spaces"
     */
    public void setChars(final String chars) {
        fChars = chars;
    }

    public String getChoice() {
        return fChoice;
    }

    /**
     * @webtest.parameter required="yes/no"
     * description="Used when storing a random token. The comma delimited choice of tokens to randomly select between. Parameters <em>from</em>, <em>to</em> and <em>length</em> must be empty."
     */
    public void setChoice(final String choice) {
        fChoice = choice;
    }

    public void doExecute() {
        String randString;
        switch (fMode) {
            case MODE_NUMBER:
                int fromInt = ConversionUtil.convertToInt(getFrom(), -1);
                int toInt = ConversionUtil.convertToInt(getTo(), -1);
                paramCheck(fromInt >= toInt, "Parameter \"from\" must be less than parameter \"to\"!");
                randString = generateRandomNumber(fromInt, toInt);
                break;
            case MODE_CHOICE:
                StringTokenizer st = new StringTokenizer(getChoice(), ",");
                paramCheck(st.countTokens() < 1, "Parameter \"choice\" must have some comma-delimited choices!");
                randString = generateRandomToken(st);
                break;
            default: // MODE_STRING
                int numChars = ConversionUtil.convertToInt(getLength(), 0);
                if (getChars() == null) {
                    randString = generateRandomChars(numChars, DEFAULT_CHARS);
                } else {
                    randString = generateRandomChars(numChars, getChars());
                }
                break;
        }
        storeProperty(randString);
    }

    private static String generateRandomToken(final StringTokenizer st) {
        String randString = "";
        for (int pos = (int) (Math.random() * st.countTokens()) + 1; pos-- > 0;) {
            randString = st.nextToken();
        }
        return randString;
    }

    private static String generateRandomNumber(int fromInt, int toInt) {
        int randValue = fromInt + (int) (Math.random() * (toInt - fromInt + 1));
        return Integer.toString(randValue);
    }

    private static String generateRandomChars(int numChars, final String chars) {
        StringBuffer randBuf = new StringBuffer();
        int l = chars.length();
        for (int i = 0; i < numChars; i++) {
            int pos = (int) (l * Math.random());
            randBuf.append(chars.charAt(pos));
        }
        return randBuf.toString();
    }

    /**
     * Verifies the parameters
     */
    protected void verifyParameters() {
        super.verifyParameters();
        nullParamCheck(getProperty(), "property");
        fMode = 0;
        if (isOnlyLengthSet()) {
            fMode = MODE_STRING;
            integerParamCheck(getLength(), "length", true);
            LOG.debug("Mode = MODE_STRING");
        } else if (isOnlyFromAndToSet()) {
            fMode = MODE_NUMBER;
            integerParamCheck(getFrom(), "from", true);
            integerParamCheck(getTo(), "to", true);
            LOG.debug("Mode = MODE_NUMBER");
        } else if (isOnlyChoiceSet()) {
            fMode = MODE_CHOICE;
            LOG.debug("Mode = MODE_CHOICE");
        }
        paramCheck(fMode == 0, "Required parameter(s) not set correctly! Need (\"from\" and \"to\") or \"length\" or \"choice\".");
    }

    private boolean isOnlyChoiceSet() {
        return getChoice() != null && getLength() == null && getFrom() == null && getTo() == null;
    }

    private boolean isOnlyFromAndToSet() {
        return getFrom() != null && getTo() != null && getChoice() == null && getLength() == null;
    }

    private boolean isOnlyLengthSet() {
        return getFrom() == null && getTo() == null && getChoice() == null && getLength() != null;
    }

}
