package org.aksw.rex.experiments;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

	private int trainingPairs;
	private int numTestPages;

	private boolean checkQuality;

	public ExperimentRunner(String index, String prop, String domain, int maxPairs, int testPages, boolean quality) throws MalformedURLException {
		this.checkQuality = quality;
		this.index = new CrawlIndex(index);
		this.pageRetriever = new ALFREDPageRetrieval(this.index);

		// N pairs for learning
		this.trainingPairs = maxPairs;
		// N pairs for testing
		this.numTestPages = testPages;

		// TODO to move as provided input
		String propertyS = "http://dbpedia.org/ontology/"+prop;
		String domainS = domain;

		this.goldenRuleL = new XPathRule("//*[@id='overview-top']/H1/SPAN[1]/text()");
		this.goldenRuleR = new XPathRule("//*[contains(text(),\"Director:\") or contains(text(),\"Directors:\")]/../A[1]/SPAN[1]/TEXT()[1]");

		DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL(domainS));

		this.property = ResourceFactory.createProperty(propertyS);

		ExampleGenerator exampleGenerator = ExampleGeneratorFactory.getInstance()
				.getExampleGenerator(this.property, trainingPairs);
		Set<Pair<Resource, Resource>> positiveExamples = exampleGenerator.getPositiveExamples();

		if (positiveExamples.size() < this.trainingPairs)
			throw new RuntimeException("Number of training pairs should be: " + this.trainingPairs
					+ " but number of pairs was " + positiveExamples.size());

		List<Pair<Resource, Resource>> examples = new LinkedList<Pair<Resource, Resource>>(
				positiveExamples);

		log.info("Number of pairs: " + examples.size());

		this.training = new HashSet<Pair<Resource, Resource>>(examples);

		this.domain = domainIdentifier.getDomain(property, new HashSet<Pair<Resource, Resource>>(
				this.training), null, false);

		log.info("Learn pairs: " + this.training.size());

		if (this.checkQuality) {
			this.testing = this.pageRetriever.getPages(numTestPages);
			log.info("Test pages: " + this.testing.size());
		}
	}

	@Override
	public void run() {

		ALFREDXPathLearner learner = new ALFREDXPathLearner(this.index, this.trainingPairs);
		List<Pair<XPathRule, XPathRule>> xpaths = learner.getXPathExpressions(this.training, null,domain);
		Rule ruleL = xpaths.get(0).getLeft();
		Rule ruleR = xpaths.get(0).getRight();

		log.info("Learned XPath L: " + ruleL);
		log.info("Learned XPath R: " + ruleR);

		learner.getIndex().close();
		if (this.checkQuality){
			List<Page> trainingPages = learner.getTrainingPages();
			
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
			QualityEvaluator representedR = testResultRule(ruleR, this.goldenRuleR, representedPagesR);
			QualityEvaluator notRepresentedL = testResultRule(ruleL, this.goldenRuleL,
					notRepresentedPagesL);
			QualityEvaluator notRepresentedR = testResultRule(ruleR, this.goldenRuleR,
					notRepresentedPagesR);
			
			log.info("I: " + trainingPages.size() + " U: " + this.testing.size());
			log.info("Left: rule " + ruleL);
			log.info("U': " + representedPagesL.size() + " U-U': " + notRepresentedPagesL.size());
			log.info("Left U': (P) " + representedL.getPrecision() + " (R) " + representedL.getRecall()
					+ " (F) " + representedL.getF());
			log.info("Left U-U': (P) " + notRepresentedL.getPrecision() + " (R) "
					+ notRepresentedL.getRecall() + " (F) " + notRepresentedL.getF());
			log.info("Right: rule " + ruleR);
			log.info("U': " + representedPagesR.size() + " U-U': " + notRepresentedPagesR.size());
			log.info("Right U': (P) " + representedR.getPrecision() + " (R) "
					+ representedR.getRecall() + " (F) " + representedR.getF());
			log.info("Right U-U': (P) " + notRepresentedR.getPrecision() + " (R) "
					+ notRepresentedR.getRecall() + " (F) " + notRepresentedR.getF());
		}
	}

	private static QualityEvaluator testResultRule(Rule result, Rule golden, List<Page> pages) {
		QualityEvaluator ev = new QualityEvaluator(result, golden, pages);
		ev.evaluate();
		return ev;
	}

	public static void main(String[] args) throws MalformedURLException {
		String index = args[0];
		String prop = args[1];
		String domain = args[2];
		int pairs = Integer.parseInt(args[3]);
		boolean quality = Boolean.parseBoolean(args[4]);
		ExperimentRunner exp = new ExperimentRunner(index, prop, domain, pairs, 10000, quality);
		exp.run();
	}
}
