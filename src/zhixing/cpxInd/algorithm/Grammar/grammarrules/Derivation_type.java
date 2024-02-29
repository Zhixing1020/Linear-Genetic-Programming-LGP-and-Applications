package zhixing.cpxInd.algorithm.Grammar.grammarrules;

import java.util.Vector;

public class Derivation_type {
	//define how type vectors of the registers change
	public Vector<String> input_type = new Vector();
	public String output_type;
	public String booleanCheck = null;
	
	//accept the type vector of each source register
	//check whether the type vector satisfy the booleanCheck
	//give the type vector of output register
	
	public void defineDerivationType(String der) {
		
		Vector<String> lines = new Vector();
		
		//input argument types
		int beginInd = 0, endInd = der.indexOf(Grammarrules.ARGMAP);
		
		if(endInd==-1) {
			System.err.println("cannot find "+Grammarrules.ARGMAP+" when defining derivation type");
			System.exit(1);
		}
		
		String inputs = der.substring(beginInd, endInd);
		String[] inputs_arg = inputs.split(Grammarrules.ARGCONCAT);
		
		for(String in : inputs_arg) {
			input_type.add(in);
		}
		
		//output argument types
		beginInd = der.indexOf(Grammarrules.ARGMAP)+2;
		endInd = der.length();
		if(der.indexOf(Grammarrules.BOOLEANCHECK_l) != -1) {
			endInd = der.indexOf(Grammarrules.BOOLEANCHECK_l);
			
			//boolean check
			int beginbool = endInd+1;
			int endbool = der.indexOf(Grammarrules.BOOLEANCHECK_r);
			booleanCheck = der.substring(beginbool, endbool);
		}
		output_type = der.substring(beginInd, endInd);
		
	}
}
