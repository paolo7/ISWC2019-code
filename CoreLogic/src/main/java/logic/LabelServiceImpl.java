package logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

public class LabelServiceImpl implements LabelService {

	private Map<String,String> knownLabels;
	
	public LabelServiceImpl() {
		knownLabels = new HashMap<String,String>();
	}
	
	// Initialise the labels with the triples from a set of existing predicates
	public LabelServiceImpl(Set<PredicateInstantiation> existingPredicates, Map<String,String> prefixes) {
		knownLabels = new HashMap<String,String>();
		Model m = RDFUtil.generateBasicModel(existingPredicates, prefixes);
		initialiseFromModel(m);
	}
	
	// Initialise the labels with the triples from an existing model
	public LabelServiceImpl(Model initialModel) {
		knownLabels = new HashMap<String,String>();
		initialiseFromModel(initialModel);
	}
	
	public void initialiseFromModel(Model m) {
		Query query = QueryFactory.create("SELECT ?uri ?label WHERE {?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label }") ;
		QueryExecution qe = QueryExecutionFactory.create(query, m);
	    ResultSet rs = qe.execSelect();
	    while (rs.hasNext())
		{
	    	QuerySolution binding = rs.nextSolution();
	    	knownLabels.put(binding.get("uri").asResource().getURI(),binding.get("label").asLiteral().getLexicalForm());
		}
	}
	
	@Override
	public String getLabel(String URI) {
		if(knownLabels.containsKey(URI)) return knownLabels.get(URI);
		return null;
	}

	@Override
	public void setLabel(String URI, String label) {
		knownLabels.put(URI, label);
	}

	@Override
	public boolean hasLabel(String URI) {
		return knownLabels.containsKey(URI);
	}

}
