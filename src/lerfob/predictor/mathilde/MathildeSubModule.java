package lerfob.predictor.mathilde;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
public abstract class MathildeSubModule extends REpiceaPredictor {
	
	double errorTotalVariance;
	
	protected MathildeSubModule(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
	}
	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	public void setParameterEstimates(GaussianEstimate betaEstimate) {
		super.setParameterEstimates(betaEstimate);
	}
	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	public void setDefaultRandomEffects(HierarchicalLevel level, Estimate<? extends StandardGaussianDistribution> estimate) {
		super.setDefaultRandomEffects(level, estimate);
	}
	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	public Matrix getParameters(MonteCarloSimulationCompliantObject stand) {
		return super.getParametersForThisRealization(stand);
	}

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	public Matrix getRandomEffects(MonteCarloSimulationCompliantObject subject) {
		return getRandomEffectsForThisSubject(subject);
	}

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	public Estimate<? extends StandardGaussianDistribution> getDefaultRandomEffects(HierarchicalLevel level) {
		return super.getDefaultRandomEffects(level);
	}

	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	public void setDefaultResidualError(Enum<?> enumVar, GaussianErrorTermEstimate estimate) {
		super.setDefaultResidualError(enumVar, estimate);
	}

	
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setDefaultBeta(repicea.stats.estimates.GaussianEstimate)
	 */
	@Override
	public GaussianErrorTermEstimate  getDefaultResidualError(Enum<?> enumVar) {
		return super.getDefaultResidualError(enumVar);
	}
	
	public Matrix getResidualErrorForThisVersion() {
		return super.getResidualError();
	}

//	/*
//	 * For extended visibility (non-Javadoc)
//	 * @see repicea.simulation.ModelBasedSimulator#setBlupsAtThisLevel(repicea.simulation.HierarchicalLevel, int, repicea.stats.estimates.Estimate)
//	 */
//	@Override
//	public void setBlupsForThisSubject(MonteCarloSimulationCompliantObject subject, Estimate<? extends StandardGaussianDistribution> blups) {
//		super.setBlupsForThisSubject(subject, blups);
//	}
	

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#setBlupsAtThisLevel(repicea.simulation.HierarchicalLevel, int, repicea.stats.estimates.Estimate)
	 */
	@Override
	public Estimate<? extends StandardGaussianDistribution> getBlupsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		return super.getBlupsForThisSubject(subject);
	}

	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#init()
	 */
	@Override
	public final void init() {}
	
	@Override
	public Matrix simulateDeviatesForRandomEffectsOfThisSubject(MonteCarloSimulationCompliantObject subject, Estimate<?> randomEffectsEstimate) {
		return super.simulateDeviatesForRandomEffectsOfThisSubject(subject, randomEffectsEstimate);
	}
}

