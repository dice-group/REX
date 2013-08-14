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

    public URLCrawlerController(String folder) throws Exception
    {
        String crawlStorageFolder = folder;
        numberOfCrawlers = 7;
        int maxDepth = 0;
        int maxOutgoingLinksToFollow = 0;
        // int maxPagesToFetch = 1;
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(maxDepth);
        config.setMaxOutgoingLinksToFollow(maxOutgoingLinksToFollow);
        // config.setMaxPagesToFetch(maxPagesToFetch);
        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        controller = new CrawlController(config, pageFetcher, robotstxtServer);

    }

    public void startCrawler() {

        /*
         * Start the crawl. This is a blocking operation, meaning that your code will reach the line after this only
         * when crawling is finished.
         */
        log.debug("Crawler started");
        controller.start(URLCrawler.class, numberOfCrawlers);
        log.debug("Crawler stopped");
    }

    public void addSeed(String url) {
        /*
         * For each crawl, you need to add some seed urls. These are the first URLs that are fetched and then the
         * crawler starts following links which are found in these pages
         */
        controller.addSeed(url);
    }
}