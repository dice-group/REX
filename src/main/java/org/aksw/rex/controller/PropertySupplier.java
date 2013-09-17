package org.aksw.rex.controller;

import java.util.ArrayList;

public class PropertySupplier {
	private ArrayList<RexPropertiesWithGoldstandard> propertiesToCheck;

	public PropertySupplier() {
		propertiesToCheck = new ArrayList<RexPropertiesWithGoldstandard>();
		// TODO extend to correct DBpedia Properties
		// IMDB movies
		// TODO first property is the same as ACtor Name
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='name']/text()", "Title", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='ratingValue']/text()", "Rating", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='ratingCount']/text()", "number of users Rating", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='reviewCount']/text()", "number of Reviews", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='director'][1]/text()[1]", "Director", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[contains(text(),'Country:')]/../A[1]/text()[1]", "Country", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='inLanguage'][1]/text()[1]", "Language", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='datePublished'][1]/text()[1]", "Release date", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='duration'][1]/text()[1]", "Runtime", "http://www.imdb.com/title"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[contains(text(),'Writer:') or contains(text(),'Writers:')]/../A[1]/text()[1]", "Writer", "http://www.imdb.com/title"));

		// IMDB actors:
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='name']/text()#NO", "Name", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='birthDate']/A[1]/text()", "Birth date", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='birthDate']/A[2]/text()", "Birth date year", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='jobTitle'][1]/text()", "Job", "http://www.imdb.com/name"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='article highlighted']/B/text()", "Prizes", "http://www.imdb.com/name"));

		// Allmusic album:
		// TODO expand to correct domain URL
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='album-artist']/A/text()", "Artist", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='album-title']/text()", "Title", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@itemprop='rating']/text()", "Rating", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='release-date']/text()", "Release date", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='duration']/text()", "Duration", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='genres']/UL/LI[1]/A/text()", "Genres", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//*[@class='styles']/UL/LI[1]/A/text()", "Styles", "http://www.allmusic.com/"));

		// Allmusic bands:
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='active'][1]/text()[1]", "active", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DIV[@class='artist-name'][1]/text()[1]", "name", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='genres'][1]/UL[1]/LI[1]/A[1]/text()[1]", "genres", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='styles'][1]/UL[1]/LI[1]/A[1]/text()[1]", "styles", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='birth'][1]/SPAN[1]/text()[1]", "birth", "http://www.allmusic.com/"));
		propertiesToCheck.add(new RexPropertiesWithGoldstandard("//DD[@class='aliases'][1]/UL/LI[1]/text()[1]", "aliases", "http://www.allmusic.com/"));

		/* TODO here ESPN sports is missing:
		 http://espn.go.com/mlb/team/roster/_/name/bal/baltimore-orioles*/
	}

	public ArrayList<RexPropertiesWithGoldstandard> getPropertiesToCheck() {
		return propertiesToCheck;
	}

	public void setPropertiesToCheck(ArrayList<RexPropertiesWithGoldstandard> propertiesToCheck) {
		this.propertiesToCheck = propertiesToCheck;
	}
}
