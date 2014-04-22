package com.canoo.webtest.plugins.pdftest.htmlunit;

/**
 * Represents a form field in a PDF document
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public interface PDFField {

	public static final Type TEXTBOX = new Type("textbox");
	public static final Type CHECKBOX = new Type("checkbox");
	public static final Type CHOICE_FIELD = new Type("choice field");
	public static final Type PUSH_BUTTON = new Type("push button");
	public static final Type RADIO_COLLECTION = new Type("radio collection");
	public static final Type SIGNATURE = new Type("signature");
	public static final Type OTHER = new Type("other");

	public static final class Type {

		private final String fDescription;

		private Type(final String description) {
			fDescription = description;
		}

		public String toString() {
			return fDescription;
		}
	}

	String getValue();
	String getName();
	boolean isReadOnly();
}
