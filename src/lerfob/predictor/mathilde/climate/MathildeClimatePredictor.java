/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class MathildeClimatePredictor extends REpiceaPredictor {
	
	private final static GregorianCalendar SystemDate = new GregorianCalendar();
	private static List<MathildeClimateStand> referenceStands;

	private double rho;
	
	public MathildeClimatePredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
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
			setParameterEstimates(new GaussianEstimate(defaultBetaMean, omega));
			oXVector = new Matrix(1, defaultBetaMean.m_iRows);
	
			rho = covparms.m_afData[1][0];
			Matrix meanRandomEffect = new Matrix(1,1);
			Matrix varianceRandomEffect = covparms.getSubMatrix(0, 0, 0, 0);
			setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(meanRandomEffect, varianceRandomEffect));
			
		} catch (Exception e) {
			System.out.println("MathildeClimateModel.init() : Unable to initialize the MathildeClimateModel module");
		}
	}

	/**
	 * This method returns a copy of static member referenceStands.
	 * @return a List of MathildeClimateStandImpl instances
	 */
	protected static List<MathildeClimateStand> getReferenceStands() {
		if (referenceStands == null) {
			instantiateReferenceStands();
		} 
		List<MathildeClimateStand> copyList = new ArrayList<MathildeClimateStand>();
		copyList.addAll(referenceStands);
		return copyList;
	}
	
	
	private synchronized static void instantiateReferenceStands() {
		CSVReader reader = null;
		try {
			if (referenceStands == null) {
				referenceStands = new ArrayList<MathildeClimateStand>();
				String path = ObjectUtility.getRelativePackagePath(MathildeClimatePredictor.class);
				String referenceStandsFilename = path + "dataBaseClimatePredictions.csv";
				reader = new CSVReader(referenceStandsFilename);
				Object[] record;
	 			while ((record = reader.nextRecord()) != null) {
	 				String experimentName = record[0].toString();
	 				double xCoord = Double.parseDouble(record[1].toString());
	 				double yCoord = Double.parseDouble(record[2].toString());
	 				int dateYr = Integer.parseInt(record[3].toString());
	 				double meanTempGrowthSeason = Double.parseDouble(record[5].toString());
	 				double predicted = Double.parseDouble(record[6].toString());
	 				MathildeClimateStandImpl stand = new MathildeClimateStandImpl(experimentName, xCoord, yCoord, dateYr, meanTempGrowthSeason, predicted);
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

	protected final synchronized double getFixedEffectPrediction(MathildeClimateStand stand, Matrix currentBeta) {
		oXVector.resetMatrix();
		double dateMinus1950 = stand.getDateYr() - 1950;
		if (dateMinus1950 < 0) {
			dateMinus1950 = SystemDate.get(Calendar.YEAR) - 1950;
		}
		
		int pointer = 0;
		oXVector.m_afData[0][pointer] = 1d;
		pointer++;
		oXVector.m_afData[0][pointer] = dateMinus1950;
		pointer++;
		
		double pred = oXVector.multiply(currentBeta).m_afData[0][0];
		
		return pred;
	}
	
	/**
	 * This method return the mean temperature of the growth season for the upcoming growth interval. 
	 * @param stand a MathildeMortalityStand stand
	 * @return
	 */
	public double getMeanTemperatureForGrowthInterval(MathildeClimateStand stand) {
		if (!areBlupsEstimated()) {
			predictBlups(stand);
		}
		Matrix currentBeta = getParametersForThisRealization(stand);
		double pred = getFixedEffectPrediction(stand, currentBeta);
		double randomEffect = getRandomEffectsForThisSubject(stand).m_afData[0][0];
		pred += randomEffect;
		double residualError = getResidualError().m_afData[0][0];
		pred += residualError;
		return pred;
	}
	
	/*
	 * For test purpose. 
	 * @param stand
	 * @return
	 */
	protected final double getFixedEffectPrediction(MathildeClimateStand stand) {
		return getFixedEffectPrediction(stand, getParameterEstimates().getMean());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private synchronized void predictBlups(MathildeClimateStand stand) {
		if (!areBlupsEstimated()) {
			List<MathildeClimateStand> stands = getReferenceStands();
			int knownStandIndex = stands.size();
			List<MathildeClimateStand> standsForWhichBlupsWillBePredicted = stand.getAllMathildeClimateStands();
			stands.addAll(standsForWhichBlupsWillBePredicted);
			
			List<String> listStandID = new ArrayList<String>();
			Map<String, MathildeClimateStand> uniqueStandMap = new HashMap<String, MathildeClimateStand>();
			for (MathildeClimateStand s : stands) {
				if (!listStandID.contains(s.getSubjectId())) {
					listStandID.add(s.getSubjectId());
					uniqueStandMap.put(s.getSubjectId(), s);
				}
			}

			Matrix defaultBeta = getParameterEstimates().getMean();
			Matrix omega = getParameterEstimates().getVariance();
			Matrix residuals = new Matrix(knownStandIndex,1);
			Matrix matX = new Matrix(stands.size(),2);
			
			for (int i = 0; i < knownStandIndex; i++) {
				MathildeClimateStandImpl standImpl = (MathildeClimateStandImpl) stands.get(i);
				residuals.m_afData[i][0] = standImpl.meanAnnualTempAbove6C - getFixedEffectPrediction(standImpl, defaultBeta);
			}

			
			Matrix matZ = new Matrix(stands.size(), listStandID.size());
			for (int i = 0; i < stands.size(); i++) {
				Matrix z_i = new Matrix(1, listStandID.size());
				MathildeClimateStand s = stands.get(i);
				z_i.m_afData[0][listStandID.indexOf(s.getSubjectId())] = 1d;
				matZ.setSubMatrix(z_i, i, 0);
				getFixedEffectPrediction(s, defaultBeta);
				matX.setSubMatrix(oXVector.getSubMatrix(0, 0, 0, 1), i, 0);
			}
			
			Matrix matG = new Matrix(listStandID.size(), listStandID.size());
			
			double variance = getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance().m_afData[0][0];
			for (int i = 0; i < matG.m_iRows; i++) {
				for (int j = i; j < matG.m_iRows; j++) {
					if (i == j) {
						matG.m_afData[i][j] = variance;
					} else {
						MathildeClimateStand stand1 = uniqueStandMap.get(listStandID.get(i));
						MathildeClimateStand stand2 = uniqueStandMap.get(listStandID.get(j));
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
						matG.m_afData[i][j] = correlation;
						matG.m_afData[j][i] = correlation;
					}
				}
			}
			double residualVariance = getDefaultResidualError(ErrorTermGroup.Default).getVariance().m_afData[0][0];
			Matrix matR = Matrix.getIdentityMatrix(stands.size()).scalarMultiply(residualVariance);
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
			Matrix varBlups = varBlupsFirstTerm.subtract(varBlupsSecondTerm);
			
			registerBlups(blups, varBlups, covariance, (List) standsForWhichBlupsWillBePredicted);
		}
		setBlupsEstimated(true);
	}

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#getBlupsForThisSubject(repicea.simulation.MonteCarloSimulationCompliantObject)
	 */
	@Override
	protected GaussianEstimate getBlupsForThisSubject(MonteCarloSimulationCompliantObject subject) {
		return super.getBlupsForThisSubject(subject);
	}
	
//	public static void main(String[] args) {
//		new MathildeClimatePredictor(false);
//	}
}