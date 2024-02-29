package zhixing.cpxInd.algorithm.Grammar.grammarrules;

import java.util.HashSet;
import java.util.Set;

public class AttributeSet{

	public String name;
	
	public Set<String> values = new HashSet();
	
	@Override
    public boolean equals(Object o) {
		if (o == this) {
            return true;
        }
		if (!(o instanceof AttributeSet)) {
            return false;
        }
		return this.name.equals(((AttributeSet)o).name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
