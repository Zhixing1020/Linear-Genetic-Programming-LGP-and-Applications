package zhixing.cpxInd.individual;

import ec.DefaultsForm;
import ec.util.Parameter;

public class LGPDefaults  implements DefaultsForm
{
public static final String P_LGP = "lgp";

/** Returns the default base. */
public static final Parameter base()
    {
    return new Parameter(P_LGP);
    }
}