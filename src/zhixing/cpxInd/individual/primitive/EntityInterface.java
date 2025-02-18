package zhixing.cpxInd.individual.primitive;

public interface EntityInterface {
	
	public double getAttributes(Object obj, Args args, int index, int bias);
	
	public String toGraphvizString();
}
