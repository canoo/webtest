package com.canoo.webtest.plugins.pdftest.htmlunit;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.enums.Enum;

/**
 * Represents a permission that may be configured on a PDF document.
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public final class PDFEncryptionPermission extends Enum {
	private static final Map<String, PDFEncryptionPermission> permissions = new HashMap<String, PDFEncryptionPermission>();

	private PDFEncryptionPermission(final String label) {
		super(label);
		permissions.put(label.toLowerCase(), this);
	}

	public final static PDFEncryptionPermission ASSEMBLY = new PDFEncryptionPermission("assembly");
	public final static PDFEncryptionPermission COPY = new PDFEncryptionPermission("copy");
	public final static PDFEncryptionPermission DEGRADED_PRINTING = new PDFEncryptionPermission("degradedPrinting");
	public final static PDFEncryptionPermission FILL_IN = new PDFEncryptionPermission("fillIn");
	public final static PDFEncryptionPermission PRINTING = new PDFEncryptionPermission("printing");
	public final static PDFEncryptionPermission MODIFY_ANNOTATIONS = new PDFEncryptionPermission("modifyAnnotations");
	public final static PDFEncryptionPermission MODIFY_CONTENTS = new PDFEncryptionPermission("modifyContents");
	public final static PDFEncryptionPermission SCREEN_READERS = new PDFEncryptionPermission("screenReaders");
	
	/**
	 * Gets the permission with the given label
	 * @param label (case insensitive)
	 * @return <code>null</code> if not encryption permission is found with this name
	 */
	public static PDFEncryptionPermission get(final String label)
	{
		if (label == null)
			throw new NullPointerException();
		
		return (PDFEncryptionPermission) permissions.get(label.toLowerCase()); 
	}
}
