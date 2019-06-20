package logic;

public abstract class PredicateAbstr implements Predicate{

	/*@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof Predicate)) {
            return false;
        }
        Predicate p = (Predicate) o;
        
        if(this.getRDFtranslation() == null) {
        	if (p.getRDFtranslation() != null) return false;
        }
        
        if(this.getRDFtranslation().size() != p.getRDFtranslation().size())
        	return false;
        
        if(this.getTextLabel().size() != p.getTextLabel().size())
        	return false;
        
        if(! this.getRDFtranslation().equals(p.getRDFtranslation()))
        	return false;
        
        for(int i = 0; i < this.getTextLabel().size(); i++) {
        	if(! this.getTextLabel().get(i).equals(p.getTextLabel().get(i)))
        		return false;
        }
         
        return this.getName().toLowerCase().equals(p.getName().toLowerCase()) && this.getVarnum() == p.getVarnum();
    }*/
	
	@Override
	public String toString() {
		// signature
		String s = "PREDICATE: "+this.getName()+"(";
		boolean first = true;
		for(int i = 0; i < this.getVarnum(); i++) {
			if(first) first = false;
			else s += ", ";
			s += "?v"+i;
		}
		s += ")\n";
		// label
		s += "LABEL    : ";
		for (TextTemplate tt : this.getTextLabel()) {	
				s += tt+" ";
		}
		s += "\n";
		// rdf conversion
		s += "RDF      : ";
		if(this.getRDFtranslation() != null) for (ConversionTriple tt : this.getRDFtranslation()) {	
			s += tt+" . \n           ";
		}
		if(this.getRDFtranslationFilters() != null) for (ConversionFilter tt : this.getRDFtranslationFilters()) {	
			s += tt+" . \n           ";
		}
		return s;
	}
	
	@Override
	public String toSPARQL() {
		String s = "";
		if(this.getRDFtranslation() != null) for (ConversionTriple tt : this.getRDFtranslation()) {	
			s += tt.toSPARQL()+" . \n           ";
		}
		if(this.getRDFtranslationFilters() != null) for (ConversionFilter tt : this.getRDFtranslationFilters()) {	
			s += tt.toSPARQL()+" . \n           ";
		}
		return s;
	}
	
/*	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + ((this.getName() == null) ? 0 : this.getName().toLowerCase().hashCode());
		result = prime * result + ((this.getTextLabel() == null) ? 0 : this.getTextLabel().hashCode());
		result = prime * result + ((this.getRDFtranslation() == null) ? 0 : this.getRDFtranslation().hashCode());
		result = prime * result + this.getVarnum();
		return result;
	}*/
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode());
		result = prime * result + ((this.getTextLabel() == null) ? 0 : this.getTextLabel().hashCode());
		result = prime * result + ((this.getRDFtranslation() == null) ? 0 : this.getRDFtranslation().hashCode());
		result = prime * result + ((this.getRDFtranslationFilters() == null) ? 0 : this.getRDFtranslationFilters().hashCode());
		result = prime * result + this.getVarnum();
		return result;
		//return 31 + this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		//if(this.toString().equals(obj.toString())) return true;
		PredicateImpl other = (PredicateImpl) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (getTextLabel() == null) {
			if (other.getTextLabel() != null)
				return false;
		} else if (!getTextLabel().equals(other.getTextLabel()))
			return false;
		if (getRDFtranslation() == null) {
			if (other.getRDFtranslation() != null)
				return false;
		} else if (!getRDFtranslation().equals(other.getRDFtranslation()))
			return false;
		if (getRDFtranslationFilters() == null) {
			if (other.getRDFtranslationFilters() != null)
				return false;
		} else if (!getRDFtranslationFilters().equals(other.getRDFtranslationFilters()))
			return false;
		if (getVarnum() != other.getVarnum())
			return false;
		return true;
	}
}
