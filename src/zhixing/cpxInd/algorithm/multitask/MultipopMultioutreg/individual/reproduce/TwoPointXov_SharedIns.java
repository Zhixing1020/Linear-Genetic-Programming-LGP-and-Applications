package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce;

import ec.BreedingSource;
import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.djss.algorithm.multitask.MultipopMultioutreg.individual.reproduce.TwoPointXov_SpecificIns4DJSS;

public class TwoPointXov_SharedIns extends TwoPointXov_SpecificIns4DJSS{
	public static final String SHARED_2POINT_CROSSOVER_MULTITASK = "shared_2p_cross_MT"; 
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(SHARED_2POINT_CROSSOVER_MULTITASK);
		
		tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
        def.push(P_TOSS),false);
		
		//tossSecondParent = false; //because in MPMO, individuals from different sub populations have different number of objectives, they cannot be exchanged directly
		
		//set its own breeding source
		for(int x=0;x<sources.length;x++)
        {
        Parameter p = base.push(P_SOURCE).push(""+x);
        Parameter d = def.push(P_SOURCE).push(""+x);

        String s = state.parameters.getString(p,d);
        if (s!=null && s.equals(V_SAME))
            {
            if (x==0)  // oops
                state.output.fatal(
                    "Source #0 cannot be declared with the value \"same\".",
                    p,d);
            
            // else the source is the same source as before
            sources[x] = sources[x-1];
            }
        else 
            {
            sources[x] = (BreedingSource)
                (state.parameters.getInstanceForParameter(
                    p,d,BreedingSource.class));
            sources[x].setup(state,p);
            }
        }
		
	}
	
	protected int getLegalTrialPopulation(EvolutionState state, int thread, int cursubpop){
		int trial = cursubpop;
		if(cursubpop==0 && state.population.subpops.length > 1){
			trial = 1 + state.random[thread].nextInt(state.population.subpops.length - 1);
		}
		else if(cursubpop > 0){
			trial = 0;
		}
		
//		int trial = state.random[thread].nextInt(state.population.subpops.length);
		return trial;
		
//        return 0;
	}
}
