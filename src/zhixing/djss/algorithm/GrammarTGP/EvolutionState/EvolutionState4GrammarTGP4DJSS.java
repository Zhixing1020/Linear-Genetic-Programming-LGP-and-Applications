package zhixing.djss.algorithm.GrammarTGP.EvolutionState;

import java.io.File;
import java.util.LinkedList;

import yimei.jss.gp.GPRuleEvolutionState;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.DoubleERC;
import yimei.jss.gp.terminal.JobShopAttribute;

public class EvolutionState4GrammarTGP4DJSS extends GPRuleEvolutionState {
	@Override
	public void initTerminalSet() {
		if (terminalFrom.equals("basic")) {
			initBasicTerminalSet();
		}
		else if (terminalFrom.equals("relative")) {
			initRelativeTerminalSet();
		}
		else if (terminalFrom.equals("relative4Grammar")) {
			initRelativeTerminalSet4Grammar();
		}
		else {
			String terminalFile = "terminals/" + terminalFrom;
			initTerminalSetFromCsv(new File(terminalFile));
		}

		if (includeErc)
			terminals.add(new DoubleERC());
	}
	
	public void initRelativeTerminalSet4Grammar() {
		terminals = new LinkedList<>();
		for (JobShopAttribute a : JobShopAttribute.relativeAttributes4grammar()) {
			terminals.add(new AttributeGPNode(a));
		}
	}
}
