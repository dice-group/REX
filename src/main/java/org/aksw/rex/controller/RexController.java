/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.rex.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
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
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
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

	public RexController(Property p, ExampleGenerator e, DomainIdentifier d, XPathLearner l, URIGenerator uriGenerator, ConsistencyChecker c, SparqlEndpoint s) {
		property = p;
		exampleGenerator = e;
		di = d;
		xpath = l;
		this.uriGenerator = uriGenerator;
		consistency = c;
		endpoint = s;
	}

	/**
	 * Runs the extraction pipeline
	 * 
	 * @param subjectRule
	 * @param objectRule
	 * 
	 * @return A set of triples
	 * @throws Exception
	 *             If URI generation does not work
	 */
	public Set<Triple> run(String subjectRule, String objectRule) throws Exception

	{
		Set<Triple> triples = Sets.newHashSet();

		// example generation
		Set<Pair<Resource, Resource>> posExamples = null;
		Set<Pair<Resource, Resource>> negExamples = null;

		// domain identification
		URL domain = di.getDomain(property, posExamples, negExamples, false);

		// XPath expression generation
		// List<Pair<XPathRule, XPathRule>> extractionRules =
		// xpath.getXPathExpressions(posExamples, negExamples, domain);
		List<Pair<XPathRule, XPathRule>> extractionRules = new ArrayList<Pair<XPathRule, XPathRule>>();

		extractionRules.add(new Pair<XPathRule, XPathRule>(new XPathRule(subjectRule), new XPathRule(objectRule)));

		if (!extractionRules.isEmpty()) {
			// currently, we assume that the best rule is the first one in the
			// list, thus we
			extractionRules = extractionRules.subList(0, 1);
			System.out.println("Top rule:\n" + extractionRules);

			// extract results from the corpus
			Set<ExtractionResult> results = xpath.getExtractionResults(extractionRules, domain);
			log.error("XpathResults extracted: " + results.size());
			// triple generation
			triples = uriGenerator.getTriples(results, property);
			log.error("Uris generated extracted: " + triples.size());

			// triple filtering
			triples = consistency.getConsistentTriples(triples);
//			triples = consistency.getConsistentTriples(triples, consistency.generateAxioms(endpoint));
			log.error("Consistency checked: " + triples.size());

		}

		return triples;
	}

	/**
	 * Runs the extraction pipeline
	 * 
	 * @param subjectRule
	 * @param objectRule
	 * 
	 * @return A set of triples
	 * @throws Exception
	 *             If URI generation does not work
	 */
	public Set<Triple> run() throws Exception

	{
		Set<Triple> triples = Sets.newHashSet();

		// example generation
		Set<Pair<Resource, Resource>> posExamples = null;
		Set<Pair<Resource, Resource>> negExamples = null;

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
			triples = uriGenerator.getTriples(results, property);

			// triple filtering
//			triples = consistency.getConsistentTriples(triples, consistency.generateAxioms(endpoint));
			triples = consistency.getConsistentTriples(triples);
		}

		return triples;
	}

	// public static void main(String[] args) throws Exception {
	// Property property =
	// ResourceFactory.createProperty("http://dbpedia.org/ontology/director");
	// SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	//
	// ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
	// exampleGenerator.setMaxNrOfPositiveExamples(100);
	// exampleGenerator.setEndpoint(endpoint);
	// exampleGenerator.setPredicate(property);
	//
	// DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new
	// URL("http://www.imdb.com/title/"));
	//
	// CrawlIndex crawlIndex = new CrawlIndex("imdb-title-index/");
	// XPathExtractor xPathExtractor = new XPathExtractor(crawlIndex);
	//
	// XPathLearner xPathLearner = new ALFREDXPathLearner(crawlIndex);
	// // XPathLearner xPathLearner = new XPathLearnerImpl(xPathExtractor,
	// // endpoint);
	// xPathLearner.setUseExactMatch(false);
	//
	// URIGenerator uriGenerator = new URIGeneratorAGDISTIS();
	//
	// Set<Triple> triples = new RexController(property, exampleGenerator,
	// domainIdentifier, xPathLearner, uriGenerator, new
	// ConsistencyCheckerImpl(endpoint), endpoint).run();
	//
	// for (Triple triple : triples) {
	// System.out.println(triple);
	// }
	// }
	/**
	 * used for generating the evaluation part
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		ArrayList<ControllerData> d = new ArrayList<ControllerData>();
		d.add(new ControllerData("imdb-title-index/", "http://dbpedia.org/ontology/director", "http://www.imdb.com/title/", "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]", "//SPAN[@itemprop='name'][1]/text()[1]"));
		// d.add(new ControllerData("imdb-title-index/",
		// "http://dbpedia.org/ontology/starring", "http://www.imdb.com/title/",
		// "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]",
		// "//*[contains(text(),\"Stars:\")]/../A[1]/SPAN[1]/TEXT()[1]"));
		d.add(new ControllerData("imdb-name-index/", "http://dbpedia.org/ontology/starring", "http://www.imdb.com/name/", "//SPAN[@itemprop='name'][1]/text()[1]", "//*[contains(text(),\"Hide \")]/../../DIV[2]/DIV[1]/B[1]/A[1]/TEXT()[1]"));
		d.add(new ControllerData("espnfc-player-index/", "http://dbpedia.org/ontology/team", "http://espnfc.com/player/_/id/",
				"//*[contains(text(),\"EUROPE\")]/../../../../../../DIV[2]/DIV[3]/DIV[1]/DIV[1]/DIV[2]/DIV[1]/DIV[1]/DIV[2]/H1[1]/TEXT()[1]", "//OPTION[@value='?'][1]/text()[1]"));
		d.add(new ControllerData("espnfc-team-index/", "http://dbpedia.org/ontology/team", "http://espnfc.com/team", "//*[contains(text(),\"M\")]/../../../../../../DIV[2]/DIV[2]/DIV[2]/TABLE[1]/TBODY[1]/TR[2]/TD[1]/A[1]/TEXT()[1]",
				"//*[contains(text(),\"VIDEO\")]/../../../../../../../../DIV[1]/DIV[4]/H1[1]/A[1]/TEXT()[1]"));
		d.add(new ControllerData("goodreads-author-index/", "http://dbpedia.org/ontology/author", "http://www.goodreads.com/author/", "//SPAN[@itemprop='name'][1]/text()[1]", "//*[contains(text(),\"by\")]/../SPAN[2]/A[1]/SPAN[1]/TEXT()[1]"));
		d.add(new ControllerData("goodreads-book-index/", "http://dbpedia.org/ontology/author", "http://www.goodreads.com/book/", "//*[contains(text(),\"...more\")]/../../SPAN[2]/A[1]/TEXT()[1]",
				"//*[contains(text(),\"api\")]/../../../../../../DIV[2]/DIV[1]/DIV[2]/DIV[3]/DIV[1]/DIV[2]/DIV[1]/SPAN[2]/A[1]/SPAN[1]/TEXT()[1]"));
		for (ControllerData ds : d) {
			try {
				Property property = ResourceFactory.createProperty(ds.dbpediaProperty);
				SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();

				ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
				exampleGenerator.setMaxNrOfPositiveExamples(100);
				exampleGenerator.setEndpoint(endpoint);
				exampleGenerator.setPredicate(property);

				DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL(ds.urlDomain));

				CrawlIndex crawlIndex = new CrawlIndex(ds.index);
				// XPathExtractor xPathExtractor = new
				// XPathExtractor(crawlIndex);

				XPathLearner xPathLearner = new ALFREDXPathLearner(crawlIndex);
				// XPathLearner xPathLearner = new
				// XPathLearnerImpl(xPathExtractor,
				// endpoint);
				xPathLearner.setUseExactMatch(false);

				URIGenerator uriGenerator = new URIGeneratorAGDISTIS();

//				ConsistencyCheckerImpl c = new ConsistencyCheckerImpl(endpoint);
//				SparqlEndpoint end = SparqlEndpoint.getEndpointDBpediaLOD2Cloud();
				String namespace = "http://dbpedia.org/ontology/";
				ConsistencyChecker c = new ConsistencyCheckerImpl(endpoint, namespace);
				
				Set<Triple> triples = new RexController(property, exampleGenerator, domainIdentifier, xPathLearner, uriGenerator, c, endpoint).run(ds.subjectRule, ds.objectRule);
				BufferedWriter bw = new BufferedWriter(new FileWriter("ntFiles/" + ds.index.replace("/", "") + ".txt"));
				for (Triple triple : triples) {
					bw.write("<" + triple.getSubject() + "> <" + triple.getPredicate() + "> <" + triple.getObject() + ">.\n");
				}
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(ds.index);
			}
		}
	}
}