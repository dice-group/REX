package org.aksw.rex.xpath;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.rex.results.ExtractionResult;
import org.aksw.rex.util.Pair;

import com.hp.hpl.jena.rdf.model.Resource;

public interface XPathLearner {
    Map<XPathExtractionRule, Double> getXPathExpressions(Set<Pair<Resource, Resource>> posExamples, Set<Pair<Resource, Resource>> negExamples, URL Domain);
    
    
    Set<ExtractionResult> getExtractionResults(List<XPathExtractionRule> expressions, URL domain);
    
    /**
	 * Whether to use exact matching or containment for node matching task in DOM tree.
	 * @param useExactMatch the useExactMatch to set
	 */
    void setUseExactMatch(boolean useExactMatch);
}
