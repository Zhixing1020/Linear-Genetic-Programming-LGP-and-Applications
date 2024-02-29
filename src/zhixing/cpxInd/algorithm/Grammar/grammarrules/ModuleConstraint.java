package zhixing.cpxInd.algorithm.Grammar.grammarrules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

public class ModuleConstraint {
	//each has 1) a name, 2) parameter list, 3) a list of derivation rules
	
	//derivation rules record how to pass the parameters, how to connect modules, 
	
	public String name; //name of this module
	
	public ArrayList<String> param_name = new ArrayList(); //parameter names
	
	public ArrayList<Set<String>> param_values = new ArrayList(); //parameter values or pronouns
	
	public ArrayList<ArrayList<DerivationRule>> derivation_siblings = new ArrayList(); //each item is a sequence derivation rule
	
	
	public Object clone(){
		//clone name, param_name, and derivation_rules
		Object obj=new Object();
		
		return obj;
	}
	
	public void setParamValues(){
		//called by DerivationRule
	}
	
	public void defineModuleconstraint(String name, String derivs, ModuleConstraintLib lib, Grammarrules gram) {
		Vector<String> lines_v = new Vector();
		String[] lines;
		int beginInd = 0, endInd = 1;
		
		//get name and parameters
		lines = name.split(Grammarrules.MODULEPARAMS_l + "|" + Grammarrules.MODULEPARAMS_r);
		
		this.name = lines[0];
		if(lines.length>1) {
			String [] params = lines[1].split(Grammarrules.MODULEPARAMS_sep); //separate the parameter names based on Grammarrules.MODULEPARAMS_sep
			for(String p : params) {
				param_name.add(p);
			}
		}
				
		//get derivation rules
		
		for(;endInd<derivs.length();endInd++) {
			if(endInd == derivs.length()-1) {
				lines_v.add(derivs.substring(beginInd, endInd+1));
			}
			else if(derivs.charAt(endInd)=='|') {
				if(endInd+1<derivs.length() && derivs.charAt(endInd+1)=='|') {
					endInd++;
					continue;
				}
				
				lines_v.add(derivs.substring(beginInd, endInd));
				beginInd = ++endInd;
			}
		}
		
		//each line is a sequence of derivation rule. 
		for(String s : lines_v) {
			ArrayList<DerivationRule> derruleseq = new ArrayList();
			
			readDerivationRule_seq(s, derruleseq, lib, gram);
			
			derivation_siblings.add(derruleseq);
		}
		
	}
	
	public void readDerivationRule_seq(String ruleseq, ArrayList<DerivationRule> derruleseq, ModuleConstraintLib lib, Grammarrules gram) {
		//this function reads the derviation rule sequences. The derivation rule sequences are defined by  Grammarrules.CONCATENATE
		int beginInd = 0, endInd = 1; //beginInd and endInd are the character indexes of sub-strings, 
		Vector<String> subrules = new Vector();
		
		for(;endInd<ruleseq.length();endInd++) {
			
			DerivationRule tmp = null;
			
			//there are three types of 
			
			if(ruleseq.substring(beginInd, beginInd+1).equals(DerivationRule.INSTR_l)) {
				
				//find Grammarrules.CONCATENATE,  because there may be boolean check and repeat number
				while(endInd < ruleseq.length() && !ruleseq.substring(endInd).startsWith(Grammarrules.CONCATENATE)) {
					endInd++;
				}
				
				String sr = ruleseq.substring(beginInd, endInd);
				
				tmp = readInstrDerivationRule(sr, gram);
			}
			else if(ruleseq.substring(beginInd, beginInd+1).equals(DerivationRule.COMPO_l)) {
				
				Stack<Integer> comp_stk = new Stack();
				
				comp_stk.push(beginInd);
				//find the complete composite sub string
				while(endInd < ruleseq.length() && !comp_stk.isEmpty()) {
					if(ruleseq.substring(endInd, endInd+1).equals(DerivationRule.COMPO_r)) {
						comp_stk.pop();
					}
					if(ruleseq.substring(endInd, endInd+1).equals(DerivationRule.COMPO_l)) {
						comp_stk.push(endInd);
					}
					endInd++;
				}
				
				//find the boolean check and repeat number
				while(endInd < ruleseq.length() && !ruleseq.substring(endInd).startsWith(Grammarrules.CONCATENATE)) {
					endInd++;
				}
				
				//subrules.add(ruleseq.substring(beginInd, ++endInd));
				
				//remove DerivationRule.COMPO_l and DerivationRule.COMPO_r
				String sr = ruleseq.substring(beginInd, endInd);
				
				tmp = readCompDerivationRule(sr, lib, gram);

			}
			else if(ruleseq.substring(beginInd, beginInd+1).matches("[a-zA-Z0-9]")) { //it is a self-defined module
				
				while(endInd < ruleseq.length() && !ruleseq.substring(endInd).startsWith(Grammarrules.CONCATENATE)) {
					endInd++;
				}
				String sr = ruleseq.substring(beginInd, endInd);
				
				tmp = readModuleDerivationRule(sr, lib, gram);
			}
			else {
				System.err.println("unknow command in ModuleConstraint: " + this.name);
				System.exit(1);
			}
			
			if(tmp != null) derruleseq.add(tmp);
			else {
				System.err.println("got an empty derivation rule in " + ruleseq);
				System.exit(1);
			}
			
			beginInd = endInd+2;
			endInd = beginInd; //the for loop will increase endInd by 1
		}
		
	}
	
	private DerivationRule readInstrDerivationRule(String cake, Grammarrules gram) {
		DerivationRule tmp = new DerivationRule();
		tmp.moduleName=DerivationRule.INSTRUCTION;
		
		//get repeat number
		String[] subcake0 = cake.split(Grammarrules.REPEAT_reg);
		if(subcake0.length>1) {
			try {
				tmp.maxrepeatnum = Integer.parseInt(subcake0[1]);
	        }
	        catch (NumberFormatException e) {
	  
	            // This is thrown when the String
	            // contains characters other than digits
	            System.out.println("Invalid integer number in "+cake);
	        }
			
		}
		else if(cake.endsWith(Grammarrules.REPEAT)) {
			tmp.maxrepeatnum = Grammarrules.MAXREPEAT; 
		}
		
		//get boolean check
		String[] subcake1 = subcake0[0].split(Grammarrules.BOOLEANCHECK_l_reg + "|" + Grammarrules.BOOLEANCHECK_r_reg);
		if(subcake1.length>1) {
			tmp.booleanCheck = subcake1[1];
		}

		String[] elements = subcake1[0].split(DerivationRule.INSTR_l+"|"+DerivationRule.INSTR_r+"|"+Grammarrules.FORMULATE_sep);
		
		//the format of instruction are fixed. 1: destination, 2: function, 3+: source
		//We define the default format of the parameters of instruction modules below
		tmp.param_name = new ArrayList(elements.length-1);
		tmp.param_pronoun = new ArrayList(elements.length-1);
		tmp.param_value = new ArrayList(elements.length-1);
		
		for(int i = 1;i<elements.length;i++) {
			tmp.param_name.add(null);
			tmp.param_pronoun.add(null);
			tmp.param_value.add(new HashSet<String>());
			
			if(i == 1) {	
				tmp.param_name.set(i-1,"destination");

				if(elements[i].startsWith(Grammarrules.SET_l.substring(1))&&elements[i].endsWith(Grammarrules.SET_r.substring(1))) {
					String[] vals = elements[i].split(Grammarrules.SET_sep+"|"+Grammarrules.SET_l+"|"+Grammarrules.SET_r);
					Set<String> v_set = new HashSet();
					for(String v : vals) {
						recursiveGetSet(v, v_set, gram);
					}
					
					tmp.param_value.set(i-1, v_set);
				}
				else {
					tmp.param_pronoun.set(i-1,elements[i]);
				}
			}
			else if(i == 2) {
				tmp.param_name.set(i-1,"function");
				
				if(elements[i].startsWith(Grammarrules.SET_l.substring(1))&&elements[i].endsWith(Grammarrules.SET_r.substring(1))) {
					String[] vals = elements[i].split(Grammarrules.SET_sep+"|"+Grammarrules.SET_l+"|"+Grammarrules.SET_r);
					Set<String> v_set = new HashSet();
					for(String v : vals) {
						recursiveGetSet(v, v_set, gram);
							
					}
					
					tmp.param_value.set(i-1, v_set);
				}
				else {
					tmp.param_pronoun.set(i-1,elements[i]);
				}
			}
			else {
				tmp.param_name.set(i-1,"source_"+(i-2));
				
				if(elements[i].startsWith(Grammarrules.SET_l.substring(1))&&elements[i].endsWith(Grammarrules.SET_r.substring(1))) {
					String[] vals = elements[i].split(Grammarrules.SET_sep+"|"+Grammarrules.SET_l+"|"+Grammarrules.SET_r);
					Set<String> v_set = new HashSet();
					for(String v : vals) {
						recursiveGetSet(v, v_set, gram);
							
					}
					
					tmp.param_value.set(i-1, v_set);
				}
				else {
					tmp.param_pronoun.set(i-1,elements[i]);
				}
				
			}
		}
		
		return tmp;
	}
	
	private DerivationRule readCompDerivationRule(String cake, ModuleConstraintLib lib, Grammarrules gram) {
		DerivationRule tmp = new DerivationRule();
		tmp.moduleName=DerivationRule.COMPOSITE;

		int beginInd = 0, endInd = 1;
		Stack<Integer> comp_stk = new Stack();
		
		comp_stk.push(beginInd);
		//find the complete composite sub string
		while(endInd < cake.length() && !comp_stk.isEmpty()) {
			if(cake.substring(endInd, endInd+1).equals(DerivationRule.COMPO_r)) {
				comp_stk.pop();
			}
			if(cake.substring(endInd, endInd+1).equals(DerivationRule.COMPO_l)) {
				comp_stk.push(endInd);
			}
			endInd++;
		}
		String sub_rules_string = cake.substring(beginInd+1, endInd-1);//remove the outest DerivationRule.COMPO_l and DerivationRule.COMPO_r
		
		if(endInd<cake.length()) { //there are boolean check or repeat number
			//get repeat number
			String cake2 = cake.substring(endInd);

//			String[] subcake0 = cake2.split(Grammarrules.REPEAT);
			
			int cut = cake2.indexOf(Grammarrules.REPEAT);
			if(cut>=0) {
				tmp.maxrepeatnum = Grammarrules.MAXREPEAT;
				
				if(!cake2.endsWith(Grammarrules.REPEAT)) {
					String repeatnum = cake2.substring(cut+1);
					try {
						tmp.maxrepeatnum = Integer.parseInt(repeatnum);
			        }
			        catch (NumberFormatException e) {
			  
			            // This is thrown when the String
			            // contains characters other than digits
			            System.out.println("Invalid integer number in "+cake);
			        }
				}
				
			}
			
			if(cut<0) cut = cake2.length();
			
			String[] subcake1 = cake2.substring(0, cut).split(Grammarrules.BOOLEANCHECK_l_reg + "|" + Grammarrules.BOOLEANCHECK_r_reg);
			if(subcake1.length>1 && subcake1[1].length()>1) {
				tmp.booleanCheck = subcake1[1];
			}


		}
		

//		String[] elements = subcake1[0].split(DerivationRule.COMPO_l+"|"+DerivationRule.COMPO_r+"|"+Grammarrules.FORMULATE_sep);
		
//		String sub_rules_string = subcake1[0].substring(1, subcake1[0].length()-1);//remove the outest DerivationRule.COMPO_l and DerivationRule.COMPO_r
		
		tmp.sub_rules = new ArrayList();
		
		readDerivationRule_seq(sub_rules_string, tmp.sub_rules, lib, gram); //recursively put the "sub_rules_string" in the composite module into tmp.sub_rules
				
		return tmp;
	}
	
	private DerivationRule readModuleDerivationRule(String cake, ModuleConstraintLib lib, Grammarrules gram) {
		DerivationRule tmp = new DerivationRule();
		
		//get repeat number
		String[] subcake0 = cake.split(Grammarrules.REPEAT_reg);
		if(subcake0.length>1) {
			try {
				tmp.maxrepeatnum = Integer.parseInt(subcake0[1]);
	        }
	        catch (NumberFormatException e) {
	  
	            // This is thrown when the String
	            // contains characters other than digits
	            System.out.println("Invalid integer number in "+cake);
	        }
			
		}
		else if(cake.endsWith(Grammarrules.REPEAT)) {
			tmp.maxrepeatnum = Grammarrules.MAXREPEAT; 
		}
		
		//get boolean check
		String[] subcake1 = subcake0[0].split(Grammarrules.BOOLEANCHECK_l_reg + "|" + Grammarrules.BOOLEANCHECK_r_reg);
		if(subcake1.length>1) {
			tmp.booleanCheck = subcake1[1];
		}
		
		//get module name
		String[] subcake2 = subcake1[0].split(Grammarrules.MODULEPARAMS_l + "|" + Grammarrules.MODULEPARAMS_r);
		tmp.moduleName = subcake2[0];
		//check the existence of the module
		boolean exists = false;
		ModuleConstraint m = null;
		for(int i = 0; i < lib.lib.size(); i++) {
			m = lib.lib.get(i);
			if(m.name.equals(tmp.moduleName)) {
				exists = true;
				break;
			}
		}
		if(!exists && !this.name.equals(tmp.moduleName)) {
			System.err.println("undefined module constraint name: "+ tmp.moduleName + " in "+lib.name);
			System.exit(1);
		}
		if(this.name.equals(tmp.moduleName)) {
			m.param_name = this.param_name;
		}
		
		//set the parameters
		String[] elements=null;
		if(subcake2.length > 1) {
			elements = subcake2[1].split(Grammarrules.FORMULATE_sep);
		}
//		else if(!m.name.equals(Grammarrules.PROGRAM)) {
//			System.err.println("there is a no-parameter module except PROGRAM: "+ tmp.moduleName + " in "+lib.name);
//			System.exit(1);
//		}
		
//		tmp.param_name = new ArrayList(elements.length);
//		tmp.param_pronoun = new ArrayList(elements.length);
//		tmp.param_value = new ArrayList(elements.length);
		
		tmp.param_name = new ArrayList();
		tmp.param_pronoun = new ArrayList();
		tmp.param_value = new ArrayList();
		
		if(elements != null)
		for(int i = 0;i<elements.length;i++) {
			tmp.param_name.add(new String(m.param_name.get(i)));
			tmp.param_pronoun.add(null);
			tmp.param_value.add(new HashSet<String>());
		}
		
		if(elements != null)
		for(int i = 0;i<elements.length;i++) {
			String[] key_val = elements[i].split(Grammarrules.SETASSIGN);
			
			int ind = tmp.param_name.indexOf(key_val[0]);
			
			if(key_val[1].startsWith(Grammarrules.SET_l.substring(1))&&key_val[1].endsWith(Grammarrules.SET_r.substring(1))) {
				String[] vals = key_val[1].split(Grammarrules.SET_sep+"|"+Grammarrules.SET_l+"|"+Grammarrules.SET_r);
				Set<String> v_set = new HashSet();
				for(String v : vals) {
					recursiveGetSet(v, v_set, gram);
				}
				
				tmp.param_value.set(ind, v_set);
			}
			else {
				tmp.param_pronoun.set(ind,key_val[1]);
			}
		}
		
		
		return tmp;
	}
	
	private void recursiveGetSet(String element, Set<String> v_set, Grammarrules gram) {
		//the attribute set can be defined recursively. This function helps to find the recursive definition.
		if(!element.isEmpty()) {
			//check whether v is an existing set
			AttributeSet as = null;
			for(AttributeSet a : gram.attributes) {
				if(a.name.equals(element)) {
					as = a;
					break;
				}
			}
			
			if(as != null) {
				for(String s : as.values) {
					recursiveGetSet(s, v_set, gram);
				}
			}
			else {
				//if no, it should be a concrete value
				v_set.add(element);
			}
			
		}
	}
	
	@Override
    public boolean equals(Object o) {
		if (o == this) {
            return true;
        }
		if (!(o instanceof ModuleConstraint)) {
            return false;
        }
		return this.name.equals(((ModuleConstraint)o).name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
