package zhixing.cpxInd.individual;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.commons.math3.util.Pair;

import ec.gp.GPNode;

public interface TGPInterface4Problem extends CpxGPInterface4Problem{

	default public ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(GPNode start_node){
		
		if(start_node == null){
			System.err.print("null GPNode in getAdjacencyTable() of TGPIndividual4MForm\n");
			System.exit(1);
		}
		
		ArrayList<Pair<String, ArrayList<String>>> res = new ArrayList<>();
		
		if(start_node.expectedChildren() == 0){
//			System.err.print("there is no adjacent table for terminals in TGPIndividual4MForm\n");
//			System.exit(1);
			return res;
		}
		
		
		
		LinkedList<GPNode> queue = new LinkedList<>();
		
		queue.add(start_node);
		
		while(!queue.isEmpty()){
			GPNode node = queue.pop();
			
			//generate the AT item and put it into results
			String check = node.toString();
			ArrayList<String> slibings = new ArrayList<>();
			
			for(int j = 0; j<node.expectedChildren(); j++) {
				slibings.add(node.children[j].toString());
			}
			
			if(node.expectedChildren()>0)
				res.add(new Pair<String, ArrayList<String>>(check, slibings));
			
			//put the children into queue
			for(int j = 0; j<node.expectedChildren(); j++) {
				if(node.children[j].expectedChildren() > 0){
					queue.add(node.children[j]);
				}
			}
		}
		
		return res;
	}
	
	
}
