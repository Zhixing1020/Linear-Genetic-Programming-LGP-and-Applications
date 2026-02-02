package zhixing.cpxInd.algorithm.UBanalysis.MovingRate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.spiderland.Psh.intStack;

import zhixing.cpxInd.algorithm.UBanalysis.theorymodel.CombinationNum;
import zhixing.cpxInd.algorithm.UBanalysis.theorymodel.add1remove1.ExplodingLasagnaModel;

public class MovingRate_Comp {
	
	final static private int MAX_u = 35;
	final static private int MAX_m = 100;
	final static private int MAX_d = 10;
	final static private int n = 2816; //Nguyen4
	final static private int m_s = 11;
	
	private static double get_Econ_d(ExplodingLasagnaModel model, int d, int m, int u) {
		
//		System.out.print("add:\n");
		
		double sum1 = 0;
		double sum1_tmp = 0;
		for(int i = 1; i<=Math.min(u, d); i++) {
			sum1_tmp = 0;
			for(int j = 0; j<=Math.min(Math.floor((u-i)/2.), d-i); j++) {
				double tmp = CombinationNum.getCombinationNum(d, i+j) * model.get_NonNeuBloatFactor_UB(m+i+j, m+i+2*j) * model.getBloatingFactor_UB(m+i+2*j, m+u);
				sum1_tmp += tmp;
			}
			sum1_tmp /=Math.min(Math.floor((u-i)/2.), d-i)+1;
			
			sum1_tmp /= Math.max(sum1_tmp, CombinationNum.getCombinationNum(m+u, u)* Math.pow(n, u)); //CombinationNum.getCombinationNum(m+u, u)* Math.pow(n, u);
			sum1 += i*sum1_tmp;
			
//			System.out.print(i+"\t"+sum1_tmp+"\n");
		}
		sum1 /= Math.min(u, d);
		
		
//		System.out.print("\nremove:\n");
		//get the sum of R(i)
		int rp_UB = (int) Math.floor(Math.min(Math.min(d, m), (d+m-1)/2.));
		int dd = Math.min(rp_UB, u);
		
		double sum_Ri = 0;
		for(int i = 1; i<=dd; i++) {
			double sum_Ri_tmp = 0;
			for(int j = 0; j<=Math.min(Math.floor((u-i)/2), rp_UB-i); j++) {
				double tmp = 0;
				if(rp_UB >= i+j && m-i-j >= u-i-j && m>=u) {
					tmp = CombinationNum.getCombinationNum(rp_UB, i+j) 
							* CombinationNum.getCombinationNum(m-i-j, u-i-j);
				}
				
				sum_Ri_tmp += tmp;
			}
			sum_Ri_tmp /= Math.min(Math.floor((u-i)/2), rp_UB-i)+1;
			
			sum_Ri += sum_Ri_tmp;
			
		}
		
		double sum2 = 0;
		double sum2_tmp = 0;
		double duplicate = 0;
		for(int i = 1; i<=dd /*&& duplicate < 1*/; i++) {
			sum2_tmp = 0;
			for(int j = 0; j<=Math.min(Math.floor((u-i)/2), rp_UB-i); j++) {
				double tmp = 0;
				if(rp_UB >= i+j && m-i-j >= u-i-j && m>=u) {
					tmp = CombinationNum.getCombinationNum(rp_UB, i+j) 
							* CombinationNum.getCombinationNum(m-i-j, u-i-j);
				}
				
				sum2_tmp += tmp;
			}
			sum2_tmp /= Math.min(Math.floor((u-i)/2), rp_UB-i)+1;
			
			double eta_r = 1;			
			if(m>=u) {
//				if(sum2_tmp > CombinationNum.getCombinationNum(m, u)) { //at least   duplication
//					eta_r = sum2_tmp / ( CombinationNum.getCombinationNum(m, u));
//				}
				
				sum2_tmp /= Math.max(sum2_tmp,CombinationNum.getCombinationNum(m, u)); // (CombinationNum.getCombinationNum(m, u)*eta_r);
			}
			else {
				sum2_tmp = 0;
			}
			
			
			duplicate += sum2_tmp; //since the duplication number increase exponentially,    because a large "i" often leads to duplications, so we sum up i from small values to large ones and truncate when the sum of probability is larger than 1
			
//			sum2_tmp = Math.min(sum2_tmp, 1);
			
			sum2 += i*sum2_tmp;
			
//			System.out.print(i+"\t"+sum2_tmp+"\n");
		}
		sum2 /= dd;
		
//		System.out.print(sum1+"\t"+sum2+"\n");
		
//		return (sum1+sum2) / 2;
		
		return 0.5*(sum1+ (1+dd)/2.);
	}
	
	public static void get_u_m_movingrate(ExplodingLasagnaModel model, String trainPath) {
		 try {
				FileWriter f = new FileWriter( trainPath + "\\u_m_movingrate.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        double rates [][] = new double [MAX_u][MAX_m];
	        
	        
	        try {
				FileWriter f = new FileWriter( trainPath + "\\u_m_movingrate.txt", true);
				
				BufferedWriter bw = new BufferedWriter(f);
				
				for(int u=1; u<=MAX_u;u++) {
		        	for(int m = 1; m<=MAX_m; m++) {
//		        		double tmp = u/2.;
//		        		
//		        		tmp *= (1./ (CombinationNum.getCombinationNum(m+u, u)*Math.pow(n, u)) + 1./CombinationNum.getCombinationNum(m, Math.min(u, m)) );
		        		
		        		double tmp = get_Econ_d(model, 10, m, u);  // fix d = 10;
		        		
		        		rates[u-1][m-1]=tmp;
		        		
//		        		bw.write(""+tmp+"\t");
		        		bw.write(""+u+"\t"+m+"\t"+tmp+"\n");
		        	}
		        	
//		        	bw.write("\n");
		        	
		        	bw.flush();
		        }
				
				bw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	}
	
	public static void get_u_d_minigen(ExplodingLasagnaModel model, String trainPath) {
		
		double delta = 1E-4;
		
		 try {
				FileWriter f = new FileWriter( trainPath + "\\u_d_minigen.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        double rates [][] = new double [MAX_u][MAX_m + m_s + 1];
	        
	        
	        try {
				FileWriter f = new FileWriter( trainPath + "\\u_d_minigen.txt", true);
				
				BufferedWriter bw = new BufferedWriter(f);
				
				for(int u=1; u<=MAX_u;u++) {
					
					int d_LB = Math.max(0, m_s - MAX_m);
					int d_UB = m_s+MAX_m;
					
		        	for(int d = d_UB, i = 0; d>=d_LB; i++, d--) {
		        		double tmp = d;
		        		
//		        		double Econ_UB = (u*d/2)*(1./(CombinationNum.getCombinationNum(MAX_m+u, u)*Math.pow(n,u)) 
//		        				+ 1./ CombinationNum.getCombinationNum(MAX_m, u) );
		        		
//		        		double Econ_UB = get_Econ_d(model, d, MAX_m, u);
//		        		
//		        		tmp = Math.log(delta / d) / Math.log(1 - Econ_UB/d);
		        		int q = 0;
		        		while(tmp > delta) {
		        			tmp = tmp - get_Econ_d(model, (int) Math.ceil(tmp), MAX_m, u);
		        			q++;
		        		}
		        		
		        		rates[u-1][i]=q;
		        		
//		        		bw.write(""+tmp+"\t");
		        		bw.write(""+u+"\t"+d+"\t"+q+"\n");
		        	}
		        	
//		        	bw.write("\n");
		        	
		        	bw.flush();
		        }
				
				bw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static void get_u_d_m_movingrate(ExplodingLasagnaModel model, String trainPath) {
		try {
			FileWriter f = new FileWriter( trainPath + "\\u_d_m_movingrate.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        double rates [][][] = new double [MAX_u][MAX_d][MAX_m];
        
        
        try {
			FileWriter f = new FileWriter( trainPath + "\\u_d_m_movingrate.txt", true);
			
			BufferedWriter bw = new BufferedWriter(f);
			
			for(int u=1; u<=MAX_u;u++) {
				for(int d = 1; d<=MAX_d; d++) {
					for(int m = 1; m<=MAX_m; m++) {
//		        		double tmp = u/2.;
//		        		
//		        		tmp *= (1./ (CombinationNum.getCombinationNum(m+u, u)*Math.pow(n, u)) + 1./CombinationNum.getCombinationNum(m, Math.min(u, m)) );
		        		
		        		double tmp = get_Econ_d(model, d, m, u);  
		        		
		        		rates[u-1][d-1][m-1]=tmp;
		        		
//		        		bw.write(""+tmp+"\t");
		        		bw.write(""+u+"\t"+d+"\t"+m+"\t"+tmp+"\n");
		        	}
				}

//	        	bw.write("\n");
	        	
	        	bw.flush();
	        }
			
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void get_u_d_m_minigen(ExplodingLasagnaModel model, String trainPath) {
		double delta = 1E-4;
		
		 try {
				FileWriter f = new FileWriter( trainPath + "\\u_d_m_minigen.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        double rates [][][] = new double [MAX_u][MAX_d][MAX_m + m_s + 1];
	        
	        
	        try {
				FileWriter f = new FileWriter( trainPath + "\\u_d_m_minigen.txt", true);
				
				BufferedWriter bw = new BufferedWriter(f);
				
				for(int u=1; u<=MAX_u;u++) {
					
					for(int m = 1; m<=MAX_m; m++) {
						int d_LB = Math.max(0, m_s - m);
						int d_UB = m_s+m;
						
			        	for(int d = MAX_d, i = 0; d>=1; i++, d--) {
			        		
			        		if(d > d_UB || d < d_LB) {
			        			rates[u-1][i][m-1] = -1;
			        			continue;
			        		}
			        		
			        		double tmp = d;
			        		
//			        		double Econ_UB = (u*d/2)*(1./(CombinationNum.getCombinationNum(MAX_m+u, u)*Math.pow(n,u)) 
//			        				+ 1./ CombinationNum.getCombinationNum(MAX_m, u) );
			        		
//			        		double Econ_UB = get_Econ_d(model, d, MAX_m, u);
//			        		
//			        		tmp = Math.log(delta / d) / Math.log(1 - Econ_UB/d);
			        		int q = 0;
			        		while(tmp > delta) {
			        			tmp = tmp - get_Econ_d(model, (int) Math.ceil(tmp), m, u);
			        			q++;
			        		}
			        		
			        		rates[u-1][i][m-1]=q;
			        		
//			        		bw.write(""+tmp+"\t");
			        		
			        	}
					}
					
					
					
		        	
//		        	bw.write("\n");
		        	
		        	
		        }
				
				for(int u=1; u<=MAX_u;u++) {
					for(int d = 1; d<=MAX_d; d++) {
						for(int m = 1; m<=MAX_m; m++) {
							bw.write(""+u+"\t"+d+"\t"+m+"\t"+rates[u-1][d-1][m-1]+"\n");
						}
					}
					bw.flush();
				}
				
				bw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public static void main(String[] args) {
		int idx = 0;
	       
		String trainPath = System.getProperty("user.dir"); 
		if(args.length > idx) {
			trainPath = args[idx];
		}
        idx ++;
        
//		ExplodingLasagnaModel model = new ExplodingLasagnaModel(m_s, n, 8, 1, 4*10^9);//R1
		ExplodingLasagnaModel model = new ExplodingLasagnaModel(m_s, n, 8, 1, 1);//Nguyen4
//      ExplodingLasagnaModel model = new ExplodingLasagnaModel(m_s, n, 8, 1, 1); //Nguyen5
//        ExplodingLasagnaModel model = new ExplodingLasagnaModel(m_s, n, 8, 1, 1); //Keijzer11
        
//       get_u_m_movingrate(model, trainPath);
//       get_u_d_minigen(model, trainPath);
       
       get_u_d_m_movingrate(model, trainPath);
       get_u_d_m_minigen(model, trainPath);
        
	}
}
