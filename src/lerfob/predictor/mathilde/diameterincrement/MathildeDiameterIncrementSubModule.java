package lerfob.predictor.mathilde.diameterincrement;

import java.util.List;

import lerfob.predictor.mathilde.MathildeSubModule;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.MonteCarloSimulationCompliantObject;

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
	protected ModelParameterEstimates getParameterEstimates() {
		return super.getParameterEstimates();
	}
	
	protected boolean areBlupsEstimated() {
		return super.areBlupsEstimated();
	}
	
	protected void setBlupsEstimated(boolean bool) {super.setBlupsEstimated(bool);}
	
}

