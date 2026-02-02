package zhixing.cpxInd.algorithm.UBanalysis.theorymodel;

public class CombinationNum {

	static public double getCombinationNum(int m, int n) {
		//m: the total number of elements
		//n: the number of selected elements
		
		if(n < 0) {
			System.err.print("the value of n: "+n+" must be >=1\n");
			System.exit(1);
		}
		if(n == 0) {
			return 1;
		}
		if(m<n) {
			System.err.print("the value of m: "+m+" must be >= the value of n: "+n+".\n");
			System.exit(1);
		}
		
		n = Math.min(n, m-n);
		
		double divident = 1;
		double divisor = 1;
		
		for(int i = 0; i<n; i++) {
			divident *= m - i;
		}
		
		divisor = getFactorial(n);
		
		return divident / divisor;
	}
	
	static public double getFactorial(int n) {
		if(n < 0) {
			System.err.print("the input of factorial must be >0\n");
			System.exit(1);
		}
		double res = 1;
		
		for(int i = 0; i<n; i++) {
			res *= n - i;
		}
		
		return res;
	}
}
