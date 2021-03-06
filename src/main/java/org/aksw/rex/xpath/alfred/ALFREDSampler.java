package org.aksw.rex.xpath.alfred;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.Page;
import model.Rule;

import org.slf4j.LoggerFactory;

/**
 * retrieves a number of sample pages and xpaths from crawl index
 * @author d.qui
 * 
 */
public class ALFREDSampler {

	private org.slf4j.Logger log = LoggerFactory.getLogger(ALFREDSampler.class);

	private List<Page> nonRepresentedPages;
	private List<Page> representedPages;

	private List<Rule> rulesSet;

	public ALFREDSampler(List<Rule> rulesSet) {

		this.rulesSet = rulesSet;

		this.nonRepresentedPages = new LinkedList<Page>();
		this.representedPages = new LinkedList<Page>();
	}

	/**
	 * adds pages to the set of samples
	 * 
	 * @param pageSet
	 */
	public void addPages(List<Page> pageSet) {
		int numPages = pageSet.size();
		int i = 0;
		int d = (int) ((double) numPages / 10);
		log.debug("Sampling on " + numPages + " pages: ");

		Iterator<Page> iterPages = pageSet.iterator();
		while (iterPages.hasNext()) {
			Page pagina = iterPages.next();
			addPage(pagina);
			i++;
			if (d != 0 && i % d == 0)
				log.info(i + " ");
		}
		log.info(" done!");
	}

	/**
	 * 
	 * @return non represented pages
	 */
	public List<Page> getNonRepresentedPages() {
		return nonRepresentedPages;
	}

	/**
	 * 
	 * @return in the sample represented pages
	 */
	public List<Page> getRepresentedPages() {
		return representedPages;
	}
	/**
	 * adds pages to the set of samples
	 * 
	 * @param pageSet
	 */
	public void addPage(Page pagina) {
		if (!this.nonRepresentedPages.contains(pagina) && !this.representedPages.contains(pagina)) {
			Map<Rule, String> rule2value = new HashMap<Rule, String>();

			for (Rule regola : this.rulesSet) {
				String estratto = regola.applyOn(pagina).getTextContent();
				rule2value.put(regola, estratto);
			}

			if (isRapresentative(rule2value)) {
				this.nonRepresentedPages.add(pagina);
			} else
				this.representedPages.add(pagina);
		}
	}

	private boolean isRapresentative(Map<Rule, String> rule2value) {
		return !sameValues(new LinkedList<String>(rule2value.values()));
	}

	private boolean sameValues(List<String> values) {
		boolean same = true;
		String firstValue = values.get(0);
		for (String otherValues : values) {
			if (!firstValue.equals(otherValues)) {
				same = false;
				break;
			}
		}
		return same;
	}
}
