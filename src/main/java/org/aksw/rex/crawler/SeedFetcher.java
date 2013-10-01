package org.aksw.rex.crawler;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeedFetcher {
	public static Logger log = LoggerFactory.getLogger(SeedFetcher.class);

	public static void main(String args[]) throws UnsupportedEncodingException, IOException {
		String charset = "UTF-8";
		Random r = new Random();
//		CrawlIndex indexTitle = new CrawlIndex("imdb-title-index");
//		while(indexTitle.size()<10000) {
//			try {
//				int x = r.nextInt(3219980);
//				DecimalFormat df = new DecimalFormat("0000000");
//				URL u = new URL("http://www.imdb.com/title/tt" + df.format(x));
//				StringWriter writer = new StringWriter();
//				IOUtils.copy(u.openStream(), writer, charset);
//				indexTitle.addDocumentToIndex(u.toString(), writer.toString());
//				log.debug(u.toExternalForm());
//			} catch (Exception e) {
//				log.error("Did not get it.");
//			}
//		}
//		indexTitle.close();
//		CrawlIndex indexName = new CrawlIndex("imdb-name-index");
//		while(indexName.size()<10000) {
//			try {
//				int x = r.nextInt(5749999);
//				DecimalFormat df = new DecimalFormat("0000000");
//				URL u = new URL("http://www.imdb.com/name/nm" + df.format(x));
//				StringWriter writer = new StringWriter();
//				IOUtils.copy(u.openStream(), writer, charset);
//				indexName.addDocumentToIndex(u.toString(), writer.toString());
//				log.debug(u.toExternalForm());
//			} catch (Exception e) {
//				log.error("Did not get it.");
//			}
//		}
//		indexName.close();
//		CrawlIndex indexBook = new CrawlIndex("goodreads-book-index");
//		while(indexBook.size()<10000) {
//			try {
//				int x = r.nextInt(5749999);
//				DecimalFormat df = new DecimalFormat("0000000");
//				URL u = new URL("http://www.goodreads.com/book/show/" + df.format(x));
//				StringWriter writer = new StringWriter();
//				IOUtils.copy(u.openStream(), writer, charset);
//				indexBook.addDocumentToIndex(u.toString(), writer.toString());
//				log.debug(u.toExternalForm());
//			} catch (Exception e) {
//				log.error("Did not get it.");
//			}
//		}
//		indexBook.close();
//		CrawlIndex indexAuthor = new CrawlIndex("goodreads-author-index");
//		while(indexAuthor.size()<10000) {
//			try {
//				int x = r.nextInt(5749999);
//				DecimalFormat df = new DecimalFormat("0000000");
//				URL u = new URL("http://www.goodreads.com/author/show/" + df.format(x));
//				StringWriter writer = new StringWriter();
//				IOUtils.copy(u.openStream(), writer, charset);
//				indexAuthor.addDocumentToIndex(u.toString(), writer.toString());
//				log.debug(u.toExternalForm());
//			} catch (Exception e) {
//				log.error("Did not get it.");
//			}
//		}
//		indexAuthor.close();
		CrawlIndex indexPlayer = new CrawlIndex("espnfc-player-index");
		while(indexPlayer.size()<10000) {
			try {
				int x = r.nextInt(200000);
				URL u = new URL("http://espnfc.com/player/_/id/" + x);
				StringWriter writer = new StringWriter();
				IOUtils.copy(u.openStream(), writer, charset);
				indexPlayer.addDocumentToIndex(u.toString(), writer.toString());
				log.debug(u.toExternalForm());
			} catch (Exception e) {
				log.error("Did not get it.");
			}
		}
		indexPlayer.close();
		CrawlIndex indexTeam = new CrawlIndex("espnfc-team-index");
		while(indexTeam.size()<1000) {
			try {
				int x = r.nextInt(100000);
				URL u = new URL("http://espnfc.com/team/_/id/" + x);
				StringWriter writer = new StringWriter();
				IOUtils.copy(u.openStream(), writer, charset);
				indexTeam.addDocumentToIndex(u.toString(), writer.toString());
				log.debug(u.toExternalForm());
			} catch (Exception e) {
				log.error("Did not get it.");
			}
		}
		indexTeam.close();
	}
}
