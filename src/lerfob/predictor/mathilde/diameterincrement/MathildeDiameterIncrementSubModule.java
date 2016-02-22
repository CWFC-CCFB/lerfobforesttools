package lerfob.predictor.mathilde.diameterincrement;

import java.util.List;

import repicea.math.Matrix;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import lerfob.predictor.mathilde.MathildeSubModule;

@SuppressWarnings("serial")
final class MathildeDiameterIncrementSubModule extends MathildeSubModule {
	
	double errorTotalVariance;
	
	protected MathildeDiameterIncrementSubModule(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
	}
	
	@Override
	protected void registerBlups(Matrix mean, Matrix variance, Matrix covariance, List<MonteCarloSimulationCompliantObject> subjectList) {
		super.registerBlups(mean, variance, covariance, subjectList);
	}

	@Override
	protected ParameterEstimates getParameterEstimates() {
		return super.getParameterEstimates();
	}
}

