/**
 * 
 */
package org.aksw.rex.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author Lorenz Buehmann
 * 
 */
public class HTMLExtractor {

	/**
	 * Return the text content of a HTML document without tags etc.
	 * @param html
	 * @return
	 */
	public static String getHTMLContent(String html) {
		Document document = Jsoup.parse(html);
		
		StringBuilder sb = new StringBuilder();
		//add title
		sb.append(document.title());
		//add body
		org.jsoup.nodes.Element body = document.body();
		sb.append(body.text());

		return sb.toString();
	}


	public static void main(String[] args) throws Exception {
		System.out.println(getHTMLContent("<html><body><div>test</div><div><a>url</a></div><p>man</p></body></html>"));
	}
}
