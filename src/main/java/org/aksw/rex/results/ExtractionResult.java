package org.aksw.rex.results;

public interface ExtractionResult {
    /**
     * 
     * @return String extracted via XPath
     */

    public String getPlainString();

    /**
     * 
     * @param plainString
     *            extracted via XPath
     */
    public void setPlainString(String plainString);
}
