package zhixing.symbolicregression.optimization;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.Individual;
import ec.app.majority.func.X;
import ec.app.tutorial4.DoubleData;
import ec.app.tutorial4.MultiValuedRegression;
import ec.app.tutorial4.Pow;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
//import zhixing.jss.cpxInd.individual.LGPIndividual4SR;
import zhixing.symbolicregression.individual.LGPIndividual4SR;

public class LGPSymbolicRegressionDistance extends GPSymbolicRegression{
	
	//take distance problem as an example
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		// TODO Auto-generated method stub
		super.setup(state, base);

		if(data==null) {
			data = new double[300][6];
			
			for(int y = 0; y < 300; y++) {
				for(int x = 0; x < 6; x++) {
					data[y][x] = state.random[0].nextDouble();
				}
//				currentX = state.random[0].nextDouble()*8.0-4;
//	            currentY = state.random[0].nextDouble()*8.0-4;
				
			}
		}
		
		X = new double [6];
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
	            for (int y=0;y<300;y++)
	                {
	                //currentX = state.random[threadnum].nextDouble()*8.0-4;
	                //currentY = state.random[threadnum].nextDouble()*8.0-4;
	            	//currentX = data[y][0];
	            	//currentY = data[y][1];
	                //expectedResult = currentX*currentX*currentY + currentX*currentY + currentY;
	                //expectedResult = (1-currentX*currentX/4-currentY*currentY/4)*Math.exp(-currentX*currentX/8-currentY*currentY/8);
	            	expectedResult = 0;
	            	for(int i = 0;i<3;i++) {
	            		expectedResult += Math.pow(data[y][i] - data[y][i+3], 2.);
	            		X[i] = data[y][i];
	            		X[i+3] = data[y][i+3];
	            	}
	            	expectedResult = Math.sqrt(expectedResult);
	            	
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
