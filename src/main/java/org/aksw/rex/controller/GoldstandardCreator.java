package org.aksw.rex.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.aksw.rex.crawler.CrawlIndex;
import org.aksw.rex.results.ExtractionResultImpl;
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
		PropertyXPathSupplierAlfred ps = new PropertyXPathSupplierAlfred();
		CrawlIndex index = new CrawlIndex("htmlIndex/");
		URIGeneratorImpl gen = new URIGeneratorImpl();
		// for (int j = 0; j < index.size(); ++j) {
		for (int j = 0; j < 20; ++j) {
			ArrayList<Pair<String, String>> d = index.getDocument(j);
			String url = d.get(0).getLeft();
			log.debug(url);
			for (RexPropertiesWithGoldstandard p : ps.getPropertiesToCheck()) {
				String domain = p.getExtractionDomainURL();
				if (url.startsWith(domain)) {
					String xpath = p.getXpath();
					try {
						log.debug("\tProperty: " + p.getPropertyURL());
						String html = d.get(0).getRight();
						// check the result when applying the generalized XPath
						// on the example URLs
						XPathFactory factory = XPathFactory.newInstance();
						XPath xpathExpression = factory.newXPath();
						Document doc = Jsoup.parse(html);
						NodeList nodeList = (NodeList) xpathExpression.evaluate(xpath, org.aksw.rex.crawler.DOMBuilder.jsoup2DOM(doc), XPathConstants.NODESET);
						for (int i = 0; i < nodeList.getLength(); ++i) {
							Node item = nodeList.item(i);
//							ExtractionResultImpl ex = new ExtractionResultImpl(s, o);
							//TODO discuss whether the subject of a new triple is the URI of the Extraction Page or a special subject marked as XPath on the site
							log.debug("\t\t" + item.getTextContent());
						}
					} catch (Exception e) {
						log.error("ERROR on URL: " + url + " property: " + p.getPropertyURL());
					}
				}
			}
		}
		index.close();
	}
}
