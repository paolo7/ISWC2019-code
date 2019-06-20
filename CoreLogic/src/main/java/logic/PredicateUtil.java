package logic;

import java.util.HashSet;
import java.util.Set;

public class PredicateUtil {

	/**
	 * If predicates contain a Predicate with signature predicateName and varNum variables, return it. Otherwise throw a runtime exception.
	 * @param predicateName
	 * @param varNum
	 * @param predicates
	 * @return
	 */
	public static Predicate get(String predicateName, int varNum, Set<Predicate> predicates) {
		Predicate predicate = null;
		for(Predicate p : predicates) {
			if(p.getName().toLowerCase().equals(predicateName.toLowerCase()) && p.getVarnum() == varNum) {
				if(predicate == null)
					predicate = p;
				else
					throw new RuntimeException("ERROR: the set of predicates contain more than one entry with predicate name "+predicateName+" and "+varNum+" variables");
			}
		}
		if(predicate != null)
			return predicate;
		else
			throw new RuntimeException("ERROR: the set of predicates does not contain an entry with predicate name "+predicateName+" and "+varNum+" variables");
	}
	
	
	public static Predicate get(PredicateTemplate pt, Set<Predicate> predicates) {
		String name = pt.getName().iterator().next().getText();
		int varNum = pt.getBindings().length;
		Predicate predicate = null;
		for(Predicate p : predicates) {
			if(p.getName().toLowerCase().equals(name.toLowerCase()) && p.getVarnum() == varNum) {
				if(predicate == null)
					predicate = p;
				else
					throw new RuntimeException("ERROR: the set of predicates contain more than one entry with predicate name "+name+" and "+varNum+" variables");
			}
		}
		if(predicate != null)
			return predicate;
		else
			throw new RuntimeException("ERROR: the set of predicates does not contain an entry with predicate name "+name+" and "+varNum+" variables");
	}
	
	/**
	 * 
	 * @param predicateName
	 * @param varNum
	 * @param predicates
	 * @return true if predicates contain a single Predicate with signature predicateName and varNum variables. Else return false.
	 */
	public static boolean containsOne(String predicateName, int varNum, Set<Predicate> predicates) {
		Predicate predicate = null;
		for(Predicate p : predicates) {
			if(p.getName().toLowerCase().equals(predicateName.toLowerCase()) && p.getVarnum() == varNum) {
				if(predicate == null)
					predicate = p;
				else
					return false;
			}
		}
		if(predicate != null)
			return true;
		else
			return false;
	}
	
	public static boolean variableCanBeLiteralInPosition(Predicate p, int position) {
		if(position < 0 || position >= p.getVarnum()) throw new RuntimeException("ERROR: wrong index for bindings");
		if(p.getRDFtranslation() != null) {
			for(ConversionTriple ct : p.getRDFtranslation()) {
				if(ct.getNoLitVariables().contains(new Integer(position))) return false;
			}
		}
		return true;
	}
	
	public static Set<PredicateInstantiation> trimConsequences(Set<PredicateInstantiation> pis) {
		Set<PredicateInstantiation> newPis = new HashSet<PredicateInstantiation>();
		/*for(PredicateInstantiation pi : pis) {
			pi.getAdditionalConstraints().clear();
		}*/
		for(PredicateInstantiation pi : pis) {
			newPis.add(new PredicateInstantiationImpl(pi.getPredicate(), pi.getBindings()));
		}
		/*for(PredicateInstantiation p1 : newPis) {
			for(PredicateInstantiation p2 : newPis) {
				if(p1.equals(p2) && p1.hashCode() != p2.hashCode())
					System.out.println(p1.equals(p2));
			}
		}*/
		return newPis;
	}
	
}
