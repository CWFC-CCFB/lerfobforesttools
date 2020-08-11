/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2020 Her Majesty the Queen in right of Canada
 * 		Mathieu Fortin for Canadian WoodFibre Centre,
 * 							Canadian Forest Service, 
 * 							Natural Resources Canada
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.predictor.volume.frenchcommercialvolume2020;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public class FrenchCommercialVolume2020PredictorTest {

	static List<FrenchCommercialVolume2020TreeImpl> Trees;
	
	
	@Test
	public void testAgainstRPredictions() throws IOException {
		FrenchCommercialVolume2020Predictor predictor = new FrenchCommercialVolume2020Predictor();
		List<FrenchCommercialVolume2020TreeImpl> trees = getTrees();
		int nbTrees = 0;
		for (FrenchCommercialVolume2020TreeImpl tree : trees) {
			double actual = predictor.predictTreeCommercialVolumeDm3(tree);
			double expected = tree.pred;
			Assert.assertEquals("Testing prediction for tree " + tree.getSubjectId(), expected, actual, 1E-8);
			nbTrees++;
		}
		System.out.println("Nb of trees successfully tested for prediction " + nbTrees);
	}

	@Test
	public void testAgainstRStandardErrorPredictions() throws IOException {
		FrenchCommercialVolume2020Predictor predictor = new FrenchCommercialVolume2020Predictor();
		List<FrenchCommercialVolume2020TreeImpl> trees = getTrees();
		int nbTrees = 0;
		for (FrenchCommercialVolume2020TreeImpl tree : trees) {
			double actual = Math.sqrt(predictor.getVarianceOfTheMean(tree));
			double expected = tree.se;
			Assert.assertEquals("Testing standard error of prediction for tree " + tree.getSubjectId(), expected, actual, 1E-8);
			nbTrees++;
		}
		System.out.println("Nb of trees successfully tested for standard error of prediction " + nbTrees);
	}

	@Test
	public void testAgainstRPredictionVariance() throws IOException {
		FrenchCommercialVolume2020Predictor predictor = new FrenchCommercialVolume2020Predictor();
		List<FrenchCommercialVolume2020TreeImpl> trees = getTrees();
		int nbTrees = 0;
		for (FrenchCommercialVolume2020TreeImpl tree : trees) {
			double actual = predictor.getPredVariance(tree);
			double expected = tree.var;
			Assert.assertEquals("Testing prediction variance (mean + residual) for tree " + tree.getSubjectId(), expected, actual, 1E-8);
			nbTrees++;
		}
		System.out.println("Nb of trees successfully tested for prediction variance " + nbTrees);
	}

	private List<FrenchCommercialVolume2020TreeImpl> getTrees() throws IOException {
		if (Trees == null) {
			Trees = new ArrayList<FrenchCommercialVolume2020TreeImpl>();
			String filename = ObjectUtility.getRelativePackagePath(getClass()) + "0_pred.csv";
			
			CSVReader reader = new CSVReader(filename);
			Object[] record;
			double dbhCm;
			double heightM;
			String speciesName;
			double pred;
			double se;
			double var;
			FrenchCommercialVolume2020TreeImpl tree;
			int id = 0;
			while((record = reader.nextRecord()) != null) {
				speciesName = record[0].toString();
				dbhCm = Double.parseDouble(record[1].toString());
				heightM = Double.parseDouble(record[2].toString());
				pred = Double.parseDouble(record[3].toString());
				se = Double.parseDouble(record[4].toString());
				var = Double.parseDouble(record[5].toString());
				
				tree = new FrenchCommercialVolume2020TreeImpl(id, dbhCm, heightM, speciesName, pred, se, var);
				Trees.add(tree);
				id++;
			}
			reader.close();
		}
		return Trees;
	}
	
	
	
}
