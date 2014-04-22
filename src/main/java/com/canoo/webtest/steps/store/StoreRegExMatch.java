// Copyright © 2002-2005 Canoo Engineering AG, Switzerland.
package com.canoo.webtest.steps.store;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.canoo.webtest.engine.StepFailedException;
import com.canoo.webtest.util.ConversionUtil;

/**
 * @author Carsten Seibert
 * @author Marc Guillemot
 * @author Paul King
 * @webtest.step category="Core"
 * name="storeRegEx"
 * alias="storeregex"
 * description="This step stores the result of a <key>regex</key> into a property. By specifying a particular group of the <key>regex</key> it is easily to extract only a subset of a given <key>regex</key>."
 */
public class StoreRegExMatch extends BaseStoreStep {
    private String fText;
    private String fGroup;

    public String getText() {
        return fText;
    }

    /**
     * @webtest.parameter required="true"
     * description="The <key>regex</key> that shall be evaluated."
     */
    public void setText(final String text) {
        fText = text;
    }


    /**
     * @webtest.parameter required="true"
     * description="The name of the property that shall receive the extracted value."
     */
    public void setProperty(final String property) {
        super.setProperty(property);
    }

    /**
     * @webtest.parameter required="no"
     * default="0 (result of the whole expression)"
     * description="The group within the <key>regex</key> to extract into the specified property."
     */
    public void setGroup(final String group) {
        fGroup = group;
    }

    public void doExecute() throws IOException {
        nullResponseCheck();
        final String text = getContext().getCurrentResponse().getWebResponse().getContentAsString(); // possible IOException

        // "." should match new lines as well, therefore the dotall flag
        final Matcher matcher = Pattern.compile(getText(), Pattern.DOTALL).matcher(text);
        if (!matcher.find()) {
            throw new StepFailedException("No match for regular expression <" + getText() + ">", this);
        }

        final int numberOfGroups = matcher.groupCount();

        int groupNumber = ConversionUtil.convertToInt(fGroup, 0);
        if (groupNumber > numberOfGroups) {
            throw new StepFailedException("Group not found: " + fGroup + " (#groups: " + numberOfGroups + ")", this);
        }

        storeProperty(matcher.group(groupNumber));
    }

    protected void verifyParameters() {
        super.verifyParameters();
        optionalIntegerParamCheck(fGroup, "group", true);
        emptyParamCheck(getText(), "Regular expression (text attribute)");
        emptyParamCheck(getProperty(), "property");
	}
}
