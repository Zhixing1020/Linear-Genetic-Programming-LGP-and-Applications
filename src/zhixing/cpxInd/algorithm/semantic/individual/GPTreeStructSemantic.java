package zhixing.cpxInd.algorithm.semantic.individual;

import java.util.HashSet;
import java.util.Iterator;

import ec.gp.GPNode;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;
import zhixing.cpxInd.individual.GPTreeStruct;

public class GPTreeStructSemantic extends GPTreeStruct{
	public SemanticVector context = null;
	
	public GPTreeStructSemantic() {
		super();
		
	}
	
	public GPTreeStructSemantic(GPTreeStruct obj) {

		child = (GPNode)(obj.child.clone());  // force a deep copy
        child.parent = this;
        child.argposition = 0;
        
        constraints = obj.constraints;
		printStyle = obj.printStyle;
		printTerminalsAsVariablesInC = obj.printTerminalsAsVariablesInC;
		printTwoArgumentNonterminalsAsOperatorsInC = obj.printTwoArgumentNonterminalsAsOperatorsInC;
        
		status = obj.status;
    	effRegisters = new HashSet<>(0);
    	Iterator<Integer> it = obj.effRegisters.iterator();
    	while(it.hasNext()) {
    		int v = it.next();
    		effRegisters.add(v);
    	}
    	type = obj.type;

	}

	public void copySemanticVectorFrom(SemanticVector obj) {
		context = (SemanticVector) obj.clone();
	}
	
	@Override
	public Object clone(){
		GPTreeStructSemantic t = (GPTreeStructSemantic) super.lightClone();
		t.context = new SemanticVector(this.context);
    	return t;
	}
	
	public void record_semantic(int dataindex, double [] registers, int datanum) {
		if(context == null) {
			context = new SemanticVector(datanum, registers.length);
		}
		
		context.setSemByRegister(dataindex, registers);
	}
}
