package zhixing.cpxInd.algorithm.Grammar.grammarrules;

import java.util.Vector;

public class TypeConstraintLib {
	//the code of type constraint is not completed
	public String name;
	
	public String typevec_name;
	
	public int vecdimen;   //dimension of the type vector in this library
	
	public Vector<String> typevec_proto = new Vector();
	
	public Vector<TypeConstraint> lib = new Vector();
	
	@Override
    public boolean equals(Object o) {
		if (o == this) {
            return true;
        }
		if (!(o instanceof TypeConstraintLib)) {
            return false;
        }
		return this.name.equals(((TypeConstraintLib)o).name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
