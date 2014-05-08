/**
 * 
 */
package org.aksw.rex.domainidentifier;

import java.net.URL;
import java.util.Set;

import org.aksw.rex.util.Pair;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * this class returns just a domain URL string which is predefined in the
 * constructor
 * 
 * @author Lorenz Buehmann
 * 
 */
public class ManualDomainIdentifier implements DomainIdentifier {

	private URL domain;

	/**
	 * a given input URI will be returned as output URI
	 * 
	 * @param domain
	 */
	public ManualDomainIdentifier(URL domain) {
		this.domain = domain;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.rex.domainidentifier.DomainIdentifier#getDomain(com.hp.hpl.jena
	 * .rdf.model.Property, java.util.Set, java.util.Set, boolean)
	 */
	@Override
	public URL getDomain(Property p, Set<Pair<Resource, Resource>> posExamples, Set<Pair<Resource, Resource>> negExamples, boolean useCache) {
		return domain;
	}

}
