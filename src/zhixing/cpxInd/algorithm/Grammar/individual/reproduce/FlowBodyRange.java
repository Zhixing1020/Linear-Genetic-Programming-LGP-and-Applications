package zhixing.cpxInd.algorithm.Grammar.individual.reproduce;

import zhixing.cpxInd.individual.primitive.Branching;
import zhixing.cpxInd.individual.primitive.IFLargerThan;
import zhixing.cpxInd.individual.primitive.IFLessEqual;

public class FlowBodyRange {
	public double upperbound;
	public double lowerbound;
	
	public FlowBodyRange() {
		this.upperbound = 1.;
		this.lowerbound = 0.;
	}
	
	public FlowBodyRange(Branching op, double cond) {
		if(op instanceof IFLargerThan) {
			this.upperbound = 1.;
			this.lowerbound = cond + 1e-7;
		}
		if(op instanceof IFLessEqual) {
			this.upperbound = cond;
			this.lowerbound = 0.;
		}
	}
	
	public FlowBodyRange interset(FlowBodyRange obj) {
		FlowBodyRange res = this.clone();
		
		res.upperbound = Math.min(res.upperbound, obj.lowerbound);
		res.lowerbound = Math.max(res.lowerbound, res.lowerbound);
		
		return res;
	}
	
	public boolean isEmpty() {
		if(upperbound < lowerbound) return true;
		
		return false;
	}
	
	public boolean isContain(FlowBodyRange obj) {
		if(upperbound >= obj.upperbound && lowerbound <= obj.lowerbound) return true;
		
		return false;
	}
	
	public FlowBodyRange clone() {
		FlowBodyRange res = new FlowBodyRange();
		res.upperbound = this.upperbound;
		res.lowerbound = this.lowerbound;
		return res;
	}
	
	public String toString() {
		return "["+lowerbound+","+upperbound+"]";
	}
}
