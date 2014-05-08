package org.aksw.rex.controller.dao;

import java.util.ArrayList;

/**
 * Xpaths extracted by Disheng via Alfred
 * 
 * @author r.usbeck
 * 
 */
public class PropertyXPathSupplierAlfred implements PropertyXPathSupplier {
	private ArrayList<RexPropertiesWithGoldstandard> propertiesToCheck;
	/**
	 * constructor that statically fills the DAO with handcrafted XPath rules
	 */
	public PropertyXPathSupplierAlfred() {
		propertiesToCheck = new ArrayList<RexPropertiesWithGoldstandard>();

		// ========================For http://www.imdb.com/title/ sub-domain

		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='name']/text()", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='ratingValue']/text()", "http://aksw.org/rating", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='ratingCount']/text()", "http://aksw.org/numberOfUserRating", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='reviewCount']/text()", "http://aksw.org/numberOfUserReviews", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='director'][1]/text()[1]", "http://dbpedia.org/ontology/director", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[contains(text(),'Country:')]/../A[1]/text()[1]", "http://dbpedia.org/ontology/country", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='inLanguage'][1]/text()[1]", "http://dbpedia.org/property/language", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard(" //*[@itemprop='datePublished'][1]/text()[1]", "http://dbpedia.org/property/released", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='duration'][1]/text()[1]", "http://dbpedia.org/ontology/Work/runtime", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[contains(text(),'Writer:') or contains(text(),'Writers:')]/../A[1]/text()[1]", "http://dbpedia.org/ontology/writer", "http://www.imdb.com/title"));

		// ========================For http://www.imdb.com/name/ sub-domain

		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='name']/text()#NO", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='birthDate']/A[1]/text()", "http://dbpedia.org/ontology/birthDate", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='birthDate']/A[2]/text()", "http://dbpedia.org/ontology/birthYear", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='jobTitle'][1]/text()", "http://dbpedia.org/property/occupation", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='article highlighted']/B/text()", "http://aksw.org/awards", "http://www.imdb.com/name"));

		// =============== All Music Album subdomain

		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='album-title']/text()", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='album-artist']/A/text()", "http://dbpedia.org/ontology/artist", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='rating']/text()", "http://aksw.org/rating", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='release-date']/text()", "http://dbpedia.org/ontology/releaseDate", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='duration']/text()", "http://dbpedia.org/ontology/runtime", "http://www.allmusic.com/album"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='genres']/UL/LI[1]/A/text()", "http://dbpedia.org/ontology/genre", "http://www.allmusic.com/album"));

		// =============== All Music Bands subdomain
		// TODO this field has no match in dbpedia ontology since it is a range
		// TODO genre and styles are hard to match to a property in dbpedia owl
		// TODO aliases has no counterpart in dbpedia
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DIV[@class='artist-name'][1]/text()[1]", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='active'][1]/text()[1]", "http://dbpedia.org/ontology/activeYearsStartYear", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='genres'][1]/UL[1]/LI[1]/A[1]/text()[1]", "http://dbpedia.org/ontology/genre", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='styles'][1]/UL[1]/LI[1]/A[1]/text()[1]", "http://dbpedia.org/ontology/genre", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='birth'][1]/SPAN[1]/text()[1]", "http://dbpedia.org/ontology/birthDate", "http://www.allmusic.com/artist/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='aliases'][1]/UL/LI[1]/text()[1]", "http://aksw.org/aliases", "http://www.allmusic.com/artist/"));

		// TODO ESPN

	}

	@Override
	public ArrayList<RexPropertiesWithGoldstandard> getPropertiesToCheck() {
		return propertiesToCheck;
	}

	public void setPropertiesToCheck(ArrayList<RexPropertiesWithGoldstandard> propertiesToCheck) {
		this.propertiesToCheck = propertiesToCheck;
	}
}
