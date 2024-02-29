package zhixing.cpxInd.algorithm.Grammar.grammarrules;

import java.util.Vector;

import org.spiderland.Psh.booleanStack;

public class ModuleConstraintLib {
	//a set of self-defined module constraints, each has 1) a name, 2) a list of module constraints
	
	public String name;
	
	public Vector<ModuleConstraint> lib = new Vector();
	
	
	@Override
    public boolean equals(Object o) {
		if (o == this) {
            return true;
        }
		if (!(o instanceof ModuleConstraintLib)) {
            return false;
        }
		return this.name.equals(((ModuleConstraintLib)o).name);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean isContains(String name) {
		boolean res = false;
		for(ModuleConstraint m : lib){
			if(m.name.equals(name)){
				res = true;
				break;
			}
		}
		return res;
	}
	
	public ModuleConstraint getModuleConstraint(final String name){
		ModuleConstraint res = null;
		for(ModuleConstraint m : lib){
			if(m.name.equals(name)){
				res = m;
			}
		}
		
		if(res == null){
			System.err.print("cannot find the Module Constraint " + name);
			System.exit(1);
		}
		
		return res;
	}
}
