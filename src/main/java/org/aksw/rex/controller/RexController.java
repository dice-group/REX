package org.aksw.rex.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.aksw.rex.uris.URIGeneratorAGDISTIS;
import org.aksw.rex.util.Pair;
import org.aksw.rex.xpath.XPathLearner;
import org.aksw.rex.xpath.alfred.ALFREDXPathLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rules.xpath.XPathRule;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.stanford.nlp.util.Quadruple;

/**
 * The heart of REX This controller extracts for a given property and lucene
 * index a set of consistent triple
 * 
 * @author ngonga
 */
public class RexController {
	Logger log = LoggerFactory.getLogger(RexController.class);
	ExampleGenerator exampleGenerator;
	DomainIdentifier di;
	Property property;
	XPathLearner xpath;
	URIGenerator uriGenerator;
	ConsistencyChecker consistency;
	SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	int topNRules = 1;
	private static URL domain;

	public RexController(Property p, ExampleGenerator e, DomainIdentifier d, XPathLearner l, URIGenerator u, ConsistencyChecker c, SparqlEndpoint s) {
		property = p;
		exampleGenerator = e;
		di = d;
		xpath = l;
		uriGenerator = u;
		consistency = c;
		endpoint = s;
	}

	public static void main(String[] args) throws Exception {
	  // Determine which property p you are looking for generating new triples <s p o>
        Property property = ResourceFactory.createProperty("http://dbpedia.org/ontology/director");
        
        		// Property property =
		// ResourceFactory.createProperty("http://dbpedia.org/ontology/author");
		
        // The SPARQL endpoint provides examples as well as an underlying schema to validate generated triples
        SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();

        // The SimpleExampleGenerator retrieves 100 random triples from the underlying knowledge base with property p
        ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
        exampleGenerator.setMaxNrOfPositiveExamples(100);
        exampleGenerator.setEndpoint(endpoint);
        exampleGenerator.setPredicate(property);

        // The ManualDomainIdentifier provides a starting point domain for the crawler. REX would also be capable of identifying a domain only by examples.
        DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL("http://www.imdb.com/title/"));
		// DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new
		// URL("http://www.goodreads.com/author/"));

        // The CrawlIndex is a LUCENE 4.X Index which will store the crawled information and provide it to the XPATHLearner.
        CrawlIndex crawlIndex = new CrawlIndex("imdb-title-index/");

        // Our XPATHLearner learns a pair of XPATHs containing s,o for the given p
        XPathLearner xPathLearner = new ALFREDXPathLearner(crawlIndex);
        xPathLearner.setUseExactMatch(false);
	// XPathExtractor xPathExtractor = new XPathExtractor(crawlIndex);
		// XPathLearner xPathLearner = new XPathLearnerImpl(xPathExtractor,
		// endpoint);


        // The URIGenerator is based on the AGDISTIS web service which will generate for a pair of s,o XPATH strings corresponding URIs from the knowledge base or new URIs if there are no to be found
        URIGenerator uriGenerator = new URIGeneratorAGDISTIS();

        // After initialising the REX Controller with instances of components needed for our pipeline you can run the controller and see the results in the returned set of quadruples (provenance)
        Set<Quadruple<Node, Node, Node, String>> quadruples = new RexController(property, exampleGenerator, domainIdentifier, xPathLearner, uriGenerator, new ConsistencyCheckerImpl(endpoint), endpoint).run();

        for (Quadruple<Node, Node, Node, String> quadruple : quadruples) {
            System.out.println(quadruple);
        }
	
	}

	/**
	 * Runs the extraction pipeline
	 * 
	 * @return A set of triples
	 * @throws Exception
	 *             If URI generation does not work
	 */
	public Set<Quadruple<Node, Node, Node, String>> run() throws Exception

	{
		Set<Quadruple<Node, Node, Node, String>> quads = Sets.newHashSet();

		// example generation
		Set<Pair<Resource, Resource>> posExamples = null;
		Set<Pair<Resource, Resource>> negExamples = null;
		posExamples = exampleGenerator.getPositiveExamples();
		negExamples = exampleGenerator.getNegativeExamples();

		// domain identification
		URL domain = di.getDomain(property, posExamples, negExamples, false);

		// XPath expression generation
		List<Pair<XPathRule, XPathRule>> extractionRules = xpath.getXPathExpressions(posExamples, negExamples, domain);

		if (!extractionRules.isEmpty()) {
			// currently, we assume that the best rule is the first one in the
			// list, thus we
			extractionRules = extractionRules.subList(0, 1);
			System.out.println("Top rule:\n" + extractionRules);

			// extract results from the corpus
			Set<ExtractionResult> results = xpath.getExtractionResults(extractionRules, domain);

			// triple generation
			quads = uriGenerator.getTriples(results, property);

			// triple filtering
			// triples = consistency.getConsistentTriples(triples,
			// consistency.generateAxioms(endpoint));
			Set<Triple> triples = quadsToTriples(quads);
			triples = consistency.getConsistentTriples(triples);
			quads = triplesToQuads(triples, quads);
		}

		return quads;
	}

	/**
	 * util function to convert triples to quadruples
	 * 
	 * @param triples
	 * @param quads
	 * @return
	 */
	private Set<Quadruple<Node, Node, Node, String>> triplesToQuads(Set<Triple> triples, Set<Quadruple<Node, Node, Node, String>> quads) {
		HashSet<Quadruple<Node, Node, Node, String>> set = Sets.newHashSet();
		for (Triple t : triples) {
			for (Quadruple<Node, Node, Node, String> q : quads) {
				if (t.getSubject().getURI().equals(q.first.getURI())) {
					if (t.getPredicate().getURI().equals(q.second.getURI())) {
						if (t.getObject().getURI().equals(q.third.getURI())) {
							set.add(q);
						}
					}
				}
			}
		}
		return set;
	}

	/**
	 * util function to convert quadruples to triples
	 * 
	 * @param quads
	 * @return
	 */
	private Set<Triple> quadsToTriples(Set<Quadruple<Node, Node, Node, String>> quads) {
		HashSet<Triple> set = Sets.newHashSet();
		for (Quadruple<Node, Node, Node, String> q : quads) {
			set.add(new Triple(q.first, q.second, q.third));
		}
		return set;
	}

	/**
	 * Runs the extraction pipeline, used for gold standard evaluation
	 * 
	 * @param subjectRule
	 * @param objectRule
	 * 
	 * @return A set of triples
	 * @throws Exception
	 *             If URI generation does not work
	 */
	public Set<Quadruple<Node, Node, Node, String>> run(String subjectRule, String objectRule) throws Exception

	{
		Set<Quadruple<Node, Node, Node, String>> quads = Sets.newHashSet();

		// example generation
		Set<Pair<Resource, Resource>> posExamples = null;
		Set<Pair<Resource, Resource>> negExamples = null;
		posExamples = exampleGenerator.getPositiveExamples();
		negExamples = exampleGenerator.getNegativeExamples();

		// domain identification
		domain = di.getDomain(property, posExamples, negExamples, false);

		// XPath expression generation
		List<Pair<XPathRule, XPathRule>> extractionRules = new ArrayList<Pair<XPathRule, XPathRule>>();

		extractionRules.add(new Pair<XPathRule, XPathRule>(new XPathRule(subjectRule), new XPathRule(objectRule)));

		if (!extractionRules.isEmpty()) {
			extractionRules = extractionRules.subList(0, 1);
			System.out.println("Top rule:\n" + extractionRules);

			// extract results from the corpus
			Set<ExtractionResult> results = xpath.getExtractionResults(extractionRules, domain);
			System.out.println("XpathResults extracted: " + results.size());

			// triple generation
			quads = uriGenerator.getTriples(results, property);
			System.out.println("Quadrupels generated extracted: " + quads.size());

			// triple filtering
			Set<Triple> triples = quadsToTriples(quads);
			triples = consistency.getConsistentTriples(triples);
			quads = triplesToQuads(triples, quads);

			// triples = consistency.getConsistentTriples(triples,
			// consistency.generateAxioms(endpoint));
			System.out.println("Consistency checked: " + quads.size());

		}

		return quads;
	}
}
