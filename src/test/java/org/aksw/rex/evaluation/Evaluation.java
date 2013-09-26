/**
 * 
 */
package org.aksw.rex.evaluation;

import java.net.URL;
import java.util.Set;

import org.aksw.rex.consistency.ConsistencyCheckerImpl;
import org.aksw.rex.controller.RexController;
import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.domainidentifier.DomainIdentifier;
import org.aksw.rex.domainidentifier.ManualDomainIdentifier;
import org.aksw.rex.examplegenerator.ExampleGenerator;
import org.aksw.rex.examplegenerator.SimpleExampleGenerator;
import org.aksw.rex.uris.URIGenerator;
import org.aksw.rex.uris.URIGeneratorImpl;
import org.aksw.rex.xpath.XPathExtractor;
import org.aksw.rex.xpath.XPathLearner;
import org.aksw.rex.xpath.XPathLearnerImpl;
import org.aksw.rex.xpath.alfred.ALFREDXPathLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class Evaluation {
	
	static SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	static boolean exactMatch = true;
	
	public static void evaluateIMDB() throws Exception{
		Property property = ResourceFactory.createProperty("http://dbpedia.org/ontology/director");
    	
    	ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
    	exampleGenerator.setMaxNrOfPositiveExamples(2);
    	exampleGenerator.setEndpoint(endpoint);
    	exampleGenerator.setPredicate(property);
    	
    	DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL("http://www.imdb.com/title/"));
    	
    	CrawlIndex crawlIndex = new CrawlIndex("imdb-index/");
    	XPathExtractor xPathExtractor = new XPathExtractor(crawlIndex);
    	
    	XPathLearner xPathLearner = new XPathLearnerImpl(xPathExtractor, endpoint);
    	xPathLearner.setUseExactMatch(exactMatch);
    	
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
	
	public static void evaluateIMDBName() throws Exception{
		Property property = ResourceFactory.createProperty("http://dbpedia.org/ontology/starring");
    	
    	ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
    	exampleGenerator.setMaxNrOfPositiveExamples(2);
    	exampleGenerator.setEndpoint(endpoint);
    	exampleGenerator.setPredicate(property);
    	
    	DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL("http://www.imdb.com/name/"));
    	
    	CrawlIndex crawlIndex = new CrawlIndex("imdb-name-index/");
    	XPathExtractor xPathExtractor = new XPathExtractor(crawlIndex);
    	
    	XPathLearner xPathLearner = new XPathLearnerImpl(xPathExtractor, endpoint);
    	xPathLearner.setUseExactMatch(exactMatch);
    	
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
	
	public static void evaluateIMDBTitle() throws Exception{
		Property property = ResourceFactory.createProperty("http://dbpedia.org/ontology/director");
    	
    	ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
    	exampleGenerator.setMaxNrOfPositiveExamples(5000);
    	exampleGenerator.setEndpoint(endpoint);
    	exampleGenerator.setPredicate(property);
    	
    	DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL("http://www.imdb.com/title/"));
    	
    	CrawlIndex crawlIndex = new CrawlIndex("imdb-title-index/");
    	XPathExtractor xPathExtractor = new XPathExtractor(crawlIndex);
    	
    	XPathLearner xPathLearner = new ALFREDXPathLearner(crawlIndex);
    	xPathLearner = new XPathLearnerImpl(xPathExtractor, endpoint);
    	xPathLearner.setUseExactMatch(exactMatch);
    	
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
	
	public static void evaluateESPN() throws Exception{
		Property property = ResourceFactory.createProperty("http://dbpedia.org/ontology/team");
    	
    	ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
    	exampleGenerator.setMaxNrOfPositiveExamples(1000);
    	exampleGenerator.setEndpoint(endpoint);
    	exampleGenerator.setPredicate(property);
    	
    	DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL("http://www.espn.go.com/"));
    	
    	CrawlIndex crawlIndex = new CrawlIndex("espn-index/");
    	XPathExtractor xPathExtractor = new XPathExtractor(crawlIndex);
    	
    	XPathLearner xPathLearner = new XPathLearnerImpl(xPathExtractor, endpoint);
    	xPathLearner.setUseExactMatch(exactMatch);
    	
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
	
	public static void main(String[] args) throws Exception {
		Evaluation.evaluateIMDBTitle();
//		Evaluation.evaluateIMDBName();
//		Evaluation.evaluateESPN();
	}

}
