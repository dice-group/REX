package org.aksw.rex.results;

public class ExtractionResultImpl implements ExtractionResult {

    private String plainString;

    public ExtractionResultImpl(String plainString) {
        this.plainString = plainString;
    }

    public String getPlainString() {
        return plainString;
    }

    public void setPlainString(String plainString) {
        this.plainString = plainString;
    }

}
