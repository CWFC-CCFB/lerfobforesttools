package lerfob.predictor.mathilde;

import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
final class MathildeSubModule extends ModelBasedSimulator {
	
	double errorTotalVariance;
	
	
	protected MathildeSubModule(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
	}
	
	protected void setBeta(GaussianEstimate betaEstimate) {
		this.defaultBeta = betaEstimate;
	}
	
	protected Matrix getParameters(MathildeStand stand) {
		return getParametersForThisRealization(stand);
	}
	
	protected Matrix getRandomEffects(MonteCarloSimulationCompliantObject subject) {
		return getRandomEffectsForThisSubject(subject);
	}
	
	protected Map<HierarchicalLevel,GaussianEstimate> getDefaultRandomEffects() {
		return defaultRandomEffects;
	}
	
	protected Map<Enum<?>, GaussianErrorTermEstimate> getDefaultResidualError() {
		return defaultResidualError;
	}
	
	protected Matrix getResidualErrorForThisVersion() {
		return super.getResidualError();
	}
}

