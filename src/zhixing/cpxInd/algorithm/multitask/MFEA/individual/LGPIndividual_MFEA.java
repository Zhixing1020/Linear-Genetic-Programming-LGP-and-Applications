package zhixing.cpxInd.algorithm.multitask.MFEA.individual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import zhixing.cpxInd.individual.LGPIndividual;

public abstract class LGPIndividual_MFEA  extends LGPIndividual {

protected List<Integer> outputRegister4Sim = new ArrayList<>();
	
	protected int curOutReg = 0;
	public int skillFactor = -1;
	public double scalarRank = 100000;
	
	public void setOutputRegister(List<Integer> tar){
		//set the output registers for all tasks
		outputRegister4Sim.clear();
		Iterator<Integer> it = tar.iterator();
		while(it.hasNext()){
			int v = it.next();
			if(v >= getNumRegs()){
				System.err.println("the index of output registers is out of range: "+getNumRegs()+".");
                System.exit(1);
			}
			outputRegister4Sim.add(v);
		}
	}
	
	public void setCurrentOutputRegister(int t){
		//set the current output for a specific task
		curOutReg = outputRegister4Sim.get(t);
		updateStatus(getTreelist().size(), new int[]{curOutReg}); 
	}
	
	public int getCurrentOutputRegister(){
		return curOutReg;
	}
	
	public void updateStatus4CurrentOutput(){
		setCurrentOutputRegister(skillFactor);
		updateStatus(getTreelist().size(), new int[]{curOutReg}); 
	}
	
	public void resetStatusFromCurrentOutput(){
		//reset the effective status of all instructions
		
		updateStatus();
		//updateStatus(trees.size(),outputRegister);
	}
	
	@Override
	public void updateStatus() {
//		Set<Integer> tmp = new HashSet(outputRegister4Sim);
//		if(tmp.size() > outputRegister.length){
//			System.err.println("the size of outputRegister4Sim is larger than outputRegister in updateStatus.");
//            System.exit(1);
//		}
//		int i = 0;
//		for(Integer out: tmp)
//			{
//				if(out >= numRegs){
//					System.err.println("the index of output registers is out of range: "+numRegs+".");
//	                System.exit(1);
//				}
//				outputRegister[i++] = out;
//			}
		
		updateStatus(getTreelist().size(),getOutputRegister());
		//updateStatus4CurrentOutput();
	}
	
	protected void copyLGPproperties(LGPIndividual_MFEA obj){
		super.copyLGPproperties(obj);
		this.skillFactor = obj.skillFactor;
		this.scalarRank = obj.scalarRank;
	}
}
