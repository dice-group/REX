package org.aksw.rex.test.xpath.alfred;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.aksw.rex.xpath.alfred.ALFREDSampler;
import org.aksw.rex.xpath.alfred.ALFREDXPathLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

import rules.xpath.XPathRule;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class ALFREDExperiment {

	@Test
	public void testGetXPathExpressions() throws MalformedURLException {
		//N pairs for learning
		int numLearnPairs = 500;
		
		//N pairs for testing
		int numTestPairs = 40000;
		
		//property
		String propertyString = "http://dbpedia.org/ontology/director";
		
		//domain
		String domainString = "http://www.imdb.com/title/";
		
		//golden rules
//		Rule goldenRuleL = new XPathRule("//*[@id='overview-top']/H1/SPAN[1]/text()");
//		Rule goldenRuleR = new XPathRule("//*[@id='overview-top']/DIV[4]/A/SPAN/text()");
		
		
		Property property = ResourceFactory.createProperty(propertyString);
    	SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
    	
    	ExampleGenerator exampleGenerator = new SimpleExampleGenerator();
    	exampleGenerator.setMaxNrOfPositiveExamples(numLearnPairs+numTestPairs);
    	exampleGenerator.setEndpoint(endpoint);
    	exampleGenerator.setPredicate(property);
		
    	System.out.println("number of pairs: "+exampleGenerator.getPositiveExamples().size());
    	
		Set<Pair<Resource, Resource>> posExamples = new HashSet<Pair<Resource, Resource>>();
		Set<Pair<Resource, Resource>> testExamples = new HashSet<Pair<Resource, Resource>>();
		Set<Pair<Resource, Resource>> examples = exampleGenerator.getPositiveExamples();
		Iterator<Pair<Resource, Resource>> iterExamples = examples.iterator();
		for (int i=0;i<numLearnPairs;i++) {
			posExamples.add(iterExamples.next());
			iterExamples.remove();
		}
		while (iterExamples.hasNext()) {
			testExamples.add(iterExamples.next());
			iterExamples.remove();
		}
		System.out.println("Learn pairs: "+posExamples.size());
		System.out.println("Test pairs: "+testExamples.size());
		
		DomainIdentifier domainIdentifier = new ManualDomainIdentifier(new URL(domainString));
		URL domain = domainIdentifier.getDomain(property, posExamples, null, false);
		
		CrawlIndex index = new CrawlIndex("htmlindex");
		ALFREDXPathLearner learner = new ALFREDXPathLearner(index);
		List<Pair<XPathRule, XPathRule>> xpaths = learner.getXPathExpressions(posExamples, null, domain);
		Rule regolaL = xpaths.get(0).getLeft();
		Rule regolaR = xpaths.get(0).getRight();
				
		Map<String, List<String>> page2valueLeft = new HashMap<String, List<String>>();
		Map<String, List<String>> page2valueRight = new HashMap<String, List<String>>();
		Map<String, List<String>> testPage2valueLeft = new HashMap<String, List<String>>();
		Map<String, List<String>> testPage2valueRight = new HashMap<String, List<String>>();
		
		ALFREDPageRetrieval pageRetr = new ALFREDPageRetrieval(learner.getIndex());
		List<Page> pagineLearn = pageRetr.getPages(posExamples, page2valueLeft, page2valueRight, domain);
		List<Page> pagineTest = pageRetr.getPages(testExamples, testPage2valueLeft, testPage2valueRight, domain);
		
		System.out.println("Learning pages: "+pagineLearn.size()+ " testing: "+pagineTest.size());
		System.exit(0);
		learner.getIndex().close();
		
		System.out.println("Left Rule: "+regolaL.encode());
		System.out.println("Right Rule: "+regolaR.encode());

		System.out.println("Test on learning pages");
		
		ALFREDSampler samplerL = learner.getSamplerLeft();
		samplerL.addPages(pagineTest);
		System.out.println("LEFT RULE:");
		System.out.println("Test on "+samplerL.getRepresentedPages().size()+" represented pages");
		System.out.println("Test on "+samplerL.getNonRepresentedPages().size()+" non represented pages - (representative: "+
				samplerL.getRepresentativePages().size()+")");

		ALFREDSampler samplerR = learner.getSamplerRight();
		samplerR.addPages(pagineTest);
		System.out.println("RIGHT RULE:");
		System.out.println("Test on "+samplerR.getRepresentedPages().size()+" represented pages");
		System.out.println("Test on "+samplerR.getNonRepresentedPages().size()+" non represented pages - (representative: "+
				samplerR.getRepresentativePages().size()+")");
	}
	
//	private static void testRule(List<Page> pagine, Rule regola, Rule goldRegola, Map<String, String> page2value) {
//	int numPageTest = pagine.size();
//	int giuste = 0, goldGiuste = 0, RvsG = 0;
//	Iterator<Page> iterPageTest = pagine.iterator();
//	while (iterPageTest.hasNext()) {
//		Page pagina = iterPageTest.next();
//		String valore = regola.applyOn(pagina).getTextContent();
//		if (valore.equals(page2value.get(pagina.getTitle()))) giuste++;
//		String goldValore = goldRegola.applyOn(pagina).getTextContent();
//		if (goldValore.equals(page2value.get(pagina.getTitle()))) goldGiuste++;
//		if (valore.equals(goldValore)) RvsG++;
//		iterPageTest.remove();
//	}
//	System.out.print("Extracted Rule: "+giuste+" su "+numPageTest+" - ");
//	System.out.printf("%.0f",((double)giuste/numPageTest)*100);
//	System.out.println("%");
//	System.out.print("Golden Rule: "+goldGiuste+" su "+numPageTest+" - ");
//	System.out.printf("%.0f",((double)goldGiuste/numPageTest)*100);
//	System.out.println("%");
//	System.out.print("Extracted Rule vs Golden Rule: "+RvsG+" su "+numPageTest+" - ");
//	System.out.printf("%.0f",((double)RvsG/numPageTest)*100);
//	System.out.println("%");
//	}
	
//	private static void testTwoRules(List<Page> pagine, Rule regolaL, Rule regolaR, Rule goldRegolaL,
//			Rule goldRegolaR, Map<String, String> page2valueLeft, Map<String, String> page2valueRight) {
//	int numPageTest = pagine.size();
//	int giusteL = 0, giusteR = 0, goldGiusteL = 0, goldGiusteR = 0, RvsGleft= 0, RvsGright = 0;
//	Iterator<Page> iterPageTest = pagine.iterator();
//	while (iterPageTest.hasNext()) {
//		Page pagina = iterPageTest.next();
//		String valoreL = regolaL.applyOn(pagina).getTextContent();
//		if (valoreL.equals(page2valueLeft.get(pagina.getTitle()))) giusteL++;
//		String valoreR = regolaR.applyOn(pagina).getTextContent();
//		if (valoreR.equals(page2valueRight.get(pagina.getTitle()))) giusteR++;
//		String goldValoreL = goldRegolaL.applyOn(pagina).getTextContent();
//		if (goldValoreL.equals(page2valueLeft.get(pagina.getTitle()))) goldGiusteL++;
//		if (valoreL.equals(goldValoreL)) RvsGleft++;
//		String goldValoreR = goldRegolaR.applyOn(pagina).getTextContent();
//		if (goldValoreR.equals(page2valueRight.get(pagina.getTitle()))) goldGiusteR++;
//		if (valoreR.equals(goldValoreR)) RvsGright++;
//	}
//	
//	testResultRule(regolaL, goldRegolaL, pagine);
//	testResultRule(regolaR, goldRegolaR, pagine);
//
//	System.out.print("Left rule: "+giusteL+" su "+numPageTest+" - ");
//	System.out.printf("%.0f",((double)giusteL/numPageTest)*100);
//	System.out.println("%");
//	System.out.print("Right rule: "+giusteR+" su "+numPageTest+" - ");
//	System.out.printf("%.0f",((double)giusteR/numPageTest)*100);
//	System.out.println("%");
//	System.out.print("Left Golden Rule: "+goldGiusteL+" su "+numPageTest+" - ");
//	System.out.printf("%.0f",((double)goldGiusteL/numPageTest)*100);
//	System.out.println("%");
//	System.out.print("Right Golden Rule: "+goldGiusteR+" su "+numPageTest+" - ");
//	System.out.printf("%.0f",((double)goldGiusteR/numPageTest)*100);
//	System.out.println("%");
//	System.out.print("Left rule vs Golden Rule: "+RvsGleft+" su "+numPageTest+" - ");
//	System.out.printf("%.0f",((double)RvsGleft/numPageTest)*100);
//	System.out.println("%");
//	System.out.print("Right rule vs Golden Rule: "+RvsGright+" su "+numPageTest+" - ");
//	System.out.printf("%.0f",((double)RvsGright/numPageTest)*100);
//	System.out.println("%");
//	}
//	
//	private static void testResultRule(Rule result, Rule golden, List<Page> pages){
//		QualityEvaluator ev = new QualityEvaluator(result, golden, pages);
//		ev.evaluate();
//	}
	
}
