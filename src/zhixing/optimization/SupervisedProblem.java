package zhixing.optimization;

import java.util.ArrayList;

public interface SupervisedProblem {
	/* it is an interface for supervised learning problems. 
	 * note that src/zhixing/symbolicregression does not use this interface because SR has been implemented for a long time
	 * */
	
	int getDatanum();
	int getDatadim();
	int getOutputnum();
	int getOutputdim();
	int[] getTargets();
	int getTargetNum();
	double[] getDataMax();
	double[] getDataMin();
	ArrayList<Double[]> getData();
	ArrayList<Double[]> getDataOutput();
	double [] getX();
	int getX_index();
	void setX_index(int ind);
	boolean istraining();
}
