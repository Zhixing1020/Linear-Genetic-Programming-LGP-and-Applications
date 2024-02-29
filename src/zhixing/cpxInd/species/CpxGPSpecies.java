package zhixing.cpxInd.species;

import java.io.DataInput;
import java.io.IOException;
import java.io.LineNumberReader;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Species;
import ec.gp.GPDefaults;
import ec.gp.GPIndividual;
import ec.gp.GPSpecies;
import ec.util.Parameter;
import zhixing.cpxInd.individual.CpxGPIndividual;
//import zhixing.jss.cpxInd.individual.CpxGPIndividual;

public class CpxGPSpecies extends GPSpecies {
	public static final String P_GPSPECIES = "species";

    public Parameter defaultBase()
        {
        return GPDefaults.base().push(P_GPSPECIES);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup_extensive(state,base);

        // check to make sure that our individual prototype is a GPIndividual
        if (!(i_prototype instanceof CpxGPIndividual))
            state.output.fatal("The Individual class for the Species " + getClass().getName() + " is must be a subclass of zhixing.jss.cpxInd.individual.CpxGPIndividual.", base );
        }    

    public Individual newIndividual(EvolutionState state, int thread) 
        {
        CpxGPIndividual newind = ((CpxGPIndividual)(i_prototype)).lightClone();
        
        // Initialize the trees
        newind.rebuildIndividual(state, thread);

        // Set the fitness
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false;

        // Set the species to me
        newind.species = this;
                
        // ...and we're ready!
        return newind;
        }


    // A custom version of newIndividual() which guarantees that the
    // prototype is light-cloned before readIndividual is issued
    public Individual newIndividual(final EvolutionState state,
        final LineNumberReader reader)
        throws IOException
        {
    	CpxGPIndividual newind = ((CpxGPIndividual)(i_prototype)).lightClone();
                
        // Set the fitness -- must be done BEFORE loading!
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // load that sucker
        newind.readIndividual(state,reader);

        // Set the species to me
        newind.species = this;

        // and we're ready!
        return newind;  
        }


    // A custom version of newIndividual() which guarantees that the
    // prototype is light-cloned before readIndividual is issued
    public Individual newIndividual(final EvolutionState state,
        final DataInput dataInput)
        throws IOException
        {
    	CpxGPIndividual newind = ((CpxGPIndividual)(i_prototype)).lightClone();
        
        // Set the fitness -- must be done BEFORE loading!
        newind.fitness = (Fitness)(f_prototype.clone());
        newind.evaluated = false; // for sanity's sake, though it's a useless line

        // Set the species to me
        newind.species = this;

        // load that sucker
        newind.readIndividual(state,dataInput);

        // and we're ready!
        return newind;  
        }
}
