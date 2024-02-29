package zhixing.cpxInd.algorithm.semantic.library;

import java.util.ArrayList;

public class SVSet extends ArrayList<SemanticVector>{
	
	private int NUMREF;
	
	public SVSet(int numref, int maxnumInput, int numReg) {
		super();
		clear();
		NUMREF = numref;
		for(int r = 0; r<NUMREF; r++) {
			add(new SemanticVector(maxnumInput, numReg));
		}
	}
	
	public SVSet(final SVSet obj) {
		super();
		assignfrom(obj);
	}
	
	public boolean isequalto(final SVSet obj) {
		for(int r = 0; r<NUMREF; r++) {
			if( this.get(r).sv_diff(obj.get(r)) > 0) return false;
		}
		return true;
	}
	
	public void assignfrom(final SVSet obj) {
		clear();
		NUMREF = obj.getNUMREF();
		for(int r = 0; r<NUMREF; r++) {
			add(new SemanticVector(obj.get(r)));
		}
	}
	
	public int getNUMREF() {
		return NUMREF;
	}
}
