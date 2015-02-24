/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.mathilde;

import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmProvider;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * This interface ensures that the Tree instance is compatible with the MathildeDiameterIncrementPredictor.
 * @author Mathieu Fortin - June 2013
 */
public interface MathildeTree extends DbhCmProvider, 
									LnDbhCmProvider, 
									MonteCarloSimulationCompliantObject {
	
	/**
	 * This enum variable defines the different species in the Mathilde simulator.
	 * @author Mathieu Fortin - June 2013
	 */
	public static enum MathildeTreeSpecies implements TextableEnum {
		CARPINUS("Hornbeam", "Charme"),   // code 17
		QUERCUS("Oak", "Ch\u00EAne"),  // code 22
		FAGUS("Beech", "H\u00EAtre"), // code 42
		OTHERS("Others", "Autres");
		
		private static Map<Integer, MathildeTreeSpecies> speciesMap;

		private Matrix dummyVariable;
		
		/**
		 * This method returns the species enum according to the code.
		 * @param code an integer (17 for Carpinus, 22 for Quercus, 42 for Fagus, and 99 for others)
		 * @return a MathildeTreeSpecies enum
		 */
		public static MathildeTreeSpecies getSpecies(int code) {
			if (speciesMap == null) {
				speciesMap = new HashMap<Integer, MathildeTreeSpecies>();
				speciesMap.put(17, CARPINUS);
				speciesMap.put(22, QUERCUS);
				speciesMap.put(42, FAGUS);
				speciesMap.put(99, OTHERS);
			}
			return speciesMap.get(code);
		}

		MathildeTreeSpecies(String englishText, String frenchText) {
			setText(englishText, frenchText);
			dummyVariable = new Matrix(1,3);
			if (this.ordinal() > 0) {
				dummyVariable.m_afData[0][this.ordinal() - 1] = 1d;
			}
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}

		/**
		 * This method returns a 1x3 row vector of dummy variables which represent the species effect.
		 * @return a Matrix instance
		 */
		public Matrix getDummyVariable() {return dummyVariable;}
	}
	
	/**
	 * This method returns the enum instance that represents the species.
	 * @return a MathildeTreeSpecies instance
	 */
	public MathildeTreeSpecies getMathildeTreeSpecies();
	
	/**
	 * This method returns the basal area of all the trees of the species with dbh larger than this tree instance.
	 * @param species the species that is to be considered
	 * @return basal area in m2/ha
	 */
	public double getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies species);

	
}
