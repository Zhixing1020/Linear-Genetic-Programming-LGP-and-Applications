package zhixing.cpxInd.algorithm.Grammar.grammarrules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.Vector;

//import ec.EvolutionState;
//import ec.util.Parameter;
//import zhixing.cpxInd.individual.LGPDefaults;

public class Grammarrules {
	//a dynamic space for predefined sets
	//type constraints
	
	public final static String DEFINESET = "defset";
	public final static String DEFINETYPE = "deftype";
	public final static String COMMENT = "#";
	public final static String BEGIN = "begin";
	public final static String END = "end";
	public final static String TYPECONSTRAINT = "typec";
	public final static String MODULECONSTRAINT = "modulec";
	public final static String TERMINATION = ";";
	public final static String INDEX = "_";
	public final static String PROGRAM = "PROGRAM"; //starting point of the derivation tree
	public final static String MAINBODY = "arith";  //this key word has been removed
	public final static String COMMA = ",";
	public final static String NOCONSTRAINT = "NOCONSTRAINT";  
	
	public final static String REGULARASSIGN = "::=";  //define derivation rules
	public final static String SEPARATE = "\\|";   //or in BNF form
	public final static String CONCATENATE = "::"; //sequential combination
	public final static String BOOLEANCHECK_l = "[";  //[...]: boolean check
	public final static String BOOLEANCHECK_r = "]";
	public final static String BOOLEANCHECK_l_reg = "\\[";  
	public final static String BOOLEANCHECK_r_reg = "\\]";
	public final static String REPEAT_reg = "\\*";  //*: repeatively recall and at least one recall. if followed by an integer K, the item maximally repeats K times.
	public final static String REPEAT = "*"; //the suffix of "_reg" indicates a regular representation. Some JAVA apis need this kind of representation as arguments.
	public final static int MAXREPEAT = 50;
	public final static String FORMULATE_sep = "\\\\"; //this is the regular expression of "\"
	
	public final static String BOOLAND = "&&";
	public final static String BOOLOR = "\\|\\|";
	
	
	public final static String ARGUMENTTYPE = "T";
	public final static String TYPEVEC_l = "\\(";
	public final static String TYPEVEC_r = "\\)";
	public final static String TYPEVEC_sep = ",";
	public final static String ARGCONCAT = COMMA;
	public final static String ARGMAP = "->";
	public final static String VECTOREQUAL = "=";
	public final static String VECTORADD = "\\+";
	public final static String VECTORSUB = "-";
	public final static String VECTORDIV = "/";
	public final static String VECTORMUL = "\\*";
	public final static String ZEROS = "zeros";
	
	public final static String SET_l = "\\{";
	public final static String SET_r = "\\}";
	public final static String SET_sep = COMMA;
	public final static String MODULEPARAMS_l = "\\(";
	public final static String MODULEPARAMS_r = "\\)";
	public final static String MODULEPARAMS_sep = FORMULATE_sep;
	public final static String EMPTYSET = "EMP";
	public final static String UNIONSET = "\\+";
	public final static String INTERSECTION = "\\^";
	public final static String DIFFERENCE = "-";
	public final static String EQUIVALENCE = "=";
	//public final static String SUBSET = "<";
	public final static String SETASSIGN = "~";
	public final static String SIZE = "\\.size";
	public final static String GET_l = "\\.get(";
	public final static String GET_r = "\\)";
	
	public Vector<AttributeSet> attributes = new Vector();
	public Vector<ModuleConstraintLib> moduleconLibs = new Vector();
	public Vector<TypeConstraintLib> typeconLibs = new Vector();
	
	
	public void readGrammarrules(String filepath) {
		
		String line;
		
		File f = new File(filepath);
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			
			while ((line = br.readLine())!=null) {
				
				if(line.startsWith(COMMENT)||line.isBlank()) continue;
				
				if(line.startsWith(DEFINESET)) {
					defineAttributeSet(br, line);
				}
				else if (line.startsWith(BEGIN+" "+TYPECONSTRAINT)) {
					defineTypeconLib(br, line);
				}
				else if (line.startsWith(BEGIN+" "+MODULECONSTRAINT)) {
					defineModuleLib(br, line);
				}
			}
			
			//define sets
			
		} catch (IOException e)  {
			System.out.print("reading grammar error\n");
			e.printStackTrace();
		}
		
	}
	
	protected void defineAttributeSet(BufferedReader br, String line) {
		
		//remove \n
		line = removeLineBreak(br,line);
		
		String [] line2 = line.split("[\\s"+TERMINATION+"]");
		
		AttributeSet as = new AttributeSet();
		
		as.name = line2[1]; //get the name of this attribute set.
		
		String[] vals = line2[2].split("["+SET_l+SET_r+SET_sep+"]");
		
		for(String v : vals) {
			if(!v.isBlank()) {
				//check whether it is an existing set
				boolean exists = false;
				for(AttributeSet t : this.attributes) {
					if(t.name.equals(v)) { //put all the elements in t into as
						for(String v2 : t.values) {
							as.values.add(v2);
						}
						exists = true;
					}
				}
				
				if(!exists) {
					as.values.add(v);
				}
			}
				
		}
		
		if(!this.attributes.contains(as)) {
			this.attributes.add(as);
		}
		else {
			System.err.print("duplicated definition on set "+as.name+"\n");
			System.exit(1);
		}
	}
	
	protected void defineTypeconLib(BufferedReader br, String line) {
		//================the type constraint-related code is incomplete============================
		TypeConstraintLib tcl = new TypeConstraintLib();
		
		String[] line2 = line.split("\\s");
		
		tcl.name = line2[1];
		
		//check whether the type constraint name has been defined
		if(this.typeconLibs.contains(tcl)) {
			System.err.println("duplicated definition on the type constraint library name: "+ tcl.name);
			System.exit(1);
		}
		
		try{
			
			while ((line = br.readLine())!=null) {
				
				if(line.startsWith(COMMENT)||line.isBlank()) continue;
				
				if(line.startsWith(DEFINETYPE)) {
					//define type vector
					line = removeLineBreak(br,line);
					
					line2 = line.split("[\\s"+TERMINATION+"]");
					
					tcl.typevec_name = line2[1];
					
					String[] vals = line2[2].split("["+TYPEVEC_l+TYPEVEC_r+TYPEVEC_sep+"]");
					
					for(String v : vals) {
						if(!v.isBlank()) {
							//check whether it is an existing set
							boolean exists = false;
							for(AttributeSet t : this.attributes) {
								if(t.name.equals(v)) { //put all the elements in t into as
									exists = true;
									break;
								}
							}
							
							if(exists) {
								tcl.typevec_proto.add(v);
							}
							else {
								System.err.println("we get an undefined attribute "+v);
								System.exit(1);
							}
						}
						
					}
					tcl.vecdimen = tcl.typevec_proto.size();
					
				}
				else if(line.startsWith(END)){
					break;
				}
				else {//the grammar file tries to define some type derivation rules
					//1. check that the type vector has been defined
					if(tcl.typevec_proto.isEmpty()) {
						System.err.print("miss a type definition\n");
						System.exit(1);
					}
					
					//2. check that the derivation rule contains REGULARASSIGN
					line = removeLineBreak(br,line);
					line = line.replaceAll("\\s", "");
					if(line.indexOf(REGULARASSIGN)==-1 || line.indexOf(TERMINATION)==-1) {
						System.err.println("cannot find "+ REGULARASSIGN + " or " + TERMINATION + " in type constraint: " + line);
						System.exit(1);
					}
					line2 = line.split(REGULARASSIGN + "|" + TERMINATION);
					
					TypeConstraint tc = new TypeConstraint();
					
					tc.defineTypeconstraint(line2[0], line2[1]);
					
					if(tcl.lib.contains(tc)) {
						System.err.println("duplicated definition on the type constraint name: "+ tc.name + " in "+tcl.name);
						System.exit(1);
					}
					
					tcl.lib.add(tc);
				}
				
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		this.typeconLibs.add(tcl);
	}
	
	protected void defineModuleLib(BufferedReader br, String line) {
		
		ModuleConstraintLib mcl = new ModuleConstraintLib();
		
		String[] line2 = line.split("\\s");
		
		mcl.name = line2[1]; //get the name of the module constraint library
		
		//check whether the module constraint name has been defined
		if(this.moduleconLibs.contains(mcl)) {
			System.err.println("duplicated definition on the module constraint name: "+ mcl.name);
			System.exit(1);
		}
				
		try{
			
			while ((line = br.readLine())!=null) {
				
				if(line.startsWith(COMMENT)||line.isBlank()) continue;
				
				if(line.startsWith(END)){
					break;
				}
				else {//the grammar file tries to define some module derivation rules
					//check that the derivation rule contains REGULARASSIGN
					line = removeLineBreak(br,line);
					line = line.replaceAll("\\s", "");
					if(line.indexOf(REGULARASSIGN)==-1 || line.indexOf(TERMINATION)==-1) {
						System.err.println("cannot find "+ REGULARASSIGN + " or " + TERMINATION + " in module constraint: " +line);
						System.exit(1);
					}
					line2 = line.split(REGULARASSIGN + "|" + TERMINATION);
					
					ModuleConstraint mc = new ModuleConstraint();
					
					mc.defineModuleconstraint(line2[0], line2[1], mcl, this);
					
					if(mcl.lib.contains(mc)) {
						System.err.println("duplicated definition on the module constraint name: "+ mc.name + " in "+mcl.name);
						System.exit(1);
					}
					
					mcl.lib.add(mc);
				}
				
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		this.moduleconLibs.add(mcl);
	}
	
	private String removeLineBreak(BufferedReader br, String line) {
		while(!line.endsWith(TERMINATION)) { //there are some content lying on the next line
			String tmp;
			try {
				if((tmp = br.readLine())!=null) {
					line+=tmp;
				}
				else {
					System.err.print("grammar rule: "+line+" is not completed\n");
					System.exit(1);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return line;
	}
	
	public int genDerivationTree() {
		//return the number of generated instructions?
		
		return 0;
	}
	
	public ModuleConstraintLib getModuleConstraintLib(final String ModuleConLibName){
		ModuleConstraintLib res = null;
		for(ModuleConstraintLib mcl : moduleconLibs) {
			if(mcl.name.equals(ModuleConLibName)) {
				res = mcl;
			}
		}
		return res;
	}
	
	public ModuleConstraint getModuleConstraint(final String ModuleConLibName, final String ModuleConName){
		ModuleConstraint res = null;
		for(ModuleConstraintLib mcl : moduleconLibs){
			if(mcl.name.equals(ModuleConLibName)){
				res = mcl.getModuleConstraint(ModuleConName);
			}
		}
		
		if(res == null){
			System.err.print("cannot find the Module Constraint in " + ModuleConLibName + " based on " + ModuleConName);
			System.exit(1);
		}
		
		return res;
	}
}
