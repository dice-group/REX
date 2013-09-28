package org.aksw.rex.test.xpath.alfred;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Page;
import model.Rule;

import org.aksw.rex.xpath.alfred.ALFREDSampler;
import org.junit.Before;
import org.junit.Test;

import rules.xpath.XPathRule;

public class ALFREDSamplerTest {
	
	private ALFREDSampler finder;
	private List<Page> pages;
	private Page onePage;
	private List<Rule> rulesSet;
	
	@Before
	public void setUp() throws Exception {
			
		this.rulesSet = new LinkedList<Rule>();
		this.rulesSet.add(new XPathRule("/HTML/BODY/text()"));
		this.rulesSet.add(new XPathRule("//BODY/text()"));
		this.rulesSet.add(new XPathRule("//text()"));
		this.rulesSet.add(new XPathRule("//*[contains(.,'A')]/text()"));
		
		this.pages = new LinkedList<Page>();
		this.pages.add(new Page("<html><body>A</body></html>", "A"));
		this.pages.add(new Page("<html><body>B</body></html>", "B"));
		this.pages.add(new Page("<html><body>C</body></html>", "C"));
		this.onePage = new Page("<html><body>D</body></html>", "D");
		
		List<List<Rule>> rulesSets = new LinkedList<List<Rule>>();
		rulesSets.add(this.rulesSet);
		this.finder = new ALFREDSampler(rulesSets);		
		this.finder.addPage(this.onePage);
		this.finder.find(this.pages);
	}
	
	@Test
	public void testGetCurrentRulesSets() {
		List<List<Rule>> curRS = this.finder.getCurrentRulesSets();
		assertEquals(2, curRS.size());
	}

	@Test
	public void testGetRulesSets2numPages() {
		Map<List<Rule>, Integer> rS2nP = this.finder.getRulesSets2NumberNotRepresentedPages();
		int numPag = rS2nP.get(this.rulesSet);
		assertEquals(3, numPag);		
	}
	
	@Test
	public void testgetOriginalRS2currentRS() {
		Map<List<Rule>, Set<List<Rule>>> oRS2cRS = this.finder.getOriginalRS2currentRS();
		assertEquals(1, oRS2cRS.keySet().size());
		assertEquals(2, oRS2cRS.get(this.rulesSet).size());
	}
	
	@Test
	public void testgetRulesSets2representativePages() {
		Map<List<Rule>, List<Page>> rS2rP = this.finder.getRulesSets2RepresentativePages();
		assertEquals(1, rS2rP.keySet().size());
		assertEquals(1, rS2rP.get(this.rulesSet).size());
		assertEquals(this.onePage, rS2rP.get(this.rulesSet).get(0));
	}
	
	@Test
	public void testgetRule2nullRate() {
		Map<Rule, Double> r2nR = this.finder.getRule2NullRate();
		assertEquals(4, r2nR.keySet().size());
		Rule containRule = null;
		for (Rule regola : this.rulesSet) {
			if (regola.encode().equals("//*[contains(.,'A')]/text()")) containRule = regola;
		}
		assertEquals(0.75, (double)r2nR.get(containRule), 0.1);
	}

	@Test
	public void testGetRepresentativePages() {
		List<Page> rapresentativePages = this.finder.getRepresentativePages();
		assertEquals(1, rapresentativePages.size());
	}

	@Test
	public void testGetNonRepresentedPages() {
		List<Page> nonRepresentedPages = this.finder.getNonRepresentedPages();
		assertEquals(3, nonRepresentedPages.size());
	}

	@Test
	public void testGetRepresentedPages() {
		List<Page> representedPages = this.finder.getRepresentedPages();
		assertEquals(1, representedPages.size());
	}

}
