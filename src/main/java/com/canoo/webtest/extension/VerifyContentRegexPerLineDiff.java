package com.canoo.webtest.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class VerifyContentRegexPerLineDiff extends VerifyContentTextDiff {
	private List patterns;

	protected void preProcess() {
		patterns = new ArrayList(tabReference.length);
		for (int i = 0; i < tabReference.length; i++) 
		{
			try
			{
				patterns.add(Pattern.compile(tabReference[i]));
			}
			catch (final PatternSyntaxException e)
			{
				throw new RuntimeException("Illegal pattern at line " + (i+1) + ": " + e.getMessage());
			}
		}
	}

	protected boolean sameContent(final int posA, final int posB) 
	{
		final Pattern pattern = (Pattern) patterns.get(posA);
		final String lineB = tabActual[posB];
		return pattern.matcher(lineB).matches();
	}
}
