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
package lerfob.predictor.mathilde.recruitment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.mathilde.MathildeTree;
import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.StatisticalUtility;
import repicea.util.ObjectUtility;

/**
 * This class predicts the dbh of the recruits in MATHILDE.
 * @author Mathieu Fortin - Octobre 2017
 */
@SuppressWarnings("serial")
public class MathildeRecruitDbhPredictor extends REpiceaPredictor {


	private static final Map<MathildeTreeSpecies, Matrix> DummyMap = new HashMap<MathildeTreeSpecies, Matrix>();
	static {
		Matrix m = new Matrix(1,4);
		m.setValueAt(0, 0, 1d);
		DummyMap.put(MathildeTreeSpecies.FAGUS, m);

		m = new Matrix(1,4);
		m.setValueAt(0, 1, 1d);
		DummyMap.put(MathildeTreeSpecies.CARPINUS, m);
		
		m = new Matrix(1,4);
		m.setValueAt(0, 2, 1d);
		DummyMap.put(MathildeTreeSpecies.QUERCUS, m);
		
		m = new Matrix(1,4);
		m.setValueAt(0, 3, 1d);
		DummyMap.put(MathildeTreeSpecies.OTHERS, m);
	}

	protected static double Dispersion = 1.178388;
	private final static double Offset = 24d / Math.PI;
	
	/**
	 * Constructor
	 * @param isVariabilityEnabled true to enable the stochastic variability.
	 */
	public MathildeRecruitDbhPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled); 
	}

	protected MathildeRecruitDbhPredictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled); // no random effect in this model
		init();
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
	}
	
	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_recruitDbh_beta.csv";
			String omegaFilename = path + "0_recruitDbh_omega.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(betaFilename);
			Matrix beta = betaMap.get();
			Matrix omega = ParameterLoader.loadMatrixFromFile(omegaFilename);	
			setParameterEstimates(new ModelParameterEstimates(beta, omega));

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("An error ocurred while reading the parameters!");
		}
	}
	
	/**
	 * This method returns the dbh of the recruit in Mathilde model.
	 * @param stand a MathildeRecruitmentStand instance
	 * @param tree a MathildeTree instance
	 * @return the dbh (cm)
	 */
	public synchronized double predictRecruitDiameter(MathildeRecruitmentStand stand, MathildeTree tree) {
		return predictRecruitDiameterWithOffset(stand, tree) + Offset;
	}

	protected double predictRecruitDiameterWithOffset(MathildeRecruitmentStand stand, MathildeTree tree) {
		Matrix beta = getParametersForThisRealization(stand);
		double mean = predictFixedEffectOnly(beta, stand, tree);
		if (isResidualVariabilityEnabled) {
			return StatisticalUtility.getRandom().nextGamma(Dispersion, mean / Dispersion);
		} else {
			return mean;
		}
	}

	protected double predictFixedEffectOnly(Matrix beta, MathildeRecruitmentStand stand, MathildeTree tree) {
		oXVector.resetMatrix();

		Matrix speciesDummy = DummyMap.get(tree.getMathildeTreeSpecies());
		double basalAreaM2Ha = stand.getBasalAreaM2Ha();

		int index = 0;
		oXVector.setSubMatrix(speciesDummy, 0, index);
		index += speciesDummy.m_iCols;
		
		oXVector.setSubMatrix(speciesDummy.scalarMultiply(basalAreaM2Ha), 0, index);
		index += speciesDummy.m_iCols;

		double linearPredictor = oXVector.multiply(beta).getValueAt(0, 0);
		return 1d/linearPredictor;
	}
	
}
