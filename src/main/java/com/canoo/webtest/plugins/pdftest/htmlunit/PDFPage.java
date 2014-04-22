package com.canoo.webtest.plugins.pdftest.htmlunit;

import java.util.List;

import com.gargoylesoftware.htmlunit.Page;

/**
 * Represents a PDF document.
 * @author Etienne Studer
 * @author Marc Guillemot
 */
public interface PDFPage extends Page {
    String MODE_NORMAL = "normal";
    String MODE_LINES = "groupByLines";

    String getDocumentTitle();
	int getNumberOfPages();
    String getText(int startPage, int endPage);

    public String getText(String fragmentSeparator, String lineSeparator, String pageSeparator, String mode);

    /**
     * 
     * @param password the password to use to decrypt
     * @throws PDFInvalidPasswordException if the password is invalid to decrypt document
     */
	void decrypt(String password);
	boolean isEncrypted();
	/**
	 * Gets the fields with the given name
	 * @param name the field name
	 * @param type the type of fields to look for
	 * @return a list of {@link PDFField}
	 */
	List getFields(String name, PDFField.Type type);
	/**
	 * Gets the fields with the given name
	 * @param name the field name
	 * @return a list of {@link PDFField}
	 */
	List getFields(String name);
	/**
	 * Gets the fields with the given name
	 * @param name the field name
	 * @param pageNumber the page number where the field should be located
	 * @param type the type of fields to look for
	 * @return a list of {@link PDFField}
	 * @throws IllegalArgumentException if the pageNumber is not a valid page number
	 */
	List getFields(String name, int pageNumber, PDFField.Type type);

	/**
	 * Gets the fields with the given name
	 * @param name the field name
	 * @param pageNumber the page number where the field should be located
	 * @return a list of {@link PDFField}
	 * @throws IllegalArgumentException if the pageNumber is not a valid page number
	 */
	List getFields(String name, int pageNumber);

	/**
	 * Gets the fields of the given page
	 * @param pageNumber the page number where the field should be located
	 * @return a list of {@link PDFField}
	 * @throws IllegalArgumentException if the pageNumber is not a valid page number
	 */
	List getFields(int pageNumber);


	/**
	 * Gets all the fields
	 * @return a list of {@link PDFField}
	 */
	List getFields();

	boolean hasPermission(final PDFEncryptionPermission permission);
    String getEncryptProperty(String key);
    public int getEncryptionStrength();
    public String getInfoProperty(String key);
    boolean isUserPassword(String password);
    boolean isOwnerPassword(String password);
    
    /**
     * Gets the hyperlinks contained in the document
     * @return a list of {@link PDFLink}
     */
    List<? extends PDFLink> getLinks();

    /**
     * Gets the bookmarks contained in the document
     * @return a list of {@link PDFBookmark}
     */
    List getBookmarks();

    /**
     * Gets the fonts contained in the document
     * @return a list of {@link PDFFont}
     */
    List getFonts();
}
