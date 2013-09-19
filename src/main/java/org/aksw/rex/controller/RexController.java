/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.rex.controller;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.rex.consistency.ConsistencyChecker;
import org.aksw.rex.consistency.ConsistencyCheckerImpl;
import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.domainidentifier.DomainIdentifier;
import org.aksw.rex.domainidentifier.GoogleDomainIdentifier;
import org.aksw.rex.domainidentifier.ManualDomainIdentifier;
import org.aksw.rex.examplegenerator.ExampleGenerator;
import org.aksw.rex.examplegenerator.SimpleExampleGenerator;
import org.aksw.rex.results.ExtractionResult;
import org.aksw.rex.uris.URIGenerator;
import org.aksw.rex.uris.URIGeneratorImpl;
import org.aksw.rex.util.Pair;
import org.aksw.rex.xpath.XPathExtractionRule;
import org.aksw.rex.xpath.XPathExtractor;
import org.aksw.rex.xpath.XPathLearner;
import org.aksw.rex.xpath.XPathLearnerImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.MapUtils;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 *
 * @author ngonga
 */
public class RexController {
    
    ExampleGenerator exampleGenerator;
    DomainIdentifier di;
    Property property;
    XPathLearner xpath;
    URIGenerator uriGenerator;
    ConsistencyChecker consistency;
    SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
    
    public RexController(Property p, ExampleGenerator e, DomainIdentifier d, 
            XPathLearner l, URIGenerator uriGenerator, ConsistencyChecker c, SparqlEndpoint s)
    {
       property = p;
       exampleGenerator = e;
       di = d;        
       xpath = l;
       this.uriGenerator = uriGenerator;
       consistency = c;
       endpoint = s;
    }
    
    /** Runs the extraction pipeline
     * 
     * @return A set of triples
     * @throws Exception If URI generation does not work
     */
    public Set<Triple> run() throws Exception
    {
    	//example generation
        Set<Pair<Resource, Resource>> posExamples = exampleGenerator.getPositiveExamples();
        Set<Pair<Resource, Resource>> negExamples = exampleGenerator.getNegativeExamples();
        
        //domain identification
        URL domain = di.getDomain(property, posExamples, negExamples, false);
        
        //XPath expression generation
        Map<XPathExtractionRule, Double> extractionRules = xpath.getXPathExpressions(posExamples, negExamples, domain);
        
        //get the top N XPath rules only
        int n = 1;
        List<XPathExtractionRule> topNExtractionRules = Lists.newArrayList();
        List<Entry<XPathExtractionRule, Double>> extractionRulesSortedByValues = MapUtils.sortByValues(extractionRules);
        for (Entry<XPathExtractionRule, Double> ruleWithScore : extractionRulesSortedByValues.subList(0, Math.min(n, extractionRulesSortedByValues.size()))) {
			topNExtractionRules.add(ruleWithScore.getKey());
		}
        
        //extract results from the corpus
        Set<ExtractionResult> results = xpath.getExtractionResults(topNExtractionRules, domain);
        
        //triple generation
        Set<Triple> triples = uriGenerator.getTriples(results, property);
        
        //triple filtering
//        triples = consistency.getConsistentTriples(triples, consistency.generateAxioms(endpoint));
        
        return triples;
    }
    
    public static void main(String[] args) throws Exception {
    	Property property = ResourceFactory.createProperty("http://dbpedia.org/ontology/director");
    	SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLOD2Cloud();
    	
    	ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
    	exampleGenerator.setMaxNrOfPositiveExamples(2);
    	exampleGenerator.setEndpoint(endpoint);
    	exampleGenerator.setPredicate(property);
    	
    	DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL("http://www.imdb.com/title/"));
    	
    	CrawlIndex crawlIndex = new CrawlIndex("imdb-index/");
    	XPathExtractor xPathExtractor = new XPathExtractor(crawlIndex);
    	
    	XPathLearner xPathLearner = new XPathLearnerImpl(xPathExtractor, endpoint);
    	xPathLearner.setUseExactMatch(false);
    	
    	URIGenerator uriGenerator = new URIGeneratorImpl();
    	
		Set<Triple> triples = new RexController(
				property, 
				exampleGenerator, 
				domainIdentifier,
				xPathLearner,
				uriGenerator,
				new ConsistencyCheckerImpl(),
				endpoint).run();
		
		for (Triple triple : triples) {
			System.out.println(triple);
		}
	}
}
