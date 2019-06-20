package benchmarking;

public class ScoreResult {
	public double time;
	public double newPredicates;
	public double ruleApplications;
	public double averageRuleApplicationTime;
	public double applicableRules;
	public ScoreResult(double time, double newPredicates, double ruleApplications, double averageRuleApplicationTime, double applicableRules) {
		this.time = time;
		this.newPredicates = newPredicates;
		this.ruleApplications = ruleApplications;
		this.averageRuleApplicationTime = averageRuleApplicationTime;
		this.applicableRules = applicableRules;
	}
	
	@Override
	public String toString() {
		return "Time: "+time+" Inferred predicates: "+newPredicates+" Rule applications: "+ruleApplications+" Individual rule application time "+averageRuleApplicationTime;
	}
}
