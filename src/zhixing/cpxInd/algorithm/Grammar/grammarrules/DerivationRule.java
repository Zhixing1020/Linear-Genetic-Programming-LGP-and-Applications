package zhixing.cpxInd.algorithm.Grammar.grammarrules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.spiderland.Psh.intStack;

public class DerivationRule {
	//record how to transfer parameters from parents to siblings by the name of parameters.
	
	//a pair of <...> or a pair of (...) can define a derivation rule, "instruction" and "composite", respectively.
	
	public final static String INSTRUCTION = "instr";
	public final static String INSTR_l = "<";
	public final static String INSTR_r = ">";
	public final static String COMPOSITE = "composite"; //this kind of derivation rules is formed by multiple sub derivation rules
	public final static String COMPO_l = "(";
	public final static String COMPO_r = ")";
		
	public String moduleName=null;
	
	public ArrayList<String> param_name = new ArrayList<>();  //must match the name and the index of the param in the corresponding module
	
	public ArrayList<String> param_pronoun = new ArrayList<>(); //it directly records the string of the pronoun
	
	public ArrayList<Set<String>> param_value = new ArrayList<>(); //the actual values of each param
	
	public String booleanCheck=null;  //type constraint checking or simple set operations
	
	public int maxrepeatnum=1;   //default maximum repeat times of this item 
	
	public ArrayList<DerivationRule> sub_rules = new ArrayList<>();   //the sub-rules of composite derivation rules
	
	
	public void setBooleanCheck(String check) {
		
	}
	
	public void defineDerivationType(String der) {
		
	}
	
	@Override
	public Object clone() {
		DerivationRule object = new DerivationRule();
		
		copyProperties(object);
		
		return object;
	}
	
	public Object lightClone() {
		DerivationRule object = new DerivationRule();
		
		copyProperties(object);
		
		return object;
	}
	
	protected void copyProperties(DerivationRule object) {
		//this function copies the properties of derivation rules in cloning
		object.moduleName = this.moduleName;
		
		object.param_name = new ArrayList<>();
		for(String p : this.param_name) {
			object.param_name.add(p);
		}
		
		object.param_pronoun = new ArrayList<>();
		for(String p : this.param_pronoun) {
			object.param_pronoun.add(p);
		}
		
		object.param_value = new ArrayList<>();
		for(Set<String> psSet : this.param_value) {
			
			Set<String> tmpSet = new HashSet<>();
			for (String s : psSet) {
				tmpSet.add(s);
			}
			
			object.param_value.add(tmpSet);
		}
		
		object.booleanCheck = this.booleanCheck;
		object.maxrepeatnum = this.maxrepeatnum;
		
		object.sub_rules = new ArrayList<>();
		for(DerivationRule rule : this.sub_rules) {
			object.sub_rules.add((DerivationRule) rule.clone());
		}
	}
	
	public boolean compatiableto(Object another) {
		if(another.getClass() != this.getClass()) {
			return false;
		}
		else if(this == another) {
			return true;
		}
		
		DerivationRule tmp = (DerivationRule) another;
		if(this.moduleName.equals(tmp.moduleName) && this.maxrepeatnum == tmp.maxrepeatnum) {
			
			if(this.booleanCheck != null && tmp.booleanCheck != null) {
				if(!this.booleanCheck.equals(tmp.booleanCheck)) return false;
			}
			if( (this.booleanCheck != null && tmp.booleanCheck == null) 
					|| (this.booleanCheck == null && tmp.booleanCheck != null) ) return false;
			
			if(this.moduleName.equals(COMPOSITE)) { //if it is a composite derivation rule
				//check the sub rules
				if(this.sub_rules.size()!=tmp.sub_rules.size()) return false;
				
				boolean comb = true;
				
				for(int j = 0; j<this.sub_rules.size(); j++) {
					comb = comb && this.sub_rules.get(j).compatiableto(tmp.sub_rules.get(j));
				}
				
				return comb;
			}
			else {//they are self-defined module or INSTRUCTION
				//check the pronoun and parameter values, the paremeter settings may be different
				//they may have different pronoun expression or predefined values
				
				//check the param_pronoun
				for(int j = 0; j<this.param_pronoun.size(); j++) {
					if( (this.param_pronoun.get(j)!=null && tmp.param_pronoun.get(j)!=null 
							&& ! this.param_pronoun.get(j).equals(tmp.param_pronoun.get(j)))
							|| (this.param_pronoun.get(j)!=null && tmp.param_pronoun.get(j)==null)
							|| (this.param_pronoun.get(j)==null && tmp.param_pronoun.get(j)!=null)
							 ) {
						return false;
					}
					
					if(this.param_pronoun.get(j)==null && tmp.param_pronoun.get(j)==null) {
						//if param_pronoun[j] == null, it is likely the parameter has predefined values, so we further check the param_value
						if(this.param_value.get(j)!=null && tmp.param_value.get(j)!=null) {
							if(this.param_value.get(j).size() != tmp.param_value.get(j).size()) return false;
						
							if(!this.param_value.get(j).containsAll(tmp.param_value.get(j))) return false;
						}
						if((this.param_value.get(j)==null && tmp.param_value.get(j)==null)) {
							System.err.println("We find a derivation rule with null param_pronoun and null param_value: " + this.moduleName);
							System.exit(1);
						}
					}
				}
				
				return true;
			}
		}
		return false;
	}
}
