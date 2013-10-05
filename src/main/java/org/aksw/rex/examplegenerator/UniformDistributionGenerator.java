/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.rex.examplegenerator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.aksw.rex.util.Pair;
import org.apache.log4j.Logger;

/**
 *
 * @author ngonga
 */
public class UniformDistributionGenerator extends SimpleExampleGenerator {

    private static final Logger logger = Logger.getLogger(UniformDistributionGenerator.class.getName());
    private Property property;
    private List<Pair<Resource, Resource>> examples = null;;

    private void getAllExamples() {
        examples = new ArrayList<Pair<Resource, Resource>>();
//		String query = "SELECT ?s ?o WHERE {?s <" + property.getURI() + "> ?o. ?s_in ?p1 ?s. ?o_in ?p2 ?o.} "
//				+ "GROUP BY ?s ?o ORDER BY DESC(COUNT(?s_in)+COUNT(?o_in)) LIMIT " + maxNrOfPositiveExamples;
        String query =
                "SELECT ?s ?o WHERE {"
                + "?s <" + property + "> ?o. }"
                + "ORDER BY DESC ( <LONG::IRI_RANK> (?o) + <LONG::IRI_RANK> (?s))";
        ResultSet rs = executeSelectQuery(query);
        QuerySolution qs;
        Resource subject;
        Resource object;
        while (rs.hasNext()) {
            qs = rs.next();
            if (qs.get("s").isURIResource()) {
                subject = qs.getResource("s");
            } else {
                logger.warn("Omitting triple:Subject " + qs.get("s") + " is not a URI resource!");
                continue;
            }
            if (qs.get("o").isURIResource()) {
                object = qs.getResource("o");
            } else {
                logger.warn("Omitting triple:Object " + qs.get("o") + " is not a URI resource!");
                continue;
            }
            examples.add(new Pair<Resource, Resource>(subject, object));
        }
    }

    @Override
    public Set<Pair<Resource, Resource>> getPositiveExamples() {
        // get all examples if we do not have any
        if(examples == null) getAllExamples();
        
        HashSet<Pair<Resource, Resource>> result = new HashSet<Pair<Resource, Resource>>();
        if (examples.size() > maxNrOfPositiveExamples) {
            int k = examples.size() / maxNrOfPositiveExamples;
            for (int i = 0; i < examples.size(); i = i + k) {
                result.add(examples.get(i));
            }
        }
        return result;
    }
}
