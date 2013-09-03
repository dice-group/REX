package org.aksw.rex.crawl;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.util.Pair;
import org.junit.Test;

public class CrawlerTest {

	@Test
	public void testSearchIndex() throws Exception {
		CrawlIndex index = new CrawlIndex("htmlindex/");
		ArrayList<Pair<String, String>> data = index.searchHTML("Cruise");
		System.out.println("#URL with Tom Cruise: "+data.size());
		for (Pair<String, String> row : data) {
			String url = row.getLeft();
			String html = row.getRight();
			System.out.println("URL with Tom Cruise: " + url);
		}
		assertTrue("Found Tom Cruise in the index.", data != null);
		assertTrue("Found several entries in index.", data.size() > 0);
	}

}
