package zhixing.cpxInd.algorithm.Graphbased.individual;


public interface GraphAttributes {

	public int getDimension();
	
	public int getDimension_fun();
	
	public int [] getStartEnd_fun();
	
	public int getDimension_cons();
	
	public int [] getStartEnd_cons();
	
	public double[] getFrequency(int start,int end);
	
	public double[] getFrequency();
	
	public double[][] getAM(int start, int end);
	
	public double[][] getAM();
}
