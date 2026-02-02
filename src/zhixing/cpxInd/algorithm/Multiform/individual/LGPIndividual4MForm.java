package zhixing.cpxInd.algorithm.Multiform.individual;

import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;

public abstract class LGPIndividual4MForm extends LGPIndividual4Graph{
	
	public boolean issameRepresentation(Object ind) {
		
		if( ind instanceof LGPIndividual4MForm ) {
			if(((LGPIndividual4MForm)ind).getRegisters().length == this.getRegisters().length)
				return true;
		}
		
		return false;
	}
}
