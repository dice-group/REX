package org.aksw.rex.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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

public class GoldstandardCreator {
	static Logger log = LoggerFactory.getLogger(GoldstandardCreator.class);

	public static void main(String args[]) throws XPathExpressionException, DOMException, IOException {
		ArrayList<PropertyXPathSupplier> ps = new ArrayList<PropertyXPathSupplier>();
		ps.add(new PropertyXPathSupplierAlfred());
		ps.add(new PropertyXPathSupplierAKSW());
		// TODO iterate indizes
		ArrayList<CrawlIndex> indizes = new ArrayList<CrawlIndex>();
		indizes.add(new CrawlIndex("imdbIndex/"));
		indizes.add(new CrawlIndex("espnfcIndex/"));
//		indizes.add(new CrawlIndex("allmusicIndex/"));
		for (CrawlIndex index : indizes) {
			// TODO change URL generation style
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
									// TODO discuss whether the subject of a new
									// triple is the URI of the Extraction Page
									// or a
									// special subject marked as XPath on the
									// site
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
}
