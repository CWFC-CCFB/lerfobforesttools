package lerfob.predictor.mathilde.diameterincrement;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
final class MathildeDiameterIncrementSubModule extends ModelBasedSimulator {
	
	double errorTotalVariance;
	
	protected MathildeDiameterIncrementSubModule(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
	}
	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	protected void setDefaultBeta(GaussianEstimate betaEstimate) {
		super.setDefaultBeta(betaEstimate);
	}
	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	protected void setDefaultRandomEffects(HierarchicalLevel level, GaussianEstimate estimate) {
		super.setDefaultRandomEffects(level, estimate);
	}
	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	protected Matrix getParameters(MonteCarloSimulationCompliantObject stand) {
		return super.getParametersForThisRealization(stand);
	}

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	protected Matrix getRandomEffects(MonteCarloSimulationCompliantObject subject) {
		return getRandomEffectsForThisSubject(subject);
	}

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	protected GaussianEstimate getDefaultRandomEffects(HierarchicalLevel level) {
		return super.getDefaultRandomEffects(level);
	}

	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	protected void setDefaultResidualError(Enum<?> enumVar, GaussianErrorTermEstimate estimate) {
		super.setDefaultResidualError(enumVar, estimate);
	}

	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	protected GaussianErrorTermEstimate  getDefaultResidualError(Enum<?> enumVar) {
		return super.getDefaultResidualError(enumVar);
	}
	
	protected Matrix getResidualErrorForThisVersion() {
		return super.getResidualError();
	}

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setBlupsAtThisLevel(repicea.simulation.HierarchicalLevel, int, repicea.stats.estimates.Estimate)
	 */
	@Override
	protected void setBlupsForThisSubject(MonteCarloSimulationCompliantObject subject, Estimate<? extends StandardGaussianDistribution> blups) {
		super.setBlupsForThisSubject(subject, blups);
	}
	

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setBlupsAtThisLevel(repicea.simulation.HierarchicalLevel, int, repicea.stats.estimates.Estimate)
	 */
	@Override
	protected Estimate<? extends StandardGaussianDistribution> getBlupsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		return super.getBlupsForThisSubject(subject);
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#init()
	 */
	@Override
	protected void init() {}
	
	
	protected Matrix simulateDeviatesForRandomEffectsOfThisSubject(MonteCarloSimulationCompliantObject subject, Estimate<?> randomEffectsEstimate) {
		return super.simulateDeviatesForRandomEffectsOfThisSubject(subject, randomEffectsEstimate);
	}
}

