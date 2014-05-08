package org.aksw.rex.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * DAO to keep the configuration of the crawler 
 * This DAO also maps the index to the topLevelDomains
 * @author Lorenz Buehmann
 *
 */
public class CrawlerConfig {
	
	Map<CrawlIndex, Set<String>> index2URLs = new HashMap<CrawlIndex, Set<String>>();
	private String topLevelDomain;
	
	
	public CrawlerConfig(String topLevelDomain, Map<CrawlIndex, Set<String>> index2URLs) {
		this.topLevelDomain = topLevelDomain;
		this.index2URLs = index2URLs;
	}
	
	/**
	 * @return the index2URLs
	 */
	public Map<CrawlIndex, Set<String>> getIndex2URLs() {
		return index2URLs;
	}
	
	public Set<String> getAllowedURLs(CrawlIndex index){
		return index2URLs.get(index);
	}
	
	/**
	 * @return the topLevelDomain
	 */
	public String getTopLevelDomain() {
		return topLevelDomain;
	}
	

}
