package datagenerator;public class View extends Statement {
    public View (Statement astate){
        _num_subgoals = astate.size();
        currentpred = 0;
        head = new Predicate(astate.head);
        body = new Predicate[astate.size()];
        for (int i = 0; i < astate.size(); i++){
            body[i] = new Predicate(astate.subgoalI(i));
        }
        
        //need to figure out what the difference is in getnumsbugoals and size
	}
    public View(){
        super();
    }
    
	public View(int num_subgoals){
		super(num_subgoals);
	}
	
	public View(String input){
		super(input);
	}
			
     public View(View a){
        _num_subgoals = a.size();
        currentpred = 0;
        head = new Predicate(a.head);
        body = new Predicate[a.size()];
         for (int i = 0; i < a.size();i++){
            body[i] = new Predicate(a.subgoalI(i));
            //body.addElement(new Predicate((Predicate)(a.body.elementAt(i))));
        }
     }//end copy constructor
}
