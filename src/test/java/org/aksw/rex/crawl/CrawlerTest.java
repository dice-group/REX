package org.aksw.rex.crawl;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.util.Pair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class CrawlerTest {
	private org.slf4j.Logger log = LoggerFactory.getLogger(CrawlerTest.class);
	private static CrawlIndex index;

	@BeforeClass
	public static void init() {
		index = new CrawlIndex("htmlindex/");
	}

	@AfterClass
	public static void finish() {
		index.close();
	}

	@Test
	public void testSearchIndex() throws Exception {

		ArrayList<Pair<String, String>> data = index.searchHTML("Cruise");
		log.debug("#URL with Tom Cruise: " + data.size());
		for (Pair<String, String> row : data) {
			String url = row.getLeft();
			String html = row.getRight();
     		log.debug("URL with Tom Cruise: " + url);
		}
		assertTrue("Found Tom Cruise in the index.", data != null);
		assertTrue("Found several entries in index.", data.size() > 0);
	}

}
