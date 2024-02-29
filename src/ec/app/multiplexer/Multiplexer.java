/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.multiplexer;
import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

/* 
 * Multiplexer.java
 * 
 * Created: Mon Nov  1 15:46:19 1999
 * By: Sean Luke
 */

/**
 * Multiplexer implements the family of <i>n</i>-Multiplexer problems.
 *
 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt><br>
 <font size=-1>classname, inherits or == ec.app.multiplexer.MultiplexerData</font></td>
 <td valign=top>(the class for the prototypical GPData object for the Multiplexer problem)</td></tr>
 <tr><td valign=top><i>base</i>.<tt>bits</tt><br>
 <font size=-1>1, 2, or 3</font></td>
 <td valign=top>(The number of address bits (1 == 3-multiplexer, 2 == 6-multiplexer, 3==11-multiplexer)</td></tr>
 </table>

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>data</tt></td>
 <td>species (the GPData object)</td></tr>
 </table>
 *
 * @author Sean Luke
 * @version 1.0 
 */

public class Multiplexer extends GPProblem implements SimpleProblemForm
    {
    private static final long serialVersionUID = 1;

    public static final int NUMINPUTS = 20;
    public static final String P_NUMBITS = "bits";

    public int bits;  // number of bits in the data

    public void setup(final EvolutionState state,
        final Parameter base)
        {
        // very important, remember this
        super.setup(state,base);

        // not using any default base -- it's not safe

        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof MultiplexerData))
            state.output.fatal("GPData class must subclass from " + MultiplexerData.class,
                base.push(P_DATA), null);

        // I figure 3 bits is plenty -- otherwise we'd be dealing with
        // REALLY big arrays!
        bits = state.parameters.getIntWithMax(base.push(P_NUMBITS),null,1,3);
        if (bits<1)
            state.output.fatal("The number of bits for Multiplexer must be between 1 and 3 inclusive");
        }


    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum)
        {
        if (!ind.evaluated)  // don't bother reevaluating
            {
            MultiplexerData input = (MultiplexerData)(this.input);
        
            input.status = (byte)bits;

            int sum = 0;
                
            ((GPIndividual)ind).getTree(0).child.eval(
                state,threadnum,input,stack,((GPIndividual)ind),this);//=========zhixing, 2021.3.26
                
            if (bits==1)
                {
                byte item1 = input.dat_3;
                byte item2 = Fast.M_3[Fast.M_3_OUTPUT];
                for(int y=0;y<MultiplexerData.MULTI_3_BITLENGTH;y++)
                    {
                    // if the first bit matches, grab it as:
                    // sum += 1 and not(item1 xor item2)
                    // that is, if item1 and item2 are the SAME at bit 1
                    // then we increase
                    sum += ( 1 & ((item1 ^ item2) ^ (-1)));
                    // shift to the next bit
                    item1 = (byte)(item1 >>> 1);
                    item2 = (byte)(item2 >>> 1);
                    }
                }
            else if (bits==2)
                {
                long item1 = input.dat_6;
                long item2 = Fast.M_6[Fast.M_6_OUTPUT];
                for(int y=0;y<MultiplexerData.MULTI_6_BITLENGTH;y++)
                    {
                    // if the first bit matches, grab it
                    sum += ( 1L & ((item1 ^ item2) ^(-1L)));
                    // shift to the next bit
                    item1 = item1 >>> 1;
                    item2 = item2 >>> 1;
                    }
                }
            else // bits==3
                {
                long item1, item2;
                for(int y=0;y<MultiplexerData.MULTI_11_NUM_BITSTRINGS; y++)
                    {
                    item1 = input.dat_11[y];
                    item2 = Fast.M_11[Fast.M_11_OUTPUT][y];
                    //System.out.println("" + y + " ### " + item1 + " " + item2);
                    for(int z=0;z<MultiplexerData.MULTI_11_BITLENGTH;z++)
                        {
                        // if the first bit matches, grab it
                        sum += ( 1L & ((item1 ^ item2) ^(-1L)));
                        // shift to the next bit
                        item1 = item1 >>> 1;
                        item2 = item2 >>> 1;
                        }
                    }
                }
                
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            if (bits==1)
                f.setStandardizedFitness(state, (Fast.M_3_SIZE - sum));
            else if (bits==2)
                f.setStandardizedFitness(state, (Fast.M_6_SIZE - sum));
            else // (bits==3)
                f.setStandardizedFitness(state, (Fast.M_11_SIZE - sum));
            f.hits = sum;
            ind.evaluated = true;
            }
        }
    }
