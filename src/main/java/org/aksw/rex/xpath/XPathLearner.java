package org.aksw.rex.xpath;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.aksw.rex.util.Pair;
import org.aksw.rex.results.ExtractionResult;
import org.w3c.dom.xpath.XPathExpression;

import com.hp.hpl.jena.rdf.model.Resource;

public interface XPathLearner {
    List<Pair<XPathExpression, XPathExpression>> getXPathExpressions(Set<Pair<Resource, Resource>> posExamples, Set<Pair<Resource, Resource>> negExamples, URL Domain);
    Set<ExtractionResult> getExtractionResults(List<Pair<XPathExpression, XPathExpression>> expressions);

}
