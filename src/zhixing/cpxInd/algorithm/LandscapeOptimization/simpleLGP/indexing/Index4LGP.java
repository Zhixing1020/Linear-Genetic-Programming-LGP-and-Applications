package zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Index;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.cpxInd.individual.primitive.FlowOperator;

public class Index4LGP extends Index<GPTreeStruct>{
	
	public static final String INDEX4LGP = "index4LGP";
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = new Parameter (INDEX4LGP);
		
		sym_prototype = (GPTreeStruct) state.parameters.getInstanceForParameter(base.push(SYMBOL_PROTO), def.push(SYMBOL_PROTO), GPTree.class);
		sym_prototype.setup(state, base.push(SYMBOL_PROTO));
		
	}
	
	@Override
	public boolean isduplicated(GPTreeStruct newsym) {
		
		boolean res = false;
		
//		for(GPTreeStruct item : symbols)
		if(!symbols.isEmpty())
		{
			GPTreeStruct item = symbols.get(0); //the transitive property of "equal"
			//genotype
//			if(item.toString().equals(newsym.toString())) {
//				return true;
//			}
			if(is_duplicate_instrs(item.child, newsym.child)) {
				return true;
			}
			
			//phenotype
			if(item.child.depth() == newsym.child.depth() && item.child.depth() == 3) {
				String [] item_comp = new String [4];
				String [] newsym_comp = new String [4];
				item_comp[0] = item.child.toString();
				item_comp[1] = item.child.children[0].toString();
				item_comp[2] = item.child.children[0].children[0].toString();
				if(item.child.children[0].children.length > 1)
				item_comp[3] = item.child.children[0].children[1].toString();
				
				newsym_comp[0] = newsym.child.toString();
				newsym_comp[1] = newsym.child.children[0].toString();
				newsym_comp[2] = newsym.child.children[0].children[0].toString();
				if(newsym.child.children[0].children.length > 1)
				newsym_comp[3] = newsym.child.children[0].children[1].toString();
				
				if(item_comp[0].equals(newsym_comp[0])) {
					if(item_comp[1].equals(newsym_comp[1])
							&& (item_comp[1].equals("+") || item_comp[1].equals("*") || item_comp[1].equals("max") || item_comp[1].equals("min") ) 
							) {
							if(item.child.children[0].children.length > 1 && newsym.child.children[0].children.length > 1 
									&& item_comp[2].equals(newsym_comp[3]) && item_comp[3].equals(newsym_comp[2])){
							
							return true;
						}
					}
					
				}
				
				if(item_comp[1].equals(newsym_comp[1]) 
						&& (item.child.children[0] instanceof FlowOperator)
						&& item_comp[2].equals(newsym_comp[2])
						&& item_comp[3].equals(newsym_comp[3])
						) { //Flow operators do not use the destination registers
					return true;
				}
			}
			
				
			
			//semantics
		}
		
		return res;
	}

	@Override
	public Object clone() {
		
		Index4LGP n = new Index4LGP();
		
		n.num_inputs = this.num_inputs;
		n.dim_inputs = this.dim_inputs;
		
		n.input_lb = this.input_lb;
		n.input_ub = this.input_ub;
		
		n.sym_prototype = this.sym_prototype;
		
		n.index = this.index;
		
		n.set_tabu_frequency(this.get_tabu_frequency());
		for(GPTreeStruct t : this.symbols) {
			n.symbols.add(t);
		}
		
		return n;
	}

	boolean is_duplicate_instrs(GPNode p1, GPNode p2) {
		
		//compare the two GPNodes
//		if(p1 instanceof Entity && p2 instanceof Entity) {
//			if(! p1.nodeEquivalentTo(p2)) {
//				return false;
//			}
//		}
//		else {
//			if(!p1.toString().equals(p2.toString())) {
//				return false;
//			}
//		}
		if(!p1.toString().equals(p2.toString())) {
			return false;
		}
		
		//compare the children
		int cn = p1.children.length;
		if(p2.children.length != cn) {
			return false;
		}
		
		boolean res = true;
		for(int c = 0; c < cn; c++) {
			res = res && is_duplicate_instrs(p1.children[c], p2.children[c]);
		}
		
		return res;
	}
}
