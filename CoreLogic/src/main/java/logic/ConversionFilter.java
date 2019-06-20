package logic;

import java.util.List;

public class ConversionFilter {

	List<TextTemplate> templates;
	
	public ConversionFilter(List<TextTemplate> templates) {
		this.templates = templates;
	}	
	
	public String toSPARQL(Binding[] bindings, String freshVarPrefix) {
		String snippet = "";
		for(TextTemplate tt : templates) {
			if (tt.isText()) snippet += tt.getText()+" ";
			else {
				if (tt.getVar() >= bindings.length) {
					snippet += "?v"+tt.getVar()+freshVarPrefix+" ";
				} else {
					Binding b = bindings[tt.getVar()];
					if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
					else snippet += "?v"+b.getVar()+" ";
				}
			}
		}
		return snippet;
	}
	
	public String toSPARQL() {
		String snippet = "";
		for(TextTemplate tt : templates) {
			if (tt.isText()) snippet += tt.getText()+" ";
			else {
				snippet += "?v"+tt.getVar()+" ";
			}
		}
		return snippet;
	}
	
	public String toSPARQL_INSERT(Binding[] bindings, String baseNew) {
		String snippet = "";
		for(TextTemplate tt : templates) {
			if (tt.isText()) snippet += tt.getText()+" ";
			else {
				if (tt.getVar() >= bindings.length) {
					snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,tt.getVar());

				} else {
					Binding b = bindings[tt.getVar()];
					
					
					
					if(b.isConstant()) {
						if(b.getConstant().isLiteral() && RDFUtil.isNumericDatatypeIRI(((ResourceLiteral) b.getConstant()).getLiteralTypeIRI()))
							snippet += b.getConstant().getLexicalValue();
						else snippet += b.getConstant().getLexicalValueExpanded();
					}
					else {
						snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,b.getVar().getVarNum());
					}
				}
			}
		}
		return snippet;
	}
	
	@Override
	public String toString() {
		String snippet = "";
		for(TextTemplate tt : templates) {
			if (tt.isText()) snippet += tt.getText()+" ";
			else snippet += "?v"+tt.getVar()+" ";
			
		}
		return snippet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((templates == null) ? 0 : templates.hashCode());
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
		ConversionFilter other = (ConversionFilter) obj;
		if (templates == null) {
			if (other.templates != null)
				return false;
		} else if (!templates.equals(other.templates))
			return false;
		return true;
	}
	
}
