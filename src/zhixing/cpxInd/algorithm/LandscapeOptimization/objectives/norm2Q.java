package zhixing.cpxInd.algorithm.LandscapeOptimization.objectives;

import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;

public class norm2Q {
	//norm2Q: ||G_j - G_i||_2, G_j=theta_j * I
	
	public static double Q(final GenoVector g1, final GenoVector g2) {
		double res = 0;
		
		if(g1.length != g2.length) {
			System.err.print("inconsistent GenoVector when calculating Q");
			System.exit(1);
		}
		
		for(int k = 0; k<g1.length; k++) {
			if(g1.G[k] < 0 && g2.G[k] < 0) break;
			res += Math.pow(subQ( g1.G[k], g2.G[k]),2);
		}
		
		return res;
	}
	
	public static double pQpI(final GenoVector[] GA1, final short [][][] theta1, final GenoVector[] GA2, final short [][][] theta2, 
			int gj, int gi, int l) {
		// get the partial Q partial I_l
		//gj, gi: indexes of individuals,  l: index of index list 

		double dQ = 0;
		
		for(int k = 0; k<GA1[gj].length; k++) {
			
			if(GA1[gj].G[k] < 0 && GA2[gi].G[k] < 0) break;
			
			dQ += subQ(GA1[gj].G[k], GA2[gi].G[k])*2*(theta1[gj][k][l] - theta2[gi][k][l]);
		}
		
		return dQ;
	}
	
	public static double subQ(int a, int b) {
		if(a >= 0 && b >= 0) {
			return a - b;
		}
		else if(a >0 || b>0) {
			return 1;
		}
		return 0;
	}
}
