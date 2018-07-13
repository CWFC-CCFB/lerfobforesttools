/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.thinners.frenchnfithinner2018;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictor.FrenchNFIThinnerSpecies;
import repicea.io.javacsv.CSVReader;
import repicea.simulation.REpiceaPredictor;
import repicea.util.ObjectUtility;

/**
 * The FrenchNFIThinnerStandingPriceProvider class provides the prices of the 
 * target species which are used by the FrenchNFIThinnerPredictor class to estimate 
 * the probability of harvesting.
 * @author Mathieu Fortin - June 2018
 *
 */
@SuppressWarnings("serial")
class FrenchNFIThinnerStandingPriceProvider extends REpiceaPredictor {

	class TargetSpeciesSelection implements Serializable {
		
		final FrenchNFIThinnerSpecies targetSpecies;
		final int yearDate;
		
		TargetSpeciesSelection(FrenchNFIThinnerSpecies targetSpecies, int yearDate) {
			this.targetSpecies = targetSpecies;
			this.yearDate = yearDate;
		}
	}
	
	
	
	final Map<FrenchNFIThinnerSpecies, FrenchNFIThinnerStandingPriceProviderSubModel> subModels;
	
	final int minimumYearDate = 2006;
	final int maximumYearDate = 2016;
	
	final int managementPlanDuration = 15; // the duration of management plan on private lands goes from 10 to 20 years
	
	final Map<String, Map<Integer, List<TargetSpeciesSelection>>> targetSpeciesSelectionMap;

	
	FrenchNFIThinnerStandingPriceProvider(boolean isVariabilityEnabled) {
		super(false, isVariabilityEnabled, false); // although it was a residual error, it is handled through the random effects for convenience
		subModels = new HashMap<FrenchNFIThinnerSpecies, FrenchNFIThinnerStandingPriceProviderSubModel>();
		for (FrenchNFIThinnerSpecies sp : FrenchNFIThinnerSpecies.values()) {
			subModels.put(sp, new FrenchNFIThinnerStandingPriceProviderSubModel(isVariabilityEnabled, this));
		}
		targetSpeciesSelectionMap = new HashMap<String, Map<Integer, List<TargetSpeciesSelection>>>();
		init();
	}
			
	@Override
	protected void init() {
		String filename = ObjectUtility.getRelativePackagePath(getClass()) + "prixBoisOnf.csv";
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				if (record[1].toString().equals("total")) {
					FrenchNFIThinnerSpecies sp = FrenchNFIThinnerSpecies.getSpeciesFromFrenchName(record[0].toString());
					if (sp != null) {
						FrenchNFIThinnerStandingPriceProviderSubModel subModel = subModels.get(sp);
						int year = Integer.parseInt(record[2].toString());
						double value = Double.parseDouble(record[3].toString());
						subModel.observedPriceMap.put(year, value);
					}
				}
			}
			for (FrenchNFIThinnerSpecies sp : FrenchNFIThinnerSpecies.values()) {
				subModels.get(sp).init();	// initialize the mean and the residual variance
			}
		} catch (Exception e) {
			System.out.println("Unable to read the price of standing volume in the FrenchNFIThinnerStandingPriceProvider class!");
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	synchronized FrenchNFIThinnerSpecies getTargetSpecies(FrenchNFIThinnerPlot plot, int yearDate) {
		TargetSpeciesSelection tSp = getTargetSpeciesSelection(plot, yearDate);
		if (tSp != null) {
			return tSp.targetSpecies;
		} else {
			Map<FrenchNFIThinnerSpecies, Double> volumeBySpecies = plot.getVolumeM3BySpecies();
			Map<FrenchNFIThinnerSpecies, Double> valueBySpecies = new HashMap<FrenchNFIThinnerSpecies, Double>();
			for (FrenchNFIThinnerSpecies sp : volumeBySpecies.keySet()) {
				double priceByM3 = getStandingPriceForThisYear(sp, yearDate, plot.getMonteCarloRealizationId());
				valueBySpecies.put(sp, volumeBySpecies.get(sp) * priceByM3);
			}
			double priceMax = 0;
			FrenchNFIThinnerSpecies speciesWithMaxValue = null; 
			for (FrenchNFIThinnerSpecies sp : valueBySpecies.keySet()) {
				if (valueBySpecies.get(sp) > priceMax) {
					priceMax = valueBySpecies.get(sp);
					speciesWithMaxValue = sp;
				}
			}
			if (speciesWithMaxValue == null) {
				int u = 0;
			}
			recordTargetSpeciesSelection(plot, speciesWithMaxValue, yearDate);
			return speciesWithMaxValue;
		}
	}
	
	
	private void recordTargetSpeciesSelection(FrenchNFIThinnerPlot plot, FrenchNFIThinnerSpecies speciesWithMaxValue, int yearDate) {
		if (!targetSpeciesSelectionMap.containsKey(plot.getSubjectId())) {
			targetSpeciesSelectionMap.put(plot.getSubjectId(), new HashMap<Integer, List<TargetSpeciesSelection>>());
		}
		Map<Integer, List<TargetSpeciesSelection>> innerMap = targetSpeciesSelectionMap.get(plot.getSubjectId());
		if (!innerMap.containsKey(plot.getMonteCarloRealizationId())) {
			innerMap.put(plot.getMonteCarloRealizationId(), new ArrayList<TargetSpeciesSelection>());
		}
		List<TargetSpeciesSelection> tSpList = innerMap.get(plot.getMonteCarloRealizationId());
		tSpList.add(new TargetSpeciesSelection(speciesWithMaxValue, yearDate));
	}
	
	
	double getStandingPriceForThisYear(FrenchNFIThinnerSpecies species, int yearDate, int monteCarloID) {
		return subModels.get(species).getStandingPriceForThisYear(yearDate, monteCarloID);
	}

	/**
	 * This method returns the target species selection that was performed in the previous 15-year period, which
	 * is the average duration for a management plan. If this selection does not exist, then it returns null.
	 * @param plot a FrenchNFIThinnerPlot plot
	 * @param yearDate the current date (year)
	 * @return a TargetSpeciesSelection instance or null
	 */
	private TargetSpeciesSelection getTargetSpeciesSelection(FrenchNFIThinnerPlot plot, int yearDate) {
		TargetSpeciesSelection tss = null;
		if (targetSpeciesSelectionMap.containsKey(plot.getSubjectId())) {
			Map<Integer, List<TargetSpeciesSelection>> innerMap = targetSpeciesSelectionMap.get(plot.getSubjectId());
			if (innerMap.containsKey(plot.getMonteCarloRealizationId())) {
				List<TargetSpeciesSelection> targetSpeciesSelections = innerMap.get(plot.getMonteCarloRealizationId());
				for (TargetSpeciesSelection sel : targetSpeciesSelections) {
					if ((yearDate - sel.yearDate) <= managementPlanDuration) {
						tss = sel;
						break;
					}
				}
			}
		}
		return tss;
	}
	
	/**
	 * This method returns the array of the prices for standing volume. If the year is smaller than 2006, it is assumed that the
	 * price is that of 2006. If the year is larger than 2016, it is assumed that the price is that of 2016.
	 * @param species a Species enum
	 * @param startingYear not included in the array
	 * @param endingYear included in the array
	 * @param monteCarloId the id of the Monte Carlo realization
	 * @return an array of double
	 */
	double[] getStandingPrices(FrenchNFIThinnerSpecies species, int startingYear, int endingYear, int monteCarloId) {
		return subModels.get(species).getStandingPrices(startingYear, endingYear, monteCarloId);
	}

	
}
