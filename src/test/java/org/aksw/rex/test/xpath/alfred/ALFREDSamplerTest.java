package org.aksw.rex.test.xpath.alfred;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

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
		
		this.finder = new ALFREDSampler(this.rulesSet);		
		this.finder.addPage(this.onePage);
		this.finder.addPages(this.pages);
	}
	
	@Test
	public void testgetRule2nullRate() {
		// TODO to add getRule2NullRate
//		Map<Rule, Double> r2nR = this.finder.getRule2NullRate();
//		assertEquals(4, r2nR.keySet().size());
//		Rule containRule = null;
//		for (Rule regola : this.rulesSet) {
//			if (regola.encode().equals("//*[contains(.,'A')]/text()")) containRule = regola;
//		}
//		assertEquals(0.75, (double)r2nR.get(containRule), 0.1);
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
