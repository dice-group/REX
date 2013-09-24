package org.aksw.rex.xpath.alfred;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.MaterializedPageSet;
import model.Page;

import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.results.ExtractionResult;
import org.aksw.rex.util.Pair;
import org.aksw.rex.xpath.XPathLearner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import org.slf4j.LoggerFactory;

import rules.xpath.XPathRule;
import alfcore.AlfCoreFacade;
import alfcore.AlfCoreFactory;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class ALFREDXPathLearner implements XPathLearner {

	private org.slf4j.Logger log = LoggerFactory.getLogger(ALFREDXPathLearner.class);
	private static CrawlIndex index;
	IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();

	public ALFREDXPathLearner() {
		index = new CrawlIndex("imdbIndex/");
	}

	@Override
	public List<Pair<XPathRule, XPathRule>> getXPathExpressions(
			Set<Pair<Resource, Resource>> posExamples, Set<Pair<Resource, Resource>> negExamples,
			URL Domain) {
		List<Pair<XPathRule, XPathRule>> res = new LinkedList<Pair<XPathRule, XPathRule>>();
		
		List<Pair<Resource, Resource>> examples = new LinkedList<Pair<Resource, Resource>>(
				posExamples);

		Map<String, String> page2valueLeft = new HashMap<String, String>();
		Map<String, String> page2valueRight = new HashMap<String, String>();
		
		examples.set(0, new Pair<Resource, Resource>(
				ResourceFactory.createResource("http://dbpedia.org/resource/Abraham_Lincoln:_Vampire_Hunter"),
				ResourceFactory.createResource("http://dbpedia.org/resource/Brad_Furman")));

		List<Page> pages = this.getPages(examples, page2valueLeft, page2valueRight);
		
		List<Page> pagesForFirstExample = getPages(examples.get(0));

		if(!pagesForFirstExample.isEmpty()){
			Page firstPage = pagesForFirstExample.get(0);

			XPathRule left = this.learnXPath(page2valueLeft, pages, firstPage);
			XPathRule right = this.learnXPath(page2valueRight, pages, firstPage);

			
			res.add(new Pair<XPathRule, XPathRule>(left, right));
		}
		
		return res;
	}

	private XPathRule learnXPath(Map<String, String> page2value, List<Page> pages, Page firstPage) {
		AlfCoreFacade facade = AlfCoreFactory.getSystemFromConfiguration(false, 10, 10, 1, 1,
				10000, "Entropy", 0.6);
		facade.setUp("DBPedia", new MaterializedPageSet(pages));

		facade.firstSample(firstPage.getTitle(), page2value.get(firstPage.getTitle()), 1);

		for (Page page : pages) {
			facade.nextSample(page.getTitle(), page2value.get(page.getTitle()), 1, "+");
		}
		XPathRule res = new XPathRule(facade.getMostCorrectVector().getRule().encode());
	
		return res;
	}

	@Override
	public Set<ExtractionResult> getExtractionResults(List<Pair<XPathRule, XPathRule>> expressions, URL domain) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Page> getPages(Collection<Pair<Resource, Resource>> resources,
			Map<String, String> page2valueLeft, Map<String, String> page2valueRight) {
		Set<Page> pages = new HashSet<Page>();

		for (Pair<Resource, Resource> pair : resources) {
			List<Page> tempPages = getPages(pair);
			pages.addAll(tempPages);

			for (Page p : tempPages) {
				page2valueLeft.put(p.getTitle(), sfp.getShortForm(IRI.create(pair.getLeft().getURI())).replace("_", " "));
				page2valueRight.put(p.getTitle(), sfp.getShortForm(IRI.create(pair.getRight().getURI())).replace("_", " "));
			}
		}

		return new LinkedList<Page>(pages);
	}

	private List<Page> getPages(Pair<Resource, Resource> resources) {
		List<Page> res = new LinkedList<Page>();
		List<Pair<String, String>> pairs = getPairs(resources);
		if(!pairs.isEmpty()){
			pairs = pairs.subList(0, Math.min(pairs.size(), 3));
		}

		for (Pair<String, String> pair : pairs) {
			log.debug("retrieved page: " + pair.getLeft());
			// TODO to filter retrieved pages by domain (title for url with www.imdb.com/title/ttxxxxx)
			if (pair.getLeft().contains("title"))
				res.add(new Page(pair.getRight(), null, pair.getLeft()));
		}
		return res;
	}

	private List<Pair<String, String>> getPairs(Pair<Resource, Resource> resources) {
		log.debug("Looking for resources " + sfp.getShortForm(IRI.create(resources.getLeft().getURI())) + " - "
				+ sfp.getShortForm(IRI.create(resources.getRight().getURI())));
		return index.searchHTML(
				"'" + sfp.getShortForm(IRI.create(resources.getLeft().getURI())).replace("_", " ") + "' AND '"
						+ sfp.getShortForm(IRI.create(resources.getRight().getURI())).replace("_", " ") + "'");
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
