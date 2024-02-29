package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual;

import java.util.Vector;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.djss.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO4DJSS;

public abstract class LGPIndividual_MPMO extends LGPIndividual_MFEA{

private static final String P_SKILLFACTOR = "skillfactor";
	
	public static double INF_SCALAR_RANK = 1e10; 
	
	public Vector<Double> scalarRank_v = null;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state,base); 
		 Parameter def = defaultBase();
		 
		// the maximum/minimum number of trees
	     skillFactor = state.parameters.getInt(base.push(P_SKILLFACTOR),def.push(P_SKILLFACTOR),0);  
	     if (skillFactor < 0) 
	         state.output.fatal("An LGPIndividual_MPMO must be assigned to at least one specific task when setting up.",
	             base.push(P_SKILLFACTOR),def.push(P_SKILLFACTOR));
	     
	}
	
	public int getEffTreesLength(int taskid){
		updateStatus(taskid);
		int res = 0;
		for(GPTreeStruct tree : getTreelist()){
			if (tree.status){
				res ++;
			}
		}
		
		return res;
	}
	
	public void updateStatus(int taskid) {
		curOutReg = outputRegister4Sim.get(taskid);
		updateStatus(getTreelist().size(), new int[]{curOutReg}); 
	}
	
	public void setCurrentOutputRegister(int t){
		//set the current output for a specific task
		curOutReg = outputRegister4Sim.get(t);
		updateStatus(getTreelist().size(), new int[]{curOutReg}); 
	}
	
	protected void copyLGPproperties(LGPIndividual_MPMO4DJSS obj){
		super.copyLGPproperties(obj);
		this.scalarRank_v = new Vector<Double>();
		for(Double d : obj.scalarRank_v){
			this.scalarRank_v.add(d);
		}
	}
}
