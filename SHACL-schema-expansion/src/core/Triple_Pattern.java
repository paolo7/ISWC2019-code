package core;

import shacl.LiteralEnforcingException;

public class Triple_Pattern {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
		Triple_Pattern other = (Triple_Pattern) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!predicate.equals(other.predicate))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}

	private Element subject;
	private Element predicate;
	private Element object;
	
	public Element get(int i) {
		switch (i) {
		case 0:	return subject;
		case 1: return predicate;
		case 2: return object;
		default: throw new IndexOutOfBoundsException("ERROR, triple indexes can only be 0, 1 or 2");
		}
	}
	public Element getSubject() {
		return subject;
	}

	public URI getPredicate() {
		return (URI) predicate;
	}

	public Element getObject() {
		return object;
	}

	public Triple_Pattern(Element subject, Element predicate, Element object) {
		if(subject instanceof Literal || predicate instanceof Literal) 
			throw new LiteralEnforcingException("ERROR, trying to initialise a triple pattern with a literal in the subject or predicate position");
		if((subject instanceof Variable && !((Variable) subject).isNoLit()) || (predicate instanceof Variable && !((Variable) predicate).isNoLit())) 
			throw new LiteralEnforcingException("ERROR, trying to initialise a triple pattern with a variable that allows literals in the subject or predicate position");
		if(predicate instanceof Variable) 
			throw new RuntimeException("ERROR, trying to initialise a triple pattern with a variable in the predicate position (disabled because of SHACL restrictions)");
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	@Override
	public String toString() {
		return "< "+subject+" "+predicate+" "+object+" >";
	}
	
	public String uri_base_for_variable_grounding = "http://vars.vars/";
	public Triple_Pattern util_ground_variables() {
		return new Triple_Pattern(
				this.getSubject() instanceof Variable ? 
						new URI(uri_base_for_variable_grounding + ( (Variable_Instance)this.getSubject() ).getIndex()) 
						: this.getSubject(),
				this.getPredicate(),
				this.getObject() instanceof Variable ? 
						new URI(uri_base_for_variable_grounding + ( (Variable_Instance)this.getObject() ).getIndex()) 
						: this.getObject()
				);
	}
}
