package zhixing.cpxInd.fitnesslandscape;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import ec.EvolutionState;
import ec.Fitness;
import ec.gp.GPProblem;
import ec.gp.GPTree;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.BoardItem;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Index;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.Objective4FLO;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.Index4LGP;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.IndexList4LGP;
import zhixing.cpxInd.fitnesslandscape.objective.FLMetrics;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.LGPIndividual;
//import zhixing.djss.algorithm.LandscapeOptimization.EvolutionStateFLO4DJSS;

public class LGPFitnessLandscape {
	
	public static final String LGPFITLANDSCAPE = "lgpFitLandscape";
	
	public static final String P_NUMOBJECTIVE = "num_metrics";
	public static final String P_DRAWFITNESSLAND = "todrawlandscape";
	public static final String P_OUTPUTMETRIC = "tooutputmetric";
	
	public static final String METRICS = "metrics";
	public static final String P_COMPRESS = "gzip";
	
	public static final String P_STATISTICS_FILE = "file";
	public int statisticslog = 0;  // stdout by default

	protected ArrayList<LGPIndividual> historyBestList = new ArrayList<>();
	
	protected ArrayList<LGPIndividual> currentBestList = new ArrayList<>();
	
	protected ArrayList<LGPIndividual> map = new ArrayList<>();
	
	protected ArrayList<Double> fitnessLandscape = new ArrayList<>();
	
	protected ArrayList<ArrayList<Double>> coordinates = new ArrayList<>();
	
	public IndexList indexlist;
	
	protected boolean todrawFL = false;
	protected boolean tooutputMetric = false;
	
	int numMetrics;
	
	protected ArrayList<FLMetrics> metric_list;
	protected boolean isevaluated = false;
	protected boolean ispreprinted = false;
	
	public void setup(final EvolutionState state, final Parameter base) {
		Parameter def = new Parameter(LGPFITLANDSCAPE);
		
		todrawFL = state.parameters.getBoolean(base.push(P_DRAWFITNESSLAND), def.push(P_DRAWFITNESSLAND), false);
		
		tooutputMetric = state.parameters.getBoolean(base.push(P_OUTPUTMETRIC), def.push(P_OUTPUTMETRIC), false);
		if(tooutputMetric) {
			File statisticsFile = state.parameters.getFile(base.push(P_STATISTICS_FILE),def.push(P_STATISTICS_FILE));
			if (statisticsFile!=null) {
				 try
                {
	                statisticslog = state.output.addLog(statisticsFile,
	                    !state.parameters.getBoolean(base.push(P_COMPRESS),null,false),
	                    state.parameters.getBoolean(base.push(P_COMPRESS),null,false));
                }
	            catch (IOException i)
                {
	            	state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
                }
	        }
			else state.output.warning("No statistics file specified, printing to stdout at end.", base.push(P_STATISTICS_FILE));
		
		}
		
		numMetrics = state.parameters.getInt(base.push(P_NUMOBJECTIVE), def.push(P_NUMOBJECTIVE));
		if(numMetrics <=0 && tooutputMetric) {
			state.output.fatal("the number of metrics for fitness landscape must be at least 1 when we want to output metrics", base.push(P_NUMOBJECTIVE), def.push(P_NUMOBJECTIVE));
		}
		if(tooutputMetric && numMetrics >= 1) {
			metric_list = new ArrayList<>();
			for(int obj = 0; obj < numMetrics; obj++) {
				FLMetrics met = (FLMetrics) state.parameters.getInstanceForParameter(base.push(METRICS).push(""+obj), def.push(METRICS).push(""+obj), FLMetrics.class);
				met.setup(state, def.push(METRICS).push(""+obj));
				metric_list.add(met);
			}
		}
		
	}
	
	public void initialize(LGPIndividual proto, IndexList indexlist) {
		
		this.indexlist = indexlist.cloneIndexList();
		
		map.clear();
		
//		fitnessLandscape.clear();
		
		LGPIndividual tmpproto = (LGPIndividual) proto.clone();
//		if(tmpproto.getTreesLength() != 2) {
//			System.err.print("please give an LGP individual prototype with two instructions\n");
//			System.exit(1);
//		}
		
		int minlen = tmpproto.getMinNumTrees();
		int maxlen = tmpproto.getMaxNumTrees();
		
		if(maxlen > 4) {
			System.err.print("please give an LGP individual prototype with maximally four instructions\n");
			System.exit(1);
		}
		
		for(int len = minlen; len<=maxlen; len++) {
			
			if(len == 1) {
				for(int i = 0; i<indexlist.size(); i++) {
					LGPIndividual tmp = (LGPIndividual) tmpproto.clone();
					tmp.addTree(0, (GPTreeStruct) ((Index4LGP) indexlist.get(i)).symbols.get(0).clone());
					while(tmp.getTreesLength() > len) {
						tmp.removeTree(len);
					}
					map.add(tmp);
				}
			}
			else if(len == 2) {
				for(int i = 0; i<indexlist.size(); i++) {
					for(int j = 0; j<indexlist.size(); j++) {
						LGPIndividual tmp = (LGPIndividual) tmpproto.clone();
						tmp.addTree(0, (GPTreeStruct) ((Index4LGP) indexlist.get(i)).symbols.get(0).clone());
						tmp.addTree(1, (GPTreeStruct) ((Index4LGP) indexlist.get(j)).symbols.get(0).clone());
						while(tmp.getTreesLength() > len) {
							tmp.removeTree(len);
						}
						map.add(tmp);
					}
				}
			}
			else if(len == 3) {
				for(int i = 0; i<indexlist.size(); i++) {
					for(int j = 0; j<indexlist.size(); j++) {
						for(int k = 0; k<indexlist.size(); k++) {
							LGPIndividual tmp = (LGPIndividual) tmpproto.clone();
							tmp.addTree(0, (GPTreeStruct) ((Index4LGP) indexlist.get(i)).symbols.get(0).clone());
							tmp.addTree(1, (GPTreeStruct) ((Index4LGP) indexlist.get(j)).symbols.get(0).clone());
							tmp.addTree(2, (GPTreeStruct) ((Index4LGP) indexlist.get(k)).symbols.get(0).clone());
							while(tmp.getTreesLength() > len) {
								tmp.removeTree(len);
							}
							map.add(tmp);
						}
					}
				}
			}
			else if(len == 4) {
				for(int i = 0; i<indexlist.size(); i++) {
					for(int j = 0; j<indexlist.size(); j++) {
						for(int k = 0; k<indexlist.size(); k++) {
							for(int o = 0; o<indexlist.size(); o++) {
								LGPIndividual tmp = (LGPIndividual) tmpproto.clone();
								tmp.addTree(0, (GPTreeStruct) ((Index4LGP) indexlist.get(i)).symbols.get(0).clone());
								tmp.addTree(1, (GPTreeStruct) ((Index4LGP) indexlist.get(j)).symbols.get(0).clone());
								tmp.addTree(2, (GPTreeStruct) ((Index4LGP) indexlist.get(k)).symbols.get(0).clone());
								tmp.addTree(3, (GPTreeStruct) ((Index4LGP) indexlist.get(o)).symbols.get(0).clone());
								while(tmp.getTreesLength() > len) {
									tmp.removeTree(len);
								}
								map.add(tmp);
							}
						}
					}
				}
			}
			
		}
		
		isevaluated = false;
		ispreprinted = false;
	}
	
	public void updateFitnessLandscape(EvolutionState state, Board board, IndexList indexlist) {
		currentBestList.clear();
		
		Board tmp = board.lightTrimCloneByBest();
		for(int b = 0; b<tmp.get(0).size(); b++) {
			historyBestList.add((LGPIndividual) tmp.get(0).get(b));
		}

		for(BoardItem item : tmp) {
			for(int b = 0; b<item.size(); b++) {
				currentBestList.add((LGPIndividual) item.get(b));
			}
		}
		
		//check whether the fitness landscape has changed 
		boolean indexChanged = false;
		if( this.indexlist.size() != indexlist.size()) {
			indexChanged = true;
		}
		else {
			for(int i = 0; i<indexlist.size(); i++) {
				if( ((Index) this.indexlist.get(i)).index != ((Index) indexlist.get(i)).index ) {
					indexChanged = true;
					break;
				}
			}
		}
		
		if(indexChanged) {
			isevaluated = false;
			ispreprinted = false;
			this.indexlist = indexlist.cloneIndexList();
		}
	}
	
	public void preprintFitnessLandscape(EvolutionState state, GPProblem problem, IndexList indexlist){
		//convert all the possible solutions into coordinates and fitness
		
		int ci = 0;
		coordinates.clear();
		fitnessLandscape.clear();
		
		for(LGPIndividual ind : map) {
//			int x = indexlist.getIndexBySymbol(ind.getTreeStruct(0));
//			int y = indexlist.getIndexBySymbol(ind.getTreeStruct(1));
//			
//			Pair<Integer, Integer> pair = new Pair(x, y);
			
			GenoVector gv = indexlist.getGenoVector(ind);
			
			ArrayList<Double> geno = new ArrayList<>();
			
			for(int in : gv.G) {
				geno.add((double) in);
			}
			
			problem.evaluate(state, ind, 0, 0);
			coordinates.add(geno);
			fitnessLandscape.add(ind.fitness.fitness());
			ci++;
			
//			if(!ind.evaluated) {
//				
//			}
//			else {
//				coordinates.set(ci, geno);
//				ci++;
//			}
			
			
		}
		
		ispreprinted = true;
	}
	
	public ArrayList<LGPIndividual> getMap(){
		return map;
	}
	
	public ArrayList<ArrayList<Double>> getCoordinates(){
		return coordinates;
	}
	
	public ArrayList<Double> getFitnesses(){
		return fitnessLandscape;
	}
	
	public void drawFitnessLandscape(EvolutionState state, GPProblem problem, IndexList indexlist){
		
		if(!todrawFL) return;
		
//		fitnessLandscape.clear();
		
//		coordinates.clear();
		
//		int ci = 0;
//		for(LGPIndividual ind : map) {
////			int x = indexlist.getIndexBySymbol(ind.getTreeStruct(0));
////			int y = indexlist.getIndexBySymbol(ind.getTreeStruct(1));
////			
////			Pair<Integer, Integer> pair = new Pair(x, y);
//			
//			GenoVector gv = indexlist.getGenoVector(ind);
//			
//			ArrayList<Integer> geno = new ArrayList<>();
//			
//			for(int in : gv.G) {
//				geno.add(in);
//			}
//			
//			if(!ind.evaluated) {
//				problem.evaluate(state, ind, 0, 0);
//				coordinates.add(geno);
//				fitnessLandscape.add(ind.fitness.fitness());
//				ci++;
//			}
//			else {
//				coordinates.set(ci, geno);
//				ci++;
//			}
//			
//			
//		}
		
		if(!ispreprinted) {
			preprintFitnessLandscape(state, problem, indexlist);
		}
			
		
		FileWriter f;
		FileWriter historyF, currentBestF;
		try {
			f = new FileWriter("D:/Application/Eclipse/workspace/GPJSS-basicLGP/GPJSS-basicLGP/fitnesslandscape.txt");
			historyF = new FileWriter("D:/Application/Eclipse/workspace/GPJSS-basicLGP/GPJSS-basicLGP/history.txt");
			currentBestF = new FileWriter("D:/Application/Eclipse/workspace/GPJSS-basicLGP/GPJSS-basicLGP/currentBest.txt");
			
			BufferedWriter bw = new BufferedWriter(f);
			for(int i = 0; i<coordinates.size(); i++) {
				for(int j = 0; j<coordinates.get(i).size(); j++) {
					bw.write(""+coordinates.get(i).get(j)+"\t");
				}
				bw.write(""+fitnessLandscape.get(i)+"\n");
				bw.flush();
			}
			bw.close();
			
			bw = new BufferedWriter(historyF);
			for(LGPIndividual ind : historyBestList) {
				
//				int x = indexlist.getIndexBySymbol(ind.getTreeStruct(0));
//				int y = indexlist.getIndexBySymbol(ind.getTreeStruct(1));
				
				GenoVector gv = indexlist.getGenoVector(ind);
				
				ArrayList<Integer> geno = new ArrayList<>();
				
				for(int in : gv.G) {
					geno.add(in);
				}
				
				for(int j = 0; j<geno.size(); j++) {
					bw.write(""+geno.get(j)+"\t");
				}
				bw.write(""+ind.fitness.fitness()+"\n");
				bw.flush();
			}
			bw.close();
			
			bw = new BufferedWriter(currentBestF);
			for(LGPIndividual ind : currentBestList) {
				
//				int x = indexlist.getIndexBySymbol(ind.getTreeStruct(0));
//				int y = indexlist.getIndexBySymbol(ind.getTreeStruct(1));
				
				GenoVector gv = indexlist.getGenoVector(ind);
				
				ArrayList<Integer> geno = new ArrayList<>();
				
				for(int in : gv.G) {
					geno.add(in);
				}
				
				for(int j = 0; j<geno.size(); j++) {
					bw.write(""+geno.get(j)+"\t");
				}
				bw.write(""+ind.fitness.fitness()+"\n");
				bw.flush();
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void evaluateMetrics(final EvolutionState state, final int thread) {
		
		for(FLMetrics met : metric_list) {
			met.evaulate(state, thread, this);
		}
		
		isevaluated = true;
	}
	
	public void logMetrics(final EvolutionState state, final int thread, GPProblem problem, IndexList indexlist) {
		if(!tooutputMetric) return;
		
		if(!ispreprinted) {
			preprintFitnessLandscape(state, problem, indexlist);
		}
		
		if(!isevaluated) {
			evaluateMetrics(state, thread);
		}
		
		state.output.print("" + state.generation  + " " , statisticslog);
		for(FLMetrics met : metric_list) {
			state.output.print("" + met.getMetricValue()  + " " , statisticslog);
		}
		
		// we're done!
        state.output.println("", statisticslog);
	}
}
