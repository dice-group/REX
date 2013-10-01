package org.aksw.rex.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class URLCrawler extends WebCrawler {

	private static Logger log = LoggerFactory.getLogger(URLCrawler.class);
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		System.out.println("Pages in Queue: " + super.getMyController().getFrontier().getQueueLength());
		if (page.getParseData() instanceof HtmlParseData) {
			byte[] contentData = page.getContentData();
			Charset charset = Charset.forName("UTF-8");
			byte[] decoded = new String(contentData, charset).getBytes();
			page.setContentData(decoded);
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			try {
				String url = new String(URLCodec.decodeUrl(page.getWebURL().getURL().getBytes()));
				String html = htmlParseData.getHtml();
				writeToIndex(url, html);
			} catch (DecoderException e) {
				log.warn("error while decoding {}", e);
			}
		}
	}

	public void writeToIndex(String url, String content) {
		
		CrawlerConfig config = (CrawlerConfig) getMyController().getCustomData();
		Map<CrawlIndex, Set<String>> index2URLs = config.getIndex2URLs();
		
		for (Entry<CrawlIndex, Set<String>> entry : index2URLs.entrySet()) {
			CrawlIndex index = entry.getKey();
			for (String urlPattern : entry.getValue()) {
				if(url.matches(urlPattern)){
					index.addDocumentToIndex(url, content);
					log.debug("\tAdded document: " + url);
				}
			}

		}
	}

	public void writeFile(String filename, String content) {
		try {
			File file = new File("crawl/" + filename + ".html");
			Files.createParentDirs(file);
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.write(content);
			out.close();
			log.debug("Wrote file: {}", file.getPath());
		} catch (IOException e) {
			log.error("{}", e);
		}
	}

	/**
	 * You should implement this function to specify whether the given URL
	 * should be crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
		return true;
//		String href = url.getURL().toLowerCase();
//		String allowedURL = ((CrawlerConfig) getMyController().getCustomData()).getTopLevelDomain();
//		return !FILTERS.matcher(href).matches() && href.startsWith(allowedURL);
	}
}
