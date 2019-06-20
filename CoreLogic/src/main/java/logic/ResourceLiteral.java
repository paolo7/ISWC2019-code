package logic;

import org.apache.jena.datatypes.RDFDatatype;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class ResourceLiteral extends ResourceAbstr {

	private String literalValue;
	
	private IRI typeIRI;
	
	public ResourceLiteral(String literalValue, IRI typeIRI) {
		this.literalValue = literalValue;
		this.typeIRI = typeIRI;
	}
	
	public ResourceLiteral(String literalValue, String typeIRI) {
		this.literalValue = literalValue;
		this.typeIRI = SimpleValueFactory.getInstance().createIRI(typeIRI);
	}
	
	@Override
	public String getLexicalValue() {
		return literalValue;
	}
	
	public String getLexicalValueExpanded() {
		return "\""+literalValue+"\"^^<"+typeIRI.stringValue()+">";
	}

	@Override
	public boolean isLiteral() {
		return true;
	}

	@Override
	public boolean isURI() {
		return false;
	}
	
	public IRI getLiteralTypeIRI() {
		return typeIRI;
	}


}
