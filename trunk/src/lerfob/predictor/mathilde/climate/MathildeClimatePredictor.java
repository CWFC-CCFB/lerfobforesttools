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
	
	private static List<MathildeClimateStandImpl> referenceStands;
	
	public MathildeClimatePredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
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
			
		} catch (Exception e) {
			System.out.println("MathildeClimateModel.init() : Unable to initialize the MathildeClimateModel module");
		}
	}

	/**
	 * This method returns a copy of static member referenceStands.
	 * @return a List of MathildeClimateStandImpl instances
	 */
	protected static List<MathildeClimateStandImpl> getReferenceStands() {
		if (referenceStands == null) {
			instantiateReferenceStands();
		} 
		List<MathildeClimateStandImpl> copyList = new ArrayList<MathildeClimateStandImpl>();
		copyList.addAll(referenceStands);
		return copyList;
	}
	
	
	private synchronized static void instantiateReferenceStands() {
		try {
			if (referenceStands == null) {
				referenceStands = new ArrayList<MathildeClimateStandImpl>();
				String path = ObjectUtility.getRelativePackagePath(MathildeClimatePredictor.class);
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
			}
		} catch (Exception e) {
			System.out.println("Unable to instantiate the reference stand list in MathildeClimatePredictor class!");
		}
	}

	protected final synchronized double getFixedEffectPrediction(MathildeClimateStand stand, Matrix currentBeta) {
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
	public double getMeanTemperatureForGrowthInterval(MathildeClimateStand stand) {
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