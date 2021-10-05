package lerfob.predictor.mathilde.diameterincrement;

import lerfob.predictor.mathilde.MathildeSubModule;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;

@SuppressWarnings("serial")
final class MathildeDiameterIncrementSubModule extends MathildeSubModule {
	
	double errorTotalVariance;
	
	protected MathildeDiameterIncrementSubModule(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
	}
	
//	@Override
//	protected void registerBlups(Matrix mean, Matrix variance, Matrix covariance, List<MonteCarloSimulationCompliantObject> subjectList) {
//		super.registerBlups(mean, variance, covariance, subjectList);
//	}

	/*
	 * Foe extended visibility (non-Javadoc)
	 * @see repicea.simulation.SensitivityAnalysisParameter#getParameterEstimates()
	 */
	@Override
	protected ModelParameterEstimates getParameterEstimates() {
		return super.getParameterEstimates();
	}
	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#doBlupsExistForThisSubject(repicea.simulation.MonteCarloSimulationCompliantObject)
	 */
	@Override
	protected boolean doBlupsExistForThisSubject(MonteCarloSimulationCompliantObject subject) {
		return super.doBlupsExistForThisSubject(subject);
	}
	
	protected void registerBlupsForThisSubject(MonteCarloSimulationCompliantObject subject, Estimate<? extends StandardGaussianDistribution> blups) {
		setBlupsForThisSubject(subject, blups);
	}
	
//	protected boolean areBlupsEstimated() {
//		return super.areBlupsEstimated();
//	}
	
//	protected void setBlupsEstimated(boolean bool) {super.setBlupsEstimated(bool);}


}

