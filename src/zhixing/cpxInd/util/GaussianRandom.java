package zhixing.cpxInd.util;

public class GaussianRandom {

	public double getGaussianRandom(double u1, double u2, double minVal, double maxVal) {
		double res = 0;
		
		double z0 = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
		
		double mean = (maxVal - minVal) / 2;
        double stdDev = 2.0; //standard deviation
        double gaussianValue = mean + stdDev * z0;

        // Clamp the value within [minVal, maxVal] if needed
        res = Math.min(maxVal, Math.max(minVal, gaussianValue));
        
        return res;
	}
}
