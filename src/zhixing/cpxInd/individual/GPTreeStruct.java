package zhixing.cpxInd.individual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;

public class GPTreeStruct extends GPTree {
	
	public static final int ARITHMETIC = 0;
	public static final int BRANCHING = 1;
	public static final int ITERATION = 2;
	
	public Boolean status; //false: non-effective, true: effective
	public Set<Integer> effRegisters; //target effective registers for this position
	
	public int type = ARITHMETIC;  //default 0. 0: arithmetic, 1: branching, 2: iteration
	
	public void updateEffRegister(Set<Integer> s){
		child.collectReadRegister(s);
	}
	
	public Set<Integer> collectReadRegister(){
		Set<Integer> s = new HashSet<>();
		child.collectReadRegister(s);
		
		return s;
	}
	
	public List<Integer> collectReadRegister_list(){
		ArrayList<Integer> l = new ArrayList<>();
		child.collectReadRegister_list(l);
		
		return l;
	}
	
	public GPTreeStruct() {
		super();
		status = false;
		effRegisters = new HashSet<>(0);
	}
	
	public Object clone(){
		GPTreeStruct t = lightClone();
        t.child = (GPNode)(child.clone());  // force a deep copy
        t.child.parent = t;
        t.child.argposition = 0;
		t.status = this.status;
    	t.effRegisters = new HashSet<>(0);
    	Iterator<Integer> it = this.effRegisters.iterator();
    	while(it.hasNext()) {
    		int v = it.next();
    		t.effRegisters.add(v);
    	}
    	t.type = type;
    	return t;
	}
	
	public GPTreeStruct lightClone(){
		GPTreeStruct t = (GPTreeStruct)(super.lightClone());
//		t.status = this.status;
//    	t.effRegisters = new HashSet<>(0);
//    	Iterator<Integer> it = this.effRegisters.iterator();
//    	while(it.hasNext()) {
//    		int v = it.next();
//    		t.effRegisters.add(v);
//    	}
//    	t.type = type;
    	return t;
	}
	
	public void assignfrom (GPTree tree){
		child = tree.child;
		owner = tree.owner;
		constraints = tree.constraints;
		printStyle = tree.printStyle;
		printTerminalsAsVariablesInC = tree.printTerminalsAsVariablesInC;
		printTwoArgumentNonterminalsAsOperatorsInC = tree.printTwoArgumentNonterminalsAsOperatorsInC;
	}
}
