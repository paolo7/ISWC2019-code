package logic;

public class TextTemplateImpl extends TextTemplateAbstr{

	private String text;
	private int var;
	
	public TextTemplateImpl(String text) {
		this.text = text;
		var = -1;
	}
	
	public TextTemplateImpl(int var) {
		if(var < -1) throw new RuntimeException("ERROR: instantiating a TextTemplate with a variable < -1");
		this.var = var;
	}

	@Override
	public boolean isText() {
		return var == -1;
	}

	@Override
	public boolean isVar() {
		return var != -1;
	}

	@Override
	public int getVar() {
		if(!isVar()) throw new RuntimeException("ERROR: Attempted to read a variable from a textual TextTemplate");
		return var;
	}

	@Override
	public String getText() {
		if(isVar()) throw new RuntimeException("ERROR: Attempted to read a String from a variable TextTemplate");
		return text;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + var;
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
		TextTemplateImpl other = (TextTemplateImpl) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (var != other.var)
			return false;
		return true;
	}

/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextTemplateImpl other = (TextTemplateImpl) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (var != other.var)
			return false;
		return true;
	}*/
	
	
}
