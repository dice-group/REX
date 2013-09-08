package org.aksw.rex.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class URLCrawlerController {

	private int numberOfCrawlers;
	private CrawlController controller;
	private Logger log = LoggerFactory.getLogger(URLCrawlerController.class);
	private CrawlIndex index;

	public static void main(String[] args) throws Exception {
		URLCrawlerController crawlControl = new URLCrawlerController("crawl");
		System.out.println("Now adding Seeds.");
		crawlControl.addSeed("http://www.imdb.com/");
		System.out.println("Seeds have been added. Crawler will be started.");
		crawlControl.startCrawler();
		System.out.println("Crawler finished.");
	}

	public URLCrawlerController(String crawlStorageFolder) throws Exception {
		numberOfCrawlers = 10;
		int maxDepth = 3;
		int maxOutgoingLinksToFollow = 1000;
		int maxPagesToFetch = 10000;
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxDepthOfCrawling(maxDepth);
		config.setMaxOutgoingLinksToFollow(maxOutgoingLinksToFollow);
		config.setMaxPagesToFetch(maxPagesToFetch);
		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		controller = new CrawlController(config, pageFetcher, robotstxtServer);

		String idxDirectory = "htmlindex/";
		index = new CrawlIndex(  idxDirectory);
		this.controller.setCustomData(index);
	}

	public void startCrawler() {

		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		log.debug("Crawler started");
		controller.start(URLCrawler.class, numberOfCrawlers);
		log.debug("Crawler stopped. Write Index.");
		index.close();
		log.debug("Index written");
	}

	public void addSeed(String url) {
		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		controller.addSeed(url);
	}
}