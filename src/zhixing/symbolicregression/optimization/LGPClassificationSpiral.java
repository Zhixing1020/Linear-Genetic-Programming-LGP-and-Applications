package zhixing.symbolicregression.optimization;

import java.awt.font.NumericShaper.Range;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.Individual;
import ec.app.tutorial4.DoubleData;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.symbolicregression.individual.LGPIndividual4SR;
//import zhixing.jss.cpxInd.individual.LGPIndividual4SR;

public class LGPClassificationSpiral extends GPSymbolicRegression {
	public static int labels[];
	
	private double rng = 2*Math.PI;
	private double initialPhase = 0.25*Math.PI;
	private int numLaps = 3;
	private int numPoints = 97;
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		// TODO Auto-generated method stub
		super.setup(state, base);

		if(data==null) {
			data = new double[200][2];
			labels = new int[200];
			
			double s, angle;
			for(int y = 0; y < numPoints; y++) {
				s = rng* y / numPoints;
				angle = initialPhase + (y / numPoints) * 360 * numLaps; 
				data[y][0] = s * Math.cos(angle * Math.PI / 180);
				data[y][1] = s * Math.sin(angle * Math.PI / 180);
				labels[y] = 1;
				
				data[y+97][0] = - data[y][0];
				data[y+97][1] = - data[y][1];
				labels[y+97] = -1;
//				currentX = state.random[0].nextDouble()*8.0-4;
//	            currentY = state.random[0].nextDouble()*8.0-4;
				
			}
		}
		
		X = new double [2];
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
	            double result = 0;
	            for (int y=0;y<194;y++)
	                {
	                //currentX = state.random[threadnum].nextDouble()*8.0-4;
	                //currentY = state.random[threadnum].nextDouble()*8.0-4;
	            	//currentX = data[y][0];
	            	//currentY = data[y][1];
	                //expectedResult = currentX*currentX*currentY + currentX*currentY + currentY;
	                //expectedResult = (1-currentX*currentX/4-currentY*currentY/4)*Math.exp(-currentX*currentX/8-currentY*currentY/8);

	            	X[0] = data[y][0];
	            	X[1] = data[y][1];
	            	
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
	        		
	        		if((((LGPIndividual4SR)ind).getRegisters()[0] > 0 && labels[y] == -1)
	        				|| (((LGPIndividual4SR)ind).getRegisters()[0] <=0 && labels[y] == 1)) {
	        			result =1;
	        		}
	                
	                if(result >= Double.POSITIVE_INFINITY || Double.isNaN(result)) {
	                	result = 1e6;
	                }
	               
	                if (result <= 0.01) hits++;
	                sum += result;                  
	                }

	            // the fitness better be KozaFitness!
	            KozaFitness f = ((KozaFitness)ind.fitness);
	            f.setStandardizedFitness(state, sum);
	            f.hits = hits;
	            ind.evaluated = true;
	            }
	        }
}
