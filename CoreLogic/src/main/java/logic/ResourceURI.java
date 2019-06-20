package logic;

public class ResourceURI extends ResourceAbstr {

	private String URI;
	
	public ResourceURI(String URI) {
		this.URI = URI;
	}
	
	@Override
	public String getLexicalValue() {
		/*if(RDFUtil.prefixes.shortForm(URI) != null)
			return RDFUtil.prefixes.shortForm(URI);
		else return URI;*/
		return RDFUtil.expandPrefix(URI);
	}
	
	public String getURI() {
		return URI;
	}

	@Override
	public boolean isLiteral() {
		return false;
	}

	@Override
	public boolean isURI() {
		return true;
	}

	@Override
	public String getLexicalValueExpanded() {
		return "<"+RDFUtil.expandPrefix(URI)+">";
	}


}