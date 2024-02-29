package zhixing.djss.util;

import org.apache.commons.math3.random.RandomDataGenerator;

import yimei.util.random.AbstractRealSampler;

public class EnergyPriceSampler extends AbstractRealSampler{
	@Override
	public double next(RandomDataGenerator rdg) {
		double value = 1e-2;  //check zhixing.jss.cpxInd.dynamicoptimization.simulation.state.SystemState4DO. The default value of energy price is set based on the value here
		double r = rdg.nextUniform(0, 1);
		if (r < 0.33) {
			value = 5e-3;
		}
		else if (r < 0.66) {
			value = 1.5e-2; //check yimei.jss.gp.terminal.JobShopAttribute. The terminal EPR is normalized based on the maximum value here
		}

		return value;
	}

	@Override
	public void setLower(double lower) {

	}

	@Override
	public void setUpper(double upper) {

	}

	@Override
	public void setMean(double mean) {

	}

	@Override
	public double getMean() {
		return 2d;
	}

	@Override
	public AbstractRealSampler clone() {
		return new EnergyPriceSampler();
	}
}
