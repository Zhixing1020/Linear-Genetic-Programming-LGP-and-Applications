package zhixing.djss.util;

import org.apache.commons.math3.random.RandomDataGenerator;

import yimei.util.random.AbstractRealSampler;

public class UserReactionFactorSampler  extends AbstractRealSampler{
	@Override
	public double next(RandomDataGenerator rdg) {
		double value = 1; //check zhixing.jss.cpxInd.dynamicoptimization.simulation.state.SystemState4DO. The default value of energy price is set based on the value here
		double r = rdg.nextUniform(0, 1);
		if (r < 0.4) {
			value = 0.2;
		}
		else if (r < 0.6) {
			value = 2.3; //check yimei.jss.gp.terminal.JobShopAttribute. The terminal SFR is normalized based on the maximum value here
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
		return new UserReactionFactorSampler();
	}
}
