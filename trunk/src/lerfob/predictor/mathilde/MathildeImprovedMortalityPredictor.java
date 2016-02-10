package lerfob.predictor.mathilde;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class MathildeImprovedMortalityPredictor extends	MathildeMortalityPredictor {

	public MathildeImprovedMortalityPredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled,	boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled,	isResidualVariabilityEnabled);
	}

	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeMortality2Beta.csv";
			String omegaFilename = path + "0_MathildeMortality2Omega.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1,betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);		
			
			numberOfParameters = -1;
			int numberOfExcludedGroups = 10;		
			
			for (int excludedGroup = 0; excludedGroup <= numberOfExcludedGroups; excludedGroup++) {			//
				Matrix betaPrelim = betaMap.get(excludedGroup);
				if (numberOfParameters == -1) {
					numberOfParameters = betaPrelim.m_iRows - 1;
				}
				Matrix defaultBetaMean = betaPrelim.getSubMatrix(0, numberOfParameters - 1, 0, 0);
				Matrix randomEffectVariance = betaPrelim.getSubMatrix(numberOfParameters, numberOfParameters, 0, 0);
				Matrix omega = omegaMap.get(excludedGroup).squareSym().getSubMatrix(0, numberOfParameters - 1, 0, numberOfParameters - 1);		
				MathildeSubModule subModule = new MathildeSubModule(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
				subModule.setDefaultBeta(new GaussianEstimate(defaultBetaMean, omega));
				subModule.setDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT, new GaussianEstimate(new Matrix(randomEffectVariance.m_iRows,1), randomEffectVariance));
				subModules.put(excludedGroup, subModule);
			}
		} catch (Exception e) {
			System.out.println("MathildeMortalityPredictor.init() : Unable to initialize the MathildeMortalityPredictor module");
		}
	}

	@Override
	public synchronized double predictEventProbability(MathildeStand stand, MathildeTree tree, Object... parms) {
		boolean windstormDisabledOverride = false;
		if (parms != null && parms.length > 0 && parms[0] instanceof Boolean) {
			windstormDisabledOverride = (Boolean) parms[0];
		}
		double upcomingWindstorm = 0d;
		if (stand.isAWindstormGoingToOccur() && !windstormDisabledOverride) {
			upcomingWindstorm = 1d;
		} 
		
		MathildeSubModule subModule;
		if (parms.length > 0 && parms[0] instanceof Integer) {
			subModule = subModules.get(parms[0]);
			if (subModule == null) {
				throw new InvalidParameterException("The integer in the parms parameter is not valid!");
			} 
		} else {
			subModule = subModules.get(0);
		}
		
		Matrix beta = subModule.getParameters(stand);
		linkFunction.setVariableValue(1, upcomingWindstorm * tree.getLnDbhCm());
		
		double pred = getFixedEffectOnlyPrediction(beta, stand, tree);
		linkFunction.setParameterValue(0, pred);

		double prob;
		linkFunction.setParameterValue(1, beta.m_afData[14][0]);
		if (isRandomEffectsVariabilityEnabled && stand.isAWindstormGoingToOccur()) {	// no need to draw a random effect if there is no windstorm
			IntervalNestedInPlotDefinition interval = getIntervalNestedInPlotDefinition(stand, stand.getDateYr());
			Matrix randomEffects = subModule.getRandomEffects(interval);
			linkFunction.setParameterValue(2, randomEffects.m_afData[0][0]);
			prob = linkFunction.getValue();
		} else {
			linkFunction.setParameterValue(2, 0d);		// random effect arbitrarily set to 0
			if (stand.isAWindstormGoingToOccur() && isGaussianQuadratureEnabled) {
				List<Integer> parameterIndices = new ArrayList<Integer>();
				parameterIndices.add(2);
				prob = ghq.getIntegralApproximation(linkFunction, parameterIndices, subModule.getDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT).getDistribution().getStandardDeviation());
			} else {									// no need to evaluate the quadrature when there is no windstorm
				prob = linkFunction.getValue();
			}
		}
		return prob;
	}

}
