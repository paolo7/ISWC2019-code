package logic;

public interface LabelService {

	public String getLabel(String URI);
	public boolean hasLabel(String URI);
	public void setLabel(String URI, String label);
	
}
