package zhixing.cpxInd.algorithm.Grammar.individual;

//mimic the ec.gp.GPNodeGatherer

import java.io.Serializable;


public class DTNodeGatherer implements Serializable {

	public DTNode node;
	
	public boolean test(final DTNode thisNode) { return true; }
}
