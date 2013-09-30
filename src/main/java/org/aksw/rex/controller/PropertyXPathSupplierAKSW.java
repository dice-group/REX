package org.aksw.rex.controller;

import java.util.ArrayList;

/**
 * Xpaths handcrafted by Muhammad Saleem
 * 
 * @author r.usbeck
 * 
 */
public class PropertyXPathSupplierAKSW implements PropertyXPathSupplier {
	private ArrayList<RexPropertiesWithGoldstandard> propertiesToCheck;

	public PropertyXPathSupplierAKSW() {
		propertiesToCheck = new ArrayList<RexPropertiesWithGoldstandard>();

		// =====================For http://www.imdb.com/title/ sub-domain

		// TODO discuss aksw.org properties
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='overview-top']/h1/span[1]", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='title_recs']/div[2]/div[1]/div[3]/div/div[1]/div[1]/a/b", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='overview-top']/div[4]/a/span", "http://dbpedia.org/ontology/director", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='title_recs']/div[2]/div/div[3]/div/div[2]/div[2]", "http://dbpedia.org/ontology/director", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='overview-top']/div[5]/a/span", "http://dbpedia.org/ontology/writer", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='titleCast']/table/tbody/tr/td/a/span", "http://aksw.org/stars", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='overview-top']/div[3]/div[3]/strong/span", "http://aksw.org/rating", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='overview-top']/div[2]/time", "http://dbpedia.org/ontology/Work/runtime", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='overview-top']/div[2]/span[4]/a", "http://dbpedia.org/property/released", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='overview-top']/div[3]/div[3]/a/span", "http://aksw.org/numberOfUserReviews", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='overview-top']/div[2]/a/span", "http://dbpedia.org/ontology/genre", "http://www.imdb.com/title"));

		// ========================For http://www.imdb.com/name/ sub-domain

		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='overview-top']/h1/span", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='name-born-info']/time/a", "http://dbpedia.org/ontology/birthDate", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='name-born-info']/a", "http://dbpedia.org/ontology/birthYear", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='name-job-categories']/a/span", "http://dbpedia.org/property/occupation", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='name-bio-text']/div/div", "http://dbpedia.org/ontology/abstract", "http://www.imdb.com/name"));

		// =============== All Music Album subdomain

		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='album-title']/text()", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='microdata-rating']/div", "http://aksw.org/rating", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[1]/section[2]/div[2]/span", "http://dbpedia.org/ontology/releaseDate", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[1]/section[2]/div[3]/span", "http://dbpedia.org/ontology/runtime", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[1]/section[2]/div[4]/div/a", "http://dbpedia.org/ontology/genre", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[1]/section[2]/div[5]/div/a", "http://dbpedia.org/ontology/genre", "http://www.allmusic.com/album"));

		// =============== All Music Bands subdomain

		// =====================All music, Artist subdomain ================
		// TODO this field has no match in dbpedia ontology since it is a range
		// TODO genre and styles are hard to match to a property in dbpedia owl
		// TODO aliases has no counterpart in dbpedia
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[2]/header/hgroup/h2", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[2]/section[2]/ul/li/div/div[2]/a", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//html/body/div/div[3]/div[1]/section[1]/div[2]/div", "http://dbpedia.org/ontology/activeYearsStartYear", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[1]/section[1]/div[4]/div", "http://dbpedia.org/ontology/genre", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[1]/section[1]/div[5]/div", "http://dbpedia.org/ontology/genre", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[1]/section[1]/div[3]/div", "http://dbpedia.org/ontology/birthDate", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div/div[3]/div[1]/section[1]/div[6]/div", "http://aksw.org/aliases", "http://www.allmusic.com/artist/"));

		// ======================== Espnn.com subdomain team

		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='header']/div[4]/h1/a", "http://www.w3.org/2000/01/rdf-schema#label", "http://espnfc.com/team"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[2]/div/div[6]/div[2]/table/tbody/tr/td[1]/a", "http://www.w3.org/2000/01/rdf-schema#label", "http://espnfc.com/team"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[2]/div/div[5]/div[2]/div/div/p[2]", "http://dbpedia.org/ontology/abstract", "http://espnfc.com/team"));

		// ======================== Espnn.com subdomain player

		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[3]/div[1]/div[1]/div[2]/div/div/div[2]/h1", "http://www.w3.org/2000/01/rdf-schema#label", "http://espnfc.com/player"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[3]/div[1]/div[1]/div[2]/div/div/div[2]/ul/li[1]", "http://dbpedia.org/ontology/number", "http://espnfc.com/player"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[3]/div[1]/div[1]/div[2]/div/div/div[2]/ul/li[2]", "http://dbpedia.org/ontology/position", "http://espnfc.com/player"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[3]/div[1]/div[1]/div[2]/div/div/div[2]/ul/li[3]", "http://aksw.org/age", "http://espnfc.com/player"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[3]/div[1]/div[1]/div[2]/div/div/div[2]/ul/li[4]", "http://dbpedia.org/ontology/birthDate", "http://espnfc.com/player"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[3]/div[1]/div[1]/div[2]/div/div/div[2]/ul/li[5]", "http://dbpedia.org/ontology/birthPlace", "http://espnfc.com/player"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[3]/div[1]/div[1]/div[2]/div/div/div[2]/ul/li[6]", "http://dbpedia.org/ontology/Person/height", "http://espnfc.com/player"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id='content']/div[3]/div[1]/div[1]/div[2]/div/div/div[2]/ul/li[7]", "http://dbpedia.org/ontology/Person/weight", "http://espnfc.com/player"));

		// ======================= Goodreads.com Author
		// TODO correct
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div[1]/div[2]/div[1]/div[1]/div[3]/div[2]/div[1]/div[1]/div/div[1]/h1/span", "http://www.w3.org/2000/01/rdf-schema#label", "http://goodreads.com/author.."));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("[@id=\"freeText2477967133066826269\"]", "influences", "http://goodreads.com/author.."));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("/html/body/div[1]/div[2]/div[1]/div[1]/div[3]/div[1]/div[9]/div[2]/div/div/div/div/a", "friends", "http://goodreads.com/author.."));
		// ======================= Goodreads.com Book
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id=\"bookTitle\"]", "http://www.w3.org/2000/01/rdf-schema#label", "http://goodreads.com/author.."));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@id=\"bookAuthors\"]/span[2]/a/span", "author", "http://goodreads.com/author.."));

	}

	@Override
	public ArrayList<RexPropertiesWithGoldstandard> getPropertiesToCheck() {
		return propertiesToCheck;
	}

	public void setPropertiesToCheck(ArrayList<RexPropertiesWithGoldstandard> propertiesToCheck) {
		this.propertiesToCheck = propertiesToCheck;
	}

}
