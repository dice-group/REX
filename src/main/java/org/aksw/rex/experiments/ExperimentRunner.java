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
		
		List<Pair<Resource, Resource>> examples = new LinkedList<>(positiveExamples);
		
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

		Map<String, String> testPage2valueLeft = new HashMap<String, String>();
		Map<String, String> testPage2valueRight = new HashMap<String, String>();

		ALFREDPageRetrieval pageRetr = new ALFREDPageRetrieval(learner.getIndex());
		List<Page> pagineTest = pageRetr.getPages(this.testing, testPage2valueLeft,
				testPage2valueRight, domain);
		learner.getIndex().close();

		log.debug("Test on learning pages");

		QualityEvaluator left = testResultRule(ruleL, this.goldenRuleL, pagineTest);
		QualityEvaluator right =testResultRule(ruleR, this.goldenRuleR, pagineTest);

		log.info("Left: rule "+ruleL);
		log.info("Left: (P) "+left.getPrecision()+" (R) "+left.getRecall()+" (F) "+left.getF());
		log.info("Right: rule "+ruleR);
		log.info("Right: (P) "+right.getPrecision()+" (R) "+right.getRecall()+" (F) "+right.getF());

//		Map<String, String> page2valueLeft = new HashMap<String, String>();
//		Map<String, String> page2valueRight = new HashMap<String, String>();
		
//		List<Page> pagineLearn = pageRetr.getPages(this.training, page2valueLeft, page2valueRight,
//				domain);
//		testTwoRules(pagineLearn, regolaL, regolaR, goldenRuleL, goldenRuleR, page2valueLeft,
//				page2valueRight);
//		
		
//		ALFREDSampler samplerL = learner.getSamplerLeft();
//		samplerL.find(pagineTest);
//		
//		log.debug("LEFT RULE:");
//		log.debug("Test on " + samplerL.getRepresentedPages().size()+" represented pages");
//		
//		testRule(samplerL.getRepresentedPages(), regolaL, goldenRuleL, testPage2valueLeft);
//		testRule(samplerL.getNonRepresentedPages(), regolaL, goldenRuleL, testPage2valueLeft);
//
//		log.debug("Test on " + samplerL.getNonRepresentedPages().size()
//				+ " non represented pages - (representative: "
//				+ samplerL.getRepresentativePages().size() + ")");
//
//		ALFREDSampler samplerR = learner.getSamplerRight();
//		samplerR.find(pagineTest);
//		log.debug("RIGHT RULE:");
//		log.debug("Test on " + samplerR.getRepresentedPages().size()
//				+ " represented pages");
//	
//		testRule(samplerR.getRepresentedPages(), regolaR, goldenRuleR, testPage2valueRight);
//		testRule(samplerR.getNonRepresentedPages(), regolaR, goldenRuleR, testPage2valueRight);
//	
//		log.debug("Test on " + samplerR.getNonRepresentedPages().size()
//				+ " non represented pages - (representative: "
//				+ samplerR.getRepresentativePages().size() + ")");
	}

//	private static void testRule(List<Page> pagine, Rule regola, Rule goldRegola,
//			Map<String, String> page2value) {
//		int numPageTest = pagine.size();
//		int giuste = 0, goldGiuste = 0, RvsG = 0;
//		Iterator<Page> iterPageTest = pagine.iterator();
//		while (iterPageTest.hasNext()) {
//			Page pagina = iterPageTest.next();
//			String valore = regola.applyOn(pagina).getTextContent();
//			if (valore.equals(page2value.get(pagina.getTitle())))
//				giuste++;
//			String goldValore = goldRegola.applyOn(pagina).getTextContent();
//			if (goldValore.equals(page2value.get(pagina.getTitle())))
//				goldGiuste++;
//			if (valore.equals(goldValore))
//				RvsG++;
//			iterPageTest.remove();
//		}
//		System.out.print("Extracted Rule: " + giuste + " su " + numPageTest + " - ");
//		System.out.printf("%.0f", ((double) giuste / numPageTest) * 100);
//		System.out.println("%");
//		System.out.print("Golden Rule: " + goldGiuste + " su " + numPageTest + " - ");
//		System.out.printf("%.0f", ((double) goldGiuste / numPageTest) * 100);
//		System.out.println("%");
//		System.out.print("Extracted Rule vs Golden Rule: " + RvsG + " su " + numPageTest + " - ");
//		System.out.printf("%.0f", ((double) RvsG / numPageTest) * 100);
//		System.out.println("%");
//	}
//
//	private static void testTwoRules(List<Page> pagine, Rule regolaL, Rule regolaR,
//			Rule goldRegolaL, Rule goldRegolaR, Map<String, String> page2valueLeft,
//			Map<String, String> page2valueRight) {
//		int numPageTest = pagine.size();
//		int giusteL = 0, giusteR = 0, goldGiusteL = 0, goldGiusteR = 0, RvsGleft = 0, RvsGright = 0;
//		Iterator<Page> iterPageTest = pagine.iterator();
//		while (iterPageTest.hasNext()) {
//			Page pagina = iterPageTest.next();
//			String valoreL = regolaL.applyOn(pagina).getTextContent();
//			if (valoreL.equals(page2valueLeft.get(pagina.getTitle())))
//				giusteL++;
//			String valoreR = regolaR.applyOn(pagina).getTextContent();
//			if (valoreR.equals(page2valueRight.get(pagina.getTitle())))
//				giusteR++;
//			String goldValoreL = goldRegolaL.applyOn(pagina).getTextContent();
//			if (goldValoreL.equals(page2valueLeft.get(pagina.getTitle())))
//				goldGiusteL++;
//			if (valoreL.equals(goldValoreL))
//				RvsGleft++;
//			String goldValoreR = goldRegolaR.applyOn(pagina).getTextContent();
//			if (goldValoreR.equals(page2valueRight.get(pagina.getTitle())))
//				goldGiusteR++;
//			if (valoreR.equals(goldValoreR))
//				RvsGright++;
//		}
//
//		
//		System.out.print("Left rule: " + giusteL + " su " + numPageTest + " - ");
//		System.out.printf("%.0f", ((double) giusteL / numPageTest) * 100);
//		System.out.println("%");
//		System.out.print("Right rule: " + giusteR + " su " + numPageTest + " - ");
//		System.out.printf("%.0f", ((double) giusteR / numPageTest) * 100);
//		System.out.println("%");
//		System.out.print("Left Golden Rule: " + goldGiusteL + " su " + numPageTest + " - ");
//		System.out.printf("%.0f", ((double) goldGiusteL / numPageTest) * 100);
//		System.out.println("%");
//		System.out.print("Right Golden Rule: " + goldGiusteR + " su " + numPageTest + " - ");
//		System.out.printf("%.0f", ((double) goldGiusteR / numPageTest) * 100);
//		System.out.println("%");
//		System.out.print("Left rule vs Golden Rule: " + RvsGleft + " su " + numPageTest + " - ");
//		System.out.printf("%.0f", ((double) RvsGleft / numPageTest) * 100);
//		System.out.println("%");
//		System.out.print("Right rule vs Golden Rule: " + RvsGright + " su " + numPageTest + " - ");
//		System.out.printf("%.0f", ((double) RvsGright / numPageTest) * 100);
//		System.out.println("%");
//	}

	private static QualityEvaluator testResultRule(Rule result, Rule golden, List<Page> pages) {
		QualityEvaluator ev = new QualityEvaluator(result, golden, pages);
		ev.evaluate();
		return ev;
	}
	
	public static void main(String[] args) throws MalformedURLException {
		ExperimentRunner exp = new ExperimentRunner("htmlindex", 100, 500, 10000, 100);
		exp.run();
	}
}
