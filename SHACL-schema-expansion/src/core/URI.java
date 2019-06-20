package core;

public class URI implements Element{
	private String URI;
	private Namespace namespace;
	private String namespacesuffix;
	
	public String getURI() {
		return URI;
	}
	public Namespace getNamespace() {
		return namespace;
	}
	public String getNamespacesuffix() {
		return namespacesuffix;
	}

	public URI(String URI) {
		Validator.validateURI(URI);
		this.namespace = null;
		this.namespacesuffix = null;
		this.URI = URI;
	}
	public URI(Namespace namespace, String namespacesuffix) {
		Validator.validateURI(namespacesuffix);
		this.namespace = namespace;
		this.namespacesuffix = namespacesuffix;
		this.URI = this.namespace.getNamespace()+this.namespacesuffix;
	}
	
	@Override
	public String toString() {
		if(namespace == null || namespacesuffix== null) return "<"+URI+">";
		else return namespace.getPrefix()+":"+namespacesuffix;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((URI == null) ? 0 : URI.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		URI other = (URI) obj;
		if (URI == null) {
			if (other.URI != null)
				return false;
		} else if (!URI.equals(other.URI))
			return false;
		return true;
	}
	@Override
	public String toSHACLconstraint() {
		if(namespace == null || namespacesuffix== null) return "<"+URI+">";
		else return namespace.getPrefix()+":"+namespacesuffix;
	}
	@Override
	public String toSPARQLelement() {
		return "<"+URI+">";
	}
	@Override
	public void enforceNoLiteral() {
		// nothing to do, a URI is never a literal
	}
	
	
}
