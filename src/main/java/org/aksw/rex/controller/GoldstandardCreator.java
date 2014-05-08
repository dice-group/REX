package org.aksw.rex.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.aksw.rex.controller.dao.PropertyXPathSupplier;
import org.aksw.rex.controller.dao.PropertyXPathSupplierAKSW;
import org.aksw.rex.controller.dao.RexPropertiesWithGoldstandard;
import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.uris.URIGeneratorImpl;
import org.aksw.rex.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to write the triples from manual gold standard and automatic AlfREX to
 * a file This class is not used for evaluation purposes.
 * 
 * @author r.usbeck
 * 
 */
public class GoldstandardCreator {
	static Logger log = LoggerFactory.getLogger(GoldstandardCreator.class);

	/**
	 * extended main to extract from each page of the indizes each pair
	 * 
	 * @param args
	 * @throws XPathExpressionException
	 * @throws DOMException
	 * @throws IOException
	 */
	public static void main1(String args[]) throws XPathExpressionException, DOMException, IOException {
		ArrayList<PropertyXPathSupplier> ps = new ArrayList<PropertyXPathSupplier>();
		// ps.add(new PropertyXPathSupplierAlfred());
		ps.add(new PropertyXPathSupplierAKSW());
		ArrayList<CrawlIndex> indizes = new ArrayList<CrawlIndex>();
		indizes.add(new CrawlIndex("goodreads-author-index/"));
		indizes.add(new CrawlIndex("goodreads-book-index/"));
		indizes.add(new CrawlIndex("imdb-name-index/"));
		indizes.add(new CrawlIndex("imdb-title-index/"));
		indizes.add(new CrawlIndex("espnfc-player-index/"));
		indizes.add(new CrawlIndex("espnfc-team-index/"));
		for (CrawlIndex index : indizes) {
			URIGeneratorImpl gen = new URIGeneratorImpl();
			for (PropertyXPathSupplier x : ps) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(index.getName().replace("/", "") + "_" + x.getClass().getCanonicalName() + ".nt"));
				for (int j = 0; j < index.size(); ++j) {
					ArrayList<Pair<String, String>> d = index.getDocument(j);
					String url = d.get(0).getLeft();
					for (RexPropertiesWithGoldstandard p : x.getPropertiesToCheck()) {
						String domain = p.getExtractionDomainURL();
						if (url.startsWith(domain)) {
							String xpath = p.getXpath();
							try {
								String html = d.get(0).getRight();
								XPathFactory factory = XPathFactory.newInstance();
								XPath xpathExpression = factory.newXPath();
								Document doc = Jsoup.parse(html);
								NodeList nodeList = (NodeList) xpathExpression.evaluate(xpath, org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(doc), XPathConstants.NODESET);
								for (int i = 0; i < nodeList.getLength(); ++i) {
									Node item = nodeList.item(i);
									bw.write("<" + url + ">\t<" + p.getPropertyURL() + ">\t" + item + ".\n");
								}
							} catch (Exception e) {
								log.error("ERROR on URL: " + url + " property: " + p.getPropertyURL());
							}
						}
					}
				}
				bw.close();
			}
			index.close();
		}
	}

	/**
	 * Simple main to get the size of the indizes
	 * 
	 * @param args
	 * @throws XPathExpressionException
	 * @throws DOMException
	 * @throws IOException
	 */
	public static void main(String args[]) throws XPathExpressionException, DOMException, IOException {
		ArrayList<CrawlIndex> indizes = new ArrayList<CrawlIndex>();
		indizes.add(new CrawlIndex("goodreads-author-index/"));
		indizes.add(new CrawlIndex("goodreads-book-index/"));
		indizes.add(new CrawlIndex("imdb-name-index/"));
		indizes.add(new CrawlIndex("imdb-title-index/"));
		indizes.add(new CrawlIndex("espnfc-player-index/"));
		indizes.add(new CrawlIndex("espnfc-team-index/"));
		for (CrawlIndex index : indizes) {
			System.out.println(index.getName() + " -> " + index.size());
			index.close();
		}
	}
}
