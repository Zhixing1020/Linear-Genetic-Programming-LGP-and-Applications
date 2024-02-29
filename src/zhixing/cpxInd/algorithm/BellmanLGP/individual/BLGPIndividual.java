package zhixing.cpxInd.algorithm.BellmanLGP.individual;

import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;

public abstract class BLGPIndividual extends LGPIndividual4Grammar{
	//use the past objective values to estimate the future objective value at the current decision situations.
	//use FOR loop to implement sequential prediction based on the past objective values within a finite horizon
	//estimated cost at this decision situation (take info of current decision situations & past objectives & input from last iteration as inputs) = decision value
	
	protected double memory [] = null; //memory is specially designed for "ForEach"
	
	
}
