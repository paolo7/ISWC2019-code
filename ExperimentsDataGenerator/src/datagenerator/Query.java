package datagenerator;

public class Query extends Statement {
	//the point here is to just make sure that you don't call a view with a statement and 
	//vice versa
	public Query (Statement astate){
		_num_subgoals = astate.size();
		currentpred = 0;
		body = astate.body;
		head = astate.head;
	}
	public Query(){
		super();
	}
	
	public Query(String input){
		super(input);
	}
}
