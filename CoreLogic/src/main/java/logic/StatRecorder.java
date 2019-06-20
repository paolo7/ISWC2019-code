package logic;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StatRecorder {

	public List<Double> avgTimeRuleApplication;
	public Set<Rule> applicableRules = new HashSet<Rule>();
	public Set<PredicateInstantiation> inferrable_triples = new HashSet<PredicateInstantiation>();
	
	public StatRecorder() {
		avgTimeRuleApplication = new LinkedList<Double>();
	}
	public double getAvgTime() {
		return getAvg(avgTimeRuleApplication);
	}
	private double getAvg(List<Double> list) {
		double tot = 0;
		for(Double d: list) tot += d;
		return tot/list.size();
	}
	
	
}
