package lerfob.carbonbalancetool.sensitivityanalysis;

import repicea.math.Matrix;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.SensitivityAnalysisParameter;
import repicea.stats.estimates.Estimate;

@SuppressWarnings({ "serial", "rawtypes" })
public abstract class CATSensitivityAnalysisParameter<E extends Estimate> extends SensitivityAnalysisParameter<E> {

	protected CATSensitivityAnalysisParameter(boolean isParametersVariabilityEnabled) {
		super(isParametersVariabilityEnabled);
	}
	
	protected abstract Matrix getParameterValueForThisSubject(MonteCarloSimulationCompliantObject subject);


}
