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
package lerfob.nutrientmodel;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import lerfob.biomassmodel.BiomassPredictionModel.BiomassCompartment;
import lerfob.nutrientmodel.NutrientConcentrationPredictionModel.Nutrient;

import org.junit.Test;

import repicea.math.Matrix;
import repicea.util.ObjectUtility;

/**
 * This JUnit test checks whether the results of the NutrientConcentrationPredictionModel are consistent.
 * @author Mathieu Fortin - March 2013
 */
public class NutrientConcentrationPredictionModelTest {

	private static class FakeTree implements NutrientCompatibleTree {

		private double dbhCm;
		
		private FakeTree(double dbhCm) {
			this.dbhCm = dbhCm;
		}
		
		
		@Override
		public double getHeightM() {return 22;}

		@Override
		public double getDbhCm() {return dbhCm;}

		@Override
		public int getAgeYr() {return 70;}

		@Override
		public FgSpecies getFgSpecies() {return FgSpecies.BEECH;}

		@Override
		public double getCrownBaseHeightM() {
			return 0;
		}

		@Override
		public double getCrossSectionDiameterCm(double heightM, boolean overBark) {
			return 0;
		}
	}
		
	@SuppressWarnings("unchecked")
	@Test
	public void differentCompartmentsTest() throws IOException, ClassNotFoundException {
		NutrientConcentrationPredictionModel.setEnabled(true);
		
		String referenceFilename = ObjectUtility.getPackagePath(this.getClass()) + "refList.ser";

		NutrientConcentrationPredictionModel model = new NutrientConcentrationPredictionModel();
		FakeTree tree = new FakeTree(30);
		
		List<BiomassCompartment> compartments = new ArrayList<BiomassCompartment>();
		compartments.add(BiomassCompartment.STEM_SUP7);
		compartments.add(BiomassCompartment.BRANCHES_0TO4);
		compartments.add(BiomassCompartment.BRANCHES_4TO7);
		
		List<Matrix> outputList = new ArrayList<Matrix>();
		
		for (Nutrient nut : Nutrient.values()) {
			for (BiomassCompartment compartment : compartments) {
				outputList.add(model.getNutrientConcentrations(nut, compartment, tree));
			}
		}
		
//		UNCOMMENT THIS PART TO SAVE A NEW REFERENCE MAP
//		try {
//			FileOutputStream fos = new FileOutputStream(referenceFilename);
//			ObjectOutputStream out = new ObjectOutputStream(fos);
//			out.writeObject(outputList);
//			out.close();
//		} catch(IOException ex) {
//			ex.printStackTrace();
//			throw ex;
//		}
//	
		System.out.println("Loading reference map...");
		List<Matrix> refList;
		try {
			FileInputStream fis = new FileInputStream(referenceFilename);
			ObjectInputStream in = new ObjectInputStream(fis);
			refList = (List<Matrix>) in.readObject();
			in.close();
		} catch(IOException ex) {
			ex.printStackTrace();
			throw ex;
		}


		assertEquals(refList.size(), outputList.size());

		int nbTested = 0;
		for (int i = 0; i < outputList.size(); i++) {
			Matrix obsMat = outputList.get(i);
			Matrix refMat = (Matrix) refList.get(i);
			for (int j = 0; j < obsMat.m_iRows; j++) {
				assertEquals(refMat.m_afData[j][0], obsMat.m_afData[j][0], 1E-6);
				nbTested++;
			}
		}
		System.out.println(nbTested + " concentrations successfully tested");
	}

	public static void main(String[] args) {
		NutrientConcentrationPredictionModel model = new NutrientConcentrationPredictionModel();
		NutrientConcentrationPredictionModel.setEnabled(true);
		List<FakeTree> trees = new ArrayList<FakeTree>();
		
		for (double dbh = 10; dbh <= 40; dbh += 10) {
			trees.add(new FakeTree(dbh));
		}
		
		
		List<BiomassCompartment> compartments = new ArrayList<BiomassCompartment>();
		compartments.add(BiomassCompartment.STEM_SUP7);
		compartments.add(BiomassCompartment.BRANCHES_0TO4);
		compartments.add(BiomassCompartment.BRANCHES_4TO7);

		for (Nutrient nut : Nutrient.values()) {
			for (BiomassCompartment compartment : compartments) {
				for (FakeTree tree : trees ) {
					System.out.println("dbh = " + tree.getDbhCm()  + "; Nutrient = " + nut.toString() + "; Compartment = " + compartment.toString() + "; Concentration = " + model.getNutrientConcentrations(nut, compartment, tree).toString());
				}
			}
		}

	}
	
	
	
	
	
}
