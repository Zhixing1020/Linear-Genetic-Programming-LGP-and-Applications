package zhixing.cpxInd.algorithm.semantic.library.produce;

import java.util.ArrayList;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public class SLATCrossover extends SLCrossover{
	
	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib) {
		
		ArrayList<LibraryItem> parents = new ArrayList<>(getNumParents());
		
		parents.add(sources[0].produce(state, thread, semlib));
		parents.add(sources[1].produce(state, thread, semlib)); // merge selection from any of the sub populations
		
		return produce(state, thread, semlib, parents);
	}
	
	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib,
			ArrayList<LibraryItem> parents) {
		
		if(parents.size() != getNumParents()) {
			System.err.print("SLATCrossover got inconsistent number of parents\n");
			System.exit(1);
		}
		
		GPTreeStruct[] trial = new GPTreeStruct[semlib.getMaxCombine()];

		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		int t1=0; //the index of GPTreeStruct in the first donor (a combination of GPTreeStruct)
		int t2=0;  //the index of GPTreeStruct in the second donor (a combination of GPTreeStruct)
		
		GPTreeStruct[] parent1 = parents.get(0).instructions;
		GPTreeStruct[] parent2 = parents.get(1).instructions;
		
		t1 = state.random[thread].nextInt(parent1.length);
		t2 = state.random[thread].nextInt(parent2.length);
		
		//transform parent2[t2] into the same representation with parent1[t1]
		ArrayList<Pair<String, ArrayList<String>>> adjlist = new ArrayList<>();
		System.err.print("SLATCrossover is unfinished\n");
		System.exit(1);
		
//		if(parent1[t1].constraints(initializer) != parent2[t2].constraints(initializer) ) {
//			if(parent1.length == 1 && parent2.length == 1) {
//				System.err.print("SLCrossover cannot find consistent instruction (GPTreeStruct) with the same initializer.\n");
//				System.exit(1);
//			}
//			else {
//				for(int a = 0; a<numTries; a++) {
//					t1 = state.random[thread].nextInt(parent1.length);
//					t2 = state.random[thread].nextInt(parent2.length);
//					if(parent1[t1].constraints(initializer) != parent2[t2].constraints(initializer) ) break;
//				}
//			}
//		}
//		if(parent1[t1].constraints(initializer) != parent2[t2].constraints(initializer) ) {
//			System.err.print("SLCrossover cannot find consistent instruction (GPTreeStruct) with the same initializer.\n");
//			System.exit(1);
//		}
		
		// validity results...
        boolean res1 = false;
        boolean res2 = false;
        
        
        // prepare the nodeselectors
        nodeselect1.reset();
        nodeselect2.reset();
        
        
        // pick some nodes
        
        GPNode p1=null;
        GPNode p2=null;
        
        for(int x=0;x<numTries;x++)
        {
            // pick a node in individual 1
            p1 = nodeselect1.pickNode(state,0,thread,null,parent1[t1]);
            
            // pick a node in individual 2
            p2 = nodeselect2.pickNode(state,0,thread,null,parent2[t2]);
            
            // check for depth and swap-compatibility limits
            res1 = verifyPoints(initializer,p2,p1);  // p2 can fill p1's spot -- order is important!
            //res2 = verifyPoints(initializer,p1,p2);  // p1 can fill p2's spot -- order is important!
            
            // did we get something that had both nodes verified?
            // we reject if EITHER of them is invalid.  This is what lil-gp does.
            // Koza only has numTries set to 1, so it's compatible as well.
            //if (res1 && res2) break;
            if (res1) break;
        }
        
        GPTreeStruct newtree;
        
        newtree = parent1[t1].lightClone();
        newtree.child = parent1[t1].child.cloneReplacing(p2,p1);
        newtree.child.parent = newtree;
        newtree.child.argposition = 0;
		
      //clone the parents
  		for(int l = 0; l<trial.length; l++) {
  			if(l==t1 && res1) {
  				trial[l] = newtree;
  			}
  			else {
  				trial[l] = (GPTreeStruct) parent1[l].clone();
  			}
  		}
		
  		LibraryItem item = new LibraryItem(trial, semlib);
		
		return item;
	}
}
