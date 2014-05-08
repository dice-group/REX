package org.aksw.rex.examplegenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.rex.util.Pair;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *Samples evenly across the rank distribution
 * @author ngonga
 */
public class UniformDistributionGenerator extends SimpleExampleGenerator {

    private static final Logger logger = Logger.getLogger(UniformDistributionGenerator.class.getName());
    private Property property;
    private List<Pair<Resource, Resource>> examples = null;;

    @Override
    /** 
     * Samples evenly across the rank distribution
     */
    public Set<Pair<Resource, Resource>> getPositiveExamples() {
        // get all examples if we do not have any
        if(examples == null) examples = getListOfPositiveExamples();
        
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
