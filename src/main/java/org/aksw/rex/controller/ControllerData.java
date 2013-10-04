package org.aksw.rex.controller;

public class ControllerData {
	public String index;
	public String dbpediaProperty;
	public String urlDomain;
	public String subjectRule;
	public String objectRule;

	public ControllerData(String index, String dbpediaProperty, String urlDomain, String subjectRule, String objectRule) {
		this.index = index;
		this.dbpediaProperty = dbpediaProperty;
		this.urlDomain = urlDomain;
		this.subjectRule = subjectRule;
		this.objectRule = objectRule;
	}
}
