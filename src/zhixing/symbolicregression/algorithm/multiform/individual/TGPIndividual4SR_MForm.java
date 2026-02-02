package zhixing.symbolicregression.algorithm.multiform.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.algorithm.Multiform.individual.TGPIndividual4MForm;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.TGPInterface4Problem;
import zhixing.symbolicregression.individual.TGPInterface4SR;

public class TGPIndividual4SR_MForm extends TGPIndividual4MForm implements TGPInterface4SR, TGPInterface4Problem{
	@Override
	public double execute(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
			Problem problem) {
//		if(input == null) {
//			input = new DoubleData();
//		}
		DoubleData tmp = new DoubleData();
		
        //assuming there is only one tree
        trees[0].child.eval(state, thread, tmp, stack, this, problem);

        input = tmp;
        return tmp.value;
	}
}
