package shacl;

public class CyclicRulesException extends RuntimeException{

	public CyclicRulesException(String string) {
		super(string);
	}
}
