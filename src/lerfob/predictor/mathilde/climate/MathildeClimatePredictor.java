/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2017 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.predictor.mathilde.climate;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.climate.REpiceaClimateChangeGenerator;
import repicea.simulation.climate.REpiceaClimateChangeTrend;
import repicea.simulation.climate.REpiceaClimateVariableChangeMap;
import repicea.simulation.climate.REpiceaClimateVariableMap;
import repicea.simulation.climate.REpiceaClimateVariableMap.ClimateVariable;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * The MathildeClimatePredictor class is a model based on the SAFRAN grid values in the LERFoB network of permanent plots. 
 * The model has plot random effects that are spatially correlated. If the plot coordinates are not available, then the
 * GeographicalCoordinatesGenerator class provides the mean coordinates for the department. The instance that implements the
 * MathildeClimatePlot interface provides the date. The class is meant to work with dates from 1900 and later. If the date 
 * is earlier than 1900, then the class assumes that the climate is similar to that of 1900.
 * @author Mathieu Fortin - October 2017
 */
@SuppressWarnings("serial")
public class MathildeClimatePredictor extends REpiceaPredictor implements REpiceaClimateChangeGenerator<MathildeClimatePlot> {

	/**
	 * Map of expected temperature changes in central Europe (from IPCC Fifth assessment report WG1 Annex 1 - pp.1350-1351)
	 */
	private final static Map<RepresentativeConcentrationPathway, Double> ExpectedChangeByTheEndOfThe21stCentury = new HashMap<RepresentativeConcentrationPathway, Double>();
	static {
		ExpectedChangeByTheEndOfThe21stCentury.put(RepresentativeConcentrationPathway.RCP2_6, 1.5d);
		ExpectedChangeByTheEndOfThe21stCentury.put(RepresentativeConcentrationPathway.RCP4_5, 2.5d);
		ExpectedChangeByTheEndOfThe21stCentury.put(RepresentativeConcentrationPathway.RCP6_0, 3.5d);
		ExpectedChangeByTheEndOfThe21stCentury.put(RepresentativeConcentrationPathway.RCP8_5, 6.0d);
	}
	
	private static List<MathildeClimatePlot> referenceStands;

	private List<String> listStandID;
	
	private double rho;
	
	private GaussianEstimate blups;
	
	private RepresentativeConcentrationPathway rcp = RepresentativeConcentrationPathway.RCP2_6; // default rcp
	
	
	public MathildeClimatePredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
	}
	
	protected MathildeClimatePredictor(boolean isParameterVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
		init();
	}

	/**
	 * Sets the Representative Concentration Pathway rcp for the climate generator.
	 * @param rcp an RepresentativeConcentrationPathway enum
	 */
	public void setRepresentativeConcentrationPathway(RepresentativeConcentrationPathway rcp) {
		this.rcp = rcp;
	}

	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeNewClimateBeta.csv";
			String omegaFilename = path + "0_MathildeNewClimateOmega.csv";
			String covparmsFilename = path + "0_MathildeNewClimateCovparms.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(betaFilename);
			ParameterMap covparmsMap = ParameterLoader.loadVectorFromFile(covparmsFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(omegaFilename);	

			Matrix defaultBetaMean = betaMap.get();
			SymmetricMatrix omega = omegaMap.get().squareSym();
			Matrix covparms = covparmsMap.get();

			setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(
					SymmetricMatrix.convertToSymmetricIfPossible(covparms.getSubMatrix(2, 2, 0, 0))));
			setParameterEstimates(new ModelParameterEstimates(defaultBetaMean, omega));
			oXVector = new Matrix(1, defaultBetaMean.m_iRows);
	
			rho = covparms.getValueAt(1, 0);
			Matrix meanRandomEffect = new Matrix(1,1);
			SymmetricMatrix varianceRandomEffect = SymmetricMatrix.convertToSymmetricIfPossible(
					covparms.getSubMatrix(0, 0, 0, 0));
			setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(meanRandomEffect, varianceRandomEffect));
			
		} catch (Exception e) {
			System.out.println("MathildeClimateModel.init() : Unable to initialize the MathildeClimateModel module");
		}
	}

	/**
	 * This method returns a copy of static member referenceStands.
	 * @return a List of MathildeClimateStandImpl instances
	 */
	protected static List<MathildeClimatePlot> getReferenceStands() {
		if (referenceStands == null) {
			instantiateReferenceStands();
		} 
		List<MathildeClimatePlot> copyList = new ArrayList<MathildeClimatePlot>();
		copyList.addAll(referenceStands);
		return copyList;
	}
	
	
	private synchronized static void instantiateReferenceStands() {
		CSVReader reader = null;
		try {
			if (referenceStands == null) {
				referenceStands = new ArrayList<MathildeClimatePlot>();
				String path = ObjectUtility.getRelativePackagePath(MathildeClimatePredictor.class);
				String referenceStandsFilename = path + "dataBaseNewClimatePredictions.csv";
				reader = new CSVReader(referenceStandsFilename);
				Object[] record;
	 			while ((record = reader.nextRecord()) != null) {
	 				String experimentName = record[0].toString();
	 				double xCoord = Double.parseDouble(record[1].toString());
	 				double yCoord = Double.parseDouble(record[2].toString());
	 				int dateYr = Integer.parseInt(record[3].toString());
	 				int growthStepLengthYr = Integer.parseInt(record[4].toString());
	 				int nbDroughtsInUpcomingGrowthStep = Integer.parseInt(record[5].toString());
	 				double meanTempGrowthSeason = Double.parseDouble(record[6].toString());
	 				double predicted = Double.parseDouble(record[7].toString());
	 				double stdErrPred = Double.parseDouble(record[8].toString());
	 				double scaledResid = Double.parseDouble(record[9].toString());
	 				MathildeClimatePlotImpl stand = new MathildeClimatePlotImpl(experimentName, 
	 						xCoord, 
	 						yCoord, 
	 						dateYr, 
	 						growthStepLengthYr,
	 						nbDroughtsInUpcomingGrowthStep,
	 						meanTempGrowthSeason, 
	 						predicted,
	 						stdErrPred * stdErrPred,
	 						scaledResid);
	 				referenceStands.add(stand);
	 			}
			}
		} catch (Exception e) {
			System.out.println("Unable to instantiate the reference stand list in MathildeClimatePredictor class!");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	protected final synchronized double getFixedEffectPrediction(MathildeClimatePlot plot, Matrix currentBeta, Matrix rcpModifiedBeta) {
		oXVector.resetMatrix();
		double dateMinus1950 = plot.getDateYr() > 2015 ? 2015 - 1950 : plot.getDateYr() - 1950; 
		
		double nbYearsAfter2015 = plot.getDateYr() > 2015 ? plot.getDateYr() - 2015 : 0d;
		if (nbYearsAfter2015 > 85) {
			nbYearsAfter2015 = 85;		// after 2100 the climate stabilizes
		}
//		double dateMinus1950 = plot.getDateYr() - 1950;

		if (dateMinus1950 < 0d) {		// if the date is earlier than 1950 then we set it to 1950
			dateMinus1950 = 0d;
		}

		double growthStepLength = plot.getGrowthStepLengthYr();
		double nbDroughts = plot.getNumberOfDroughtsDuringUpcomingGrowthStep();
		
		
		int pointer = 0;
		oXVector.setValueAt(0, pointer, 1d);
		pointer++;
		oXVector.setValueAt(0, pointer, dateMinus1950);
		pointer++;
		oXVector.setValueAt(0, pointer, nbDroughts / growthStepLength);
		pointer++;
		
		double pred = oXVector.multiply(currentBeta).getValueAt(0, 0) + nbYearsAfter2015 * rcpModifiedBeta.getValueAt(1, 0);
		
		return pred;
	}
	
	/**
	 * This method return the mean temperature of the growth season for the upcoming growth interval. 
	 * @param stand a MathildeMortalityStand stand
	 * @return
	 */
	private double predictMeanTemperatureForGrowthInterval(MathildeClimatePlot stand) {
		if (!doBlupsExistForThisSubject(stand)) {
			predictBlups(stand);
		}
		Matrix currentBeta = getParametersForThisRealization(stand);
		Matrix rcpModifiedBeta = getParametersForThisRealization(stand, rcp);
		double pred = getFixedEffectPrediction(stand, currentBeta, rcpModifiedBeta);
		double randomEffect = getRandomEffectsForThisSubject(stand).getValueAt(0, 0);
		pred += randomEffect;
		double residualError = getResidualError().getValueAt(0, 0) / Math.sqrt(stand.getGrowthStepLengthYr());
		pred += residualError;
		return pred;
	}
	
	private Matrix getParametersForThisRealization(MathildeClimatePlot stand, RepresentativeConcentrationPathway rcp) {
		Matrix currentBeta = getParametersForThisRealization(stand).getDeepClone();
		double rcpFactor = MathildeClimatePredictor.ExpectedChangeByTheEndOfThe21stCentury.get(rcp) / MathildeClimatePredictor.ExpectedChangeByTheEndOfThe21stCentury.get(RepresentativeConcentrationPathway.RCP2_6);
		
		currentBeta.setValueAt(1, 0, currentBeta.getValueAt(1, 0) * rcpFactor);
		return currentBeta;
	}

	
	/*
	 * For test purpose. 
	 * @param stand
	 * @return
	 */
	final double getFixedEffectPrediction(MathildeClimatePlot stand) {
		return getFixedEffectPredictionWithoutRCP(stand, getParameterEstimates().getMean());
	}

	

	private double getFixedEffectPredictionWithoutRCP(MathildeClimatePlot stand, Matrix mean) {
		return getFixedEffectPrediction(stand, mean, mean);
	}

	/*
	 * For test purpose. 
	 * @param stand
	 * @return
	 */
	final double getFixedEffectPredictionVariance(MathildeClimatePlot stand) {
		Matrix omegaRelatedVariance = oXVector.multiply(getParameterEstimates().getVariance()).multiply(oXVector.transpose());
		return omegaRelatedVariance.getValueAt(0, 0);
	}

	/*
	 * For test purpose. 
	 * @param stand
	 * @return
	 */
	double getResidualVariance(MathildeClimatePlotImpl stand) {
		double residualVariance = getDefaultResidualError(ErrorTermGroup.Default).getVariance().scalarMultiply(1d / stand.getGrowthStepLengthYr()).getValueAt(0, 0);
		return residualVariance;
	}
	
	private synchronized void predictBlups(MathildeClimatePlot stand) {
		if (!doBlupsExistForThisSubject(stand)) {
			List<MathildeClimatePlot> stands = getReferenceStands();
			int knownStandIndex = stands.size();
			List<MathildeClimatePlot> standsForWhichBlupsWillBePredicted = stand.getAllMathildeClimatePlots();
			stands.addAll(standsForWhichBlupsWillBePredicted);
			
			List<String> tempListStandID = new ArrayList<String>();
			Map<String, MathildeClimatePlot> uniqueStandMap = new HashMap<String, MathildeClimatePlot>();
			for (MathildeClimatePlot s : stands) {
				if (!tempListStandID.contains(s.getSubjectId())) {
					tempListStandID.add(s.getSubjectId());
					uniqueStandMap.put(s.getSubjectId(), s);
				}
			}

			Matrix defaultBeta = getParameterEstimates().getMean();
			Matrix omega = getParameterEstimates().getVariance();
			Matrix residuals = new Matrix(knownStandIndex,1);
			
			Matrix matX = new Matrix(stands.size(), oXVector.m_iCols);
			Matrix matL = new Matrix(stands.size(),1);
			
			for (int i = 0; i < knownStandIndex; i++) {
				MathildeClimatePlotImpl standImpl = (MathildeClimatePlotImpl) stands.get(i);
				residuals.setValueAt(i, 0, standImpl.meanAnnualTempAbove6C - getFixedEffectPredictionWithoutRCP(standImpl, defaultBeta));
			}

			
			Matrix matZ = new Matrix(stands.size(), tempListStandID.size());
			for (int i = 0; i < stands.size(); i++) {
				Matrix z_i = new Matrix(1, tempListStandID.size());
				MathildeClimatePlot s = stands.get(i);
				z_i.setValueAt(0, tempListStandID.indexOf(s.getSubjectId()), 1d);
				matZ.setSubMatrix(z_i, i, 0);
				getFixedEffectPredictionWithoutRCP(s, defaultBeta);
				matX.setSubMatrix(oXVector.getSubMatrix(0, 0, 0, 1), i, 0);
				matL.setValueAt(i, 0, 1d / s.getGrowthStepLengthYr());
			}
			
			Matrix matG = new Matrix(tempListStandID.size(), tempListStandID.size());
			
			double variance = getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance().getValueAt(0, 0);
			for (int i = 0; i < matG.m_iRows; i++) {
				for (int j = i; j < matG.m_iRows; j++) {
					if (i == j) {
						matG.setValueAt(i, j, variance);
					} else {
						MathildeClimatePlot stand1 = uniqueStandMap.get(tempListStandID.get(i));
						MathildeClimatePlot stand2 = uniqueStandMap.get(tempListStandID.get(j));
						double y_resc1 = stand1.getLatitudeDeg();
						double x_resc1 = stand1.getLongitudeDeg();
						double y_resc2 = stand2.getLatitudeDeg();
						double x_resc2 = stand2.getLongitudeDeg();
						double y_diff = y_resc1 - y_resc2;
						double x_diff = x_resc1 - x_resc2;
						double d = Math.sqrt(y_diff * y_diff + x_diff * x_diff);
						double correlation = 0d;
						if (d <= rho) {
							correlation = variance * (1 - 3*d/(2*rho) + d*d*d/(2*rho*rho*rho));
						}
						matG.setValueAt(i, j, correlation);
						matG.setValueAt(j, i, correlation);
					}
				}
			}
			double residualVariance = getDefaultResidualError(ErrorTermGroup.Default).getVariance().getValueAt(0, 0);
//			Matrix matR = Matrix.getIdentityMatrix(stands.size()).scalarMultiply(residualVariance);
			Matrix matR = matL.matrixDiagonal().scalarMultiply(residualVariance);
			Matrix matV = matZ.multiply(matG).multiply(matZ.transpose()).add(matR); 

			Matrix invVk = matV.getSubMatrix(0, knownStandIndex - 1, 0, knownStandIndex-1).getInverseMatrix();
			Matrix matXk = matX.getSubMatrix(0, knownStandIndex - 1, 0, matX.m_iCols - 1);
			Matrix covV = matV.getSubMatrix(knownStandIndex, matV.m_iRows - 1, 0, knownStandIndex-1);
			Matrix blups = covV.multiply(invVk).multiply(residuals);
			
			Matrix matVu = matV.getSubMatrix(knownStandIndex, matV.m_iRows - 1, knownStandIndex, matV.m_iCols - 1);
			Matrix matRu = matR.getSubMatrix(knownStandIndex, matR.m_iRows - 1, knownStandIndex, matR.m_iCols - 1);
			
			Matrix varBlupsFirstTerm = matVu.subtract(covV.multiply(invVk).multiply(covV.transpose())).subtract(matRu);
			Matrix covariance = covV.multiply(invVk).multiply(matXk).multiply(omega).scalarMultiply(-1d);
			Matrix varBlupsSecondTerm = covariance.multiply(matXk.transpose()).multiply(invVk).multiply(covV.transpose());
			SymmetricMatrix varBlups = SymmetricMatrix.convertToSymmetricIfPossible(
					varBlupsFirstTerm.subtract(varBlupsSecondTerm));
			
			this.blups = new GaussianEstimate(blups, varBlups);
			listStandID = new ArrayList<String>();
			for (int index = 0; index < standsForWhichBlupsWillBePredicted.size(); index++) {
				MathildeClimatePlot plot = standsForWhichBlupsWillBePredicted.get(index); 
				setBlupsForThisSubject(plot,
						new GaussianEstimate(blups.getSubMatrix(index, index, 0, 0), 
								SymmetricMatrix.convertToSymmetricIfPossible(varBlups.getSubMatrix(index, index, index, index))));
				listStandID.add(plot.getSubjectId());
			}
		}
	}

	@Override
	protected Matrix simulateDeviatesForRandomEffectsOfThisSubject(MonteCarloSimulationCompliantObject subject, Estimate<?> randomEffectsEstimate) {
		if (listStandID.contains(subject.getSubjectId())) {
			Matrix simulatedBlups = blups.getRandomDeviate();
			List<MathildeClimatePlot> standList = ((MathildeClimatePlot) subject).getAllMathildeClimatePlots();
			for (MathildeClimatePlot s : standList) {
				int index = listStandID.indexOf(s.getSubjectId());
				setDeviatesForRandomEffectsOfThisSubject(s, simulatedBlups.getSubMatrix(index, index, 0, 0));
			}
			return simulatedBlups.getDeepClone();
		} else {
			throw new InvalidParameterException("The stand has no blups which is abnormal!");
		}
	}
	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#getBlupsForThisSubject(repicea.simulation.MonteCarloSimulationCompliantObject)
	 */
	@Override
	protected Estimate<? extends StandardGaussianDistribution> getBlupsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		return super.getBlupsForThisSubject(subject);
	}
	
	/*
	 * For extended visibility
	 */
	protected final Matrix getRandomEffects(MonteCarloSimulationCompliantObject subject) {
		return super.getRandomEffectsForThisSubject(subject);
	}

	@Override
	public REpiceaClimateVariableMap getClimateVariables(MathildeClimatePlot plot) {
		REpiceaClimateVariableMap map = new REpiceaClimateVariableMap();
		map.put(ClimateVariable.MeanGrowingSeasonTempC, predictMeanTemperatureForGrowthInterval(plot));
		return map;
	}


	@Override
	public REpiceaClimateChangeTrend getClimateTrendForThisStand(MonteCarloSimulationCompliantObject plot) {
		REpiceaClimateChangeTrend trend = new REpiceaClimateChangeTrend();
		REpiceaClimateVariableChangeMap oMap = new REpiceaClimateVariableChangeMap();
		oMap.put(ClimateVariable.MeanGrowingSeasonTempC, getParametersForThisRealization((MathildeClimatePlot) plot).getValueAt(1, 0));
		trend.addSegment(1950, 2015, oMap);
		oMap = new REpiceaClimateVariableChangeMap();
		oMap.put(ClimateVariable.MeanGrowingSeasonTempC, getParametersForThisRealization((MathildeClimatePlot) plot, rcp).getValueAt(1, 0));
		trend.addSegment(2015, 2100, oMap);
		return trend;
	}

	
//	public static void main(String[] args) {
//		new MathildeClimatePredictor(false);
//	}

}