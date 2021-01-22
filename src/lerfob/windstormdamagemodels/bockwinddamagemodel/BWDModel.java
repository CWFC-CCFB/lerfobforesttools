/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.windstormdamagemodels.bockwinddamagemodel;

import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;



/**
 * This class implements the wind damage model found in Bock et al. 2005. This model is designed for beech stands in NorthEast France.
 * @see <a href=http://documents.irevues.inist.fr/handle/2042/5032>
 * Bock, J., I. Vinkler, P. Duplat, J.-P. Renaud, V. Badeau, and J.-L. Dupouey. 2005 Stabilite au vent des hetraies : les enseignements
 * de la tempete de 1999. Revue forestiere francaise LVII-2: 143-158.
 * </a>
 * @author Mathieu Fortin - October 2010
 */
public final class BWDModel {
	
	private enum TreeLevelVersion {
		/**
		 * The version that includes the height and dbh.
		 */
		Simple,
		/**
		 * The version that includes the crown length and radius.
		 */
		Enhanced
	}
	
	private final Matrix standLevelParameters;
	private final Map<TreeLevelVersion, Matrix> treeLevelParameters;
	
	/**
	 * Simple constructor for this class
	 */
	public BWDModel() {
		standLevelParameters = new Matrix(8,1);
		standLevelParameters.m_afData[0][0] = -29.45;	// intercept
		standLevelParameters.m_afData[1][0] = 24.52;		// if wind speed is between 120 and 140 km/h
		standLevelParameters.m_afData[2][0] = 25.58;		// if wind speed exceeds 140 km/h
		standLevelParameters.m_afData[3][0] = -1.55;		// if dominant height is smaller than or equal to 23.5 m
		standLevelParameters.m_afData[4][0] = 0.0791;	// continue variable - dominant height if greater than 23.5
		standLevelParameters.m_afData[5][0] = 1.33;		// compact clay is found within the first 50 cm of soil
		standLevelParameters.m_afData[6][0] = 1.80;		// if rock is found within the first 50 cm of soil
		standLevelParameters.m_afData[7][0] = 0.85; 		// if on flat lands		

		Matrix parameters;
		treeLevelParameters = new HashMap<TreeLevelVersion, Matrix>();
		
		parameters = new Matrix(6,1);
		parameters.m_afData[0][0] = -7.97;		// intercept 
		parameters.m_afData[1][0] = 2.99;		// ln(htot)
		parameters.m_afData[2][0] = -0.015;	// htot/dbh
		parameters.m_afData[3][0] = 0.54;		// if C horizon is above 50cm
		parameters.m_afData[4][0] = 0.01;		// if C horizon is between 50 and 70cm
		parameters.m_afData[5][0] = -0.55;		// if C horizon is below 70cm
		treeLevelParameters.put(TreeLevelVersion.Simple, parameters);
		
		parameters = new Matrix(4,1);
		parameters.m_afData[0][0] = -7.39;	// intercept
		parameters.m_afData[1][0] = 6.21;	// ln(ln(lever height * ln(crown radius)))
		parameters.m_afData[2][0] = 1.14;	// if C horizon is above 50cm
		parameters.m_afData[3][0] = 0.52;	// if C horizon is between 50 and 70cm
		treeLevelParameters.put(TreeLevelVersion.Enhanced, parameters);
	}
	



	/**
	 * This method returns the proportion of damaged trees always in terms of number of stems. The
	 * calibration data come from a sample in Northeastern France. The mean wind speed is around 120-140 km/h, with some
	 * above 140 km/h (very few observations below 120 km/h).
	 * @param stand = a BWDStand instance
	 * @param windSpeed = the windspeed in km/h (double)
	 * @return the proportion of damaged trees (double)
	 * @throws Exception
	 */
	public double getProportionOfDamagedTrees(BWDStand stand, double windSpeed) throws Exception {
		Matrix xVector = new Matrix(1,8);
		
		xVector.m_afData[0][0] = 1d;
		
		if (windSpeed >= 120 && windSpeed <= 140) {
			xVector.m_afData[0][1] = 1d;
		} else {
			if (windSpeed > 140) {
				xVector.m_afData[0][2] = 1d;
			}
		}
		
		double dominantHeight = stand.getDominantHeightM();
		if (dominantHeight <= 23.5) {
			xVector.m_afData[0][3] = 1d;
		} else  {
			xVector.m_afData[0][4] = dominantHeight;
		}
		
		if (stand.isCompactClayInFirst50cm()) {
			xVector.m_afData[0][5] = 1d;
		}
		
		if (stand.isRockInTheFirst50cm()) {
			xVector.m_afData[0][6] = 1d;
		}
		
		if (stand.isFlatLand()) {
			xVector.m_afData[0][7] = 1d;
		}
		
		double xBeta = xVector.multiply(standLevelParameters).m_afData[0][0];
		
		double proportion = Math.exp(xBeta) / (1 + Math.exp(xBeta));
		
		// TODO : how to implement the scale
		return proportion;
	}
	
	/**
	 * This method returns the probability of wind damage for an individual tree. It is designed for wind speeds above 140 km/h.
	 * The calibration data mostly come from the For�t de Haye (see Vincent Badeau's thesis).
	 * @param tree = a BWDTree instance
	 * @return the probability of stand damage (double)
	 * @throws Exception
	 */
	public double getProbabilityOfDamageForThisTree(BWDTree tree) throws Exception {
		double cHorizonDepth = tree.getSoilHorizonCDepthM();
		double treeHeight = tree.getHeightM();
		double treeDbh = tree.getDbhCm();
		
		double hLever_LnCrownRadius = 0d;
		TreeLevelVersion modelVersion = TreeLevelVersion.Simple;
		if (tree.getCrownLengthM() > 0 && tree.getCrownRadiusM() > 0) {
			double crownLength = tree.getCrownLengthM();
			double crownRadius = tree.getCrownRadiusM();
			double heightLever = treeHeight - crownLength * .5;
			hLever_LnCrownRadius = heightLever  * Math.log(crownRadius);
			if (hLever_LnCrownRadius > 1) {									// this condition ensures that ln(ln(hLever_LnCrownRadius)) is feasible
				modelVersion = TreeLevelVersion.Enhanced;
			}
		}

		Matrix parameters = treeLevelParameters.get(modelVersion);
		
		Matrix xVector;
		
		
		if (modelVersion == TreeLevelVersion.Simple) {
			
			xVector = new Matrix(1,6);
			xVector.m_afData[0][0] = 1d;
			xVector.m_afData[0][1] = Math.log(treeHeight);
			xVector.m_afData[0][2] = treeHeight / treeDbh * 100;
			
			if (cHorizonDepth < 50) {
				xVector.m_afData[0][3] = 1d;
			} else if (cHorizonDepth < 70) {
				xVector.m_afData[0][4] = 1d;
			} else {
				xVector.m_afData[0][5] = 1d;
			}
		} else  {
			xVector = new Matrix(1,4);
			xVector.m_afData[0][0] = 1d;
			xVector.m_afData[0][1] = Math.log(Math.log(hLever_LnCrownRadius));
			if (cHorizonDepth < 50) {
				xVector.m_afData[0][2] = 1d;
			} else if (cHorizonDepth < 70) {
				xVector.m_afData[0][3] = 1d;
			} 
		}
		
		double xBeta = xVector.multiply(parameters).m_afData[0][0];
		double probability = Math.exp(xBeta) / (1 + Math.exp(xBeta));
		return probability;
	}
	
}
