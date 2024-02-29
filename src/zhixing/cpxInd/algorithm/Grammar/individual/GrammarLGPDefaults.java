package zhixing.cpxInd.algorithm.Grammar.individual;

import ec.DefaultsForm;
import ec.util.Parameter;

public class GrammarLGPDefaults implements DefaultsForm
{
public static final String P_GrammarLGP = "grammar_lgp";

/** Returns the default base. */
public static final Parameter base()
    {
    return new Parameter(P_GrammarLGP);
    }
}
