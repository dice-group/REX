package org.aksw.rex.xpath.alfred;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.MaterializedPageSet;
import model.Page;
import model.Rule;
import model.RulePageMatrix;
import model.Vector;

import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.results.ExtractionResult;
import org.aksw.rex.util.Pair;
import org.aksw.rex.xpath.XPathLearner;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import org.slf4j.LoggerFactory;

import rules.xpath.XPathRule;
import alfcore.AlfCoreFacade;
import alfcore.AlfCoreFactory;

import com.hp.hpl.jena.rdf.model.Resource;

public class ALFREDXPathLearner implements XPathLearner {

	private org.slf4j.Logger log = LoggerFactory.getLogger(ALFREDXPathLearner.class);
	private CrawlIndex index;
	IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();
	private ALFREDSampler samplerLeft;
	private ALFREDSampler samplerRight;

	public ALFREDXPathLearner(CrawlIndex index) {
		this.index = index;
	}

	@Override
	public List<Pair<XPathRule, XPathRule>> getXPathExpressions(
			Set<Pair<Resource, Resource>> posExamples, Set<Pair<Resource, Resource>> negExamples,
			URL Domain) {
		List<Pair<XPathRule, XPathRule>> res = new LinkedList<Pair<XPathRule, XPathRule>>();

		Map<String, String> page2valueLeft = new HashMap<String, String>();
		Map<String, String> page2valueRight = new HashMap<String, String>();

		ALFREDPageRetrieval pageRetr = new ALFREDPageRetrieval(this.index);
		List<Page> pages = pageRetr.getPages(posExamples, page2valueLeft, page2valueRight, Domain);
		log.debug("N found pages: "+pages.size());
		
		if(!pages.isEmpty()) {
			Page firstPage = pages.get(0);
			log.debug("First page: "+firstPage.getTitle());
			
			this.samplerLeft = null;
			XPathRule left = this.learnXPath(page2valueLeft, pages, firstPage);
			XPathRule right = this.learnXPath(page2valueRight, pages, firstPage);
			
			res.add(new Pair<XPathRule, XPathRule>(left, right));
		} else {
			log.debug("Error: No page found");
		}
		
		return res;
	}

	private XPathRule learnXPath(Map<String, String> page2value, List<Page> pages, Page firstPage) {
		AlfCoreFacade facade = AlfCoreFactory.getSystemFromConfiguration(false, 10, 10, 1, 1,
				10000, "Entropy", 0.6);
		facade.setUp("DBPedia", new MaterializedPageSet(pages));
		
		log.debug("First page value: "+page2value.get(firstPage.getTitle()));
		facade.firstSample(firstPage.getTitle(), page2value.get(firstPage.getTitle()), 1);

		for (Page page : pages) {
			facade.nextSample(page.getTitle(), page2value.get(page.getTitle()), 1, "+");
		}
		XPathRule res = new XPathRule(facade.getMostCorrectVector().getRule().encode());
		
		if (this.samplerLeft == null) {
			this.samplerLeft = generateSampler(facade);
		} else {
			this.samplerRight = generateSampler(facade);
		}

		//TODO apply sampler on all sub-domain pages
//		sampler.find( ??? );		
//		this.log.info("N represented pages: "+sampler.getRepresentedPages().size());
//		this.log.info("N non represented pages: "+sampler.getNonRepresentedPages().size()+" (representative: "+
//				sampler.getRepresentativePages().size()+")");
		
		return res;
	}

	/**
	 * @return
	 * null if getXPathExpressions was never called
	 * left rule sampler otherwise
	 */
	public ALFREDSampler getSamplerLeft() {
		return this.samplerLeft;
	}
	
	/**
	 * @return
	 * null if getXPathExpressions was never called
	 * right rule sampler otherwise
	 */
	public ALFREDSampler getSamplerRight() {
		return this.samplerRight;
	}

	private ALFREDSampler generateSampler(AlfCoreFacade facade) {
		//build rulesSets - TODO move into AlfCoreFacade
		
		List<Vector> vectors = facade.getVectors();
		RulePageMatrix matrix = facade.getRulePageMatrix();
		List<List<Rule>> rulesSets = new LinkedList<List<Rule>>();
		double prob = 0;
		for (Vector vettore : vectors) {
			prob += matrix.getProbability(vettore);
		}
		double probThreshold = prob/vectors.size();
		for (Vector vettore : vectors) {
			if(matrix.getProbability(vettore) >= probThreshold) {
				List<Rule> rulesSet = new LinkedList<Rule>();
				rulesSet.addAll(vettore.getRules());
				rulesSets.add(rulesSet);
			}
		}
		
		ALFREDSampler sampler = new ALFREDSampler(rulesSets);
		
		return sampler;
	}
	
	@Override
	public Set<ExtractionResult> getExtractionResults(List<Pair<XPathRule, XPathRule>> expressions, URL domain) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public CrawlIndex getIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * @see org.aksw.rex.xpath.XPathLearner#setUseExactMatch(boolean)
	 */
	@Override
	public void setUseExactMatch(boolean useExactMatch) {
		
	}
}
