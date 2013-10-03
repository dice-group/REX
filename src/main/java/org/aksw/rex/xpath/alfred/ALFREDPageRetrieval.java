package org.aksw.rex.xpath.alfred;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Page;

import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.domainidentifier.ManualDomainIdentifier;
import org.aksw.rex.examplegenerator.ExampleGenerator;
import org.aksw.rex.experiments.ExampleGeneratorFactory;
import org.aksw.rex.util.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import org.slf4j.LoggerFactory;

import rules.dom.TextElementFinder;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class ALFREDPageRetrieval {
	
	private org.slf4j.Logger log = LoggerFactory.getLogger(ALFREDPageRetrieval.class);
	private CrawlIndex index;
	private IRIShortFormProvider sfp;
	
	public ALFREDPageRetrieval(CrawlIndex index) {
		this.index = index;
		this.sfp = new SimpleIRIShortFormProvider();
	}
	
	public List<Page> getPages(int number){
		List<Page> pages = new LinkedList<Page>(this.index.getPages(number));
		log.debug("Retrieving: "+pages.size()+" of "+number);
		return pages;
	}

	public List<Page> getPages(Property property, int numPages, String domainS) {
		// retrieve a big number of pairs
		ExampleGenerator generator = ExampleGeneratorFactory.getInstance().getExampleGenerator(property, numPages*100);

		Set<Pair<Resource, Resource>> examples = generator.getPositiveExamples();
		
		// TODO to remove
		Map<String, List<String>> page2valueLeft = new HashMap<String, List<String>>();
		Map<String, List<String>> page2valueRight = new HashMap<String, List<String>>();
		
		ManualDomainIdentifier domainIdentifier = null;
		try {
			domainIdentifier = new ManualDomainIdentifier(new URL(domainS));
		} catch (MalformedURLException e) {
			throw new RuntimeException("URL "+domainS+" not well formatted");
		}
		
		URL domain = domainIdentifier.getDomain(property, examples, null, false);
		
		List<Page> trainingPages = this.getPages(generator.getPositiveExamples(), page2valueLeft,
				page2valueRight, domain);
		
		List<Page> res = (trainingPages.size() > numPages)? trainingPages.subList(0, numPages): trainingPages;
		return res;
	}
	
	/**
	 * Return list of pages from a given list of pairs
	 * 
	 * @param resources
	 * @param page2valueLeft
	 * @param page2valueRight
	 * @param domain
	 * @return
	 */
	public List<Page> getPages(Collection<Pair<Resource, Resource>> resources,
			Map<String, List<String>> page2valueLeft, Map<String, List<String>> page2valueRight, URL domain) {
		Set<Page> pages = new HashSet<Page>();
		
		Iterator<Pair<Resource, Resource>> iterResources = resources.iterator();
		while (iterResources.hasNext()) {
			Pair<Resource, Resource> pair = iterResources.next();
			if (pair.getLeft() != null && pair.getRight() != null) {
				List<Page> tempPages = getPages(pair, domain);
				pages.addAll(tempPages); 
	
				for (Page p : tempPages) {
					this.addToMap(p.getTitle(), sfp.getShortForm(IRI.create(pair.getLeft().getURI())).replace("_", " "), page2valueLeft);
					this.addToMap(p.getTitle(), sfp.getShortForm(IRI.create(pair.getRight().getURI())).replace("_", " "),page2valueRight);
				}
			}
		}

		return new LinkedList<Page>(pages);
	}
	
	private void addToMap(String key, String value, Map<String, List<String>> map){
		if (map.containsKey(key)){
			map.get(key).add(value);
		} else {
			List<String> values = new LinkedList<String>();
			values.add(value);
			map.put(key, values);
		}
	}

	private List<Page> getPages(Pair<Resource, Resource> resources, URL domain) {
		List<Page> res = new LinkedList<Page>();
		
		//check if well formed resource pair
		String leftValue = resources.getLeft().getLocalName().replace("_", " ");
		String rightValue = resources.getRight().getLocalName().replace("_", " ");
		if (!leftValue.equals("") && !rightValue.equals("")) {
			List<Pair<String, String>> pairs = getPairs(resources);
			if(!pairs.isEmpty()){
				pairs = pairs.subList(0, Math.min(pairs.size(), 1));
			}
			for (Pair<String, String> pair : pairs) {			
				//results filtering
				if (domain == null || pair.getLeft().startsWith(domain.toString())) { //TODO improve Domain based filtering
					Page page = new Page(pair.getRight(), null, pair.getLeft());
					TextElementFinder TEfinderL = new TextElementFinder(page, leftValue);
					if (TEfinderL.getNodeWithTextContent() != null) {
						TextElementFinder TEfinderR = new TextElementFinder(page, resources.getRight().getLocalName().replace("_", " "));
						if (TEfinderR.getNodeWithTextContent() != null) {
							res.add(page);
							//log.debug("Retrieved page: " + pair.getLeft());
						}
					}
				}
			}
		}
		
		return res;
	}

	private List<Pair<String, String>> getPairs(Pair<Resource, Resource> resources) {
//		log.debug("Looking for resources " + sfp.getShortForm(IRI.create(resources.getLeft().getURI())) + " - "
//				+ sfp.getShortForm(IRI.create(resources.getRight().getURI())));
		return index.searchHTML(
				"\"" + sfp.getShortForm(IRI.create(resources.getLeft().getURI())).replace("_", " ") + "\" AND \""
						+ sfp.getShortForm(IRI.create(resources.getRight().getURI())).replace("_", " ") + "\"");
	}
}
