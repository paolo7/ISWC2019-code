package shacl;

import java.io.StringWriter;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

import core.Triple_Pattern;
import core.Util;

public class Schema {

	private String original_schema_SHACL;
	private Model schema_SHACL;
	private Set<Triple_Pattern> schema_Graph;
	private Set<Existential_Constraint> schema_Existentials;
	
	public Schema(String SHACL) {
		Schema s = Translator_to_Graph_Pattern.translate(schema_SHACL, null);
		original_schema_SHACL = SHACL;
		schema_SHACL = s.getSchema_SHACL();
		schema_Graph = s.getSchema_Graph();
	}
	


	public Schema(Set<Triple_Pattern> schema_Graph, Set<Existential_Constraint> schema_Existentials) {
		this.schema_Graph = schema_Graph;
		this.schema_Existentials = schema_Existentials;
		this.schema_SHACL = Util.unprefixedTurtleToModel(Translator_to_SHACL.translateToTurtleSHACL(schema_Graph));
		
	}

	public Model getSchema_SHACL() {
		return schema_SHACL;
	}

	public Set<Triple_Pattern> getSchema_Graph() {
		return schema_Graph;
	}

	public Set<Existential_Constraint> getSchema_Existentials() {
		return schema_Existentials;
	}
	
	public String getOriginal_schema_SHACL() {
		return original_schema_SHACL;
	}
	
	public String pretty_print_string(){
		String result = "------------------------\nSCHEMA: \n"
				+ "-Graph:\n";
		for(Triple_Pattern tp : schema_Graph) {
			result += "|  "+tp+"\n";
		}		
		result += "-Existentials:\n";
		for(Existential_Constraint ex : schema_Existentials) {
			result += "|  "+ex+"\n";
		}		
		result += "-SHACL translation:\n\n";
		StringWriter sw = new StringWriter();
		schema_SHACL.write(sw, "TURTLE");
		result += sw.toString();
		return result+"\n------------------------\n";
				
	}
}
