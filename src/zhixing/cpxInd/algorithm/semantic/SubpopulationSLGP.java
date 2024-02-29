package zhixing.cpxInd.algorithm.semantic;

import ec.ECDefaults;
import ec.EvolutionState;
import ec.Species;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public class SubpopulationSLGP extends ec.Subpopulation {

	public static final String P_SUBPOPULATION = "subpop_sem";
	public static final String P_SEMANTICLIBRARY = "semantic_library";
	
	public SemanticLibrary semanticLib;
	
	public Parameter defaultBase()
    {
		return new Parameter(P_SUBPOPULATION);
    }
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = defaultBase();
		
		semanticLib = (SemanticLibrary) state.parameters.getInstanceForParameter(
	            base.push(P_SEMANTICLIBRARY),def.push(P_SEMANTICLIBRARY), Object.class);
		semanticLib.setup(state, base.push(P_SEMANTICLIBRARY));
		
	}
}
