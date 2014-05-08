package org.aksw.rex.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import org.aksw.rex.consistency.ConsistencyChecker;
import org.aksw.rex.consistency.ConsistencyCheckerImpl;
import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.domainidentifier.DomainIdentifier;
import org.aksw.rex.domainidentifier.ManualDomainIdentifier;
import org.aksw.rex.examplegenerator.ExampleGenerator;
import org.aksw.rex.examplegenerator.SimpleExampleGenerator;
import org.aksw.rex.uris.URIGenerator;
import org.aksw.rex.uris.URIGeneratorAGDISTIS;
import org.aksw.rex.xpath.XPathLearner;
import org.aksw.rex.xpath.alfred.ALFREDXPathLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.stanford.nlp.util.Quadruple;

/**
 * 
 * used for generating the evaluation part
 * 
 * @author r.usbeck
 * 
 */
public class REX_Evaluation {

	private static URL domain;

	public static void main(String[] args) throws Exception {

		ArrayList<ControllerData> d = new ArrayList<ControllerData>();

		// ESOP XPATHs from Disheng
		getESOPXpathData(d);

		// golden XPATHs from Disheng
		// getGoldenRuleData(d);
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
				BufferedWriter bw = new BufferedWriter(new FileWriter("ESWCFiles/" + domain.toExternalForm().replaceAll("//", "").replaceAll("/", "") + ".txt"));
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
/**
 * This method fills the array d with Xpath Data calculated by AlfRex
 * @param d
 */
	private static void getESOPXpathData(ArrayList<ControllerData> d) {
		// d.add(new ControllerData("imdb-title-index/",
		// "http://dbpedia.org/ontology/director", "http://www.imdb.com/title/",
		// "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]",
		// "//*[contains(text(),\"Director:\")]/../A[1]/SPAN[1]/TEXT()[1]"));
		// d.add(new ControllerData("imdb-title-index/",
		// "http://dbpedia.org/ontology/starring", "http://www.imdb.com/title/",
		// "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]",
		// "//*[contains(text(),\"Stars:\")]/../A[1]/SPAN[1]/TEXT()[1]"));
		// d.add(new ControllerData("imdb-name-index/",
		// "http://dbpedia.org/ontology/starring", "http://www.imdb.com/name/",
		// "//*[contains(text(),\"Hide\")]/../../DIV[2]/DIV[1]/B[1]/A[1]/TEXT()[1]",
		// "//SPAN[@itemprop='name'][1]/text()[1]"));
		d.add(new ControllerData("espnfc-player-index/", "http://dbpedia.org/ontology/team", "http://espnfc.com/player/_/id/", "//DIV[@class='profile']/H1[1]/text()[1]", "//TD[@align='left'][1]/text()[1]"));
		// d.add(new ControllerData("espnfc-team-index/",
		// "http://dbpedia.org/ontology/team", "http://espnfc.com/team",
		// "//*[contains(text(),\"GOALS\")]/../../TR[2]/TD[1]/A[1]/TEXT()[1]",
		// "/HTML/BODY[1]/DIV[2]/DIV[1]/DIV[1]/DIV[1]/DIV[4]/H1[1]/A[1]/text()[1]"));
		// d.add(new ControllerData("goodreads-author-index/",
		// "http://dbpedia.org/ontology/author",
		// "http://www.goodreads.com/author/",
		// "//A[@itemprop='url']/SPAN[1]/text()[1]",
		// "//A[@class='authorName']/SPAN[1]/text()[1]"));
		// d.add(new ControllerData("goodreads-book-index/",
		// "http://dbpedia.org/ontology/author",
		// "http://www.goodreads.com/book/", "//*[@id='bookTitle']/text()",
		// "//*[contains(text(),\"api\")]/../../../../../../DIV[2]/DIV[1]/DIV[2]/DIV[3]/DIV[1]/DIV[2]/DIV[1]/SPAN[2]/A[1]/SPAN[1]/TEXT()[1]"));
	}
	/**
	 * This method fills the array d with Xpath Data manually created
	 * @param d
	 */
	private static void getGoldenRuleData(ArrayList<ControllerData> d) {

		// d.add(new ControllerData("imdb-title-index/",
		// "http://dbpedia.org/ontology/director", "http://www.imdb.com/title/",
		// "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]",
		// "//*[contains(text(),\"Director:\") or contains(text(),\"Directors:\")]/../A[1]/SPAN[1]/TEXT()[1]"));

		// d.add(new ControllerData("imdb-title-index/",
		// "http://dbpedia.org/ontology/starring", "http://www.imdb.com/title/",
		// "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]",
		// "//*[@id='titleCast']/TABLE/TBODY/TR/TD/A/SPAN/text()"));
		// d.add(new ControllerData("imdb-name-index/",
		// "http://dbpedia.org/ontology/starring", "http://www.imdb.com/name/",
		// "//*[contains(text(),\"Take The Quiz!\")]/../SPAN[1]/A[1]/TEXT()[1]",
		// "//*[@id='filmo-head-actor' or @id='filmo-head-actress']/following-sibling::*[1]/DIV/B/A/text()"));
		d.add(new ControllerData("espnfc-player-index/", "http://dbpedia.org/ontology/team", "http://espnfc.com/player/_/id/", "//*[@class='profile']/H1/text()", "//*[contains(text(),'Teams')]/../UL/LI/A/text()"));
		// d.add(new ControllerData("espnfc-team-index/",
		// "http://dbpedia.org/ontology/team", "http://espnfc.com/team",
		// "//*[contains(text(),'SQUAD')]/../../DIV/DIV/TABLE/TBODY/TR[@class='evenrow' or @class='oddrow']/TD[3]/A/text()",
		// "//*[@class='section-title']/text()"));
		// d.add(new ControllerData("goodreads-author-index/",
		// "http://dbpedia.org/ontology/author",
		// "http://www.goodreads.com/author/",
		// "//*[@class='bookTitle']/SPAN/text()",
		// "//*[@class='authorName']/SPAN/text()"));
		// d.add(new ControllerData("goodreads-book-index/",
		// "http://dbpedia.org/ontology/author",
		// "http://www.goodreads.com/book/", "//*[@id='bookTitle']/text()",
		// "//*[@id='bookAuthors']/SPAN[2]/A/SPAN/text()"));
	}
}
