package zhixing.djss.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.TerminalERCUniform;
import zhixing.djss.individual.LGPIndividual4DJSS;

public class ReadConstantRegisterGPNode extends TerminalERCUniform{
	@Override
	public void eval(EvolutionState state, int thread, GPData input,
            ADFStack stack, GPIndividual individual, Problem problem){
		
		//DoubleData data = ((DoubleData)input);
        //data.value = (double)((LGPIndividual)individual).getConstantRegisters()[((AttributeGPNode)terminal).getJobShopAttribute().ordinal()];
	}
}
