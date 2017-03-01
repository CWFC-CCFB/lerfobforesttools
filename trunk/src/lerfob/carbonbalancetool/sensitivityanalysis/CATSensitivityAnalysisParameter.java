package lerfob.carbonbalancetool.sensitivityanalysis;

import repicea.math.Matrix;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.SensitivityAnalysisParameter;
import repicea.stats.estimates.Estimate;

@SuppressWarnings({ "serial", "rawtypes" })
public abstract class CATSensitivityAnalysisParameter<E extends Estimate> extends SensitivityAnalysisParameter<E> {

	private double multiplier;

	protected CATSensitivityAnalysisParameter(boolean isParametersVariabilityEnabled) {
		super(isParametersVariabilityEnabled);
	}
	
	protected void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}
	
	protected double getMultiplier() {return multiplier;}
	
	protected Matrix getParameterValueForThisSubject(MonteCarloSimulationCompliantObject subject) {
		return getParametersForThisRealization(subject).scalarMultiply(getMultiplier()).scalarAdd(1d);
	}

	protected void setParametersVariabilityEnabled(boolean enabled) {
		this.isParametersVariabilityEnabled = enabled;
	}

}
