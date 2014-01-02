/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.rex.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
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
import org.aksw.rex.experiments.ExperimentRunner;
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
	public Set<Quadruple<Node, Node, Node, String>> run(String subjectRule, String objectRule) throws Exception

	{
		Set<Quadruple<Node, Node, Node, String>> quads = Sets.newHashSet();

		// example generation
		Set<Pair<Resource, Resource>> posExamples = null;
		Set<Pair<Resource, Resource>> negExamples = null;

		// domain identification
		domain = di.getDomain(property, posExamples, negExamples, false);

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
			quads = uriGenerator.getTriples(results, property);
//			BufferedWriter bw = new BufferedWriter(new FileWriter("ntFiles/withOutConsistency" + domain.toExternalForm().replaceAll("//", "").replaceAll("/", "") + ".txt"));
//			for (Quadruple<Node, Node, Node, String> q : quads) {
//				bw.write("<" + q.first.getURI() + "> <" + q.second().getURI() + "> <" + q.third().getURI() + "> <" + q.fourth() + ">.\n");
//			}
//			bw.flush();
//			bw.close();
			log.error("Quadrupels generated extracted: " + quads.size());

			// triple filtering
			Set<Triple> triples = quadsToTriples(quads);
			triples = consistency.getConsistentTriples(triples);
			quads = triplesToQuads(triples, quads);

			// triples = consistency.getConsistentTriples(triples,
			// consistency.generateAxioms(endpoint));
			log.error("Consistency checked: " + quads.size());

		}

		return quads;
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
	public Set<Quadruple<Node, Node, Node, String>> run() throws Exception

	{
		Set<Quadruple<Node, Node, Node, String>> quads = Sets.newHashSet();

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

	private Set<Triple> quadsToTriples(Set<Quadruple<Node, Node, Node, String>> quads) {
		HashSet<Triple> set = Sets.newHashSet();
		for (Quadruple<Node, Node, Node, String> q : quads) {
			set.add(new Triple(q.first, q.second, q.third));
		}
		return set;
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

		// ESOP XPATHs from Disheng
		getESOPXpathData(d);

		// golden XPATHs from Disheng
//		 getGoldenRuleData(d);
		for (ControllerData ds : d) {
System.out.println(ds.dbpediaProperty);
			try {
				Property property = ResourceFactory.createProperty(ds.dbpediaProperty);
				SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpediaLiveOpenLink();

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

				// ConsistencyCheckerImpl c = new
				// ConsistencyCheckerImpl(endpoint);
				// SparqlEndpoint end =
				// SparqlEndpoint.getEndpointDBpediaLOD2Cloud();
				String namespace = "http://dbpedia.org/ontology/";
				ConsistencyChecker c = new ConsistencyCheckerImpl(endpoint, namespace);

				Set<Quadruple<Node, Node, Node, String>> quads = new RexController(property, exampleGenerator, domainIdentifier, xPathLearner, uriGenerator, c, endpoint).run(ds.subjectRule, ds.objectRule);
				BufferedWriter bw = new BufferedWriter(new FileWriter("ESWCFiles/" + domain.toExternalForm().replaceAll("//", "").replaceAll("/", "") +".txt"));
				for (Quadruple<Node, Node, Node, String> q : quads) {
					bw.write("<" + q.first.getURI() + "> <" + q.second().getURI() + "> <" + q.third().getURI() + "> <" + q.fourth() + ">.\n");
				}
				bw.flush();
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(ds.index);
			}
		}
	}

	private static void getESOPXpathData(ArrayList<ControllerData> d) {
//		d.add(new ControllerData("imdb-title-index/", "http://dbpedia.org/ontology/director", "http://www.imdb.com/title/", "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]", "//*[contains(text(),\"Director:\")]/../A[1]/SPAN[1]/TEXT()[1]"));
//		d.add(new ControllerData("imdb-title-index/", "http://dbpedia.org/ontology/starring", "http://www.imdb.com/title/", "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]", "//*[contains(text(),\"Stars:\")]/../A[1]/SPAN[1]/TEXT()[1]"));
//		d.add(new ControllerData("imdb-name-index/", "http://dbpedia.org/ontology/starring", "http://www.imdb.com/name/", "//*[contains(text(),\"Hide\")]/../../DIV[2]/DIV[1]/B[1]/A[1]/TEXT()[1]", "//SPAN[@itemprop='name'][1]/text()[1]"));
		d.add(new ControllerData("espnfc-player-index/", "http://dbpedia.org/ontology/team", "http://espnfc.com/player/_/id/", "//DIV[@class='profile']/H1[1]/text()[1]", "//TD[@align='left'][1]/text()[1]"));
//		d.add(new ControllerData("espnfc-team-index/", "http://dbpedia.org/ontology/team", "http://espnfc.com/team", "//*[contains(text(),\"GOALS\")]/../../TR[2]/TD[1]/A[1]/TEXT()[1]", "/HTML/BODY[1]/DIV[2]/DIV[1]/DIV[1]/DIV[1]/DIV[4]/H1[1]/A[1]/text()[1]"));
//		d.add(new ControllerData("goodreads-author-index/", "http://dbpedia.org/ontology/author", "http://www.goodreads.com/author/", "//A[@itemprop='url']/SPAN[1]/text()[1]", "//A[@class='authorName']/SPAN[1]/text()[1]"));
//		d.add(new ControllerData("goodreads-book-index/", "http://dbpedia.org/ontology/author", "http://www.goodreads.com/book/", "//*[@id='bookTitle']/text()", "//*[contains(text(),\"api\")]/../../../../../../DIV[2]/DIV[1]/DIV[2]/DIV[3]/DIV[1]/DIV[2]/DIV[1]/SPAN[2]/A[1]/SPAN[1]/TEXT()[1]"));
	}

	private static void getGoldenRuleData(ArrayList<ControllerData> d) {

//		d.add(new ControllerData("imdb-title-index/", "http://dbpedia.org/ontology/director", "http://www.imdb.com/title/", "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]", "//*[contains(text(),\"Director:\") or contains(text(),\"Directors:\")]/../A[1]/SPAN[1]/TEXT()[1]"));

//		d.add(new ControllerData("imdb-title-index/", "http://dbpedia.org/ontology/starring", "http://www.imdb.com/title/", "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]", "//*[@id='titleCast']/TABLE/TBODY/TR/TD/A/SPAN/text()"));
//		d.add(new ControllerData("imdb-name-index/", "http://dbpedia.org/ontology/starring", "http://www.imdb.com/name/", "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]", "//*[@id='filmo-head-actor' or @id='filmo-head-actress']/following-sibling::*[1]/DIV/B/A/text()"));
		d.add(new ControllerData("espnfc-player-index/", "http://dbpedia.org/ontology/team", "http://espnfc.com/player/_/id/", "//*[@class='profile']/H1/text()", "//*[contains(text(),'Teams')]/../UL/LI/A/text()"));
//		d.add(new ControllerData("espnfc-team-index/", "http://dbpedia.org/ontology/team", "http://espnfc.com/team", "//*[contains(text(),'SQUAD')]/../../DIV/DIV/TABLE/TBODY/TR[@class='evenrow' or @class='oddrow']/TD[3]/A/text()", "//*[@class='section-title']/text()"));
//		d.add(new ControllerData("goodreads-author-index/", "http://dbpedia.org/ontology/author", "http://www.goodreads.com/author/", "//*[@class='bookTitle']/SPAN/text()", "//*[@class='authorName']/SPAN/text()"));
//		d.add(new ControllerData("goodreads-book-index/", "http://dbpedia.org/ontology/author", "http://www.goodreads.com/book/", "//*[@id='bookTitle']/text()", "//*[@id='bookAuthors']/SPAN[2]/A/SPAN/text()"));
	}
}