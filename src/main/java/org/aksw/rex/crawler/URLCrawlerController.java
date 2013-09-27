package org.aksw.rex.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

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
		Map<CrawlIndex, Set<String>> index2URLs = new HashMap<CrawlIndex, Set<String>>();
		index2URLs.put(new CrawlIndex("imdb-title-index"), Sets.newHashSet("http://www.imdb.com/title/tt([0-9])*/$"));
		index2URLs.put(new CrawlIndex("imdb-name-index"), Sets.newHashSet("http://www.imdb.com/name/nm([0-9])*/$"));
		CrawlerConfig crawlIndexConfig = new CrawlerConfig("http://www.imdb.com/", index2URLs);
		URLCrawlerController crawlControl = new URLCrawlerController("crawlIMDB", crawlIndexConfig);
//		URLCrawlerController crawlControl = new URLCrawlerController("crawlESPNFC", "espnfcIndex");
		System.out.println("Now adding Seeds.");
		crawlControl.addSeed("http://www.imdb.com/");
//		crawlControl.addSeed("http://www.imdb.com/");
//		crawlControl.addSeed("http://www.allmusic.com/artist/jack-johnson-mn0000120010");
//		crawlControl.addSeed("http://espnfc.com/");
//		crawlControl.addSeed("http://espnfc.com/tables/_/league/uefa.champions/uefa-champions-league?cc=5739");
		System.out.println("Seeds have been added. Crawler will be started.");
		crawlControl.startCrawler();
		System.out.println("Crawler finished.");
	}

	public URLCrawlerController(String crawlStorageFolder, CrawlerConfig crawlIndexConfig) throws Exception {
		numberOfCrawlers = 10;
		int maxDepth = 3;
		int maxOutgoingLinksToFollow = 1000;
		String userAgentName = "googlebot";
		userAgentName = "crawler4j";
		int maxPagesToFetch = 500000;
//		int maxPagesToFetch = 200000;
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxDepthOfCrawling(maxDepth);
		config.setMaxOutgoingLinksToFollow(maxOutgoingLinksToFollow);
		config.setMaxPagesToFetch(maxPagesToFetch);
		config.setIncludeBinaryContentInCrawling(false);
		config.setUserAgentString(userAgentName);
		config.setResumableCrawling(true);
		/*
		 * Instantiate the controller for this crawl.
		 */
		PageFetcher pageFetcher = new PageFetcher(config);
		pageFetcher.getHttpClient().getParams().setParameter("Accept-Language", "en");
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		robotstxtConfig.setUserAgentName(userAgentName);
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		controller = new CrawlController(config, pageFetcher, robotstxtServer);

		this.controller.setCustomData(crawlIndexConfig);
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