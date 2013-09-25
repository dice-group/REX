/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.rex.controller;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.aksw.rex.consistency.ConsistencyChecker;
import org.aksw.rex.consistency.ConsistencyCheckerImpl;
import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.domainidentifier.DomainIdentifier;
import org.aksw.rex.domainidentifier.ManualDomainIdentifier;
import org.aksw.rex.examplegenerator.ExampleGenerator;
import org.aksw.rex.examplegenerator.SimpleExampleGenerator;
import org.aksw.rex.results.ExtractionResult;
import org.aksw.rex.uris.URIGenerator;
import org.aksw.rex.uris.URIGeneratorImpl;
import org.aksw.rex.util.Pair;
import org.aksw.rex.xpath.XPathExtractor;
import org.aksw.rex.xpath.XPathLearner;
import org.aksw.rex.xpath.XPathLearnerImpl;
import org.aksw.rex.xpath.alfred.ALFREDXPathLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;

import rules.xpath.XPathRule;

import com.google.common.collect.Sets;
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
    int topNRules = 1;
    
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
    	Set<Triple> triples = Sets.newHashSet();
    	
    	//example generation
        Set<Pair<Resource, Resource>> posExamples = exampleGenerator.getPositiveExamples();
        Set<Pair<Resource, Resource>> negExamples = exampleGenerator.getNegativeExamples();
        
        //domain identification
        URL domain = di.getDomain(property, posExamples, negExamples, false);
        
        //XPath expression generation
        List<Pair<XPathRule, XPathRule>> extractionRules = xpath.getXPathExpressions(posExamples, negExamples, domain);
        
        if(!extractionRules.isEmpty()){
        	 //currently, we assume that the best rule is the first one in the list, thus we
            extractionRules = extractionRules.subList(0, 1);
            System.out.println("Top rule:\n" + extractionRules);
            
            //extract results from the corpus
            Set<ExtractionResult> results = xpath.getExtractionResults(extractionRules, domain);
            
            //triple generation
            triples = uriGenerator.getTriples(results, property);
            
            //triple filtering
//            triples = consistency.getConsistentTriples(triples, consistency.generateAxioms(endpoint));
        }
        
        return triples;
    }
    
    public static void main(String[] args) throws Exception {
    	Property property = ResourceFactory.createProperty("http://dbpedia.org/ontology/director");
    	SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLOD2Cloud();
    	
    	ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
    	exampleGenerator.setMaxNrOfPositiveExamples(100);
    	exampleGenerator.setEndpoint(endpoint);
    	exampleGenerator.setPredicate(property);
    	
    	DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL("http://www.imdb.com/title/"));
    	
    	CrawlIndex crawlIndex = new CrawlIndex("imdb-title-index/");
    	XPathExtractor xPathExtractor = new XPathExtractor(crawlIndex);
    	
//    	XPathLearner xPathLearner = new ALFREDXPathLearner(crawlIndex);
    	XPathLearner xPathLearner = new XPathLearnerImpl(xPathExtractor, endpoint);
    	xPathLearner.setUseExactMatch(false);
    	
    	URIGenerator uriGenerator = new URIGeneratorImpl();
    	
		Set<Triple> triples = new RexController(
				property, 
				exampleGenerator, 
				domainIdentifier,
				xPathLearner,
				uriGenerator,
				new ConsistencyCheckerImpl(endpoint),
				endpoint).run();
		
		for (Triple triple : triples) {
			System.out.println(triple);
		}
    }


	public static void main2(String[] args) throws Exception {
		PropertyXPathSupplierAlfred ps = new PropertyXPathSupplierAlfred();

		for (RexPropertiesWithGoldstandard p : ps.getPropertiesToCheck()) {
			String propertyURL = p.getPropertyURL();
			URL domainURL = new URL(p.getExtractionDomainURL());
			Property property = ResourceFactory.createProperty(propertyURL);
			SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
			ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
			exampleGenerator.setEndpoint(endpoint);
			exampleGenerator.setPredicate(property);
			
			DomainIdentifier domainIdentifier = new ManualDomainIdentifier(domainURL);
			
			CrawlIndex crawlIndex = new CrawlIndex("imdb-index/");
	    	XPathExtractor xPathExtractor = new XPathExtractor(crawlIndex);
	    	
	    	XPathLearner xPathLearner = new XPathLearnerImpl(xPathExtractor, endpoint);
	    	xPathLearner.setUseExactMatch(false);
	    	
	    	URIGenerator uriGenerator = new URIGeneratorImpl();
	    	
			new RexController(
					property, 
					exampleGenerator, 
					domainIdentifier,
					xPathLearner,
					uriGenerator,
					new ConsistencyCheckerImpl(endpoint),
					endpoint).run();
		}
	}
}
