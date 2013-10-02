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
import org.aksw.rex.util.Pair;
import org.aksw.rex.xpath.alfred.ALFREDPageRetrieval;
import org.aksw.rex.xpath.alfred.ALFREDSampler;
import org.aksw.rex.xpath.alfred.ALFREDXPathLearner;
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
	private List<Page> testing;
	private Set<Pair<Resource, Resource>> training;

	private CrawlIndex index;

	private ALFREDPageRetrieval pageRetriever;

	private int trainingPages;
	private int numTestPages;

	public ExperimentRunner(String index, int minPages, int maxPages, int testPages,
			int intervalPages) throws MalformedURLException {
		this.index = new CrawlIndex(index);
		this.pageRetriever = new ALFREDPageRetrieval(new CrawlIndex("imdb-title-index"));
		
		// N pairs for learning
		this.trainingPages = maxPages;
		// N pairs for testing
		this.numTestPages = testPages;

		// TODO to move as provided input
		String propertyS = "http://dbpedia.org/ontology/director";
		String domainS = "http://www.imdb.com/title/";

		this.goldenRuleL = new XPathRule("//*[@id='overview-top']/H1/SPAN[1]/text()");
		this.goldenRuleR = new XPathRule("//*[contains(text(),\"Director:\") or contains(text(),\"Directors:\")]/../A[1]/SPAN[1]/TEXT()[1]");

		DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL(domainS));

		this.property = ResourceFactory.createProperty(propertyS);
		
		ExampleGenerator exampleGenerator = ExampleGeneratorFactory.getInstance().getExampleGenerator(this.property, trainingPages*100);
		Set<Pair<Resource, Resource>> positiveExamples = exampleGenerator.getPositiveExamples();
		
		if(positiveExamples.size() < maxPages)
			throw new RuntimeException("Number of training pairs should be: "+ maxPages + " but number of pairs was " +positiveExamples.size());
		
		List<Pair<Resource, Resource>> examples = new LinkedList<Pair<Resource, Resource>>(positiveExamples);
		
		log.debug("Number of pairs: "+examples.size());
		
		this.testing = this.pageRetriever.getPages(numTestPages);
		this.training = new HashSet<Pair<Resource, Resource>>(examples.subList(0, maxPages));
		
		 
		this.domain = domainIdentifier.getDomain(property, new HashSet<Pair<Resource, Resource>>(
this.training), null, false);
		
		log.debug("Learn pairs: " + this.training.size());
		log.debug("Test pages: " + this.testing.size());
	}

	@Override
	public void run() {

		ALFREDXPathLearner learner = new ALFREDXPathLearner(this.index, this.trainingPages);
		List<Pair<XPathRule, XPathRule>> xpaths = learner.getXPathExpressions(this.training, null,
				domain);
		Rule ruleL = xpaths.get(0).getLeft();
		Rule ruleR = xpaths.get(0).getRight();

		log.debug("Learned XPath L: " + ruleL);
		log.debug("Learned XPath R: " + ruleR);

		Map<String, String> page2valueLeft = new HashMap<String, String>();
		Map<String, String> page2valueRight = new HashMap<String, String>();

		ALFREDPageRetrieval pageRetr = new ALFREDPageRetrieval(learner.getIndex());

		List<Page> trainingPages = pageRetr.getPages(this.training, page2valueLeft,
				page2valueRight, domain);
		
		learner.getIndex().close();

		ALFREDSampler samplerL = learner.getSamplerLeft();
		ALFREDSampler samplerR = learner.getSamplerRight();
		
		samplerL.addPages(this.testing);
		samplerR.addPages(this.testing);
		
		List<Page> representedPagesL = samplerL.getRepresentativePages();
		List<Page> representedPagesR = samplerR.getRepresentativePages();
		List<Page> notRepresentedPagesL = samplerL.getNonRepresentedPages();
		List<Page> notRepresentedPagesR = samplerR.getNonRepresentedPages();
		
		log.debug("Test on learning pages");

		QualityEvaluator representedL = testResultRule(ruleL, this.goldenRuleL, representedPagesL);
		QualityEvaluator representedR =testResultRule(ruleR, this.goldenRuleR, representedPagesR);
		QualityEvaluator notRepresentedL = testResultRule(ruleL, this.goldenRuleL, notRepresentedPagesL);
		QualityEvaluator notRepresentedR =testResultRule(ruleR, this.goldenRuleR, notRepresentedPagesR);

		log.info("I: "+trainingPages.size() + " U: "+this.testing.size());
		log.info("Left: rule "+ruleL);
		log.info("U': "+representedPagesL.size() + " U-U': "+notRepresentedPagesL.size());
		log.info("Left U': (P) "+representedL.getPrecision()+" (R) "+representedL.getRecall()+" (F) "+representedL.getF());
		log.info("Left U-U': (P) "+notRepresentedL.getPrecision()+" (R) "+notRepresentedL.getRecall()+" (F) "+notRepresentedL.getF());
		log.info("Right: rule "+ruleR);
		log.info("U': "+representedPagesR.size() + " U-U': "+notRepresentedPagesR.size());
		log.info("Right U': (P) "+representedR.getPrecision()+" (R) "+representedR.getRecall()+" (F) "+representedR.getF());
		log.info("Right U-U': (P) "+notRepresentedR.getPrecision()+" (R) "+notRepresentedR.getRecall()+" (F) "+notRepresentedR.getF());
	}

	private static QualityEvaluator testResultRule(Rule result, Rule golden, List<Page> pages) {
		QualityEvaluator ev = new QualityEvaluator(result, golden, pages);
		ev.evaluate();
		return ev;
	}
	
	public static void main(String[] args) throws MalformedURLException {
		ExperimentRunner exp = new ExperimentRunner("htmlindex", 100, 400, 1000, 100);
		exp.run();
	}
}
