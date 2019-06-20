package logic;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;

public interface ExternalDB {

	public void loadRDF(File file, RDFFormat dataFormat) throws RDFParseException, RepositoryException, IOException;

	public void loadRDF(Reader reader, RDFFormat dataFormat) throws RDFParseException, RepositoryException, IOException;
	
	public void clearDB();
	
	public TupleQueryResult query(String queryString);

	public void setNamespace(String prefix, String name);

	public Map<String, String> getNamespaces();
	
	public int countTriples();
	
	public void insertFullyInstantiatedPredicate(PredicateInstantiation pi, String baseBlank);


}
