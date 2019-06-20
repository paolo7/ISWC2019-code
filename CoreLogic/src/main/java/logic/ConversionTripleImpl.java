package logic;

public class ConversionTripleImpl extends ConversionTripleAbstr{

	private Binding subject;
	private Binding predicate;
	private Binding object;
	
	public ConversionTripleImpl(Binding subject, Binding predicate, Binding object) {
		if((subject.isConstant() && subject.getConstant().isLiteral() ) || (predicate.isConstant() && predicate.getConstant().isLiteral() ))
			throw new MappingInvalidException("Error: trying to create a triple with a literal in the subject or predicate position");
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		
	}
	
	@Override
	public Binding getSubject() {
		return subject;
	}

	@Override
	public Binding getPredicate() {
		return predicate;
	}

	@Override
	public Binding getObject() {
		return object;
	}



/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConversionTripleImpl other = (ConversionTripleImpl) obj;
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
	}*/
	
	

}
