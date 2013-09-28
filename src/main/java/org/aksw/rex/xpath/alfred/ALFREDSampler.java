package org.aksw.rex.xpath.alfred;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Page;
import model.Rule;

import org.slf4j.LoggerFactory;

public class ALFREDSampler {
	private org.slf4j.Logger log = LoggerFactory.getLogger(ALFREDSampler.class);
	
	private List<List<Rule>> originalRulesSets;
	private List<List<Rule>> currentRulesSets;
	private List<Rule> allRules;	
	private Map<List<Rule>, Integer> rulesSets2numPages;
	private Map<List<Rule>, List<Page>> rulesSets2representativePages;
	private Map<Rule, List<Rule>> rule2originalRulesSet;
	private Map<Rule, Integer> rule2numNull;
	private List<Page> representativePages;
	private List<Page> nonRepresentedPages;
	private List<Page> representedPages;
	
	public ALFREDSampler(List<List<Rule>> rulesSets) {
		
		this.originalRulesSets = rulesSets;
		this.currentRulesSets = new LinkedList<List<Rule>>();
		this.rulesSets2numPages = new HashMap<List<Rule>, Integer>();
		this.allRules = new LinkedList<Rule>();
		this.rule2originalRulesSet = new HashMap<Rule, List<Rule>>();
		this.rulesSets2representativePages = new HashMap<List<Rule>, List<Page>>();
		this.rule2numNull = new HashMap<Rule, Integer>();
		
		for (List<Rule> rulesSet : rulesSets) {
			List<Rule> nuovoRulesSet = new LinkedList<Rule>();
			nuovoRulesSet.addAll(rulesSet);
			this.currentRulesSets.add(nuovoRulesSet);
			this.rulesSets2numPages.put(rulesSet, new Integer(0));
			this.allRules.addAll(rulesSet);
			for (Rule regola : rulesSet) {
				this.rule2originalRulesSet.put(regola, rulesSet);
				this.rule2numNull.put(regola, new Integer(0));
			}
			this.rulesSets2representativePages.put(rulesSet, new LinkedList<Page>());
		}
		
		this.representativePages = new LinkedList<Page>();
		this.nonRepresentedPages = new LinkedList<Page>();
		this.representedPages = new LinkedList<Page>();		
	}
	
	public void find(List<Page> pageSet){
		int numPages = pageSet.size();
		int i = 0;
		int d = (int)((double)numPages/10);
		log.debug("Sampling on "+numPages+" pages: ");
		
		Iterator<Page> iterPages = pageSet.iterator();
		while (iterPages.hasNext()) {
			Page pagina = iterPages.next();
			addPage(pagina);
			i++;
			if (d!=0 && i%d == 0) log.debug(i+" ");
		}		
		log.debug(" done!");
	}
	
	public List<List<Rule>> getCurrentRulesSets() {
		return currentRulesSets;
	}
	
	public Map<List<Rule>, Integer> getRulesSets2NumberNotRepresentedPages() {
		return rulesSets2numPages;
	}

	/**
	 * Returns the rules sets generated from the each original rules set.
	 * 
	 * @return
	 */
	public Map<List<Rule>, Set<List<Rule>>> getOriginalRS2currentRS() {
		Map<List<Rule>, Set<List<Rule>>> originalRS2currentRS = new HashMap<List<Rule>, Set<List<Rule>>>();
		Map<Rule, List<Rule>> rule2currentRulesSet = new HashMap<Rule, List<Rule>>();
		for (List<Rule> rulesSet : this.originalRulesSets) {
			originalRS2currentRS.put(rulesSet, new HashSet<List<Rule>>());
		}
		for (List<Rule> ruleSet : this.currentRulesSets) {
			for (Rule regola : ruleSet) {
				rule2currentRulesSet.put(regola, ruleSet);
			}
		}
		for (Rule regola : this.rule2originalRulesSet.keySet()) {
			Set<List<Rule>> oldSet = originalRS2currentRS.get(this.rule2originalRulesSet.get(regola));
			oldSet.add(rule2currentRulesSet.get(regola));
		}
		
		return originalRS2currentRS;
	}

	/**
	 * Representative pages in U-I'
	 * 
	 * @return
	 */
	public Map<List<Rule>, List<Page>> getRulesSets2RepresentativePages() {
		return rulesSets2representativePages;
	}
	
	public Map<Rule, Double> getRule2NullRate() {
		Map<Rule, Double> rule2nullRate = new HashMap<Rule, Double>();
		int numPage = this.nonRepresentedPages.size()+this.representedPages.size();
		for (Rule regola : this.rule2numNull.keySet()) {
			rule2nullRate.put(regola, ((double)this.rule2numNull.get(regola))/numPage);
		}
		
		return rule2nullRate;
	}

	public List<Page> getRepresentativePages() {
		return representativePages;
	}

	public List<Page> getNonRepresentedPages() {
		return nonRepresentedPages;
	}

	public List<Page> getRepresentedPages() {
		return representedPages;
	}

	public void addPage(Page pagina){
		
			Map<Rule, String> rule2value = new HashMap<Rule, String>();
			
			for (Rule regola : this.allRules) {
				String estratto = regola.applyOn(pagina).getTextContent();
				rule2value.put(regola, estratto);
				if (estratto == "") {
					int numNull = this.rule2numNull.get(regola);
					numNull++;
					this.rule2numNull.put(regola, numNull);
				}
			}
			
			if (notRapresented(rule2value, pagina)) {
				if (isRapresentative(rule2value, pagina)) {
					this.representativePages.add(pagina);
				}
				this.nonRepresentedPages.add(pagina);
			} else {
				this.representedPages.add(pagina);
			}
	}
	
	private boolean notRapresented(Map<Rule, String> rule2value, Page pagina) {

		boolean notSameValues = false;

		for (List<Rule> rulesSet : this.originalRulesSets) {
			List<String> values = new ArrayList<String>();

			for (Rule regola : rulesSet) {
				values.add(rule2value.get(regola));
			}

			if (!sameValues(values)) {
				notSameValues = true;
				int numPages = this.rulesSets2numPages.get(rulesSet);
				numPages++;
				this.rulesSets2numPages.put(rulesSet, new Integer(numPages));
			}

		}
		
		return notSameValues;
	}
	
	private boolean isRapresentative(Map<Rule, String> rule2value, Page pagina) {
		boolean notSameValues = false;

		List<List<Rule>> newRulesSets = new LinkedList<List<Rule>>();
		for (List<Rule> rules : this.currentRulesSets) {
			List<String> values = new ArrayList<String>();

			for (Rule regola : rules) {
				values.add(rule2value.get(regola));
			}

			if (!sameValues(values)) {
				notSameValues = true;
				newRulesSets.addAll(groupForExtractedValues(values, rules));
				Rule regola = rules.get(0);
				List<Rule> rulesSet = this.rule2originalRulesSet.get(regola);
				List<Page> pageList = this.rulesSets2representativePages.get(rulesSet);
				pageList.add(pagina);
			} else {
				newRulesSets.add(rules);
			}

		}

		this.currentRulesSets = newRulesSets;
		
		return notSameValues;
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
    
    private List<List<Rule>> groupForExtractedValues(List<String> values, List<Rule> rules) {
		Map<String, List<Rule>> val2rules = new HashMap<String, List<Rule>>();
		List<List<Rule>> result = new LinkedList<List<Rule>>();
		int i = 0;
		for (Rule r : rules) {
			String val = values.get(i);
			if (val2rules.containsKey(val)) {
				val2rules.get(val).add(r);
			} else {
				List<Rule> newSet = new LinkedList<Rule>();
				newSet.add(r);
				val2rules.put(val, newSet);
			}
			i++;
		}
		for (List<Rule> ruleGroup : val2rules.values()) {
			result.add(ruleGroup);
		}
		return result;
	}

	
}
