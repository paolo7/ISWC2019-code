package core;

import shacl.LiteralEnforcingException;

public class Literal implements Element{
	private String value;
	private URI datatype;
	
	public String getValue() {
		return value;
	}

	public URI getDatatype() {
		return datatype;
	}

	
	public Literal(String value) {
		this.value = value;
		this.datatype = new URI(new Namespace("xs","http://www.w3.org/2001/XMLSchema#string"),"string");		
	}
	
	public Literal(String value, URI datatype) {
		this.value = value;
		this.datatype = datatype;		
	}
	
	@Override
	public String toString() {
		return "\"" + value + "\"^^"+datatype;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datatype == null) ? 0 : datatype.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Literal other = (Literal) obj;
		if (datatype == null) {
			if (other.datatype != null)
				return false;
		} else if (!datatype.equals(other.datatype))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toSHACLconstraint() {
		return "\"" + value + "\"";
	}

	@Override
	public String toSPARQLelement() {
		return "\"" + value + "\"";
	}

	@Override
	public void enforceNoLiteral() {
		throw new LiteralEnforcingException("ERROR, a literal cannot be forced not be a literal");
	}
}
