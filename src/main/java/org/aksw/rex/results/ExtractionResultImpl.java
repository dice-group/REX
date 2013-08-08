package org.aksw.rex.results;

public class ExtractionResultImpl implements ExtractionResult {

    private String subject;
    private String object;

    public String getSubject() {
        return subject;
    }

    public String getObject() {
        return object;
    }

    public void setSubject(String s) {
        subject = s;
    }

    public void setObject(String o) {
        object = o;
    }
    
    public ExtractionResultImpl(String s, String o)
    {
        subject = s;
        object = o;
    }
    
}
