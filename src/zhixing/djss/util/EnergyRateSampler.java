package zhixing.djss.util;

import org.apache.commons.math3.random.RandomDataGenerator;

import yimei.util.random.AbstractRealSampler;

public class EnergyRateSampler  extends AbstractRealSampler{
	@Override
	public double next(RandomDataGenerator rdg) {
		//double value = 2; //check zhixing.jss.cpxInd.dynamicoptimization.simulation.state.SystemState4DO. The default value of energy price is set based on the value here
		return rdg.nextUniform(1.2, 3);
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
		return new EnergyRateSampler();
	}
}
