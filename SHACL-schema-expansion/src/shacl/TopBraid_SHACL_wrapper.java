package shacl;

import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import core.Triple_Pattern;

public class TopBraid_SHACL_wrapper {

	

	public static boolean validate(Model dataModel, Set<Triple_Pattern> patterns) {
		return validate(dataModel, Translator_to_SHACL.schemaToSHACLModel(patterns));
	}
	
	public static boolean validate(Model dataModel, Model shapesModel) {
		
		/*
		 dataModel.write(System.out, "TURTLE") ;
			System.out.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
		shapesModel.write(System.out, "TURTLE") ;
		System.out.println("\n#####################################################################\n\n\n");
		*/
		//ValidationUtil validator = new ValidationUtil();
		Resource report = ValidationUtil.validateModel(dataModel, shapesModel, true);
		return report.getProperty(SH.conforms).getBoolean();
		
	}
}
