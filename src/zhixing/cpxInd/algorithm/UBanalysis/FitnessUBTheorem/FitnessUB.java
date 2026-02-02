package zhixing.cpxInd.algorithm.UBanalysis.FitnessUBTheorem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.util.PublicCloneable;

import ec.EvolutionState;
import ec.Population;
import ec.gp.GPBreedingPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.individual.GPTreeStructSemantic;
import zhixing.cpxInd.algorithm.semantic.individual.SLGPIndividual;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;
import zhixing.cpxInd.individual.LGPIndividual;
import ec.gp.GPProblem;
import zhixing.cpxInd.algorithm.UBanalysis.FitnessUBTheorem.optimization.FUBproblem;
import zhixing.cpxInd.algorithm.UBanalysis.individual.reproduce.PureMacroMutation4FUB;

public class FitnessUB {

	
	//there is a function to sample programs based on initialization and genetic operators
	//define a unified form of the semantic vectors
	//estimate the Lemma 1 to 3
	//1. sample semantics S_1, S_2,  2. sample programs g_1, g_2,  3. get g_1(S_1), g_1(S_2), g_2(S_1), g_2(S_2),  4. estimate
	
	public final static String P_FUB = "FitnessUpperBound";
	public final static String P_OPERATOR = "operator";
	public final static String P_SAMPLEFACTOR = "sample_factor"; //the number of sampling offspring for each program
	public final static String P_WORKPLACE = "workplace";
	
	protected GPBreedingPipeline op;
	protected int sampleFactor;
	protected String filepath;
	protected FUBproblem problem;
	protected SemanticVector inputSV = null;
	
	protected ArrayList<SampleItem> itemlist;
	
	double Theta_s = 0;
	double K = 0;
	double Theta_f = 0;
	double Theta_F = 0;
	int max_progsize = 0;
	
	public void setup(final EvolutionState state, final Parameter base) {
		
		Parameter p = new Parameter(P_FUB);
		
		op = (GPBreedingPipeline) state.parameters.getInstanceForParameter(p.push(P_OPERATOR), null, GPBreedingPipeline.class);
		if(op == null) {
			state.output.fatal("the operator for FitnessUpperBound objects is empty\n",
					p.push(P_OPERATOR),null);
		}
		op.setup(state, p.push(P_OPERATOR));
		
		sampleFactor = state.parameters.getIntWithDefault(p.push(P_SAMPLEFACTOR), null, 10);
		if(sampleFactor < 1) {
			state.output.fatal("the sampling factor of FitnessUpperBound objects must be >= 1 \n");
		}
		
		filepath = state.parameters.getStringWithDefault( new Parameter(P_WORKPLACE) , null, System.getProperty("user.dir"));
		
		if(! (state.evaluator.p_problem instanceof FUBproblem)) {
			state.output.fatal("the optimization problem for FitnessUpperBound objects must implement FUBproblem interface. \n");
		}
		problem = (FUBproblem) state.evaluator.p_problem;
		
		
		itemlist = new ArrayList<>();
		
		Theta_s = 0;
		K = 0;
		Theta_f = 0;
		Theta_F = 0;
		
		try {
			FileWriter f = new FileWriter( filepath + "\\d_Detailfit.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addItemsFromPop(EvolutionState state, int thread) {
		
		//for each individual in the population, clone it as g_1,  and apply operators to produce g_2 (or multiple g_2).
		Population pop = state.population;
		
		for(int i = 0; i < pop.subpops[0].individuals.length; i++) {
			
			if(inputSV == null) {
				inputSV = (SemanticVector) ((SLGPIndividual) pop.subpops[0].individuals[i]).getInputS().clone();
			}
			
			for(int c = 0; c < sampleFactor; c++) {
				SLGPIndividual ind = (SLGPIndividual) pop.subpops[0].individuals[i].clone();
				
				int step = state.random[thread].nextInt(ind.getMaxNumTrees())+1;
				
				SLGPIndividual offsp = (SLGPIndividual) samplePrograms(ind, op, step, state, thread);
				
				SampleItem item = new SampleItem(ind, offsp, step);
				
				item.evaluateItem(state, (GPProblem) problem);
				
				itemlist.add(item);
			}
			
		}
	}
	
	public void clearItems() {
		itemlist.clear();
	}
	
	public void updateCoeffs(EvolutionState state, int thread) {
		addItemsFromPop(state, thread);
		
		estimateAllCoeff(state, 0, thread);
		
		//logging d_Detailfit
		try {
			FileWriter f = new FileWriter( filepath + "\\d_Detailfit.txt", true);
			
			BufferedWriter bw = new BufferedWriter(f);
			
			for(SampleItem item : itemlist) {
				bw.write(""+item.d+"\t"+item.Detailfit+"\n");
			}
			
			bw.flush();
			
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		clearItems();
	}
	
	protected LGPIndividual samplePrograms(LGPIndividual ind, GPBreedingPipeline operator, int stepsize, EvolutionState state, int thread) {
		//ind: the seed individual,  operator: the genetic operator that samples neighbors
		
		LGPIndividual newInd = ((PureMacroMutation4FUB)operator).produce(0, ind, stepsize, state, thread);
		
		return newInd;
	}
	
//	protected SemanticVector sampleSemantics(EvolutionState state, int thread, int numInput, int numReg, double data_rng) {
//		SemanticVector sv = new SemanticVector(numInput, numReg);
//		
//		sv.randSetSem(state, thread, -data_rng, data_rng);
//		
//		return sv;
//	}
	 
	 protected static double countInstrEditDis(LGPIndividual ind1, LGPIndividual ind2) {
		 double d = 0;
		 
		 int i, j;
		 for(i = 0, j=0; i<ind1.getTreesLength() && j<ind2.getTreesLength(); i++,j++) {
			 if( ind1.getTreeStruct(i).toString().equals(ind2.getTreeStruct(j).toString())) continue;
			 
			 int jj= j+1;
			 for(; jj<ind2.getTreesLength(); jj++) {
				 if( ind1.getTreeStruct(i).toString().equals(ind2.getTreeStruct(jj).toString())) {
					 break;
				 }
			 }
			 if(jj >= ind2.getTreesLength()) {
				 d++;
			 }
			 else {
				 j = jj;
			 }
			 
		 }
		 d+= ind1.getTreesLength() - i;
		 
		 for(i = 0, j=0; i<ind2.getTreesLength() && j<ind1.getTreesLength(); i++,j++) {
			 if( ind2.getTreeStruct(i).toString().equals(ind1.getTreeStruct(j).toString())) continue;
			 
			 int jj= j+1;
			 for(; jj<ind1.getTreesLength(); jj++) {
				 if( ind2.getTreeStruct(i).toString().equals(ind1.getTreeStruct(jj).toString())) {
					 break;
				 }
			 }
			 if(jj >= ind1.getTreesLength()) {
				 d++;
			 }
			 else {
				 j = jj;
			 }
			 
		 }
		 d+= ind2.getTreesLength() - i;
		 
		 return d;
	 }
	
//	public double estimateThetaS(EvolutionState state, int subpop, int thread) {
//		if(itemlist == null) {
//			System.err.print("please initialize itemlist before using FitnessUB objects\n");
//			return 0;
//		}
//		
//		//check all items, each item has (programsize1*programsize2)/2 \theta_s, identify the largest one 
//		Theta_s = 0;
//		double theta_s;
//		
//		for(SampleItem item : itemlist) {
//			for(int i = 0; i<item.g_1.getTreesLength(); i++) {
//				if(! item.g_1.getTreeStruct(i).status) continue;
//				
//				for(int j = 0; j<item.g_2.getTreesLength(); j++) {
//					if(! item.g_2.getTreeStruct(j).status) continue;
//					
//					SemanticVector S1 = ((GPTreeStructSemantic) item.g_1.getTreeStruct(i)).context;
//					SemanticVector S2 = ((GPTreeStructSemantic) item.g_2.getTreeStruct(j)).context;
//					//evalute \theta_{s12}
//					theta_s = S1.sv_diff(S2);
//					
//					if(theta_s > Theta_s) {
//						Theta_s = theta_s;
//					}
//				}
//			}
//		}
//		
//		return Theta_s;
//	}
//	
//	public double estimateK(EvolutionState state, int subpop, int thread) {
//		if(itemlist == null) {
//			System.err.print("please initialize itemlist before using FitnessUB objects\n");
//			return 0;
//		}
//		
//		K = 0;
//		
//		double theta_s;
//		double detail_fit;
//		
//		//check the fitness of all semantics
//		for(SampleItem item : itemlist) {
//			for(int i = 0; i<item.g_1.getTreesLength(); i++) {
//				if(! item.g_1.getTreeStruct(i).status) continue;
//				
//				for(int j = 0; j<item.g_2.getTreesLength(); j++) {
//					if(! item.g_2.getTreeStruct(j).status) continue;
//					
//					SemanticVector S1 = ((GPTreeStructSemantic) item.g_1.getTreeStruct(i)).context;
//					SemanticVector S2 = ((GPTreeStructSemantic) item.g_2.getTreeStruct(j)).context;
//					//evalute \theta_{s12}
//					theta_s = S1.sv_diff(S2);
//					
//					if(theta_s == 0) continue; //they are the same semantics, K = 0;
//					
//					//evaluate the fitness of S1 and S2
//					double fit1 = problem.evaluate(state, subpop, thread, S1);
//					double fit2 = problem.evaluate(state, subpop, thread, S2);
//					detail_fit = Math.abs(fit1 - fit2); 
//					
//					if(detail_fit / theta_s > K) {
//						K = detail_fit / theta_s;
//					}
//				}
//			}
//		}
//		
//		return K;
//	}
//	
//	public double estimateThetaf(EvolutionState state, int subpop, int thread) {
//		if(itemlist == null) {
//			System.err.print("please initialize itemlist before using FitnessUB objects\n");
//			return 0;
//		}
//		
//		Theta_f = 0;
//		
//		double theta_s;
//		double thetaf;
//		
//		for(SampleItem item : itemlist) {
//			for(int i = 0; i<item.g_1.getTreesLength(); i++) {
//				if(! item.g_1.getTreeStruct(i).status) continue;
//				
//				for(int j = 0; j<item.g_2.getTreesLength(); j++) {
//					if(! item.g_2.getTreeStruct(j).status 
//							|| ! item.g_2.getTreeStruct(j).toString().equals(item.g_1.getTreeStruct(i).toString())) continue;
//					
//					SemanticVector S1 = inputSV;
//					for(int ii = i - 1; ii>=0; ii--) {
//						if(item.g_1.getTreeStruct(ii).status) {
//							S1 = ((GPTreeStructSemantic) item.g_1.getTreeStruct(ii)).context;
//							break;
//						}
//					}
//					SemanticVector S2 = inputSV;
//					for(int jj = j - 1; jj>=0; jj--) {
//						if(item.g_2.getTreeStruct(jj).status) {
//							S2 = ((GPTreeStructSemantic) item.g_2.getTreeStruct(jj)).context;
//							break;
//						}
//					}
//					
//					//evalute \theta_{s12}
//					theta_s = S1.sv_diff(S2);
//					
//					if(theta_s == 0) continue; //they are the same semantics;
//					
//					SemanticVector fS1 = ((GPTreeStructSemantic) item.g_1.getTreeStruct(i)).context;
//					SemanticVector fS2 = ((GPTreeStructSemantic) item.g_2.getTreeStruct(j)).context;
//					
//					//evaluate the fitness of S1 and S2
//					thetaf = fS1.sv_diff(fS2) - theta_s;
//					
//					if(thetaf > Theta_f) {
//						Theta_f = thetaf;
//					}
//				}
//			}
//		}
//		
//		return Theta_f;
//	}
//	
//	public double estimateThetaF(EvolutionState state, int subpop, int thread) {
//		if(itemlist == null) {
//			System.err.print("please initialize itemlist before using FitnessUB objects\n");
//			return 0;
//		}
//		
//		Theta_F = 0;
//		
//		double theta_s;
//		double thetaF;
//		
//		for(SampleItem item : itemlist) {
//			for(int i = 0; i<item.g_1.getTreesLength(); i++) {
//				if(! item.g_1.getTreeStruct(i).status) continue;
//				
//				for(int j = 0; j<item.g_2.getTreesLength(); j++) {
//					if(! item.g_2.getTreeStruct(j).status ) continue;
//					
//					SemanticVector S1 = inputSV;
//					for(int ii = i - 1; ii>=0; ii--) {
//						if(item.g_1.getTreeStruct(ii).status) {
//							S1 = ((GPTreeStructSemantic) item.g_1.getTreeStruct(ii)).context;
//							break;
//						}
//					}
//					SemanticVector S2 = inputSV;
//					for(int jj = j - 1; jj>=0; jj--) {
//						if(item.g_2.getTreeStruct(jj).status) {
//							S2 = ((GPTreeStructSemantic) item.g_2.getTreeStruct(jj)).context;
//							break;
//						}
//					}
//					
//					//evalute \theta_{s12}
//					theta_s = S1.sv_diff(S2);
//					
//					if(theta_s == 0) continue; //they are the same semantics;
//					
//					SemanticVector fS1 = ((GPTreeStructSemantic) item.g_1.getTreeStruct(i)).context;
//					SemanticVector fS2 = ((GPTreeStructSemantic) item.g_2.getTreeStruct(j)).context;
//					
//					//evaluate the fitness of S1 and S2
//					thetaF = fS1.sv_diff(fS2) - theta_s;
//					
//					if(thetaF > Theta_F) {
//						Theta_F = thetaF;
//					}
//				}
//			}
//		}
//		
//		return Theta_F;
//	}
	
	public void estimateAllCoeff(EvolutionState state, int subpop, int thread) {
		if(itemlist == null) {
			System.err.print("please initialize itemlist before using FitnessUB objects\n");
			return;
		}
		
		double K_fit = K*Theta_s;
		
		for(SampleItem item : itemlist) {
			
			max_progsize = Math.max(max_progsize, item.g_1.getTreesLength());
			max_progsize = Math.max(max_progsize, item.g_2.getTreesLength());
			
			for(int i = 0; i<item.g_1.getTreesLength(); i++) {
				if(! item.g_1.getTreeStruct(i).status) continue;
				
				for(int j = 0; j<item.g_2.getTreesLength(); j++) {
					if(! item.g_2.getTreeStruct(j).status) continue;
					
					SemanticVector S1 = ((GPTreeStructSemantic) item.g_1.getTreeStruct(i)).context;
					SemanticVector S2 = ((GPTreeStructSemantic) item.g_2.getTreeStruct(j)).context;
					//evalute \theta_{s12}
					double theta_s = S1.sv_diff(S2);
					double theta_s1 = Math.max(theta_s, S1.sv_diff(inputSV));
					theta_s1 = Math.max(theta_s1, S2.sv_diff(inputSV));
					//Theta_s
					if(theta_s1 > Theta_s) {
						Theta_s = theta_s1;
					}
					
					//K
					//evaluate the fitness of S1 and S2
					double fit1 = problem.evaluate(state, subpop, thread, S1);
					double fit2 = problem.evaluate(state, subpop, thread, S2);
					double detail_fit = Math.abs(fit1 - fit2); 
					
					if(theta_s != 0 && detail_fit > K_fit) {
						K_fit = detail_fit;
					}
					
					
					
				}
			}
			
			//Theta_f and _F
			int i1 = item.g_1.getTreesLength(), i2 = item.g_2.getTreesLength();
			for(int i = 1; i1-i>=0 && i2-i>=0; i++) {
				if(! item.g_1.getTreeStruct(i1-i).status) continue;
				if(! item.g_2.getTreeStruct(i2-i).status) continue;
				
				SemanticVector S1 = ((GPTreeStructSemantic) item.g_1.getTreeStruct(i1-i)).context;
				SemanticVector S2 = ((GPTreeStructSemantic) item.g_2.getTreeStruct(i2-i)).context;
				//evalute \theta_{s12}
				double theta_s = S1.sv_diff(S2);
				
				SemanticVector inS1 = inputSV;
				for(int ii = i1 - i - 1; ii>=0; ii--) {
					if(item.g_1.getTreeStruct(ii).status) {
						inS1 = ((GPTreeStructSemantic) item.g_1.getTreeStruct(ii)).context;
						break;
					}
				}
				SemanticVector inS2 = inputSV;
				for(int jj = i2 - i - 1; jj>=0; jj--) {
					if(item.g_2.getTreeStruct(jj).status) {
						inS2 = ((GPTreeStructSemantic) item.g_2.getTreeStruct(jj)).context;
						break;
					}
				}
				
				double in_theta_s = inS1.sv_diff(inS2);
				if(in_theta_s == 0) continue;
				
				//evaluate the fitness of S1 and S2
				double out_theta = theta_s - in_theta_s;
				
				if(item.g_2.getTreeStruct(i2-i).toString().equals(item.g_1.getTreeStruct(i1-i).toString())) { //theta_f
					if(out_theta > Theta_f) {
						Theta_f = out_theta;
					}
				}
				
				if(out_theta > Theta_F) {
					Theta_F = out_theta;
				}
			}
		}
		
		K = K_fit / Theta_s;
	}
	
	public void summarize() {
		
		System.out.print(""+Theta_s+"\t"+K+"\t"+Theta_f+"\t"+Theta_F+"\n");
		
		try {
			FileWriter f = new FileWriter( filepath + "\\d_Detailfit.txt", true);
			
			BufferedWriter bw = new BufferedWriter(f);
			
//			for(SampleItem item : itemlist) {
//				bw.write(""+item.d+"\t"+item.Detailfit+"\n");
//			}
			
			bw.write("Theta_s="+Theta_s+"\tK="+K+"\tTheta_f="+Theta_f+"\tTheta_F="+Theta_F+"\n");
			bw.write("slope="+K*(Theta_F - Theta_f)+"\tintercept="+K*max_progsize*Theta_f+"\n");
			
			bw.flush();
			
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
