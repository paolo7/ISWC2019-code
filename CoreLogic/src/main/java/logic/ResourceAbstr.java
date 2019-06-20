package logic;

public abstract class ResourceAbstr implements Resource {

	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof Resource)) {
            return false;
        }
        Resource p = (Resource) o;
        return this.getLexicalValue().toLowerCase().equals(p.getLexicalValue().toLowerCase());
    }
	
	@Override
	public String toString() {
		if(this.isLiteral()) return "\""+this.getLexicalValue()+"\"";
		return this.getLexicalValue();
	}
	
	@Override
	public int hashCode() {
		String lv = this.getLexicalValue().toLowerCase();
		final int prime = 31;
		int result = 7;
		result = prime * result + ((lv == null) ? 0 : lv.hashCode());
		return result;
	}
	
}
