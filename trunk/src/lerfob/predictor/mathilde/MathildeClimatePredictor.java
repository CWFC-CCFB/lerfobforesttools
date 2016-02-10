package lerfob.predictor.mathilde;

import java.util.ArrayList;
import java.util.List;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class MathildeClimatePredictor extends ModelBasedSimulator {
	
	protected final List<MathildeClimateStandImpl> referenceStands;
	
	
	public MathildeClimatePredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		referenceStands = new ArrayList<MathildeClimateStandImpl>();
		init();
	}

	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeClimateBeta.csv";
			String omegaFilename = path + "0_MathildeClimateOmega.csv";
			String covparmsFilename = path + "0_MathildeClimateCovparms.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(betaFilename);
			ParameterMap covparmsMap = ParameterLoader.loadVectorFromFile(covparmsFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(omegaFilename);	

			Matrix defaultBetaMean = betaMap.get();
			Matrix omega = omegaMap.get().squareSym();
			Matrix covparms = covparmsMap.get();

			setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(covparms.getSubMatrix(2, 2, 0, 0)));
			setDefaultBeta(new GaussianEstimate(defaultBetaMean, omega));
			oXVector = new Matrix(1, defaultBetaMean.m_iRows);
			
			Matrix meanRandomEffect = new Matrix(1,1);
			Matrix varianceRandomEffect = covparms.getSubMatrix(0, 0, 0, 0);
			setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(meanRandomEffect, varianceRandomEffect));
			
			String referenceStandsFilename = path + "dataBaseClimatePredictions.csv";
			CSVReader reader = new CSVReader(referenceStandsFilename);
			Object[] record;
 			while ((record = reader.nextRecord()) != null) {
 				String experimentName = record[0].toString();
 				double x_resc = Double.parseDouble(record[1].toString());
 				double y_resc = Double.parseDouble(record[2].toString());
 				int dateYr = Integer.parseInt(record[3].toString());
 				double meanTempGrowthSeason = Double.parseDouble(record[5].toString());
 				double predicted = Double.parseDouble(record[6].toString());
 				MathildeClimateStandImpl stand = new MathildeClimateStandImpl(experimentName, x_resc, y_resc, dateYr, meanTempGrowthSeason, predicted);
 				referenceStands.add(stand);
 			}
		} catch (Exception e) {
			System.out.println("MathildeClimateModel.init() : Unable to initialize the MathildeClimateModel module");
		}
	}

	protected final synchronized double getFixedEffectPrediction(MathildeStand stand, Matrix currentBeta) {
		oXVector.resetMatrix();

//		double upcomingDrought = 0d;
//		if (stand.isADroughtGoingToOccur()) {
//			upcomingDrought = 1d;
//		}
		double dateMinus1950 = stand.getDateYr() - 1950;
		
		int pointer = 0;
		oXVector.m_afData[0][pointer] = 1d;
		pointer++;
		oXVector.m_afData[0][pointer] = dateMinus1950;
		pointer++;
//		oXVector.m_afData[0][pointer] = upcomingDrought;
//		pointer++;
		
		double pred = oXVector.multiply(currentBeta).m_afData[0][0];
		
		return pred;
	}
	
	/**
	 * This method return the mean temperature of the growth season for the upcoming growth interval. 
	 * @param stand a MathildeMortalityStand stand
	 * @return
	 */
	public double getMeanTemperatureForGrowthInterval(MathildeStand stand) {
		Matrix currentBeta = getParametersForThisRealization(stand);
		double pred = getFixedEffectPrediction(stand, currentBeta);
		double randomEffect = getRandomEffectsForThisSubject(stand).m_afData[0][0];
		pred += randomEffect;
		double residualError = getResidualError().m_afData[0][0];
		pred += residualError;
		return pred;
	}
	
	public static void main(String[] args) {
		new MathildeClimatePredictor(false, false, false);
	}
}