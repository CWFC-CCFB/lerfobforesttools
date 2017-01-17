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
package lerfob.predictor.dopalep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public class DopalepDbhIncPredictorTest {

	private static List<DopalepTreeImpl> trees; 
	
	
	private static void ReadTrees() throws IOException {
		if (trees == null) {
			trees = new ArrayList<DopalepTreeImpl>();
			String filename = ObjectUtility.getPackagePath(DopalepDbhIncPredictorTest.class) + "0_DopalepPredictions.csv";
			Map<Integer, DopalepPlotImpl> knownIdp = new HashMap<Integer, DopalepPlotImpl>();
			CSVReader reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				int idp = Integer.parseInt(record[0].toString());
				if (!knownIdp.containsKey(idp)) {
					knownIdp.put(idp, new DopalepPlotImpl());
				}
				DopalepPlotImpl plot = knownIdp.get(idp);
				double dbhCm = Double.parseDouble(record[1].toString());
				double gOthers = Double.parseDouble(record[2].toString());
				double pred = Double.parseDouble(record[3].toString());
				trees.add(new DopalepTreeImpl(plot, dbhCm, gOthers, pred));
			}
			reader.close();
		}
	}
	
	@Test
	public void checkPredictionsOnTranformedScale() throws IOException {
		ReadTrees();
		int i = 0;
		DopalepDbhIncrementPredictor predictor = new DopalepDbhIncrementPredictor(false, false, false); 
		for (DopalepTreeImpl tree : trees) {
			i++;
			double actual = predictor.getFixedEffectOnlyPrediction(tree.plot, tree);
			double expected = tree.getPred();
			Assert.assertEquals("Testing tree " + i, expected, actual, 1E-8);
		}
		System.out.println("Trees successfully tested in DopalepDbhIncrementPredictor " + i);
	}
}
