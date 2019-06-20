package core;

import java.util.HashSet;
import java.util.Set;

public class SearchUtil {

	public static Set<Triple_Pattern> searchByPredicate(URI predicate, Set<Triple_Pattern> set){
		Set<Triple_Pattern> result = new HashSet<Triple_Pattern>();
		for (Triple_Pattern tp : set) {
			if(tp.getPredicate().equals(predicate)) result.add(tp);
		}
		return result;
	}
	
	public static Set<Triple_Pattern> searchBySubject(Element el, Set<Triple_Pattern> set){
		Set<Triple_Pattern> result = new HashSet<Triple_Pattern>();
		for (Triple_Pattern tp : set) {
			if(tp.getSubject().equals(el)) result.add(tp);
			else if(el instanceof Variable && tp.getSubject() instanceof Variable) {
				if(
						((Variable)el).isNoLit() == ((Variable)tp.getSubject()).isNoLit()
						)
					result.add(tp);
			}
		}
		return result;
	}
	
	public static Set<Triple_Pattern> searchWithConstantSubjects(Set<Triple_Pattern> set){
		Set<Triple_Pattern> result = new HashSet<Triple_Pattern>();
		for (Triple_Pattern tp : set) {
			if(tp.getSubject() instanceof URI) result.add(tp);
		}
		return result;
	}
	
	public static Set<Triple_Pattern> searchByObject(Element el, Set<Triple_Pattern> set){
		Set<Triple_Pattern> result = new HashSet<Triple_Pattern>();
		for (Triple_Pattern tp : set) {
			if(tp.getObject().equals(el)) result.add(tp);
		}
		return result;
	}
	
	public static Set<URI> searchURISubjects(Set<Triple_Pattern> set){
		Set<URI> result = new HashSet<URI>();
		for (Triple_Pattern tp : set) {
			if(tp.getSubject() instanceof URI) result.add((URI) tp.getSubject());
		}
		return result;
	}
	public static Set<Element> searchConstantObjects(Set<Triple_Pattern> set){
		Set<Element> result = new HashSet<Element>();
		for (Triple_Pattern tp : set) {
			if(tp.getObject() instanceof URI || tp.getObject() instanceof Literal) result.add(tp.getObject());
		}
		return result;
	}
	
	public static Set<URI> searchURIs(Set<Triple_Pattern> set){
		Set<URI> result = new HashSet<URI>();
		for (Triple_Pattern tp : set) {
			if(tp.getSubject() instanceof URI) result.add((URI) tp.getSubject());
			if(tp.getPredicate() instanceof URI) result.add((URI) tp.getPredicate());
			if(tp.getObject() instanceof URI) result.add((URI) tp.getObject());
		}
		return result;
	}
	
	public static Set<Element> getObjects(Set<Triple_Pattern> set){
		Set<Element> result = new HashSet<Element>();
		for (Triple_Pattern tp : set) {
			result.add(tp.getObject());
		}
		return result;
	}
	public static Set<Element> getSubjects(Set<Triple_Pattern> set){
		Set<Element> result = new HashSet<Element>();
		for (Triple_Pattern tp : set) {
			result.add(tp.getSubject());
		}
		return result;
	}
	
	public static boolean containsVar(boolean noLit, Set<Element> elements) {
		for(Element e : elements) 
			if(e instanceof Variable && ((Variable)e).isNoLit() == noLit) return true;
		return false;
	}
	
	public static Set<Triple_Pattern> searchByObjectSubsumedBy(Element object, Set<Triple_Pattern> set){
		Set<Triple_Pattern> result = new HashSet<Triple_Pattern>();
		//Set<Triple_Pattern> setWithPredicate = searchByPredicate(predicate, set);
		for (Triple_Pattern tp : set) {
			if(isElementSubsumedBy(tp.getObject(), object)) {
				result.add(tp);
			}
		}
		return result;
	}
	
	public static Set<Triple_Pattern> searchByObjectSubsuming(Element object, Set<Triple_Pattern> set){
		Set<Triple_Pattern> result = new HashSet<Triple_Pattern>();
		//Set<Triple_Pattern> setWithPredicate = searchByPredicate(predicate, set);
		for (Triple_Pattern tp : set) {
			if(isElementSubsumedBy(object, tp.getObject())) {
				result.add(tp);
			}
		}
		return result;
	}
	
	public static Set<Element> searchPossibleSubjectsForPredicateObjectIntersect(URI predicate, Element object, Set<Triple_Pattern> set){
		Set<Element> result = new HashSet<Element>();
		Set<Triple_Pattern> setWithPredicate = searchByPredicate(predicate, set);
		for (Triple_Pattern tp : setWithPredicate) {
			if(isElementSubsumedBy(object, tp.getObject()) || isElementSubsumedBy(tp.getObject(), object)) {
				result.add(tp.getSubject());
			}
		}
		return result;
	}
	
	public static boolean isTripleSubsumedByTriplePatterns(Triple_Pattern tp, Set<Triple_Pattern> patterns2) {
		Set<Triple_Pattern> patterns1 = new HashSet<Triple_Pattern>();
		patterns1.add(tp);
		return areTriplePatternsSubsumedBy(patterns1, patterns2);
	}
	
	public static boolean areTriplePatternsSubsumedBy(Set<Triple_Pattern> patterns1, Set<Triple_Pattern> patterns2) {
		if(patterns2.size() == 0) return false;
		for(Triple_Pattern tp1 : patterns1) {
			boolean tp1_subsumed = false;
			for(Triple_Pattern tp2 : patterns2) {
				if(isTripleSubsumedBy(tp1, tp2)) tp1_subsumed = true;
			}
			if(!tp1_subsumed) return false;
		}
		
		return true;
	}
	
	public static boolean isTripleSubsumedBy(Triple_Pattern tp1, Triple_Pattern tp2) {
		return isElementSubsumedBy(tp1.getSubject(), tp2.getSubject())
				&& isElementSubsumedBy(tp1.getPredicate(), tp2.getPredicate())
				&& isElementSubsumedBy(tp1.getObject(), tp2.getObject()) ;
	}
	
	public static boolean isElementSubsumedBy(Element el1, Element el2) {
		// equivalent elements subsume each other
		if(el1.equals(el2)) return true;
		// otherwise, if they are different:
		if(el2 instanceof Variable) {
			// a lit allowed variable subsumes everything
			if(!((Variable)el2).isNoLit())
				return true;
			else {
				// if they are both Vars-, they subsume each other
				if(el1 instanceof Variable) {
					if(((Variable)el1).isNoLit())
						return true;
					else return false;
					
				}
				// a no-lit variable only subsumes URIs
				if(el1 instanceof URI) return true;
				else return false;
			}
		} else {
			// if el2 is not a variable, and it is different from el1, then it cannot subsume it
			return false;
		}
	}

	private static boolean containsPredicate(URI predicate, Set<Triple_Pattern> patterns) {
		for(Triple_Pattern tp : patterns) {
			if(tp.getPredicate().equals(predicate)) return true;
		}
		return false;
	}
}
