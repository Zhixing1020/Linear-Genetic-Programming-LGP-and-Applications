package zhixing.cpxInd.algorithm.semantic.library;

import zhixing.cpxInd.algorithm.semantic.library.fitness.SLFitness;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public class LibraryItem {

	public GPTreeStruct [] instructions;
	public SVSet outSVs;
	public double frequency;
	public SLFitness fitness;
	public boolean evaluated = false;
	
	public LibraryItem(GPTreeStruct [] ins, SemanticLibrary semlib) {
		instructions = ins;
		
		outSVs = new SVSet(semlib.getNumRef(), semlib.getMaxNumInput(), semlib.getNumRegs());
		
		frequency = 0;
		
		evaluated = false;
		
		fitness = (SLFitness) semlib.getFitnessProto().clone();
	}
	
	public LibraryItem(GPTreeStruct [] ins, SVSet outsv, double fre, boolean evaluated, SLFitness fit_prototype) {
		instructions = ins;
		
		outSVs = outsv;
		
		frequency = fre;
		
		this.evaluated = evaluated;
		
		fitness = (SLFitness) fit_prototype.clone();
	}
}
