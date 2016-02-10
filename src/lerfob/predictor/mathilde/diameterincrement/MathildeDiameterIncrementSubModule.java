package lerfob.predictor.mathilde.diameterincrement;

import lerfob.predictor.mathilde.MathildeSubModule;

@SuppressWarnings("serial")
final class MathildeDiameterIncrementSubModule extends MathildeSubModule {
	
	double errorTotalVariance;
	
	protected MathildeDiameterIncrementSubModule(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
	}
	
}

