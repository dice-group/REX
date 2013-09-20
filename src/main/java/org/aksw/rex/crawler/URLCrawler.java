package org.aksw.rex.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
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
		log.debug("Pages in Queue: " + super.getMyController().getFrontier().getQueueLength());
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
		CrawlIndex index = (CrawlIndex) super.getMyController().getCustomData();
		index.addDocumentToIndex(url, content);
		log.debug("\tAdded document: " + url);
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
	 * You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(WebURL url) {
		String href = url.getURL().toLowerCase();
//		if (href.startsWith("http://www.imdb.com/title"))
//			return true;
//		if (href.startsWith("http://www.imdb.com/name"))
//			return true;
		if (href.equals("http://espnfc.com/"))
			return true;
		if (href.startsWith("http://espnfc.com/team/"))
			return true;
		if (href.startsWith("http://espnfc.com/player/"))
			return true;
//		if (href.startsWith("http://www.allmusic.com/artist/"))
//			return true;
//		if (href.startsWith("http://www.allmusic.com/album"))
//			return true;
//		return !FILTERS.matcher(href).matches();
		return false;
	}
}
