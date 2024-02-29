package zhixing.symbolicregression.optimization;

import org.apache.commons.math3.random.ISAACRandom;
import org.spiderland.Psh.intStack;

//import com.sun.istack.internal.FinalArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.app.tutorial4.DoubleData;
import ec.app.tutorial4.MultiValuedRegression;
import ec.gp.GPIndividual;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.symbolicregression.individual.LGPIndividual4SR;
//import zhixing.jss.cpxInd.individual.LGPIndividual4SR;

public class LGPSymbolicRegressionDoubleSin extends GPSymbolicRegression{
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		// TODO Auto-generated method stub
		super.setup(state, base);
		
		if(data==null) {
			data = new double[100][1];
			
			
			for(int y = 0; y < 100; y++) {
				data[y][0] = 2*Math.PI*y/100;
//				currentX = state.random[0].nextDouble()*8.0-4;
//	            currentY = state.random[0].nextDouble()*8.0-4;
				
			}
		}
		
		X = new double [1];
	}
	
	@Override
	public void evaluate(final EvolutionState state, 
	        final Individual ind, 
	        final int subpopulation,
	        final int threadnum)
	        {
	        if (!ind.evaluated)  // don't bother reevaluating
	            {
	            DoubleData input = (DoubleData)(this.input);
	        
	            int hits = 0;
	            double sum = 0.0;
	            double expectedResult;
	            double result;
	            for (int y=0;y<100;y++)
	                {
	            	X[0] = data[y][0];
	                expectedResult = Math.sin(X[0]);
	                DoubleData tmp = new DoubleData();
	                int treeNum = ((LGPIndividual4SR)ind).getTreesLength();
	                
	                ((LGPIndividual4SR)ind).resetRegisters(this, (LGPIndividual4SR) ind);
	                
	        		for(int index = 0; index<treeNum; index++){
	        			GPTreeStruct tree = ((LGPIndividual4SR)ind).getTreeStruct(index);
	        			if(tree.status) {
	        				tree.child.eval(null, 0, tmp, null, (LGPIndividual4SR)ind, this);
		        			 if(((LGPIndividual4SR)ind).getRegisters()[((WriteRegisterGPNode)tree.child).getIndex()] 
		        					 >= Double.POSITIVE_INFINITY) {
		 	                	int a =1;
		 	                }
	        			}
	        			
	        		}

	                result = Math.abs(expectedResult - ((LGPIndividual4SR)ind).getRegisters()[0]);
	                
	                if(result >= Double.POSITIVE_INFINITY || Double.isNaN(result)) {
	                	result = 1e6;
	                }
	               
	                if (result <= 0.01) hits++;
	                sum += result*result;                  
	                }

	            // the fitness better be KozaFitness!
	            KozaFitness f = ((KozaFitness)ind.fitness);
	            f.setStandardizedFitness(state, sum);
	            f.hits = hits;
	            ind.evaluated = true;
	            }
	        }
}
