package zhixing.cpxInd.algorithm.Grammar.grammarrules;

import java.util.ArrayList;
import java.util.Vector;

public class TypeConstraint {
	//the code of type constraint is not completed
	public String name;
	
	public ArrayList<Derivation_type> derivate_types = new ArrayList(); //define how type vectors of the registers change
	
	public void defineTypeconstraint(String name, String derivs) {
		
		this.name = name;
		
		//String[] lines = derivs.split("("+gram.SEPARATE+")");
		
		Vector<String> lines = new Vector();
		int beginInd = 0, endInd = 1;
		for(;endInd<derivs.length();endInd++) {
			if(endInd == derivs.length()-1) {
				lines.add(derivs.substring(beginInd, endInd+1));
			}
			else if(derivs.charAt(endInd)=='|') {
				if(endInd+1<derivs.length() && derivs.charAt(endInd+1)=='|') {
					endInd++;
					continue;
				}
				
				lines.add(derivs.substring(beginInd, endInd));
				beginInd = endInd + 1;
				endInd++;
			}
		}
		
		for(String s : lines) {
			Derivation_type der = new Derivation_type();
			der.defineDerivationType(s);
			derivate_types.add(der);
		}
		
	}
	
	
	@Override
    public boolean equals(Object o) {
		if (o == this) {
            return true;
        }
		if (!(o instanceof TypeConstraint)) {
            return false;
        }
		return this.name.equals(((TypeConstraint)o).name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
