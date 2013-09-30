package org.aksw.rex.experiments;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Page;
import model.Rule;

import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.domainidentifier.DomainIdentifier;
import org.aksw.rex.domainidentifier.ManualDomainIdentifier;
import org.aksw.rex.examplegenerator.ExampleGenerator;
import org.aksw.rex.examplegenerator.SimpleExampleGenerator;
import org.aksw.rex.util.Pair;
import org.aksw.rex.xpath.alfred.ALFREDPageRetrieval;
import org.aksw.rex.xpath.alfred.ALFREDXPathLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.LoggerFactory;

import rules.xpath.XPathRule;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class ExperimentRunner implements Runnable {

	private org.slf4j.Logger log = LoggerFactory.getLogger(ExperimentRunner.class);

	private XPathRule goldenRuleL;
	private XPathRule goldenRuleR;
	private Property property;
	private URL domain;
	private Set<Pair<Resource, Resource>> testing;
	private Set<Pair<Resource, Resource>> training;

	private CrawlIndex index;

	public ExperimentRunner(String index, int minPages, int maxPages, int testPages,
			int intervalPages) throws MalformedURLException {

		// N pairs for learning
		int numLearnPairs = maxPages;
		// N pairs for testing
		int numTestPairs = testPages;

		// TODO to move as provided input
		String propertyS = "http://dbpedia.org/ontology/director";
		String domainS = "http://www.imdb.com/title/";

		this.goldenRuleL = new XPathRule("//*[@id='overview-top']/H1/SPAN[1]/text()");
		this.goldenRuleR = new XPathRule("//*[contains(text(),\"Director:\") or contains(text(),\"Directors:\")]/../A[1]/SPAN[1]/TEXT()[1]");

		DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL(domainS));

		this.property = ResourceFactory.createProperty(propertyS);
		
		ExampleGenerator exampleGenerator = getExampleGenerator(this.property, numLearnPairs, numTestPairs);
		Set<Pair<Resource, Resource>> positiveExamples = exampleGenerator.getPositiveExamples();
		
		if(positiveExamples.size() < maxPages)
			throw new RuntimeException("Number of training pairs should be: "+ maxPages + " but number of pairs was " +positiveExamples.size());
		
		List<Pair<Resource, Resource>> examples = new LinkedList<Pair<Resource, Resource>>(positiveExamples);
		
		this.testing = new HashSet<Pair<Resource, Resource>>(examples.subList(maxPages, examples.size()));
		this.training = new HashSet<Pair<Resource, Resource>>(examples.subList(0, maxPages));
		
		 
		this.domain = domainIdentifier.getDomain(property, new HashSet<Pair<Resource, Resource>>(
this.training), null, false);
		
		log.debug("Learn pairs: " + this.training.size());
		log.debug("Test pairs: " + this.testing.size());

		this.index = new CrawlIndex("htmlindex");
	}

	private ExampleGenerator getExampleGenerator(Property property, int numLearnPairs, int numTestPairs) {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();

		ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
		exampleGenerator.setMaxNrOfPositiveExamples(numLearnPairs + numTestPairs);
		exampleGenerator.setEndpoint(endpoint);
		exampleGenerator.setPredicate(property);
		return exampleGenerator;
	}

	@Override
	public void run() {

		ALFREDXPathLearner learner = new ALFREDXPathLearner(this.index);
		List<Pair<XPathRule, XPathRule>> xpaths = learner.getXPathExpressions(this.training, null,
				domain);
		Rule ruleL = xpaths.get(0).getLeft();
		Rule ruleR = xpaths.get(0).getRight();

		log.debug("Learned XPath L: " + ruleL);
		log.debug("Learned XPath R: " + ruleR);

		Map<String, String> page2valueLeft = new HashMap<String, String>();
		Map<String, String> page2valueRight = new HashMap<String, String>();

		ALFREDPageRetrieval pageRetr = new ALFREDPageRetrieval(learner.getIndex());
		List<Page> testPages = pageRetr.getPages(this.testing, page2valueLeft,
				page2valueRight, domain);
		List<Page> trainingPages = pageRetr.getPages(this.training, page2valueLeft,
				page2valueRight, domain);
		
		learner.getIndex().close();

		log.debug("Test on learning pages");

		QualityEvaluator left = testResultRule(ruleL, this.goldenRuleL, testPages);
		QualityEvaluator right =testResultRule(ruleR, this.goldenRuleR, testPages);

		log.info("I: "+trainingPages.size() + " U-I: "+testPages.size());
		log.info("Left: rule "+ruleL);
		log.info("Left: (P) "+left.getPrecision()+" (R) "+left.getRecall()+" (F) "+left.getF());
		log.info("Right: rule "+ruleR);
		log.info("Right: (P) "+right.getPrecision()+" (R) "+right.getRecall()+" (F) "+right.getF());
	}

	private static QualityEvaluator testResultRule(Rule result, Rule golden, List<Page> pages) {
		QualityEvaluator ev = new QualityEvaluator(result, golden, pages);
		ev.evaluate();
		return ev;
	}
	
	public static void main(String[] args) throws MalformedURLException {
		ExperimentRunner exp = new ExperimentRunner("htmlindex", 100, 500, 39000, 100);
		exp.run();
	}
}
