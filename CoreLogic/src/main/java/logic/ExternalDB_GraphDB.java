package logic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.http.config.HTTPRepositoryConfig;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

import GraphDB.EmbeddedGraphDB;
import GraphDB.QueryUtil;
import GraphDB.UpdateUtil;


public class ExternalDB_GraphDB implements ExternalDB{

	/*public static void setLoggingLevelToError() {
	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
	    root.setLevel(ch.qos.logback.classic.Level.ERROR);
	}*/
	
	RepositoryConnection connection;
	
	RemoteRepositoryManager remoteManager = null;
	String remoteTempId = null;
	
	public ExternalDB_GraphDB() throws Exception {
		connection = EmbeddedGraphDB.openConnectionToTemporaryRepository("rdfs");
		//enable GeoSPARQL
		sparqlInsert("PREFIX : <http://www.ontotext.com/plugins/geosparql#>\n" + 
				"\n" + 
				"INSERT DATA {\n" + 
				"  _:s :enabled \"true\" .\n" + 
				"}");
	}
	
	@Override
	public int countTriples() {
		TupleQueryResult result = query("SELECT (COUNT(*) AS ?no) WHERE { ?s ?p ?o  }");
		int triples = -1;
		while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value no = bindingSet.getBinding("no").getValue();
            triples = Integer.parseInt(no.stringValue());
        }
        result.close();
        return triples;
	}
	
	public ExternalDB_GraphDB(String endpoint, String repoID) {
		//SPARQLRepository rep = new SPARQLRepository(endpoint);
		//rep.initialize();
		RepositoryManager repositoryManager =
		        new RemoteRepositoryManager( endpoint );
		repositoryManager.initialize();
		
		connection = repositoryManager.getRepository(repoID).getConnection();
		sparqlInsert("PREFIX : <http://www.ontotext.com/plugins/geosparql#>\n" + 
				"\n" + 
				"INSERT DATA {\n" + 
				"  _:s :enabled \"true\" .\n" + 
				"}");
	}
	
	/**
	 * It will create and use a new repository with a name based on "basename" and copying the configuration
	 * of another existing repository with id "defaultConfigRepo"
	 * @param endpoint
	 * @param defaultConfigRepo
	 * @param basename
	 * @throws IOException
	 */
	public ExternalDB_GraphDB(String endpoint, String defaultConfigRepo, String basename) throws IOException {
		//SPARQLRepository rep = new SPARQLRepository(endpoint);
		//rep.initialize();
		remoteManager =
		        new RemoteRepositoryManager( endpoint );
		remoteManager.initialize();
		//remoteTempId = remoteManager.getNewRepositoryID(basename);
		remoteTempId = basename+new Date().getTime();
		// load a configuration file
		TreeModel graph = new TreeModel();
		RepositoryConfig config = remoteManager.getRepositoryConfig(defaultConfigRepo);
		config.setID(remoteTempId);
		remoteManager.addRepositoryConfig(config);
		//HTTPRepositoryConfig config = new HTTPRepositoryConfig();
		//RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
        //rdfParser.setRDFHandler(new StatementCollector(graph));
        //rdfParser.parse(configRDF, RepositoryConfigSchema.NAMESPACE);
        //configRDF.close();
        //config.parse(graph, SimpleValueFactory.getInstance().createIRI("http://www.example.com#repoconfigid"));
		
		connection = remoteManager.getRepository(remoteTempId).getConnection();
		//connection = remoteManager.getRepository(repoID).getConnection();
		sparqlInsert("PREFIX : <http://www.ontotext.com/plugins/geosparql#>\n" + 
				"\n" + 
				"INSERT DATA {\n" + 
				"  _:s :enabled \"true\" .\n" + 
				"}");
	}
	
	public void clearDB() {
		remoteManager.removeRepository(remoteTempId);
	}
	
	//http://graphdb.ontotext.com/free/devhub/familyrelations.html
	public void sparqlInsert(String insertQuery) {
		connection.begin();
		UpdateUtil.executeUpdate(connection, String.format(insertQuery) );
		connection.commit();
	}
	
	public boolean sparqlAsk(String insertQuery) {
		connection.begin();
		boolean answer = QueryUtil.evaluateAskQuery(connection, String.format(insertQuery) );
		connection.commit();
		return answer;
	}
	
	@Override
	public void setNamespace(String prefix, String name) {
		connection.begin();
		connection.setNamespace(prefix, name);
		connection.commit();
	}
	
	@Override
	public Map<String,String> getNamespaces(){
		Map<String,String> m = new HashMap<String,String>();
		for(Namespace n : Iterations.asList(connection.getNamespaces())) {
			m.put(n.getPrefix(), n.getName());
		}
		return m;
	}
	
	@Override
	public void loadRDF(File file, RDFFormat dataFormat) throws RDFParseException, RepositoryException, IOException {
		connection.begin();
		connection.add(file, null, dataFormat);
		connection.commit();
	}
	@Override
	public void loadRDF(Reader reader, RDFFormat dataFormat) throws RDFParseException, RepositoryException, IOException {
		connection.begin();
		connection.add(reader, null, dataFormat);
		connection.commit();
	}
	
	@Override
	public TupleQueryResult query(String queryString) {
		return QueryUtil.evaluateSelectQuery(connection,queryString);
	}

	@Override
	public void insertFullyInstantiatedPredicate(PredicateInstantiation pi, String baseNew) {
		//if(pi.hasVariables()) throw new RuntimeException("ERROR, cannot assert this predicate instantion defined under START AVAILABLE ASSERTED as it contains variables: "+pi.toSPARQL());
		// If the statements don't already exist, insert them
		if (!sparqlAsk("ASK {\n" + pi.toSPARQL_INSERT(null)+ "}"))
			sparqlInsert("INSERT DATA {\n" + pi.toSPARQL_INSERT(baseNew)+ "}");
	}
}
