package logic;

public abstract class TextTemplateAbstr implements TextTemplate{


	
	@Override
	public String toString() {
		if(this.isText()) return this.getText();
		else return "[?v"+this.getVar()+"]";
	}
	

}
