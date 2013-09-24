package org.aksw.rex.controller;

public class RexPropertiesWithGoldstandard {
	public RexPropertiesWithGoldstandard(String xpath, String propertyURL, String extractionDomainURL) {
		Xpath = xpath;
		PropertyURL = propertyURL;
		ExtractionDomainURL = extractionDomainURL;
	}

	private String Xpath;
	private String PropertyURL;
	private String ExtractionDomainURL;

	public String getXpath() {
		return Xpath;
	}

	public void setXpath(String xpath) {
		Xpath = xpath;
	}

	public String getPropertyURL() {
		return PropertyURL;
	}

	public void setPropertyURL(String propertyURL) {
		PropertyURL = propertyURL;
	}

	public String getExtractionDomainURL() {
		return ExtractionDomainURL;
	}

	public void setExtractionDomainURL(String extractionDomainURL) {
		ExtractionDomainURL = extractionDomainURL;
	}
}
