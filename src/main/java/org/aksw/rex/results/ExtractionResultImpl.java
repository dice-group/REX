package org.aksw.rex.results;

public class ExtractionResultImpl implements ExtractionResult {

    private String subject;
    private String object;

    @Override
	public String getSubject() {
        return subject;
    }

    @Override
	public String getObject() {
        return object;
    }

    @Override
	public void setSubject(String s) {
        subject = s;
    }

    @Override
	public void setObject(String o) {
        object = o;
    }
    
    public ExtractionResultImpl(String s, String o)
    {
        subject = s;
        object = o;
    }
    
}
