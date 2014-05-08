package org.aksw.rex.crawler;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * Used to configure the WebCrawler. Very unpolite crawler. We got blocked on
 * several domains.
 * 
 * @author r.usbeck
 * */
public class URLCrawlerController {
	private int numberOfCrawlers;
	private CrawlController controller;
	private static Logger log = LoggerFactory.getLogger(URLCrawlerController.class);
	private CrawlerConfig crawlIndexConfig;

	public static void main(String[] args) throws Exception {
		// Map<CrawlIndex, Set<String>> index2URLs = new HashMap<CrawlIndex,
		// Set<String>>();
		// index2URLs.put(new CrawlIndex("goodreads-book-index"),
		// Sets.newHashSet("http://www.goodreads.com/book/show/([0-9])*([a-zA-Z_\\.])*"));
		// index2URLs.put(new CrawlIndex("goodreads-author-index"),
		// Sets.newHashSet("http://www.goodreads.com/author/show/([0-9])*([a-zA-Z_\\.])*"));
		// CrawlerConfig crawlIndexConfig = new
		// CrawlerConfig("http://www.goodreads.com/", index2URLs);
		// URLCrawlerController crawlControl = new
		// URLCrawlerController("crawlGoodRead", crawlIndexConfig);
		// try {
		// crawlControl.addSeed("http://www.goodreads.com/");
		//
		// crawlControl.startCrawler();
		//
		// // Wait for 30 seconds
		// Thread.sleep(30 * 1000);
		//
		// crawlControl.shutdown();
		// crawlControl.waitUntilFinish();
		// } catch (Exception e) {
		// log.error("goodreads warf einen Fehler");
		// }
		Map<CrawlIndex, Set<String>> index2URLs = new HashMap<CrawlIndex, Set<String>>();
		index2URLs.put(new CrawlIndex("imdb-title-index"), Sets.newHashSet("http://www.imdb.com/title/tt([0-9])*/$"));
		index2URLs.put(new CrawlIndex("imdb-name-index"), Sets.newHashSet("http://www.imdb.com/name/nm([0-9])*/$"));
		CrawlerConfig crawlIndexConfig = new CrawlerConfig("http://www.imdb.com/", index2URLs);
		URLCrawlerController crawlControl = new URLCrawlerController("crawlIMDB", crawlIndexConfig);
		Random r = new Random();
		// TODO correct number format
		for (int i = 0; i < 50000; i++) {
			int x = r.nextInt(9999999);
			DecimalFormat df = new DecimalFormat("0000000");
			df.format(x);
			crawlControl.addSeed("http://www.imdb.com/title/tt" + x);
		}
		for (int i = 0; i < 50000; i++) {
			int x = r.nextInt(9999999);
			DecimalFormat df = new DecimalFormat("0000000");
			df.format(x);
			crawlControl.addSeed("http://www.imdb.com/name/nm" + x);
		}
		crawlControl.startCrawler();

		// Wait for 30 seconds
		Thread.sleep(30 * 1000);

		crawlControl.shutdown();
		crawlControl.waitUntilFinish();
		// try {
		// index2URLs = new HashMap<CrawlIndex, Set<String>>();
		// index2URLs.put(new CrawlIndex("espnfc-player-index"),
		// Sets.newHashSet("^http://espnfc.com/player(.)*"));
		// index2URLs.put(new CrawlIndex("espnfc-team-index"),
		// Sets.newHashSet("^http://espnfc.com/team(.)*"));
		// crawlIndexConfig = new CrawlerConfig("http://espnfc.com/",
		// index2URLs);
		// crawlControl = new URLCrawlerController("crawlESPNFC",
		// crawlIndexConfig);
		// crawlControl.addSeed("http://espnfc.com/");
		//
		// crawlControl.startCrawler();
		//
		// // Wait for 30 seconds
		// Thread.sleep(30 * 1000);
		// // Send the shutdown request and then wait for finishing
		// crawlControl.shutdown();
		// crawlControl.waitUntilFinish();
		// } catch (Exception e) {
		// log.error("ESPNF warf einen Fehler");
		// }
		// try {
		// index2URLs = new HashMap<CrawlIndex, Set<String>>();
		// index2URLs.put(new CrawlIndex("meps-index"),
		// Sets.newHashSet("http://www.europarl.europa.eu/meps/en/([0-9])*/([a-zA-Z_\\+])*(home.html)$"));
		// crawlIndexConfig = new
		// CrawlerConfig("http://www.europarl.europa.eu/meps/en", index2URLs);
		// crawlControl = new URLCrawlerController("crawlMEP",
		// crawlIndexConfig);
		// crawlControl.addSeed("http://www.europarl.europa.eu/meps/en/full-list.html");
		//
		// crawlControl.startCrawler();
		//
		// // Wait for 30 seconds
		// Thread.sleep(30 * 1000);
		//
		// // Send the shutdown request and then wait for finishing
		// crawlControl.shutdown();
		// crawlControl.waitUntilFinish();
		//
		// } catch (Exception e) {
		// log.error("MEP warf einen Fehler");
		// }
		// log.info("CRAWL finished");
	}

	private void waitUntilFinish() {

	}

	private void shutdown() {
		for (CrawlIndex i : crawlIndexConfig.getIndex2URLs().keySet()) {
			i.close();
		}

	}

	/**
	 * Initialized the Webcrawler with certain properties from the main-method
	 * 
	 * @param crawlStorageFolder
	 * @param crawlIndexConfig
	 * @throws Exception
	 */
	public URLCrawlerController(String crawlStorageFolder, CrawlerConfig crawlIndexConfig) throws Exception {
		this.crawlIndexConfig = crawlIndexConfig;
		numberOfCrawlers = 10;
		int maxDepth = 0;
		int maxOutgoingLinksToFollow = 0;
		String userAgentName = "googlebot";
		userAgentName = "crawler4j";
		int maxPagesToFetch = 100000;
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxDepthOfCrawling(maxDepth);
		config.setMaxOutgoingLinksToFollow(maxOutgoingLinksToFollow);
		config.setMaxPagesToFetch(maxPagesToFetch);
		config.setIncludeBinaryContentInCrawling(false);
		config.setUserAgentString(userAgentName);
		config.setFollowRedirects(true);
		// config.setPolitenessDelay(5000);
		// config.setResumableCrawling(true);
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

	/**
	 * starts the crawling until the number of crawled pages is reached
	 */
	public void startCrawler() {

		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */
		log.debug("Crawler started");
		controller.start(URLCrawler.class, numberOfCrawlers);
		log.debug("Crawler stopped. Write Index.");
		log.debug("Index written");
	}

	/**
	 * used to add seed. Seeds could be generated by SeedFetcher.
	 * 
	 * @param url
	 */
	public void addSeed(String url) {
		/*
		 * For each crawl, you need to add some seed urls. These are the first
		 * URLs that are fetched and then the crawler starts following links
		 * which are found in these pages
		 */
		controller.addSeed(url);
	}
}