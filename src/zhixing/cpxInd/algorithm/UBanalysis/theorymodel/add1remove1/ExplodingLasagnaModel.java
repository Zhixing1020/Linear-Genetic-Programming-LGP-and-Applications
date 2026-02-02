package zhixing.cpxInd.algorithm.UBanalysis.theorymodel.add1remove1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.spiderland.Psh.booleanStack;

import ec.util.Parameter;
import zhixing.cpxInd.algorithm.UBanalysis.theorymodel.CombinationNum;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.breeder.MPMO_Breeder;

public class ExplodingLasagnaModel {
	
	protected int m_star; //the optimal program size, i.e., the smallest program size that contain the optimal solution.
	protected int numInstruction; //the number of total instruction
//	protected int numStayInstruction; //the number of staying instruction
	protected int numGstar_Mstar = 1; // the number of optimal solution with the optimal program size
	protected int numRegister; //the number of registers
	protected int numOutRegister; //the number of output registers
	protected double dpFactor = 1; //the duplicate factor
//	protected int max_m;  //the maximum program size
	
	protected String trainPath = System.getProperty("user.dir");
	protected final Boolean do_log = true;
	
	public ExplodingLasagnaModel (int m_s, int n, int numreg, int numoutreg, int ngs_ms) {
		setM_star(m_s);
		setNumInstruction(n);
		setNumRegister(numreg);
		setNumOutRegister(numoutreg);
		setNumGstar_Mstar(ngs_ms);
	}
	
	private void setM_star(int m_s) {
		m_star = m_s;
	}
	private void setNumInstruction(int n) {
		if(n <= 0) {
			System.err.print("the to-be-set value of numInstruction: "+ n +" is illegal\n");
			System.exit(1);
		}
		numInstruction = n;
	}

	private void setNumGstar_Mstar(int ngs_ms) {
		numGstar_Mstar = ngs_ms;
	}
	private void setNumRegister(int nr) {
		numRegister = nr;
	}
	private void setNumOutRegister(int nor) {
		numOutRegister = nor;
	}
	private void setDuplicateFactor(int m, double LB_d, double UB_d) {
		double num = getN_d_m_UB(m, (int) UB_d, LB_d, UB_d);
		
		dpFactor = num / Math.pow(numInstruction, m);
	}
	public void setTrainPath(String path) {
		trainPath = path;
	}
	
	private double get_d_LB(int m) {
		return Math.max(0, m_star - m);
	}
	private double get_d_UB(int m) {
		return m_star + m;
//		return get_d_LB(m) + 10;
	}
	private double get_mp_LB(int m, double d) {		
		double r_UB = Math.min((d-m_star+m)/2, d);
		r_UB = Math.min(m, r_UB);
		
		double mp_LB = m+d-2*r_UB;
		
//		return Math.max(m_star, m-d);
		return Math.max(m_star, mp_LB);
	}
	private double get_mp_UB(int m, double d) {
		
		double r_LB = Math.max(0, d-m_star);
		
		return m+d-2*r_LB;
	}
	
	private double getN_d_m_UB(int m, int d, double LB_d, double UB_d) {
		//the number of individuals with size m that access the optimal solution with the shortest path of d moves.
		//m: the current program size
		//d: the number of different instruction from the optimal solution
		
		if(d == LB_d) {
			if(d > 0)
				return getN_Gstar_m_UB(m_star)*CombinationNum.getCombinationNum(m_star, d);
			else
				return getN_Gstar_m_UB(m);
		}
		
		double LB_mp = get_mp_LB(m, d);
		double UB_mp = get_mp_UB(m, d);
		
		double res = 0;
		
		for(int mp = (int) LB_mp; mp <= UB_mp; mp++) {
//			res += getN_gm_constrfrom_gstar_mp_d(m, d, mp);
			double tmp = getN_d_mp_m_UB(m, d, mp);
			res += tmp;
		}		
		return res;
	}
	
	private double getN_d_m_LB(int m, int d, double LB_d, double UB_d) {
		//the number of individuals with size m that access the optimal solution with the shortest path of d moves.
		//m: the current program size
		//d: the number of different instruction from the optimal solution
		
		if(d == LB_d) {
			if(d > 0)
				return getN_Gstar_m_UB(m_star)*CombinationNum.getCombinationNum(m_star, d);
			else
				return getN_Gstar_m_LB(m);
		}
		
		double LB_mp = get_mp_LB(m, d);
		double UB_mp = get_mp_UB(m, d);
		
		double res = 0;
		
		for(int mp = (int) LB_mp; mp <= UB_mp; mp++) {
//			res += getN_gm_constrfrom_gstar_mp_d(m, d, mp);
			double tmp = getN_d_mp_m_LB(m, d, mp);
			res += tmp;
		}
		
		return res;
	}
	
	private double getN_d_mp_m_UB(int m, int d, int mp) {
		//the number of individuals with size m that access the optimal solution g*(mp) with the shortest path of d moves.
		
		//m: the current program size
		//d: the number of different instruction from the optimal solution
		//mp: the program size of optimal solutions
		
		if(mp < m_star) {
			return 0;
		}
		
		if( (mp - m + d) % 2 != 0) return 0;
		
		double res = 0;
		double C = 0;
		double num_add;

		int r = (int) ((mp - m + d)*0.5); //number of removing instructions
		
		if(r == 0 || r >= mp) C = 1;
		else C = CombinationNum.getCombinationNum(mp, r);
		
		double C2 = 1;
		if(mp - r > 0) C2 = CombinationNum.getCombinationNum(m, mp-r);
		
		num_add = C2 * Math.pow((numInstruction),  d-r);
		
//		res = C*num_add;
//		
//		double n_gs_mp = getN_Gstar_m(mp); //the total number of g*(mp)
//		
//		return n_gs_mp / (getBloatingFactor_LB(Math.max(mp-r, m_star), mp)) *res;
		
		double n_gs_mp = getN_Gstar_m_UB(mp); //the total number of g*(mp)
		
		double omega = get_omega(mp - r);
		
		double norm_LB = 1; //Math.pow((double)numInstruction*omega / numRegister , r); //getBloatingFactor_LB(Math.max(mp-r, m_star), mp);
		
		res = Math.min(Math.pow(numInstruction, mp-r) , n_gs_mp * C / norm_LB);
		
		res *= num_add;
		
		return res;
	}
	
	private double getN_d_mp_m_LB(int m, int d, int mp) {
		//the number of individuals with size m that access the optimal solution g*(mp) with the shortest path of d moves.
		
		//m: the current program size
		//d: the number of different instruction from the optimal solution
		//mp: the program size of optimal solutions
		
		if(mp < m_star) {
			return 0;
		}
		
		if( (mp - m + d) % 2 != 0) return 0;
		
		double res = 0;
		double C = 0;
		double num_add;

		int r = (int) ((mp - m + d)*0.5); //number of removing instructions
		
		if(r == 0 || r >= mp) C = 1;
		else C = CombinationNum.getCombinationNum(mp, r);
		
		double C2 = 1;
		if(mp - r > 0) C2 = CombinationNum.getCombinationNum(m, mp-r);
		
		num_add = C2 * Math.pow((numInstruction),  d-r);
		
		
//		double n_gs_mp = getN_Gstar_m_LB(mp); //the total number of g*(mp)
//		
//		double norm_UB = CombinationNum.getCombinationNum(mp, r) * Math.pow((numRegister - numOutRegister)*numInstruction/numRegister, r); //n_gs_mp; // getBloatingFactor(Math.max(mp-r, m_star), mp);
//		
//		res = Math.min(Math.pow(numInstruction, mp-r) , n_gs_mp * C / norm_UB);
//		
//		res *= num_add;
		
		double norm_UB = CombinationNum.getCombinationNum(mp + d - r, d - r);
		
		res = num_add / norm_UB;
		
		return res;
	}
	
	private double getN_Gstar_m_UB(int m) {
		return getBloatingFactor_UB(m_star, m)*numGstar_Mstar;
	}
	
	private double getN_Gstar_m_LB(int m) {
		return getBloatingFactor_LB(m_star, m)*numGstar_Mstar;
	}
	
	public double getBloatingFactor_UB(int m1, int m2) {
		//m1: the program size before bloating
		//m2: the program size after bloating
		
		if(m1 == m2 /*|| numStayInstruction == 0*/) return 1;
		
		if(m2<m1) {
			System.err.print("the program size after bloating must be larger than the one before bloating\n");
			System.exit(1);
		}
		
		double omega = get_omega(m1);
		
//		double comb = CombinationNum.getCombinationNum(m2, m2-m1);
		
		double ns = m2 * Math.pow((0.5/numRegister)*(1 + numRegister - numOutRegister - omega)*(numRegister - numOutRegister + omega)*numInstruction, m2-m1);
		
		return ns;
		
	}
	
	private double getBloatingFactor_LB(int m1, int m2) {
		
		if(m1 == m2 /*|| numStayInstruction == 0*/) return 1;
		
		if(m2<m1) {
			System.err.print("the program size after bloating must be larger than the one before bloating\n");
			System.exit(1);
		}
		
		double omega = get_omega(m1);
		
//		double comb = CombinationNum.getCombinationNum(m2, m2-m1);
		double ns = 0;
		for(double i = omega; i<=numRegister-numOutRegister; i++) {
			ns += Math.pow(i*numInstruction/numRegister, m2-m1);
		}
		
		return ns;
		
	}
	
	public double get_NonNeuBloatFactor_UB(int m1, int m2) {
		if(m1 == m2 /*|| numStayInstruction == 0*/) return 1;
		
		if(m2<m1) {
			System.err.print("the program size after bloating must be larger than the one before bloating\n");
			System.exit(1);
		}
		
		double lambda = get_lambda(m1);
		
		double ns = m2 * Math.pow((0.5/numRegister)*(1 + lambda - numOutRegister)*(numOutRegister + lambda)*numInstruction, m2-m1);
		
		return ns;
	}
	
	private double get_omega(int m1) {
		//m1: the program size before neutral bloating
		//\omega = max(1, R-\rho-m1)
		return Math.max(1, numRegister - numOutRegister - m1);
	}
	
	private double get_lambda(int m1) {
		//m1: the program size before non-neutral bloating
		//\lambda = min(R, \rho+m1)
		return Math.min(numRegister, numOutRegister + m1); 
	}
	
	public double getExpectationUB_dstar_m(int m, boolean log) {
		//the expectation of the number of different instructions from optimal solutions when the program size is m
		//m: the current program size
		
		double LB_d = get_d_LB(m);
		double UB_d = get_d_UB(m);
		
		double res = 0;
		

		Vector<Double> countN = countN_m(m);
//		double sum = 0.;
//		for(Double c : countN) {
//			sum += c;
//		}
//		System.out.println(dpFactor);
//		double norm = (1. / (numInstruction*((Math.pow(numInstruction, max_m) - 1)/(numInstruction - 1))));
		
		FileWriter f;
		Vector<Integer> d_vector = new Vector<>();
		Vector<Double> p_vector = new Vector<>();
		
		for(int i = 0, d = (int) LB_d; d<= UB_d; i++, d++) {
//			double p = countN.get(i) / Math.pow(numInstruction, m);
			double p = countN.get(i) ; 
			
//			System.out.print("d=" + d +":"+ p + "\t");
//			double p = countN.get(i) * norm;
			res += p * d; //(1./(d+0.001));
			
			d_vector.add(d);
			p_vector.add(p);
		}
		
		if(log) {
			try {
				f = new FileWriter( trainPath + "\\m_d.txt", true);
				
				BufferedWriter bw = new BufferedWriter(f);
				
				bw.write("m="+m+"\t");
				
				for(int i = 0; i<d_vector.size(); i++) {
					bw.write("" + d_vector.get(i) + "\t");
				}
				bw.write("\n");
				
				bw.write("m="+m+"\t");
				
				for(int i = 0; i<p_vector.size(); i++) {
					bw.write("" + p_vector.get(i) + "\t");
				}
				bw.write("\n");
				
				bw.flush();
				
				bw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		System.out.println();
		return res;
	}
	
	public double getExpectation_dstar_m(int m, boolean log) {
		//the expectation of the number of different instructions from optimal solutions when the program size is m
		//m: the current program size
		
		double LB_d = get_d_LB(m);
		double UB_d = get_d_UB(m);
		
		double res = 0;
		

		Vector<Double> countN = countN_m_estimate(m);
//		double sum = 0.;
//		for(Double c : countN) {
//			sum += c;
//		}
//		System.out.println(dpFactor);
//		double norm = (1. / (numInstruction*((Math.pow(numInstruction, max_m) - 1)/(numInstruction - 1))));
		
		FileWriter f;
		Vector<Integer> d_vector = new Vector<>();
		Vector<Double> p_vector = new Vector<>();
		
		for(int i = 0, d = (int) LB_d; d<= UB_d; i++, d++) {
//			double p = countN.get(i) / Math.pow(numInstruction, m);
			double p = countN.get(i) ; 
			
//			System.out.print("d=" + d +":"+ p + "\t");
//			double p = countN.get(i) * norm;
			res += p * d; //(1./(d+0.001));
			
			d_vector.add(d);
			p_vector.add(p);
		}
		
		if(log) {
			try {
				f = new FileWriter( trainPath + "\\m_d.txt", true);
				
				BufferedWriter bw = new BufferedWriter(f);
				
				bw.write("m="+m+"\t");
				
				for(int i = 0; i<d_vector.size(); i++) {
					bw.write("" + d_vector.get(i) + "\t");
				}
				bw.write("\n");
				
				bw.write("m="+m+"\t");
				
				for(int i = 0; i<p_vector.size(); i++) {
					bw.write("" + p_vector.get(i) + "\t");
				}
				bw.write("\n");
				
				bw.flush();
				
				bw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		System.out.println();
		return res;
	}
	
	public double getStdUB_dstar_m(int m) {
		//the expectation of the number of different instructions from optimal solutions when the program size is m
		//m: the current program size
		
		double LB_d = get_d_LB(m);
		double UB_d = get_d_UB(m);
		
		double res = 0;
		
//		Vector<Double> countN = countN_m(m);
//		double sum = 0.;
//		for(Double c : countN) {
//			sum += c;
//		}
		
		double exp = getExpectationUB_dstar_m(m, false);
		
		for(int i = 0, d = (int) LB_d; d<= UB_d; i++, d++) {
			
			res += (d - exp)*(d - exp);
			
		}
		
		res = Math.sqrt(res / (UB_d - LB_d));

//		System.out.println();
		return res;
	}
	
	public double getStd_dstar_m(int m) {
		//the expectation of the number of different instructions from optimal solutions when the program size is m
		//m: the current program size
		
		double LB_d = get_d_LB(m);
		double UB_d = get_d_UB(m);
		
		double res = 0;
		
//		Vector<Double> countN = countN_m(m);
//		double sum = 0.;
//		for(Double c : countN) {
//			sum += c;
//		}
		
		double exp = getExpectation_dstar_m(m, false);
		
		for(int i = 0, d = (int) LB_d; d<= UB_d; i++, d++) {
			
			res += (d - exp)*(d - exp);
			
		}
		
		res = Math.sqrt(res / (UB_d - LB_d));

//		System.out.println();
		return res;
	}
	
	public Vector<Double> countN_m(int m){
		Vector<Double> res = new Vector<>();  //the number of accessible LGP solutions with a given program size and with different editing distance
//		Vector<Double> res_unreach = new Vector<>(); //the number of unaccessible LGP solutions with different program size because of the program size limit
		
		Vector<Double> res_LB = new Vector<>();
		
		double LB_d = get_d_LB(m);
		double UB_d = get_d_UB(m);
		
		for(int d = (int) LB_d; d<= UB_d; d++) {
			if(m > 0)
				res.add(getN_d_m_UB(m,d, LB_d, UB_d));
			else
				res.add(0.);
			
		}
		
		for(int d = (int) LB_d; d<= UB_d; d++) {
			if(m > 0)
				res_LB.add(getN_d_m_LB(m,d, LB_d, UB_d));
			else
				res_LB.add(0.);
			
		}
		
		if(m <= 0) return res;
		
		//get the duplicated factor
		Vector<Double> tmpres = new Vector<>();
		tmpres.add(res.get(0));
//		double sum = res.get(0);
//		double sum = res_LB.get(res_LB.size() - 1);
		double sum = Math.pow(numInstruction, m);
		for(int i = 1; i<res.size(); i++) {
			
//			sum += Math.max(1, res_LB.get(i) - res.get(i-1));
			
			tmpres.add(Math.max(0, res.get(i) - res_LB.get(i-1)));
			
//			sum += res.get(i);
			
		}
		
		//remove the unaccessible solutions and scale
		for(int i = 0; i<res.size(); i++) {
//			double tmp = Math.max(res.get(i) - res_unreach.get(i), 0); 
//			
//			if(tmp > 0)	tmp /= dpFactor;
			
//			res.set(i, tmpres.get(i) / dpFactor);
			res.set(i, tmpres.get(i) / sum);
		}
		
		return res;
	}
	
	public Vector<Double> countN_m_estimate(int m){
		Vector<Double> res = new Vector<>();  //the number of accessible LGP solutions with a given program size and with different editing distance
//		Vector<Double> res_unreach = new Vector<>(); //the number of unaccessible LGP solutions with different program size because of the program size limit
		
		Vector<Double> res_LB = new Vector<>();
		
		double LB_d = get_d_LB(m);
		double UB_d = get_d_UB(m);
		
		for(int d = (int) LB_d; d<= UB_d; d++) {
			if(m > 0)
				res.add(getN_d_m_UB(m,d, LB_d, UB_d));
			else
				res.add(0.);
			
		}
		
		for(int d = (int) LB_d; d<= UB_d; d++) {
			if(m > 0)
				res_LB.add(getN_d_m_LB(m,d, LB_d, UB_d));
			else
				res_LB.add(0.);
			
		}
		
		if(m <= 0) return res;
		
		//get the duplicated factor
		Vector<Double> tmpres = new Vector<>();
		tmpres.add(res.get(0));
//		double sum = res.get(0);
//		double sum = res_LB.get(res_LB.size() - 1);
		double sum = 0;
		for(int i = 1; i<res.size(); i++) {
			
//			sum += Math.max(1, res_LB.get(i) - res.get(i-1));
			
			tmpres.add(Math.max(0, res.get(i) - res_LB.get(i-1)));
			
//			sum += res.get(i);
			sum += Math.max(0, res.get(i) - res_LB.get(i-1));
		}
		
		//remove the unaccessible solutions and scale
		for(int i = 0; i<res.size(); i++) {
//			double tmp = Math.max(res.get(i) - res_unreach.get(i), 0); 
//			
//			if(tmp > 0)	tmp /= dpFactor;
			
//			res.set(i, tmpres.get(i) / dpFactor);
			res.set(i, tmpres.get(i) / sum);
		}
		
		return res;
	}
	
	public double getPcon_m_V(int m, int V, double d) {
		//the probability of constructive moves after moving the program size m by V.
		//V: the variation step size of program size. >0: increase program size, <0: decrease the program size
		//d: the instruction editing distance of an LGP solution
		
		double LB_d = get_d_LB(m);
		double UB_d = get_d_UB(m);
		
		double LB_dp = get_d_LB(m+V);
		double UB_dp = get_d_UB(m+V);
		
//		double norm = (1. / (numInstruction*((Math.pow(numInstruction, max_m) - 1)/(numInstruction - 1))));
		
		double res = 0;
		
		Vector<Double> countN = countN_m_estimate(m);
//		double sum = 0.;
//		for(Double c : countN) {
//			sum += c;
//		}
		Vector<Double> countN_m2 = countN_m_estimate(m+V);
//		double sum2 = 0.;
//		for(Double c : countN_m2) {
//			sum2 += c;
//		}
		Vector<Double> countN_m3 = countN_m_estimate(m - V);
//		double sum3 = 0.;
//		for(Double c : countN_m3) {
//			sum3 += c;
//		}
		
		double tmpres = 0;
		for(int dp = (int) LB_dp, j = 0; dp<Math.min(d, UB_dp) ; dp++, j++) {
			
			tmpres += (d - dp) * countN_m2.get(j) ;
		}
		
		res = tmpres;
		
		return res;
	}
	
	public double getPneu_m_V(int m, int V, double d) {
		//the probability of constructive moves after moving the program size m by V.
		//V: the variation step size of program size. >0: increase program size, <0: decrease the program size
		//d: the instruction editing distance of an LGP solution
		
		double LB_d = get_d_LB(m);
		double UB_d = get_d_UB(m);
		
		double LB_dp = get_d_LB(m+V);
		double UB_dp = get_d_UB(m+V);
		
//		double norm = (1. / (numInstruction*((Math.pow(numInstruction, max_m) - 1)/(numInstruction - 1))));
		
		double res = 0;
		
		Vector<Double> countN = countN_m(m);
//		double sum = 0.;
//		for(Double c : countN) {
//			sum += c;
//		}
		Vector<Double> countN_m2 = countN_m(m+V);
//		double sum2 = 0.;
//		for(Double c : countN_m2) {
//			sum2 += c;
//		}
		Vector<Double> countN_m3 = countN_m(m - V);
//		double sum3 = 0.;
//		for(Double c : countN_m3) {
//			sum3 += c;
//		}
		
		double tmpres = 0;
		if(d >= LB_dp) {
			for(int dp = (int) LB_dp, j = 0; dp< UB_dp ; dp++, j++) {

				if(dp >= d) {
					tmpres = countN_m2.get(j) ;
					break;
				}
				
			}
		}

		res = tmpres;
		
		return res;
	}
	
	public static void main(String[] args) {
		int idx = 0;
	       
		String trainPath = System.getProperty("user.dir"); 
		if(args.length > idx) {
			trainPath = args[idx];
		}
        idx ++;
		
        int m_s = 11;
        int n = 2816;
        int numreg = 8;
        int numoutreg = 1;
        int ngs_ms = 1;
        
//		ExplodingLasagnaModel model = new ExplodingLasagnaModel(8, 2816, 8, 1, 4*10^9);//R1
		ExplodingLasagnaModel model = new ExplodingLasagnaModel(11, 2816, 8, 1, 1);//Nguyen4
//      ExplodingLasagnaModel model = new ExplodingLasagnaModel(6, 2816, 8, 1, 1); //Nguyen5
//        ExplodingLasagnaModel model = new ExplodingLasagnaModel(7, 3328, 8, 1, 1); //Keijzer11
		model.setTrainPath(trainPath);
		
//		Model model = new Model(1, 1000, 4, 1, 1, 30);
		
		//the expectation of d* of different program size m
		try {
			FileWriter f = new FileWriter( trainPath + "\\m_d.txt");
			FileWriter f2 = new FileWriter( trainPath + "\\m_Ed.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double expectation = 0;
		double standarddevi = 0;
		ArrayList<Double> Ed = new ArrayList<>();
		
		for(int m = 1; m<=100; m++) {
			
			expectation = model.getExpectationUB_dstar_m(m, !model.do_log);
			standarddevi = model.getStdUB_dstar_m(m);
			
			System.out.println("m=" + m + ": exp=" + expectation + ", std="+standarddevi+",   "+ (expectation - standarddevi));
			Ed.add(expectation);
		}
		
		if(!model.do_log) {
			try {
				FileWriter f = new FileWriter( trainPath + "\\m_Ed.txt", true);
				
				BufferedWriter bw = new BufferedWriter(f);
				
				bw.write("m\tE(min d|m)\n");
				
				for(int m = 1; m<=Ed.size(); m++) {
					bw.write(""+m+"\t"+Ed.get(m-1)+"\n");
				}
				
				bw.flush();
				
				bw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.print("\n\n===========\n\n");
		
		//the probability of constructive moves of adding / removing one instruction at the program size m
//		for(int m = 1; m<=100; m++) {
//			
//			expectation = model.getExpectation_dstar_m(m, !model.do_log);
//			standarddevi = model.getStd_dstar_m(m);
//			
//			double coef = 0;
//			double d_LB = Math.max(0, m_s - m);
//			double d_UB = m_s + m;
//			
//			double set_d = d_UB; //expectation + (d_UB - expectation)/2;//d_LB+1; //d_LB+ (expectation - d_LB)/2;  //expectation - coef*standarddevi;
//			
//			double pcon_add = model.getPcon_m_V(m, 1, Math.max(model.get_d_LB(m+1), set_d));
//			double pcon_remove = model.getPcon_m_V(m, -1, Math.max(model.get_d_LB(m-1), set_d));
//			
//			double pneu_add = model.getPneu_m_V(m, 1, Math.max(model.get_d_LB(m+1), set_d));
//			double pneu_remove = model.getPneu_m_V(m, -1, Math.max(model.get_d_LB(m-1), set_d));
//			
//			String move = "add";
//			double add = pcon_add + pneu_add;
//			double remove = pcon_remove + pneu_remove;
//			if(add < remove) move = "remove";
//			if(add == remove ) move = "draw";
//			System.out.println("m=" + m + ": " + add + " vs. "+ remove +" ==> "+move+"\t"+ (add - remove)/Math.max(add, remove));
//			
//			
//			if(model.do_log) {
//				try {
//					FileWriter f = new FileWriter( trainPath + "\\m_d.txt", true);
//					
//					BufferedWriter bw = new BufferedWriter(f);
//					
//					bw.write(""+m+"\t"+(add - remove)/Math.max(add, remove)+"\n");
//					
//					bw.flush();
//					
//					bw.close();
//					
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
		
		for(int m = 1; m<=100; m++) {
			expectation = model.getExpectation_dstar_m(m, !model.do_log);
			standarddevi = model.getStd_dstar_m(m);
			
			double coef = 0;
			double d_LB = Math.max(0, m_s - m);
			double d_UB = m_s + m;
			
			for(double d = d_LB+1; d<=d_UB; d++) {
				double pcon_add = model.getPcon_m_V(m, 1, Math.max(model.get_d_LB(m+1), d));
				double pcon_remove = model.getPcon_m_V(m, -1, Math.max(model.get_d_LB(m-1), d));
				
				double pneu_add = model.getPneu_m_V(m, 1, Math.max(model.get_d_LB(m+1), d));
				double pneu_remove = model.getPneu_m_V(m, -1, Math.max(model.get_d_LB(m-1), d));
				
				double add = pcon_add + pneu_add;
				double remove = pcon_remove + pneu_remove;
				
				if(model.do_log) {
					try {
						FileWriter f = new FileWriter( trainPath + "\\m_d.txt", true);
						
						BufferedWriter bw = new BufferedWriter(f);
						
						bw.write(""+m+"\t"+d+"\t"+(add - remove)/Math.max(add, remove)+"\n");
						
						bw.flush();
						
						bw.close();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
